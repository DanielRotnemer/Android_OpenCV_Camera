package com.moviera.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.gms.common.images.Size;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.*;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback
{
    private Camera mCamera = null;
    private ImageView MyCameraPreview = null;
    private Bitmap bitmap = null;
    private int[] pixels = null;
    private byte[] FrameData = null;
    private int imageFormat;
    private int PreviewSizeWidth;
    private int PreviewSizeHeight;
    private boolean bProcessing = false;

    Handler mHandler = new Handler(Looper.getMainLooper());

    public CameraPreview(int PreviewlayoutWidth, int PreviewlayoutHeight,
                         ImageView CameraPreview)
    {
        PreviewSizeWidth = PreviewlayoutWidth;
        PreviewSizeHeight = PreviewlayoutHeight;
        MyCameraPreview = CameraPreview;
        bitmap = Bitmap.createBitmap(PreviewSizeWidth, PreviewSizeHeight, Bitmap.Config.ARGB_8888);
        pixels = new int[PreviewSizeWidth * PreviewSizeHeight];
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        // At preview mode, the frame data will push to here.
        if (imageFormat == ImageFormat.NV21)
        {
            //We only accept the NV21(YUV420) format.
            if ( !bProcessing )
            {
                FrameData = data;
                mCamera.addCallbackBuffer(data);
                mHandler.post(DoImageProcessing);
            }
        }
    }

    public void onPause()
    {
        mCamera.stopPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
    {
        Camera.Parameters parameters;

        parameters = mCamera.getParameters();
        // Set the camera preview size
        parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        mCamera.setDisplayOrientation(90);

        imageFormat = parameters.getPreviewFormat();

        mCamera.setParameters(parameters);

        mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0)
    {
        mCamera = Camera.open();
        try
        {
            // If did not set the SurfaceHolder, the preview area will be black.
            mCamera.setPreviewDisplay(arg0);
           // mCamera.setPreviewCallback(this);

            int size = PreviewSizeHeight * PreviewSizeWidth;

            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.addCallbackBuffer(createPreviewBuffer(size));
            mCamera.addCallbackBuffer(createPreviewBuffer(size));
            mCamera.addCallbackBuffer(createPreviewBuffer(size));
            mCamera.addCallbackBuffer(createPreviewBuffer(size));
        }
        catch (IOException e)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    private byte[] createPreviewBuffer(int previewSize)
    {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        long sizeInBits = previewSize * bitsPerPixel;
        int bufferSize = (int) Math.ceil(sizeInBits / 8.0d) + 1;

        //
        // NOTICE: This code only works when using play services v. 8.1 or higher.
        //

        // Creating the byte array this way and wrapping it, as opposed to using .allocate(),
        // should guarantee that there will be an array to work with.
        byte[] byteArray = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        if (!buffer.hasArray() || (buffer.array() != byteArray)) {
            // I don't think that this will ever happen.  But if it does, then we wouldn't be
            // passing the preview content to the underlying detector later.
            throw new IllegalStateException("Failed to create valid buffer for camera source.");
        }
        return byteArray;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0)
    {
        mCamera.setPreviewCallbackWithBuffer(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    static {
        System.loadLibrary("native-lib");
    }

    //
    // Native JNI
    //

    public native boolean ImageProcessing(int width, int height,
                                          byte[] NV21FrameData, int[] pixels);

    private Runnable DoImageProcessing = new Runnable()
    {
        public void run()
        {
            Log.d("MyRealTimeImageProcessing", "DoImageProcessing():");
            bProcessing = true;
            ImageProcessing(PreviewSizeWidth, PreviewSizeHeight, FrameData, pixels);

            // check for wrapping the byte[] data array fater processing in Buffer and pass it to GLES20.glTexSubImage2D()

            // consider to push pixels back to glsurfaceview as mentioned in:
            // https://stackoverflow.com/questions/31359081/modify-and-update-camera-frame-via-glsurfaceview

            bitmap.setPixels(pixels, 0, PreviewSizeWidth, 0, 0, PreviewSizeWidth, PreviewSizeHeight);
            MyCameraPreview.setImageBitmap(bitmap);
            bProcessing = false;
        }
    };
}
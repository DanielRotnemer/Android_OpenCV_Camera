package com.moviera.opengl5;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback
{
    private Camera mCamera = null;
    private GLSurfaceView5 MyCameraPreview = null;
    private Bitmap bitmap = null;
    private int[] pixels = null;
    private byte[] FrameData = null;
    private int imageFormat;
    private int PreviewSizeWidth = 1920;
    private int PreviewSizeHeight = 1080;
    private boolean bProcessing = false;

    Handler mHandler = new Handler(Looper.getMainLooper());

    public CameraPreview(int PreviewlayoutWidth, int PreviewlayoutHeight,
                         GLSurfaceView5 CameraPreview) // this imageview should be changed to surfacetexture or glsurfaceview in order to push the processed frame back to it here
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
        mCamera.setPreviewCallback(null);
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

    private Runnable DoImageProcessing = new Runnable()
    {
        public void run()
        {
            Log.d("MyRealTimeImageProcessing", "DoImageProcessing():");
            bProcessing = true;

            // consider to push pixels back to glsurfaceview as mentioned in:
            // https://stackoverflow.com/questions/31359081/modify-and-update-camera-frame-via-glsurfaceview


            IntBuffer buffer = IntBuffer.allocate(pixels.length);
            buffer.put(pixels);
            buffer.position(0);

            /*GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);*/

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, MyCameraPreview.getRenderer().texture);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, PreviewSizeWidth, PreviewSizeHeight, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, buffer);

            //bitmap.setPixels(pixels, 0, PreviewSizeWidth, 0, 0, PreviewSizeWidth, PreviewSizeHeight);
            //MyCameraPreview.setImageBitmap(bitmap);
            bProcessing = false;
        }
    };
}

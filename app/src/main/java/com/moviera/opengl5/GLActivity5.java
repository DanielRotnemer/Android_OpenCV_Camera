package com.moviera.opengl5;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.moviera.R;
import com.moviera.preview.CameraPreview;

import java.io.IOException;

// no  need here to implement onpreview frame because it is implemented in the camerapreview class
// send surfacetexture or glsurfaceview to camerapreview to update the preview

public class GLActivity5 extends AppCompatActivity implements SurfaceTexture.OnFrameAvailableListener,
        Camera.PreviewCallback
{
    private Camera mCamera;
    private GLSurfaceView5 glSurfaceView;
    private SurfaceTexture surfaceTexture;
    private GLRenderer5 renderer;

    private int[] pixels = null;
    private byte[] FrameData = null;
    private int imageFormat;
    private int PreviewSizeWidth = 1920;
    private int PreviewSizeHeight = 1080;
    private boolean bProcessing = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ImageView MyCameraPreview = null;
    private Bitmap bitmap = null;

    private CameraPreview cameraPreview;
    private FrameLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GLSurfaceView5(this);
        renderer = glSurfaceView.getRenderer();
        setContentView(glSurfaceView);

        SurfaceView camView = new SurfaceView(this);
        SurfaceHolder camHolder = camView.getHolder();
        cameraPreview = new CameraPreview(PreviewSizeWidth, PreviewSizeHeight, MyCameraPreview);
        camHolder.addCallback(cameraPreview);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mainLayout = (FrameLayout) findViewById(R.id.frameLayout5);
        mainLayout.addView(camView, new ViewGroup.LayoutParams(PreviewSizeWidth, PreviewSizeHeight));
        mainLayout.addView(MyCameraPreview, new ViewGroup.LayoutParams(PreviewSizeWidth, PreviewSizeHeight));
    }

    public void startCamera(int texture)
    {
        surfaceTexture = new SurfaceTexture(texture);
        surfaceTexture.setOnFrameAvailableListener(this);
        renderer.setSurface(surfaceTexture);

        mCamera = Camera.open();

        try
        {
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
        }
        catch (IOException ioe)
        {
            Log.w("GLActivity5","CAM LAUNCH FAILED");
        }
    }

    public void onFrameAvailable(SurfaceTexture surfaceTexture)
    {
        glSurfaceView.requestRender();
    }

    @Override
    public void onPause()
    {
        mCamera.stopPreview();
        mCamera.release();
        System.exit(0);
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

    private Runnable DoImageProcessing = new Runnable()
    {
        public void run()
        {
            Log.d("MyRealTimeImageProcessing", "DoImageProcessing():");
            bProcessing = true;
            //ImageProcessing(PreviewSizeWidth, PreviewSizeHeight, FrameData, pixels);

            // consider to push pixels back to glsurfaceview as mentioned in:
            // https://stackoverflow.com/questions/31359081/modify-and-update-camera-frame-via-glsurfaceview

            bitmap.setPixels(pixels, 0, PreviewSizeWidth, 0, 0, PreviewSizeWidth, PreviewSizeHeight);
            MyCameraPreview.setImageBitmap(bitmap);
            bProcessing = false;
        }
    };
}

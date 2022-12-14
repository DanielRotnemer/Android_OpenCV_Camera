package com.moviera.preview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.moviera.R;

public class MyRealTimeImageProcessing extends AppCompatActivity
{

    private CameraPreview camPreview;
    private ImageView MyCameraPreview = null;
    private FrameLayout mainLayout;
    private int PreviewSizeWidth = 1920;
    private int PreviewSizeHeight= 1080;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Set this APK Full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Set this APK no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_my_real_time_image_processing);

        //
        // Create my camera preview
        //
        MyCameraPreview = new ImageView(this);

        SurfaceView camView = new SurfaceView(this);
        SurfaceHolder camHolder = camView.getHolder();
        camPreview = new CameraPreview(PreviewSizeWidth, PreviewSizeHeight, MyCameraPreview);

        camHolder.addCallback(camPreview);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mainLayout = (FrameLayout) findViewById(R.id.frameLayout1);
        mainLayout.addView(camView, new ViewGroup.LayoutParams(PreviewSizeWidth, PreviewSizeHeight));
        mainLayout.addView(MyCameraPreview, new ViewGroup.LayoutParams(PreviewSizeWidth, PreviewSizeHeight));
    }
    protected void onPause()
    {
        if ( camPreview != null)
            camPreview.onPause();
        super.onPause();
    }
}

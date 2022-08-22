package com.moviera.gpudemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.hardware.Camera;
import android.os.Bundle;

import com.moviera.R;
import com.moviera.gpuimage.GPUImageView;

public class GpuActivity extends AppCompatActivity
{
    ConstraintLayout mainGpuLayout;
    GPUImageView gpuImageView;
    Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpu);
        gpuImageView = findViewById(R.id.gpu_image_view);
        mainGpuLayout = findViewById(R.id.mainGpuLayout);

        mainGpuLayout.post(new Runnable()
        {
            @Override
            public void run()
            {
                camera = camera.open(0);
                gpuImageView.setUpCamera(camera,0,false, false);
            }
        });
    }
}

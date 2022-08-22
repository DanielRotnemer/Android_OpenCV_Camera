package com.moviera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.moviera.gpudemo.GpuActivity;
import com.moviera.opengl.GLActivity;
import com.moviera.opengl1.OpenGlActivity;
import com.moviera.opengl2.GLActivity2;
import com.moviera.opengl3.FaceDetectActivity;
import com.moviera.opengl5.GLActivity5;
import com.moviera.preview.MyRealTimeImageProcessing;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static
    {
        System.loadLibrary("native-lib");
        if (OpenCVLoader.initDebug()) {
            Log.d("Main", "OpenCV working");
        }
        else {
            Log.d("Main", "OpenCV not working");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public void tv_OnClick(View view)
    {
        int id = view.getId();
        Intent intent = new Intent(getBaseContext(), RequestCameraPermissionsActivity.class);

        if (id == R.id.cameraActivity)
            intent = new Intent(getBaseContext(), CameraActivity.class);
        if (id == R.id.opengl0)
            intent = new Intent(getBaseContext(), GLActivity.class);
        if (id == R.id.opengl1)
            intent = new Intent(getBaseContext(), OpenGlActivity.class);
        if (id == R.id.opengl2)
            intent = new Intent(getBaseContext(), GLActivity2.class);
        if (id == R.id.opengl3)
            intent = new Intent(getBaseContext(), FaceDetectActivity.class);
        if (id == R.id.opengl4)
            intent = new Intent(getBaseContext(), MyRealTimeImageProcessing.class);
        if (id == R.id.opengl5)
            intent = new Intent(getBaseContext(), GLActivity5.class);
        if (id == R.id.gpucamera)
            intent = new Intent(getBaseContext(), GpuActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}

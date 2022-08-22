package com.moviera.opengl1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.moviera.R;

public class OpenGlActivity extends AppCompatActivity
{
    private static final String TAG = "OpenGlActivity";
    CameraGLSurfaceView glSurfaceView = null;
    //ImageButton shutterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_gl);
        glSurfaceView = findViewById(R.id.camera_textureview);
        //shutterBtn = (ImageButton)findViewById(R.id.btn_shutter);
        initViewParams();
        //Photograph
        //shutterBtn.setOnClickListener(new BtnListeners());
    }

    private void initViewParams(){
        ViewGroup.LayoutParams params = glSurfaceView.getLayoutParams();
        Point p = getScreenMetrics(this);
        params.width = p.x; //view width
        params.height = p.y; //view high
        //Set the width and height of GLSurfaceView
        glSurfaceView.setLayoutParams(params);
        //Set the size of ImageButton
        /*ViewGroup.LayoutParams p2 = shutterBtn.getLayoutParams();
        p2.width = 100;
        p2.height = 100;
        shutterBtn.setLayoutParams(p2);*/
    }

    private Point getScreenMetrics(Context context){
        DisplayMetrics dm =context.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        return new Point(w_screen, h_screen);
    }

    //Photograph
    /*private class BtnListeners implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_shutter:
                    CameraInterface.getInstance().doTakePicture();
                    break;
                default:break;
            }
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        //Change the z-order of views in the tree, so it is above other peer views.
        glSurfaceView.bringToFront();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}

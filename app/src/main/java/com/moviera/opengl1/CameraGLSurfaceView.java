package com.moviera.opengl1;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener, Camera.PreviewCallback
{
    private static final String TAG = "CameraGLSurfaceView";
    Context mContext;
    //Capturing frames from an image stream in the form of OpenGL ES textures, I call them texture layers
    SurfaceTexture mSurface;
    //Texture id used
    int mTextureID = -1;
    DirectDrawer mDirectDrawer;

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setEGLContextClientVersion(2);
        setRenderer(this);
        //According to the monitoring of the texture layer, the data is drawn.
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //Get the texture id of view surface
        mTextureID = createTextureID();
        //Use this texture id to get the texture layer Surface Texture
        mSurface = new SurfaceTexture(mTextureID);
        //Monitor Texture Layer
        mSurface.setOnFrameAvailableListener(this);
        mDirectDrawer = new DirectDrawer(mTextureID);
        //Turn on the camera without preview
        CameraInterface.getInstance().doOpenCamera();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        //If you haven't previewed yet, start previewing
        if(!CameraInterface.getInstance().isPreviewing()){
            CameraInterface.getInstance().doStartPreview(mSurface);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        float[] mtx = new float[16];
        //Update texture image to the nearest frame from image stream
        mSurface.updateTexImage();
        mSurface.getTransformMatrix(mtx);
        mDirectDrawer.draw();

        /*int width = getWidth(), height = getHeight();

        byte b[] = new byte[width * (0 + height)];
        ByteBuffer byteBuffer = ByteBuffer.wrap(b);
        byteBuffer.position(0);

        GLES20.glReadPixels(0, 0, getWidth(), getHeight(), GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);

        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
        for (int i = 0, k = 0; i < height; i++, k++) {
            for (int j = 0; j < width; j++) {
                Log.i(TAG, "onDrawFrame ByteBuffer Pixel value: " + b[i * width + j]);
            }
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();
        CameraInterface.getInstance().doStopCamera();
    }

    private int createTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //Callback interface to notify new stream frames are available.
        //Log.i(TAG, "onFrameAvailable...");
        //If the texture layer has new data, it notifies view to draw
        this.requestRender();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }
}
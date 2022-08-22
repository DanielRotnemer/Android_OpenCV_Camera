package com.moviera.opengl1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraInterface {
    private Camera mCamera;
    private Camera.Parameters mParams;
    private boolean isPreviewing = false;
    private static CameraInterface mCameraInterface;

    private CameraInterface() {

    }
    public static synchronized CameraInterface getInstance(){
        if(mCameraInterface == null){
            mCameraInterface = new CameraInterface();
        }
        return mCameraInterface;
    }
    //Turn on the camera.
    public void doOpenCamera() {
        if(mCamera == null){
            mCamera = Camera.open();
        }else{
            doStopCamera();
        }
    }
    /*Preview Camera with TextureView*/
    public void doStartPreview(SurfaceTexture surface){
        if(isPreviewing){
            mCamera.stopPreview();
            return;
        }
        if(mCamera != null){
            try {
                //Preview the camera screen to the texture layer, the texture layer has data, and then inform view to draw, at this time, no preview started.
                mCamera.setPreviewTexture(surface);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Really open the preview, Camera.startPrieView()
            initCamera();
        }
    }

    /**
     * Stop previewing and release Camera
     */
    public void doStopCamera(){
        if(null != mCamera)
        {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            isPreviewing = false;
            mCamera.release();
            mCamera = null;
        }
    }
    /**
     * Photograph
     */
    public void doTakePicture(){
        if(isPreviewing && (mCamera != null)){
            mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
        }
    }

    public boolean isPreviewing(){
        return isPreviewing;
    }

    private void initCamera()
    {
        if (mCamera != null)
        {
            mParams = mCamera.getParameters();
            mParams.setPictureFormat(PixelFormat.JPEG);//Set up the format of the pictures stored after taking pictures
            mCamera.setDisplayOrientation(90);
            //Setting Camera to Continuous Autofocus Mode
            mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            mCamera.setParameters(mParams);
            mCamera.startPreview();//Open Preview
            //Setting Preview Flag
            isPreviewing = true;
        }
    }

    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback()
            //If this parameter is set to Null, there will be no clipping sound.
    {
        public void onShutter() {
        }
    };

    Camera.PictureCallback mJpegPictureCallback = new Camera.PictureCallback()
    {
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap b = null;
            if(null != data){
                b = BitmapFactory.decodeByteArray(data, 0, data.length);//Data is byte data, parsed into bitmaps
                mCamera.stopPreview();
                isPreviewing = false;
            }
            //Save the picture to sdcard
            if(null != b)
            {
                //The picture is rotated down here. The picture taken by the camera is upside down.
                Bitmap rotaBitmap = getRotateBitmap(b, 90.0f);
                saveBitmap(rotaBitmap);
            }
            //Enter Preview Again
            mCamera.startPreview();
            isPreviewing = true;
        }

    };

    //Rotating picture
    private Bitmap getRotateBitmap(Bitmap b, float rotateDegree){
        Matrix matrix = new Matrix();
        matrix.postRotate((float)rotateDegree);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
    }

    private static String initPath(){
        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/" + "PlayCamera";
        File f = new File(storagePath);
        if(!f.exists()){
            f.mkdir();
        }
        return storagePath;
    }

    private void saveBitmap(Bitmap b) {
        String path = initPath();
        String jpegName = path + "/" + System.currentTimeMillis() +".jpg";
        try {
            BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream(jpegName));
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
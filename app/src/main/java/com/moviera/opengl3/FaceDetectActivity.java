package com.moviera.opengl3;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.moviera.R;

public class FaceDetectActivity extends Activity {

    private GLSurfaceView mGLView = null;
    private CameraClass mCameraObject;
    private boolean appIsExiting=false;

    private native void CreateObjectNative(AssetManager assetManager, String pathToInternalDir);
    private native void DeleteObjectNative();
    private native void SetCameraPreviewDimsNative(int previewWidth, int previewHeight);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AssetManager assetManager = getAssets();
        String pathToInternalDir = getFilesDir().getAbsolutePath();

        // call the native constructors to create an object
        CreateObjectNative(assetManager, pathToInternalDir);

        mCameraObject = new CameraClass();
        if(!mCameraObject.IsResolutionSupported()) {
            ShowExitDialog(this, getString(R.string.exit_no_resolution));
            return;
        }
        SetCameraPreviewDimsNative(mCameraObject.GetPreviewWidth(), mCameraObject.GetPreviewHeight());

        // layout has only two components, a GLSurfaceView and a TextView
        setContentView(R.layout.activity_face_detect);
        mGLView = (MyGLSurfaceView) findViewById (R.id.gl_surface_view);

    }

    @Override
    protected void onResume() {

        super.onResume();

        if(appIsExiting) {
            return;
        }

        // Android suggests that we call onResume on GLSurfaceView
        if (mGLView != null) {
            mGLView.onResume();
        }

        // initialize the camera again in case activity was paused and resumed
        mCameraObject.InitializeCamera();
        mCameraObject.StartCamera();

    }

    @Override
    protected void onPause() {

        super.onPause();

        // Android suggests that we call onPause on GLSurfaceView
        if(mGLView != null) {
            mGLView.onPause();
        }

        mCameraObject.StopCamera();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        // We are exiting the activity, let's delete the native objects
        DeleteObjectNative();

    }

    public void ShowExitDialog(final Activity activity, String exitMessage){
        appIsExiting = true;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setMessage(exitMessage)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        activity.finish();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * load libCornerNative.so since it has all the native functions
     */
    static {
        System.loadLibrary("CornerNative");
    }
}

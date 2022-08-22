package com.moviera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Contour;
import com.google.android.gms.vision.face.Landmark;
import com.moviera.utilities.ImageConverter;
import com.moviera.utilities.SingleMediaScanner;
import com.moviera.utilities.Utilities;
import com.moviera.vision.*;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class CameraActivity extends AppCompatActivity
{
    private VisionCameraSource mCameraSource = null;
    private VisionCameraSourcePreview mPreview;
    private VisionGraphicOverlay mGraphicOverlay;
    private static final int RC_HANDLE_GMS = 9001;
    private static final String TAG = "CameraActivity";
    private ImageView flipCameraButton, cameraTimerButton, effects, upload, flashButton;
    private ConstraintLayout cameraControlsWrapper, filtersWrapper;

    private int cameraHeight = 1284, cameraWidth = 720;
    private int cameraFacing = VisionCameraSource.CAMERA_FACING_BACK;

    private VisionFilter currentFilter = new VisionFilter(VisionColors.COLOR_BROWN,
            VisionColors.COLOR_DARKPINK, VisionFilter.VISION_FILTER_LINECONTOURS, 1, true, false);

    private int lipstickColor = 0;
    private int COLOR_CHOICES[] =
    {
        0x00000000,
        0x9955037A,
        0x996E4730,
        0x99D61B1B,
        0x99740505,
        0x99113FD8,
        0x99051B66,
        0x991989A5,
        0x99F3E353,
        0x99F34DA0,
        0x996E0339,
        0x99026B51,
        0x990BC999,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.dark_color));
        }

        flipCameraButton = findViewById(R.id.flipCameraButton);
        flashButton = findViewById(R.id.flashButton);
        cameraTimerButton = findViewById(R.id.cameraTimerButton);
        if (Utilities.FrontCameraAvailable() == false) {
            flipCameraButton.setVisibility(View.GONE);
        }
        else
        {
            flipCameraButton.setOnClickListener(v -> {
                flipCamera();
            });
        }

        mPreview = (VisionCameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (VisionGraphicOverlay) findViewById(R.id.faceOverlay);
        filtersWrapper = findViewById(R.id.filtersWrapper);
        cameraControlsWrapper = findViewById(R.id.cameraControlsWrapper);

        mPreview.post(new Runnable()
        {
            @Override
            public void run() {
                cameraHeight = mPreview.getHeight();
                cameraWidth = mPreview.getWidth();
                mPreview.setCameraSize(cameraWidth, cameraHeight);
                createCameraSource();
                startCameraSource();
            }
        });

        effects = findViewById(R.id.effects);
        effects.post(new Runnable()
        {
            @Override
            public void run()
            {
                Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.hearts);
                Bitmap circularBitmap = ImageConverter.getRoundedCornerBitmap(bitmap, 45);
                effects.setImageBitmap(circularBitmap);

                int imagesToShow[] = { R.drawable.hearts, R.drawable.bright_heart, R.drawable.love_makeup1 };
                animateImageView(effects, imagesToShow, 0,true);
            }
        });

        upload = findViewById(R.id.upload);
        upload.post(new Runnable()
        {
            @Override
            public void run()
            {
                Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.upload);
                Bitmap circularBitmap = ImageConverter.getRoundedCornerBitmap(bitmap, 45);
                upload.setImageBitmap(circularBitmap);
            }
        });
    }

    private void animateImageView(final ImageView imageView, final int images[], final int imageIndex, final boolean forever)
    {
        int fadeInDuration = 500;
        int timeBetween = 3000;
        int fadeOutDuration = 1000;

        imageView.setVisibility(View.INVISIBLE);
        Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), images[imageIndex]);
        Bitmap circularBitmap = ImageConverter.getRoundedCornerBitmap(bitmap, 45);
        imageView.setImageBitmap(circularBitmap);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(1);
        imageView.setAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation) {
                if (images.length - 1 > imageIndex) {
                    animateImageView(imageView, images, imageIndex + 1,forever);
                }
                else {
                    if (forever == true){
                        animateImageView(imageView, images, 0, forever);
                    }
                }
            }
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
            }
        });
        animation.start();
    }

    private void createCameraSource()
    {
        int landmarkType = currentFilter.getFilter() == VisionFilter.VISION_FILTER_LETTERBOX ?
                FaceDetector.ALL_LANDMARKS : FaceDetector.CONTOUR_LANDMARKS;
        int mode = currentFilter.getFilter() == VisionFilter.VISION_FILTER_LETTERBOX ?
                FaceDetector.FAST_MODE : FaceDetector.SELFIE_MODE;

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setMode(mode)
                .setLandmarkType(landmarkType)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new VisionCameraSource.Builder(context, detector)
                .setRequestedPreviewSize(cameraHeight, cameraWidth)
                .setFacing(cameraFacing)
                .setRequestedFps(30.0f)
                .build();
    }

    private void startCameraSource()
    {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null)
        {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay, cameraHeight, cameraWidth);
            }
            catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    public void flipCamera()
    {
        if (cameraFacing == VisionCameraSource.CAMERA_FACING_BACK) {
            cameraFacing = VisionCameraSource.CAMERA_FACING_FRONT;
            flashButton.setVisibility(View.GONE);
        }
        else {
            cameraFacing = VisionCameraSource.CAMERA_FACING_BACK;
            flashButton.setVisibility(View.VISIBLE);
        }

        mCameraSource.release();
        mPreview.stop();

        mPreview = findViewById(R.id.preview);
        mGraphicOverlay = findViewById(R.id.faceOverlay);
        mPreview.setCameraSize(cameraWidth, cameraHeight);

        createCameraSource();
        startCameraSource();
    }

    public void startRecording_OnClick(View view)
    {
        mCameraSource.takePicture(null, takePictureCallback);
    }

    public void flashButton_OnClick(View view)
    {
        boolean success = false;
        if (mCameraSource.getFlashMode().equals(VisionCameraSource.FLASH_MODE_OFF)) {
            success = mCameraSource.setFlashMode(VisionCameraSource.FLASH_MODE_TORCH);
        }
        else {
            success = mCameraSource.setFlashMode(VisionCameraSource.FLASH_MODE_OFF);
        }
        if (success == false)
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(CameraActivity.this);
            alert.setMessage("Unable to set the requested flash mode");
            alert.show();
        }
    }

    private File getOutputMediaFile()
    {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        else
        {
            File outputFile = new File(Environment.getExternalStorageDirectory() + "/Moviera", new Date().getTime() + ".jpg");
            return outputFile;
        }
    }

    private Bitmap viewToBitmap(Bitmap capturedImage)
    {
        Bitmap viewBitmap = Bitmap.createBitmap(cameraWidth, cameraHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(viewBitmap);
        mGraphicOverlay.draw(canvas);
        return Utilities.OverlayBitmap(capturedImage, viewBitmap);
    }

    private VisionCameraSource.PictureCallback takePictureCallback = new VisionCameraSource.PictureCallback()
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try
            {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                if (cameraFacing == VisionCameraSource.CAMERA_FACING_FRONT) {
                    bitmap = Utilities.FlipBitmap(bitmap, true, false);
                }
                bitmap = Utilities.AdjustBitmapResolution(bitmap);

                Bitmap resultBitmap = viewToBitmap(bitmap);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                data = stream.toByteArray();

                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                new SingleMediaScanner(getBaseContext(), pictureFile); // REFRESH GALLERY
                camera.startPreview();
            }
            catch (FileNotFoundException e) {
                new androidx.appcompat.app.AlertDialog.Builder(getBaseContext()).setMessage("File not found.").show();
            }
            catch (IOException e) {
                new androidx.appcompat.app.AlertDialog.Builder(getBaseContext()).setMessage("Unable to get file data.").show();
            }
        }
    };

    public void effects_OnClick(View view)
    {
        cameraControlsWrapper.setVisibility(View.GONE);
        TranslateAnimation anim = new TranslateAnimation(0, 0, cameraHeight, cameraHeight - 300);
        anim.setDuration(600);
        anim.setFillEnabled(false);
        anim.setFillAfter(false);
        filtersWrapper.startAnimation(anim);
        filtersWrapper.setVisibility(View.VISIBLE);
    }

    public void cameraControlsContainer_OnClick(View view)
    {
        if (filtersWrapper.getVisibility() == View.GONE)
            return;
        TranslateAnimation anim = new TranslateAnimation(0, 0, cameraHeight - 300, cameraHeight);
        anim.setDuration(600);
        anim.setFillEnabled(false);
        anim.setFillAfter(false);
        filtersWrapper.startAnimation(anim);
        filtersWrapper.setVisibility(View.GONE);
        cameraControlsWrapper.setVisibility(View.VISIBLE);
    }

    public void filterTriggers_OnClick(View view)
    {
        lipstickColor = (lipstickColor + 1) % COLOR_CHOICES.length;

        TextView filterTv = (TextView)view;
        if (filterTv.getText().equals("1")) {
            currentFilter = new VisionFilter(COLOR_CHOICES[lipstickColor],
                    VisionColors.COLOR_DARKPINK, VisionFilter.VISION_FILTER_LIPSTICK, 1, true, false);
        }
        if (filterTv.getText().equals("2")) {
            currentFilter = new VisionFilter(VisionColors.COLOR_BROWN,
                    VisionColors.COLOR_DARKPINK, VisionFilter.VISION_FILTER_DOTCONTOURS, 1, true, false);
        }
        if (filterTv.getText().equals("3")) {
            currentFilter = new VisionFilter(VisionColors.COLOR_BROWN,
                    VisionColors.COLOR_DARKPINK, VisionFilter.VISION_FILTER_LINECONTOURS, 1, true, false);
        }
        if (filterTv.getText().equals("4")) {
            currentFilter = new VisionFilter(VisionColors.COLOR_BROWN,
                    VisionColors.COLOR_DARKPINK, VisionFilter.VISION_FILTER_LETTERBOX, 1, true, false);
        }
        if (filterTv.getText().equals("5")) {
            currentFilter = new VisionFilter(VisionColors.COLOR_BROWN,
                    VisionColors.COLOR_DARKPINK, VisionFilter.VISION_FILTER_COLOREDEYES, 1, true, false);
        }
        if (filterTv.getText().equals("6")) {
            currentFilter = new VisionFilter(VisionColors.COLOR_BROWN,
                    VisionColors.COLOR_DARKPINK, VisionFilter.VISION_FILTER_NONE, 1, true, false);
        }

        mCameraSource.release();
        mPreview.stop();

        mPreview = findViewById(R.id.preview);
        mGraphicOverlay = findViewById(R.id.faceOverlay);
        mPreview.setCameraSize(cameraWidth, cameraHeight);

        createCameraSource();
        startCameraSource();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        mPreview = findViewById(R.id.preview);
        mPreview.post(new Runnable()
        {
            @Override
            public void run() {
                cameraHeight = mPreview.getHeight();
                cameraWidth = mPreview.getWidth();
                mPreview.setCameraSize(cameraWidth, cameraHeight);
                createCameraSource();
                startCameraSource();
            }
        });
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face>
    {
        private VisionGraphicOverlay mOverlay;
        private VisionFaceGraphic mFaceGraphic;

        GraphicFaceTracker(VisionGraphicOverlay overlay)
        {
            mOverlay = overlay;
            mFaceGraphic = new VisionFaceGraphic(overlay, currentFilter);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }
}

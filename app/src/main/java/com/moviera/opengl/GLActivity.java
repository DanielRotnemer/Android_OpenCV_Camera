package com.moviera.opengl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import com.moviera.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class GLActivity extends AppCompatActivity implements GestureDetector.OnGestureListener
{
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private ConstraintLayout container;
    private CameraRenderer renderer;
    private TextureView textureView;
    private int filterId = R.id.filter0;
    private int mCurrentFilterId = 0;

    String[] TITLES = {"Original", "EdgeDectection", "Pixelize",
            "EMInterference", "TrianglesMosaic", "Legofied",
            "TileMosaic", "Blueorange", "ChromaticAberration",
            "BasicDeform", "Contrast", "NoiseWarp", "Refraction",
            "Mapping", "Crosshatch", "LichtensteinEsque",
            "AsciiArt", "MoneyFilter", "Cracked", "Polygonization",
            "JFAVoronoi", "BlackAndWhite", "Gray", "Negative",
            "Nostalgia", "Casting", "Relief", "Swirl", "HexagonMosaic",
            "Mirror", "Triple", "Cartoon", "WaterReflection"
    };

    Integer[] FILTER_RES_IDS = {R.id.filter0, R.id.filter1, R.id.filter2, R.id.filter3, R.id.filter4,
            R.id.filter5, R.id.filter6, R.id.filter7, R.id.filter8, R.id.filter9, R.id.filter10,
            R.id.filter11, R.id.filter12, R.id.filter13, R.id.filter14, R.id.filter15, R.id.filter16,
            R.id.filter17, R.id.filter18, R.id.filter19, R.id.filter20,
            R.id.filter21, R.id.filter22, R.id.filter23, R.id.filter24,
            R.id.filter25, R.id.filter26, R.id.filter27, R.id.filter28,
            R.id.filter29, R.id.filter30, R.id.filter31, R.id.filter32};

    ArrayList<Integer> mFilterArray = new ArrayList<>(Arrays.asList(FILTER_RES_IDS));

    GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gl);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(0x000000);
        }

        container = findViewById(R.id.container);

        setupCameraPreviewView();
        mGestureDetector = new GestureDetector(this, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupCameraPreviewView();
                }
            }
        }
    }

    void setupCameraPreviewView() {
        renderer = new CameraRenderer(this);
        textureView = new TextureView(this);
        container.addView(textureView, 0);
        textureView.setSurfaceTextureListener(renderer);

//        textureView.setOnTouchListener(this);
        textureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        textureView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                renderer.onSurfaceTextureSizeChanged(null, v.getWidth(), v.getHeight());
            }
        });
    }

    public boolean filters_OnClick(View view)
    {
        filterId = view.getId();
        if (renderer != null)
            renderer.setSelectedFilter(filterId);
        mCurrentFilterId = mFilterArray.indexOf(filterId);
        return true;
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        filterId = item.getItemId();

        // TODO: need tidy up
        if (filterId == R.id.capture) {
            Toast.makeText(this,
                    capture() ? "The capture has been saved to your sdcard root path." :
                            "Save failed!",
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        setTitle(item.getTitle());

        if (renderer != null)
            renderer.setSelectedFilter(filterId);
        mCurrentFilterId = mFilterArray.indexOf(filterId);
        return true;
    }*/

    private boolean capture() {
        String mPath = genSaveFileName(getTitle().toString() + "_", ".png");
        File imageFile = new File(mPath);
        if (imageFile.exists()) {
            imageFile.delete();
        }

        // create bitmap screen capture
        Bitmap bitmap = textureView.getBitmap();
        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private String genSaveFileName(String prefix, String suffix) {
        Date date = new Date();
        SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyyMMdd_hhmmss");
        String timeString = dateformat1.format(date);
        String externalPath = Environment.getExternalStorageDirectory().toString();
        return externalPath + "/" + prefix + timeString + suffix;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {

        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float velocity = Math.abs(velocityX) > Math.abs(velocityY) ? velocityX : velocityY;
        int step = velocity > 0 ? -1 : 1;
        mCurrentFilterId = circleLoop(TITLES.length, mCurrentFilterId, step);
        setTitle(TITLES[mCurrentFilterId]);
        if (renderer != null) {
            renderer.setSelectedFilter(FILTER_RES_IDS[mCurrentFilterId]);
        }
        return true;
    }

    private int circleLoop(int size, int currentPos, int step) {
        if (step == 0) {
            return currentPos;
        }

        if (step > 0) {
            if (currentPos + step >= size) {
                return (currentPos + step) % size;
            } else {
                return currentPos + step;
            }
        } else {
            if (currentPos + step < 0) {
                return currentPos + step + size;
            } else {
                return currentPos + step;
            }
        }
    }
}

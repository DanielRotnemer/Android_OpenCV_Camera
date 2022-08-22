package com.moviera.opengl.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.moviera.R;
import com.moviera.opengl.MyGLUtils;

public class CrosshatchFilter extends CameraFilter {
    private int program;

    public CrosshatchFilter(Context context) {
        super(context);

        // Build shaders
        program = MyGLUtils.buildProgram(context, R.raw.vertext, R.raw.crosshatch);
    }

    @Override
    public void onDraw(int cameraTexId, int canvasWidth, int canvasHeight) {
        setupShaderInputs(program,
                new int[]{canvasWidth, canvasHeight},
                new int[]{cameraTexId},
                new int[][]{});
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
package com.moviera.opengl5;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GLSurfaceView5 extends GLSurfaceView
{
    GLRenderer5 renderer;

    public GLSurfaceView5(Context context)
    {
        super(context);
        setEGLContextClientVersion(2);

        renderer = new GLRenderer5((GLActivity5) context);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public GLSurfaceView5(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setEGLContextClientVersion(2);

        renderer = new GLRenderer5((GLActivity5) context);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public GLRenderer5 getRenderer()
    {
        return renderer;
    }
}

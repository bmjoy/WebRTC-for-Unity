package org.webrtc;

import android.opengl.EGL14;
import android.opengl.EGLContext;

/**
 * Created by bhadriche on 8/3/2017.
 */

public class EglBase14Wrapper {
    EglBase14.Context eglBase14Context;

    public EglBase14Wrapper(EGLContext context) {
        this.eglBase14Context = new EglBase14.Context(context);
    }

    public EglBase14.Context getEglBaseContext() {
        return eglBase14Context;
        //return new EglBase14.Context(this.eglContext);
    }
}

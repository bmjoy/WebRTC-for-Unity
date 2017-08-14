package com.ibicha.webrtc;

import android.app.Activity;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.util.Log;

import org.webrtc.EglBase;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoRenderer;

/**
 * Created by bhadriche on 8/1/2017.
 */

public class UnityEGLUtils {
    private static final String TAG = UnityEGLUtils.class.getSimpleName();


    private static PeerConnectionFactory factory;
    private static EglBase rootEglBase;

    public static EGLContext unityContext = EGL14.EGL_NO_CONTEXT;
    public static EGLDisplay unityDisplay = EGL14.EGL_NO_DISPLAY;
    public static EGLSurface unityDrawSurface = EGL14.EGL_NO_SURFACE;
    public static EGLSurface unityReadSurface = EGL14.EGL_NO_SURFACE;

    public static PeerConnectionFactory getFactory(Activity mainActivity) {
        if (factory != null) {
            return factory;
        }
        PeerConnectionFactory.initializeAndroidGlobals(mainActivity.getApplicationContext(), WebRTC.HW_ACCELERATE);
        factory = new PeerConnectionFactory(new PeerConnectionFactory.Options());

        if (WebRTC.HW_ACCELERATE) {
            factory.setVideoHwAccelerationOptions(getRootEglBase().getEglBaseContext(), getRootEglBase().getEglBaseContext());
        }

        return factory;
    }


    private static EglBase getRootEglBase() {
        //If no acceleration, no need for any EGL work.
        if (!WebRTC.HW_ACCELERATE)
            return null;

        if (rootEglBase != null) {
            return rootEglBase;
        }

        EGLContext eglContext = unityContext; // getEglContext();
        EGLDisplay eglDisplay = unityDisplay; // getEglDisplay();
        int[] configAttributes = getEglConfigAttr(eglDisplay, eglContext);

        rootEglBase = EglBase.createEgl14(eglContext, configAttributes);
        rootEglBase.createDummyPbufferSurface();
        return rootEglBase;
    }

    //This is called from the Unity main thread, so we can keep a reference to it
    public static void setUnityContext() {
        unityContext = EGL14.eglGetCurrentContext();
        unityDisplay = EGL14.eglGetCurrentDisplay();
        unityDrawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
        unityReadSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
        Log.d(TAG, "eglContextSet: unityThread.getName() " + Thread.currentThread().getName());

        if (unityContext == EGL14.EGL_NO_CONTEXT) {
            Log.d(TAG, "eglContextSet: unityContext == EGL_NO_CONTEXT");
        }
        if (unityDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.d(TAG, "eglContextSet: unityDisplay == EGL_NO_DISPLAY");
        }
        if (unityDrawSurface == EGL14.EGL_NO_SURFACE) {
            Log.d(TAG, "eglContextSet: unityDrawSurface == EGL_NO_SURFACE");
        }
        if (unityReadSurface == EGL14.EGL_NO_SURFACE) {
            Log.d(TAG, "eglContextSet: unityReadSurface == EGL_NO_SURFACE");
        }
        Log.d(TAG, "eglContextSet: DONE");

    }

    private static int[] getEglConfigAttr(EGLDisplay eglDisplay, EGLContext eglContext) {
        int[] keys = {EGL14.EGL_CONFIG_ID};
        int[] configAttributes = new int[keys.length * 2 + 1];

        for (int i = 0; i < keys.length; i++) {
            configAttributes[i * 2] = keys[i];
            if (!EGL14.eglQueryContext(eglDisplay, eglContext, keys[i], configAttributes, i * 2 + 1)) {
                throw new RuntimeException("eglQueryContext failed: 0x" + Integer.toHexString(EGL14.eglGetError()));
            }
        }

        configAttributes[configAttributes.length - 1] = EGL14.EGL_NONE;
        return configAttributes;
    }
}

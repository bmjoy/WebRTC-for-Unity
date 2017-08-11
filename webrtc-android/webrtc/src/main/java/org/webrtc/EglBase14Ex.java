package org.webrtc;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.util.Log;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Field;

/**
 * Created by bhadriche on 8/11/2017.
 */

public class EglBase14Ex extends EglBase14 {

    private static final String TAG = EglBase14Ex.class.getSimpleName();

    private EglBase14Ex(Context sharedContext, int[] configAttributes) {
        super(sharedContext, configAttributes);
    }


    public static EglBase14Ex create(Context sharedContext, EGLDisplay eglDisplay,  int[] configAttributes, int clientVersion) {
        Objenesis objenesis = new ObjenesisStd();
        ObjectInstantiator thingyInstantiator = objenesis.getInstantiatorOf(EglBase14Ex.class);
        EglBase14Ex eglBase = (EglBase14Ex) thingyInstantiator.newInstance();

        eglBase.setEglSurface(EGL14.EGL_NO_SURFACE);
        eglBase.setEglDisplay(eglDisplay);
        eglBase.setEglConfig(s_getEglConfig(eglBase.getEglDisplay(), configAttributes));
        eglBase.setEglContext(s_createEglContext(sharedContext, eglBase.getEglDisplay(), eglBase.getEglConfig(), clientVersion));

        return eglBase;
    }

    private static Field eglContextField;
    private static Field eglConfigField;
    private static Field eglDisplayField;
    private static Field eglSurfaceField;

    public EGLSurface getEglSurface() {
        try {
            if (eglSurfaceField == null) {
                eglSurfaceField = EglBase14Ex.class.getSuperclass().getDeclaredField("eglSurface");
                eglSurfaceField.setAccessible(true);
            }
            return (EGLSurface) eglSurfaceField.get(this);
        } catch (Exception ex) {
            Log.e(TAG, "getEglSurface: ", ex);
            return null;
        }
    }

    private void setEglSurface(EGLSurface eglSurface) {
        try {
            if (eglSurfaceField == null) {
                eglSurfaceField = EglBase14Ex.class.getSuperclass().getDeclaredField("eglSurface");
                eglSurfaceField.setAccessible(true);
            }
            eglSurfaceField.set(this, eglSurface);
        } catch (Exception ex) {
            Log.e(TAG, "setEglSurface: ", ex);
        }
    }

    private EGLDisplay getEglDisplay() {
        try {
            if (eglDisplayField == null) {
                eglDisplayField = EglBase14Ex.class.getSuperclass().getDeclaredField("eglDisplay");
                eglDisplayField.setAccessible(true);
            }
            return (EGLDisplay) eglDisplayField.get(this);
        } catch (Exception ex) {
            Log.e(TAG, "getEglDisplay: ", ex);
            return null;
        }
    }

    private void setEglDisplay(EGLDisplay eglDisplay) {
        try {
            if (eglDisplayField == null) {
                eglDisplayField = EglBase14Ex.class.getSuperclass().getDeclaredField("eglDisplay");
                eglDisplayField.setAccessible(true);
            }
            eglDisplayField.set(this, eglDisplay);
        } catch (Exception ex) {
            Log.e(TAG, "setEglDisplay: ", ex);
        }
    }

    private EGLConfig getEglConfig() {
        try {
            if (eglConfigField == null) {
                eglConfigField = EglBase14Ex.class.getSuperclass().getDeclaredField("eglConfig");
                eglConfigField.setAccessible(true);
            }
            return (EGLConfig) eglConfigField.get(this);
        } catch (Exception ex) {
            Log.e(TAG, "getEglConfig: ", ex);
            return null;
        }
    }

    private void setEglConfig(EGLConfig eglConfig) {
        try {
            if (eglConfigField == null) {
                eglConfigField = EglBase14Ex.class.getSuperclass().getDeclaredField("eglConfig");
                eglConfigField.setAccessible(true);
            }
            eglConfigField.set(this, eglConfig);
        } catch (Exception ex) {
            Log.e(TAG, "setEglConfig: ", ex);
        }
    }

    public EGLContext getEglContext() {
        try {
            if (eglContextField == null) {
                eglContextField = EglBase14Ex.class.getSuperclass().getDeclaredField("eglContext");
                eglContextField.setAccessible(true);
            }
            return (EGLContext) eglContextField.get(this);
        }catch (Exception ex) {
            Log.e(TAG, "getEglContext: ", ex);
            return null;
        }
    }

    private void setEglContext(EGLContext eglContext) {
        try {
            if (eglContextField == null) {
                eglContextField = EglBase14Ex.class.getSuperclass().getDeclaredField("eglContext");
                eglContextField.setAccessible(true);
            }
            eglContextField.set(this, eglContext);
        } catch (Exception ex) {
            Log.e(TAG, "setEglContext: ", ex);
        }
    }

    private static EGLDisplay s_getEglDisplay() {
        EGLDisplay eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("Unable to get EGL14 display");
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw new RuntimeException("Unable to initialize EGL14");
        }
        return eglDisplay;
    }

    private static EGLConfig s_getEglConfig(EGLDisplay eglDisplay, int[] configAttributes) {
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(
                eglDisplay, configAttributes, 0, configs, 0, configs.length, numConfigs, 0)) {
            throw new RuntimeException("Unable to find any matching EGL config");
        }
        return configs[0];
    }

    private static EGLContext s_createEglContext(EglBase14.Context sharedContext, EGLDisplay eglDisplay, EGLConfig eglConfig, int clientVersion) {
        if (sharedContext != null && getEgl14Context(sharedContext) == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("Invalid sharedContext");
        }
        int[] contextAttributes = {EGL14.EGL_CONTEXT_CLIENT_VERSION, clientVersion, EGL14.EGL_NONE};
        EGLContext rootContext =
                sharedContext == null ? EGL14.EGL_NO_CONTEXT : getEgl14Context(sharedContext);
        EGLContext eglContext =
                EGL14.eglCreateContext(eglDisplay, eglConfig, rootContext, contextAttributes, 0);
        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("Failed to create EGL context");
        }
        return eglContext;
    }

    private static Field egl14ContextField;

    private static EGLContext getEgl14Context(EglBase14.Context sharedContext) {
        try {
            if (egl14ContextField == null) {
                egl14ContextField = EglBase14.Context.class.getDeclaredField("egl14Context");
                egl14ContextField.setAccessible(true);
            }
            return (EGLContext) egl14ContextField.get(sharedContext);
        } catch (Exception ex) {
            Log.e(TAG, "getEgl14Context: ", ex);
            return null;
        }
    }
}

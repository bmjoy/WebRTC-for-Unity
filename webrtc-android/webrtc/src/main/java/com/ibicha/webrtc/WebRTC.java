package com.ibicha.webrtc;

import android.app.Activity;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.util.Log;
import android.widget.Toast;

import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.lang.reflect.Field;

/**
 * Created by bhadriche on 7/31/2017.
 */

public class WebRTC  {
    private static final String TAG = WebRTC.class.getSimpleName();

    public static final boolean hwAccelerate = true;

    private static Activity _mainActivity;
    public static Activity getMainActivity() {
        try {
            if (_mainActivity == null) {
                Class<?> unityPlayerClass = Class.forName("com.unity3d.player.UnityPlayer");
                Field currentActivityField = unityPlayerClass.getDeclaredField("currentActivity");
                _mainActivity = (Activity) currentActivityField.get(null);
            }
            return _mainActivity;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void StartScreenCapture(ScreenCaptureCallback callback) {
        ScreenCapture.getInstance().StartScreenCapture(getMainActivity(), callback);
    }

    public static void StartCameraCapture(boolean fontCamera, ScreenCaptureCallback callback) {
        CameraCapture.StartCameraCapture(getMainActivity(), fontCamera, callback);
    }

    private static Toast logToast;
    private static void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(getMainActivity(), msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

}

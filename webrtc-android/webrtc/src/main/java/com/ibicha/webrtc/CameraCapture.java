package com.ibicha.webrtc;

import android.app.Activity;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.util.Log;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.Logging;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import javax.microedition.khronos.egl.EGL10;


/**
 * Created by bhadriche on 7/31/2017.
 */

public class CameraCapture {
    private static final String TAG = CameraCapture.class.getSimpleName();

    private static final int HD_VIDEO_WIDTH = 1280;
    private static final int HD_VIDEO_HEIGHT = 720;

    private static int videoWidth;
    private static int videoHeight;
    private static int videoFps;

    static void StartCameraCapture(Activity mainActivity, boolean frontCamera, ScreenCaptureCallback callback) {
        StartCameraCapture(mainActivity, frontCamera, callback, 0, 0, 0);
    }

    static void StartCameraCapture(Activity mainActivity, boolean frontCamera, final ScreenCaptureCallback callback, int videoWidth, int videoHeight, int videoFps) {
        final VideoCapturer videoCapturer;
        if (frontCamera) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(mainActivity.getApplicationContext()));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }

        if (videoWidth == 0 || videoHeight == 0) {
            videoWidth = HD_VIDEO_WIDTH;
            videoHeight = HD_VIDEO_HEIGHT;
        }
        if (videoFps == 0) {
            videoFps = 30;
        }
        CameraCapture.videoWidth = videoWidth;
        CameraCapture.videoHeight = videoHeight;
        CameraCapture.videoFps = videoFps;


        PeerConnectionFactory.initializeAndroidGlobals(mainActivity.getApplicationContext(), WebRTC.hwAccelerate);
        final PeerConnectionFactory factory = new PeerConnectionFactory(new PeerConnectionFactory.Options());

        final int finalVideoWidth = videoWidth;
        final int finalVideoHeight = videoHeight;
        final int finalVideoFps = videoFps;

        UnityEGLContext.attachToUnity(factory);

        VideoSource videoSource = factory.createVideoSource(videoCapturer);
        videoCapturer.startCapture(finalVideoWidth, finalVideoHeight, finalVideoFps);
        VideoTrack videoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
        videoTrack.setEnabled(true);
        videoTrack.addRenderer(new VideoRenderer(new VideoRenderer.Callbacks() {
            @Override
            public void renderFrame(VideoRenderer.I420Frame i420Frame) {
                Log.d(TAG, "renderFrame: texture:" + i420Frame.textureId + " size:" + i420Frame.width + "x" + i420Frame.height +
                " yuvFrame:" + i420Frame.yuvFrame);
                callback.renderFrame(i420Frame);
            }
        }));
        Log.d(TAG, "onVideoCapturerStarted");
        callback.onVideoCapturerStarted(videoTrack);

    }


    private static VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

}

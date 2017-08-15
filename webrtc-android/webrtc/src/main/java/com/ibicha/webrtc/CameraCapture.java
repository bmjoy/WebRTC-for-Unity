package com.ibicha.webrtc;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.Logging;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;


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

    static void StartCameraCapture(Activity mainActivity, boolean frontCamera, VideoCallback callback) {
        StartCameraCapture(mainActivity, frontCamera, callback, 0, 0, 0);
    }

    static void StartCameraCapture(Activity mainActivity, boolean frontCamera, final VideoCallback callback, int videoWidth, int videoHeight, int videoFps) {
        final VideoCapturer videoCapturer = createCameraCapturer(mainActivity.getApplicationContext(), frontCamera);

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


        final int finalVideoWidth = videoWidth;
        final int finalVideoHeight = videoHeight;
        final int finalVideoFps = videoFps;

        VideoSource videoSource = UnityEGLUtils.getFactory(mainActivity).createVideoSource(videoCapturer);
        videoCapturer.startCapture(finalVideoWidth, finalVideoHeight, finalVideoFps);
        VideoTrack videoTrack = UnityEGLUtils.getFactory(mainActivity).createVideoTrack("ARDAMSv0", videoSource);
        videoTrack.setEnabled(true);
        videoTrack.addRenderer(new VideoRenderer(new VideoRenderer.Callbacks() {
            @Override
            public void renderFrame(VideoRenderer.I420Frame i420Frame) {
                if (i420Frame.yuvFrame) {
                    throw new UnsupportedOperationException("Only texture frames.");
                }
                Log.d(TAG, "renderFrame: texture:" + i420Frame.textureId + " size:" + i420Frame.width + "x" + i420Frame.height +
                        " rotation:" + i420Frame.rotationDegree);
                callback.renderFrame(i420Frame.rotatedWidth(), i420Frame.rotatedHeight(), i420Frame.textureId, i420Frame);

            }
        }));
        Log.d(TAG, "onVideoCapturerStarted");
        callback.onVideoCapturerStarted(videoCapturer, videoTrack);
    }

    private static VideoCapturer createCameraCapturer(Context context, boolean frontCamera) {
        CameraEnumerator enumerator = new Camera1Enumerator(WebRTC.HW_ACCELERATE);// new Camera2Enumerator(context);
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if ((enumerator.isFrontFacing(deviceName) && frontCamera) || (enumerator.isBackFacing(deviceName) && !frontCamera)) {
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
            Logging.d(TAG, "Creating other camera capturer.");
            VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
            if (videoCapturer != null) {
                return videoCapturer;
            }

        }

        return null;
    }

}

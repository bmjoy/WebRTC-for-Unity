package com.ibicha.webrtc;

import android.app.Activity;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES31;
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

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

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
        if (frontCamera && WebRTC.hwAccelerate) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(mainActivity.getApplicationContext()));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(WebRTC.hwAccelerate));
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
                if (i420Frame.yuvFrame) {
                    Log.d(TAG, i420Frame.toString());
                } else {
                    Log.d(TAG, "renderFrame: texture:" + i420Frame.textureId + " size:" + i420Frame.width + "x" + i420Frame.height +
                            " yuvFrame:" + i420Frame.yuvFrame);
                }
                callback.renderFrame(i420Frame);
            }
        }));
        Log.d(TAG, "onVideoCapturerStarted");
        callback.onVideoCapturerStarted(videoTrack);

    }

    private static void copyPlane(ByteBuffer src, ByteBuffer dst) {
        src.position(0).limit(src.capacity());
        dst.put(src);
        dst.position(0).limit(dst.capacity());
    }

    public static byte[] getFrameBuffer(VideoRenderer.I420Frame src) {
        if (src.yuvStrides[0] != src.width)
            return convertLineByLine(src);
        if (src.yuvStrides[1] != src.width / 2)
            return convertLineByLine(src);
        if (src.yuvStrides[2] != src.width / 2)
            return convertLineByLine(src);

        byte[] bytes = new byte[src.yuvStrides[0] * src.height +
                src.yuvStrides[1] * src.height / 2 +
                src.yuvStrides[2] * src.height / 2];
        ByteBuffer tmp = ByteBuffer.wrap(bytes, 0, src.width * src.height);
        copyPlane(src.yuvPlanes[0], tmp);

        byte[] tmparray = new byte[src.width / 2 * src.height / 2];
        tmp = ByteBuffer.wrap(tmparray, 0, src.width / 2 * src.height / 2);

        copyPlane(src.yuvPlanes[2], tmp);
        for (int row = 0; row < src.height / 2; row++) {
            for (int col = 0; col < src.width / 2; col++) {
                bytes[src.width * src.height + row * src.width + col * 2] = tmparray[row * src.width / 2 + col];
            }
        }
        copyPlane(src.yuvPlanes[1], tmp);
        for (int row = 0; row < src.height / 2; row++) {
            for (int col = 0; col < src.width / 2; col++) {
                bytes[src.width * src.height + row * src.width + col * 2 + 1] = tmparray[row * src.width / 2 + col];
            }
        }
        return bytes;


//        byte[] buffer = new byte[3*4+
//                i420Frame.yuvStrides[0] * i420Frame.height +
//                i420Frame.yuvStrides[1] * i420Frame.height / 2 +
//                i420Frame.yuvStrides[2] * i420Frame.height / 2 + 1000000];
//
//        ByteBuffer intBuffer = ByteBuffer.allocate(4);
//        int index = 0;
//        for (int i = 0; i < 3; i++) {
//            intBuffer.putInt(0, i420Frame.yuvStrides[i]);
//            intBuffer.rewind();
//            intBuffer.get(buffer, index, 4);
//            index+=4;
//        }
//        for (int i = 0; i < 3; i++) {
//            int size = i == 0 ? i420Frame.yuvStrides[i] * i420Frame.height : i420Frame.yuvStrides[i] * i420Frame.height / 2;
//            i420Frame.yuvPlanes[i].get(buffer, index, size);
//            index += size;
//        }
//
//        return  buffer;
    }

    public static byte[] convertLineByLine(org.webrtc.VideoRenderer.I420Frame src) {
        byte[] bytes = new byte[src.width * src.height * 3 / 2];
        int i = 0;
        byte[] plane0Bytes = new byte[src.yuvPlanes[0].capacity()];
        byte[] plane1Bytes = new byte[src.yuvPlanes[1].capacity()];
        byte[] plane2Bytes = new byte[src.yuvPlanes[2].capacity()];
        src.yuvPlanes[0].get(plane0Bytes, 0, plane0Bytes.length);
        src.yuvPlanes[1].get(plane1Bytes, 0, plane1Bytes.length);
        src.yuvPlanes[2].get(plane2Bytes, 0, plane2Bytes.length);

        for (int row = 0; row < src.height; row++) {
            for (int col = 0; col < src.width; col++) {
                bytes[i++] = plane0Bytes[col + row * src.yuvStrides[0]];
            }
        }
        for (int row = 0; row < src.height / 2; row++) {
            for (int col = 0; col < src.width / 2; col++) {
                bytes[i++] = plane2Bytes[col + row * src.yuvStrides[2]];
                bytes[i++] = plane1Bytes[col + row * src.yuvStrides[1]];
            }
        }
        return bytes;

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

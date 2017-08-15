package com.ibicha.webrtc;

import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

/**
 * Created by bhadriche on 8/15/2017.
 */

public interface VideoCallback {
    public void onVideoCapturerStarted(VideoCapturer videoCapturer, VideoTrack videoTrack);

    public void renderFrame(int width, int height, int texture, VideoRenderer.I420Frame i420Frame);

    public void onVideoCapturerStopped();

    public void onVideoCapturerError(String error);

}

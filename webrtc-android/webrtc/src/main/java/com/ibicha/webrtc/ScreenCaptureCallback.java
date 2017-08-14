package com.ibicha.webrtc;

import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

/**
 * Created by bhadriche on 7/31/2017.
 */

public interface ScreenCaptureCallback {
    public void onVideoCapturerStarted(VideoTrack capturer);

    public void renderFrame(int width, int height, int texture, VideoRenderer.I420Frame i420Frame);

    public void onVideoCapturerStopped();

    public void onVideoCapturerError(String error);

}

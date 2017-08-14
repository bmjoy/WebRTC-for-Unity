package com.ibicha.webrtc;

import android.content.Intent;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

/**
 * Created by bhadriche on 7/31/2017.
 */

public class WebRTCActivity extends UnityPlayerActivity {

    public UnityPlayer getUnityPlayer() {
        return mUnityPlayer;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultHelper.triggerOnActivityResult(requestCode, resultCode, data);
    }

}

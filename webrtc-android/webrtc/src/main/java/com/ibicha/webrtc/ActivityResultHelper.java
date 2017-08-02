package com.ibicha.webrtc;

import android.content.Intent;

import java.util.ArrayList;

/**
 * Created by bhadriche on 7/31/2017.
 */

public class ActivityResultHelper {
    public interface ActivityResultListener
    {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    private static ArrayList<ActivityResultListener> listeners = new ArrayList<ActivityResultListener> ();

    public static boolean addListener (ActivityResultListener listener)
    {
        if(!listeners.contains(listener)) {
            return listeners.add(listener);
        }
        return false;
    }

    public static boolean removeListener (ActivityResultListener listener)
    {
        return listeners.remove(listener);
    }

    public static void removeListeners ()
    {
        listeners.clear();
    }


    public static void triggerOnActivityResult(int requestCode, int resultCode, Intent data) {
        for (ActivityResultListener listener : listeners)
        {
            if(listener!=null) {
                listener.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}

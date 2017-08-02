package com.ibicha.webrtc;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by bhadriche on 8/1/2017.
 */

public class ThreadUtils {

    private static ThreadUtils _instance;
    private static ThreadUtils getInstance() {
        if (_instance == null) {
            _instance = new ThreadUtils();
            _instance.queue = new LinkedList<Runnable>();
        }
        return _instance;
    }

    private Queue<Runnable> queue;

    public static void RunOnRenderThread(Runnable runnable) {
        getInstance().queue.add(runnable);
    }

    public void doQueue() {
        while (!getInstance().queue.isEmpty()){
            getInstance().queue.poll().run();
        }
    }
}

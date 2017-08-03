using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;

namespace iBicha {
	public class FrameKiller : MonoBehaviour {

		[RuntimeInitializeOnLoadMethod]
		static void createInstance()
		{
			if (_instance == null) {
				GameObject go = Camera.main.gameObject;
				_instance = go.AddComponent<FrameKiller> ();
			}
		}

		private static FrameKiller _instance;
		private static FrameKiller Instance {
			get {
				createInstance ();
				return _instance;
			}
		}

		private static Queue<AndroidJavaObject> queue = new Queue<AndroidJavaObject> ();

		public static void ScheduleKillFrame(AndroidJavaObject frame) {
			queue.Enqueue (frame);
		}

		/*void OnPreRender() {
			while (ThreadUtils.queue.Count>0) {
				Action action = ThreadUtils.queue.Dequeue();
				action.Invoke ();
			}
		}*/


		public void OnPostRender()
		{
			while (queue.Count>0) {
				AndroidJavaObject frame = queue.Dequeue();
				WebRTCAndroid.KillFrame (frame);
			}
		}
	}
}

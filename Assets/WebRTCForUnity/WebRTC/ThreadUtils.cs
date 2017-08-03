using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;

namespace iBicha
{
	public class ThreadUtils : MonoBehaviour {
		[RuntimeInitializeOnLoadMethod]
		static void createInstance()
		{
			if (_instance == null) {
				GameObject go = new GameObject ("{ThreadUtils}");
				_instance = go.AddComponent<ThreadUtils> ();
			}
		}

		private static ThreadUtils _instance;
		private static ThreadUtils Instance {
			get {
				createInstance ();
				return _instance;
			}
		}

		public static Queue<Action> queue = new Queue<Action> ();

		public static void RunOnRenderThread(Action action) {
			queue.Enqueue (action);
		}


		void Update () {
			while (queue.Count>0) {
				Action action = queue.Dequeue();
				action.Invoke ();
			}
		}
	}

}

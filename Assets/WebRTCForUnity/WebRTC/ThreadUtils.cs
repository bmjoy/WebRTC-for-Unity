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
				GameObject go = new GameObject ("{ThreadUtils}");;
				_instance = go.AddComponent<ThreadUtils> ();
				DontDestroyOnLoad (go);
			}
		}

		private static ThreadUtils _instance;
		private static ThreadUtils Instance {
			get {
				createInstance ();
				return _instance;
			}
		}

		private int camerasLeft = 0;

		void Awake() {
			Camera.onPreRender += onPreRender;
			Camera.onPostRender += onPostRender;
		}

		void OnDestroy() {
			Camera.onPreRender -= onPreRender;
			Camera.onPostRender -= onPostRender;
		}

		public static Queue<Action> updateQueue = new Queue<Action> ();
		public static Queue<Action> preRenderQueue = new Queue<Action> ();
		public static Queue<Action> postRenderQueue = new Queue<Action> ();
		private static object updateQueueLock = new object();
		private static object preRenderQueueLock = new object();
		private static object postRenderQueueLock = new object();

		public static void RunOnUpdate(Action action) {
			lock (updateQueueLock)  
			{  
				updateQueue.Enqueue (action);
			}  

		}

		public static void RunOnPreRender(Action action) {
			lock (preRenderQueueLock)  
			{  
				preRenderQueue.Enqueue (action);
			}  

		}
		public static void RunOnPostRender(Action action) {
			lock (postRenderQueueLock)  
			{  
				postRenderQueue.Enqueue (action);
			}  
		}

		void onPreRender(Camera cam) {
			if (camerasLeft <= 0) {
				camerasLeft = Camera.allCamerasCount;
				onPreRenderLoop ();
			}
		}

		void onPostRender(Camera cam)
		{
			camerasLeft--;
			if (camerasLeft <= 0) {
				onPostRenderLoop ();
			}
		}

		void Update () {
			onUpdateLoop ();
		}
			
		void onPreRenderLoop() {
			lock (preRenderQueueLock)  
			{  
				DrainQueue (preRenderQueue);
			}  
		}


		void onPostRenderLoop()
		{
			lock (postRenderQueueLock)  
			{  
				DrainQueue (postRenderQueue);
			}  
		}

		void onUpdateLoop () {
			lock (updateQueueLock)  
			{  
				DrainQueue (updateQueue);
			}  
		}

		void DrainQueue(Queue<Action> queue) {
			while (queue.Count>0) {
				Action action = queue.Dequeue();
				if (action != null) {
					action.Invoke ();
				}
			}
		}
	}

}

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
				GameObject go = Camera.main.gameObject;
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

		void Awake() {
			_instance = this;
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


		void Update () {
			lock (updateQueueLock)  
			{  
				DrainQueue (updateQueue);
			}  
		}


		void OnPreRender() {
			lock (preRenderQueueLock)  
			{  
				DrainQueue (preRenderQueue);
			}  
		}

		public void OnPostRender()
		{
			lock (postRenderQueueLock)  
			{  
				DrainQueue (postRenderQueue);
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

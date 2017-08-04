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

		public static void RunOnUpdate(Action action) {
			updateQueue.Enqueue (action);
		}

		public static void RunOnPreRender(Action action) {
			preRenderQueue.Enqueue (action);
		}
		public static void RunOnPostRender(Action action) {
			postRenderQueue.Enqueue (action);
		}


		void Update () {
			DrainQueue (updateQueue);
		}


		void OnPreRender() {
			DrainQueue (preRenderQueue);
		}

		public void OnPostRender()
		{
			DrainQueue (postRenderQueue);
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

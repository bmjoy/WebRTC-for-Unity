using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Events;
using System;


namespace iBicha {
	public class VideoRendererBehaviour : MonoBehaviour {
		public Texture idleTexture; 
		public Texture errorTexture;

		public bool renderToOwn = true;

		public List<Material> materials;

		public UnityEvent CaptureStarted;
		public UnityEvent CaptureStopped;
		public CaptureErrorEvent CaptureError;
		public CaptureTextureEvent CaptureTexture;

		private bool isRendering = false;

		void Start ()
		{
			if (renderToOwn) {
				Renderer rend = GetComponent<Renderer> ();
				if (rend != null) {
					materials.Add (rend.material);
				}
			}

			if (idleTexture != null) {
				for (int i = 0; i < materials.Count; i++) {
					materials [i].mainTexture = idleTexture;
				}
			}
		}

		public void OnVideoCapturerStarted() {
			isRendering = true;
			CaptureStarted.Invoke ();
		}

		public void OnVideoCapturerStopped() {
			isRendering = false;
			for (int i = 0; i < materials.Count; i++) {
				materials [i].mainTexture = idleTexture;
			}
			CaptureStopped.Invoke ();
		}

		public void OnVideoCapturerError(string error) {
			isRendering = false;
			Debug.LogError ("OnVideoCapturerError " + error);
			for (int i = 0; i < materials.Count; i++) {
				materials [i].mainTexture = errorTexture;
			}
			CaptureError.Invoke (error);
		}

		public void OnTexture(Texture texture) {
			for (int i = 0; i < materials.Count; i++) {
				materials [i].mainTexture = texture;
			}
			CaptureTexture.Invoke (texture);
		}
	}
}

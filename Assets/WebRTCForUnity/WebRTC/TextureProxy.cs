using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;

namespace iBicha
{
	public class TextureProxy : MonoBehaviour
	{
		public Texture idleTexture;
		public Texture errorTexture;

		public ScreenCaptureCallback screenCaptureCallback;

		public Material[] materials;
		// Use this for initialization
		void Start ()
		{
			if (materials == null || materials.Length == 0) {
				Renderer rend = GetComponent<Renderer> ();
					
				if (rend != null) {
					materials = new Material[1];
					materials [0] = rend.material;
				}
			}

			if (materials == null || materials.Length == 0) {
				this.enabled = false;
				return;
			}

			for (int i = 0; i < materials.Length; i++) {
				materials [i].mainTexture = idleTexture;
			}

			screenCaptureCallback = new ScreenCaptureCallback ();

			screenCaptureCallback.OnVideoCapturerStarted += OnVideoCapturerStarted;
			screenCaptureCallback.OnVideoCapturerStopped += OnVideoCapturerStopped;
			screenCaptureCallback.OnVideoCapturerError += OnVideoCapturerError;
			screenCaptureCallback.OnTexture += OnTexture;
		}

		void Destroy() {
			screenCaptureCallback.OnVideoCapturerStarted -= OnVideoCapturerStarted;
			screenCaptureCallback.OnVideoCapturerStopped -= OnVideoCapturerStopped;
			screenCaptureCallback.OnVideoCapturerError -= OnVideoCapturerError;
			screenCaptureCallback.OnTexture -= OnTexture;
		}

		void OnVideoCapturerStarted() {
			print ("OnVideoCapturerStarted");
		}

		void OnVideoCapturerStopped() {
			print ("onVideoCapturerStopped");
			for (int i = 0; i < materials.Length; i++) {
				materials [i].mainTexture = idleTexture;
			}
		}

		void OnVideoCapturerError(string error) {
			print ("OnVideoCapturerError " + error);
			for (int i = 0; i < materials.Length; i++) {
				materials [i].mainTexture = errorTexture;
			}
		}

		void OnTexture(Texture texture) {
			for (int i = 0; i < materials.Length; i++) {
				materials [i].mainTexture = texture;
			}
		}

	}



}
 
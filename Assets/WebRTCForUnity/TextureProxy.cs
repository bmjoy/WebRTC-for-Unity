using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace iBicha
{
	public class TextureProxy : MonoBehaviour {
		public Material material;
		public ScreenCaptureCallback screenCaptureCallback;
		// Use this for initialization
		void Start () {
			if (material == null) {
				enabled = false;
				return;
			}
			screenCaptureCallback = new ScreenCaptureCallback();
			screenCaptureCallback.OnVideoCapturerStarted += () => {
				print("OnVideoCapturerStarted");
			};
			screenCaptureCallback.OnVideoCapturerStopped += () => {
				print("onVideoCapturerStopped");
			};
			screenCaptureCallback.OnVideoCapturerError += (error) => {
				print("OnVideoCapturerError " + error);
			};
			screenCaptureCallback.OnTexture += (texture) => {
				material.mainTexture = texture;
			};

		}

	}

}
 
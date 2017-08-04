using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace iBicha
{
	public class TextureProxy : MonoBehaviour {
		public ScreenCaptureCallback screenCaptureCallback;
		public Material[] materials;
		// Use this for initialization
		void Start () {
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
				for (int i = 0; i < materials.Length; i++) {
					materials[i].mainTexture = texture;
				}
			};
		}
	}
}
 
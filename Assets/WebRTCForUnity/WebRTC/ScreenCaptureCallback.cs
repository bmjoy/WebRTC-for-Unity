using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;

namespace iBicha {
	public class ScreenCaptureCallback : AndroidJavaProxy {
		public Texture2D Texture;

		public event Action OnVideoCapturerStarted;
		public event Action<Texture2D> OnTexture;
		public event Action OnVideoCapturerStopped;
		public event Action<string> OnVideoCapturerError;


		public ScreenCaptureCallback () : base ("com.ibicha.webrtc.ScreenCaptureCallback") {
		}

		public static void KillFrame(AndroidJavaObject i420Frame) {
			ThreadUtils.RunOnPostRender (() => {
				WebRTCAndroid.KillFrame(i420Frame);
			});
		}
		public void onVideoCapturerStarted(AndroidJavaObject capturer){
			ThreadUtils.RunOnUpdate (() => {
				Action OnVideoCapturerStartedHandler = OnVideoCapturerStarted;
				if (OnVideoCapturerStartedHandler != null) {
					OnVideoCapturerStartedHandler ();
				}
			});
		}


		public void renderFrame(AndroidJavaObject i420Frame){
			ThreadUtils.RunOnPreRender (() => {
				WebRTCAndroid.switchToUnityContext();
				IntPtr textureId = new IntPtr(i420Frame.Get<int> ("textureId"));
				if(Texture == null) {
					int width = i420Frame.Get<int> ("width");
					int height = i420Frame.Get<int> ("height");
					Debug.Log(string.Format("GotFrame: {0}x{1}", width,height));
					Texture = Texture2D.CreateExternalTexture (width, height, TextureFormat.RGB24, false, false, textureId); 
				} else {
					Texture.UpdateExternalTexture(textureId);
				}

				Action<Texture2D> OnTextureHandler = OnTexture;
				if (OnTextureHandler != null) {
					OnTextureHandler (Texture);
					KillFrame(i420Frame);
				}

			});
		}

		public void onVideoCapturerStopped(){
			Texture = null;
			ThreadUtils.RunOnUpdate (() => {
				Action OnVideoCapturerStoppedHandler = OnVideoCapturerStopped;
				if (OnVideoCapturerStoppedHandler != null) {
					OnVideoCapturerStoppedHandler ();
				}
			});
		}

		public void onVideoCapturerError(string error){
			Texture = null;
			ThreadUtils.RunOnUpdate (() => {
				Action<string> OnVideoCapturerErrorHandler = OnVideoCapturerError;
				if (OnVideoCapturerErrorHandler != null) {
					OnVideoCapturerErrorHandler (error);
				}
			});
		}
	}

}

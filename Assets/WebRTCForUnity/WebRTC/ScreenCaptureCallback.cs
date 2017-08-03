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

		public void onVideoCapturerStarted(AndroidJavaObject capturer){
			ThreadUtils.RunOnRenderThread (() => {
				Action OnVideoCapturerStartedHandler = OnVideoCapturerStarted;
				if (OnVideoCapturerStartedHandler != null) {
					OnVideoCapturerStartedHandler ();
				}
			});
		}


		public void renderFrame(AndroidJavaObject i420Frame){
			ThreadUtils.RunOnRenderThread (() => {
				FrameKiller.ScheduleKillFrame (i420Frame);
				//WebRTCAndroid.switchToUnityContext();
				IntPtr textureId = new IntPtr(i420Frame.Get<int> ("textureId"));
				if(Texture == null) {
					int width = i420Frame.Get<int> ("width");
					int height = i420Frame.Get<int> ("height");
					Texture = Texture2D.CreateExternalTexture (width, height, TextureFormat.RGBA32, false, false, textureId); 
					Action<Texture2D> OnTextureHandler = OnTexture;
					if (OnTextureHandler != null) {
						OnTextureHandler (Texture);
					}
				} else {
					Texture.UpdateExternalTexture(textureId);
				}
			});
		}

		public void onVideoCapturerStopped(){
			Texture = null;
			ThreadUtils.RunOnRenderThread (() => {
				Action OnVideoCapturerStoppedHandler = OnVideoCapturerStopped;
				if (OnVideoCapturerStoppedHandler != null) {
					OnVideoCapturerStoppedHandler ();
				}
			});
		}

		public void onVideoCapturerError(string error){
			Texture = null;
			ThreadUtils.RunOnRenderThread (() => {
				Action<string> OnVideoCapturerErrorHandler = OnVideoCapturerError;
				if (OnVideoCapturerErrorHandler != null) {
					OnVideoCapturerErrorHandler (error);
				}
			});
		}
	}

}

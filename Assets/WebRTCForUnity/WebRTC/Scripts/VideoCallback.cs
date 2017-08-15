using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;


namespace iBicha
{
	public class VideoCallback : AndroidJavaProxy {
		public event Action<AndroidJavaObject, AndroidJavaObject> OnVideoCapturerStarted;
		public event Action<Texture> OnTexture;
		public event Action OnVideoCapturerStopped;
		public event Action<string> OnVideoCapturerError;

		private Texture2D nativeTexture;
		private RenderTexture rTexture;

		private static Material _WebRTCmat;
		private static Material WebRTCmat{
			get {
				if (_WebRTCmat == null) {
					_WebRTCmat = Resources.Load<Material> ("WebRTCMaterial");
				}
				return _WebRTCmat;
			}
		}

		private int width;
		private int height;

		public VideoCallback () : base ("com.ibicha.webrtc.VideoCallback")
		{
		}

		public void onVideoCapturerStarted (AndroidJavaObject videoCapturer, AndroidJavaObject videoTrack)
		{
			ThreadUtils.RunOnUpdate (() => {
				Action<AndroidJavaObject, AndroidJavaObject> OnVideoCapturerStartedHandler = OnVideoCapturerStarted;
				if (OnVideoCapturerStartedHandler != null) {
					OnVideoCapturerStartedHandler (videoCapturer, videoTrack);
				}
			});
		}


		public void renderFrame (int width, int height, int textureName, AndroidJavaObject i420Frame)
		{

			ThreadUtils.RunOnUpdate (() => {
				if (!WebRTCmat.shader.isSupported) {
					onVideoCapturerError ("Unsupported shader");
					return;
				}

				IntPtr textureId = new IntPtr (textureName);
				if (nativeTexture!= null || this.width != width || this.height != height) {
					CleanUp();
					this.width = width;
					this.height = height;
					nativeTexture = Texture2D.CreateExternalTexture (width, height, TextureFormat.YUY2, false, false, textureId);
					rTexture = new RenderTexture (width, height, 0, RenderTextureFormat.ARGB32);

					Action<Texture> OnTextureHandler = OnTexture;
					if (OnTextureHandler != null) {
						OnTextureHandler (rTexture);
					}

				} else {
					nativeTexture.UpdateExternalTexture (textureId);
				}

				Graphics.Blit (nativeTexture, rTexture, WebRTCmat);

				WebRTCAndroid.KillFrame (i420Frame);
			});
		}

		public void onVideoCapturerStopped ()
		{
			ThreadUtils.RunOnUpdate (() => {
				CleanUp ();
				Action OnVideoCapturerStoppedHandler = OnVideoCapturerStopped;
				if (OnVideoCapturerStoppedHandler != null) {
					OnVideoCapturerStoppedHandler ();
				}
			});
		}

		public void onVideoCapturerError (string error)
		{
			ThreadUtils.RunOnUpdate (() => {
				CleanUp ();
				Action<string> OnVideoCapturerErrorHandler = OnVideoCapturerError;
				if (OnVideoCapturerErrorHandler != null) {
					OnVideoCapturerErrorHandler (error);
				}
			});
		}

		void CleanUp ()
		{
			if (nativeTexture != null) {
				GameObject.Destroy (nativeTexture);
				nativeTexture = null;
			}
			if (rTexture != null) {
				rTexture.Release ();
				GameObject.Destroy (rTexture);
				rTexture = null;
			}
		}

	}

}

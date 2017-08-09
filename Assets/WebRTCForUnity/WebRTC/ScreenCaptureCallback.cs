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

		private Texture2D nativeTexture;

		public ScreenCaptureCallback () : base ("com.ibicha.webrtc.ScreenCaptureCallback") {
		}

		public void onVideoCapturerStarted(AndroidJavaObject capturer){
			ThreadUtils.RunOnUpdate (() => {
				Action OnVideoCapturerStartedHandler = OnVideoCapturerStarted;
				if (OnVideoCapturerStartedHandler != null) {
					OnVideoCapturerStartedHandler ();
				}
			});
		}


		public void renderFrameTexture(int width, int height, int texture, AndroidJavaObject i420Frame){
			ThreadUtils.RunOnUpdate (() => {
				IntPtr textureId = new IntPtr(texture);
				if(nativeTexture == null) {
					nativeTexture = Texture2D.CreateExternalTexture(width, height, TextureFormat.RGBA32, false,false, textureId);
				} else {
					nativeTexture.UpdateExternalTexture(textureId);
				}

				if(Texture == null || Texture.width != width || Texture.height != height) {
					Texture = new Texture2D(width, height, TextureFormat.RGBA32, false);
					Graphics.ConvertTexture(nativeTexture, Texture);
					Texture.filterMode = FilterMode.Point;
					Texture.wrapMode = TextureWrapMode.Clamp;
				}

				Graphics.CopyTexture(nativeTexture, Texture);

				Action<Texture2D> OnTextureHandler = OnTexture;
				if (OnTextureHandler != null) {
					OnTextureHandler (Texture);
				}

				ThreadUtils.RunOnPostRender(()=>{
					WebRTCAndroid.KillFrame(i420Frame);
				});
			});
		}


		public void renderFrameBuffer(int width, int height, AndroidJavaObject bufferWrap, AndroidJavaObject i420Frame){
			ThreadUtils.RunOnUpdate (() => {
				if(Texture == null || Texture.width != width || Texture.height != height) {
					Texture = new Texture2D(width, height, TextureFormat.ARGB32, false);
					Texture.filterMode = FilterMode.Point;
					Texture.wrapMode = TextureWrapMode.Clamp;
				}

				byte[] buffer = bufferWrap.Call<byte[]>("getBuffer");
				Debug.Log(buffer.Length);
				Texture.LoadRawTextureData(buffer);
				Texture.Apply();
				Action<Texture2D> OnTextureHandler = OnTexture;
				if (OnTextureHandler != null) {
					OnTextureHandler (Texture);
				}

				ThreadUtils.RunOnPostRender(()=>{
					WebRTCAndroid.KillFrame(i420Frame);
				});
			});
		}


		public void onVideoCapturerStopped(){
			ThreadUtils.RunOnUpdate (() => {
				GameObject.Destroy (Texture);
				GameObject.Destroy (nativeTexture);
				Texture = null;
				Texture = nativeTexture;
				Action OnVideoCapturerStoppedHandler = OnVideoCapturerStopped;
				if (OnVideoCapturerStoppedHandler != null) {
					OnVideoCapturerStoppedHandler ();
				}
			});
		}

		public void onVideoCapturerError(string error){
			ThreadUtils.RunOnUpdate (() => {
				GameObject.Destroy (Texture);
				GameObject.Destroy (nativeTexture);
				Texture = null;
				Texture = nativeTexture;
				Action<string> OnVideoCapturerErrorHandler = OnVideoCapturerError;
				if (OnVideoCapturerErrorHandler != null) {
					OnVideoCapturerErrorHandler (error);
				}
			});
		}
	}

}

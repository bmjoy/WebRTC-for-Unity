using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;

namespace iBicha {
	public class ScreenCaptureCallback : AndroidJavaProxy {
		private int bufferIndex = 0;
		public Texture2D[] TextureBuffers = null;
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
			ThreadUtils.RunOnPreRender (() => {
				IntPtr textureId = new IntPtr(texture);
				if(nativeTexture == null) {
					nativeTexture = Texture2D.CreateExternalTexture(width, height, TextureFormat.YUY2, false,false, textureId);
				} else {
					nativeTexture.UpdateExternalTexture(textureId);
				}

				/*if(TextureBuffers == null || TextureBuffers[0].width != width || TextureBuffers[0].height != height) {
					TextureBuffers = new Texture2D[2];
					for (int i = 0; i < TextureBuffers.Length; i++) {
						TextureBuffers[i] =new Texture2D(width, height, TextureFormat.YUY2, false);
						Graphics.ConvertTexture(nativeTexture, TextureBuffers[i]);
						TextureBuffers[i].filterMode = FilterMode.Point;
						TextureBuffers[i].wrapMode = TextureWrapMode.Clamp;
					}
				}

				Graphics.CopyTexture(nativeTexture, TextureBuffers[bufferIndex]);
				*/
				Action<Texture2D> OnTextureHandler = OnTexture;
				if (OnTextureHandler != null) {
					OnTextureHandler (nativeTexture);
					/*OnTextureHandler (TextureBuffers[bufferIndex]);
					bufferIndex = (bufferIndex + 1) % 2;*/
				}

				ThreadUtils.RunOnPostRender(()=>{
					WebRTCAndroid.KillFrame(i420Frame);
				});
			});
		}


		public void renderFrameBuffer(int width, int height, AndroidJavaObject bufferWrap, AndroidJavaObject i420Frame){
			ThreadUtils.RunOnUpdate (() => {
				if(TextureBuffers == null || TextureBuffers[0].width != width || TextureBuffers[0].height != height) {
					TextureBuffers = new Texture2D[2];
					for (int i = 0; i < TextureBuffers.Length; i++) {
						TextureBuffers[i] = new Texture2D(width, height, TextureFormat.ARGB32, false);
						TextureBuffers[i].filterMode = FilterMode.Point;
						TextureBuffers[i].wrapMode = TextureWrapMode.Clamp;
					}
				}

				byte[] buffer = bufferWrap.Call<byte[]>("getBuffer");
				TextureBuffers[bufferIndex].LoadRawTextureData(buffer);
				TextureBuffers[bufferIndex].Apply();
				Action<Texture2D> OnTextureHandler = OnTexture;
				if (OnTextureHandler != null) {
					OnTextureHandler (TextureBuffers[bufferIndex]);
					bufferIndex = (bufferIndex + 1) % 2;
				}

				ThreadUtils.RunOnPostRender(()=>{
					WebRTCAndroid.KillFrame(i420Frame);
				});
			});
		}


		public void onVideoCapturerStopped(){
			ThreadUtils.RunOnUpdate (() => {
				CleanUp();
				Action OnVideoCapturerStoppedHandler = OnVideoCapturerStopped;
				if (OnVideoCapturerStoppedHandler != null) {
					OnVideoCapturerStoppedHandler ();
				}
			});
		}

		public void onVideoCapturerError(string error){
			ThreadUtils.RunOnUpdate (() => {
				CleanUp();
				Action<string> OnVideoCapturerErrorHandler = OnVideoCapturerError;
				if (OnVideoCapturerErrorHandler != null) {
					OnVideoCapturerErrorHandler (error);
				}
			});
		}

		void CleanUp() {
			if (TextureBuffers != null) {
				for (int i = 0; i < TextureBuffers.Length; i++) {
					GameObject.Destroy (TextureBuffers[i]);
				}
				GameObject.Destroy (nativeTexture);
				TextureBuffers = null;
				nativeTexture = null;
			}
		}
	}

}

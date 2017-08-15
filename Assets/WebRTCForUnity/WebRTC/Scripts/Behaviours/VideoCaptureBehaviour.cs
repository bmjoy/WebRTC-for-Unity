using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace iBicha
{
	public class VideoCaptureBehaviour : MonoBehaviour {

		public VideoCapturer.CaptureSource source;
		public bool captureOnStart = false;
		public VideoRendererBehaviour[] videoRenderers;


		private VideoCapturer capturer;

		void Start() {
			capturer = VideoCapturer.CreateCapturer (source);
			capturer.Callback.OnVideoCapturerStarted += OnVideoCapturerStarted;
			capturer.Callback.OnVideoCapturerStopped += OnVideoCapturerStopped;
			capturer.Callback.OnVideoCapturerError += OnVideoCapturerError;
			capturer.Callback.OnTexture += OnTexture;
			if (captureOnStart) {
				capturer.StartCapture ();
			}
		}


		void Destroy() {
			capturer.Callback.OnVideoCapturerStarted -= OnVideoCapturerStarted;
			capturer.Callback.OnVideoCapturerStopped -= OnVideoCapturerStopped;
			capturer.Callback.OnVideoCapturerError -= OnVideoCapturerError;
			capturer.Callback.OnTexture -= OnTexture;
		}
		 
		public void StartCapture() {
			capturer.StartCapture ();
		}

		public void StopCapture() {
			capturer.StopCapture ();
		}


		void OnVideoCapturerStarted(AndroidJavaObject videoCapturer, AndroidJavaObject videoTrack) {
			for (int i = 0; i < videoRenderers.Length; i++) {
				if (videoRenderers [i].enabled) {
					videoRenderers [i].OnVideoCapturerStarted ();
				}
			}
 		}

		void OnVideoCapturerStopped() {
			for (int i = 0; i < videoRenderers.Length; i++) {
				if (videoRenderers [i].enabled) {
					videoRenderers [i].OnVideoCapturerStopped ();
				}
			}
		}

		void OnVideoCapturerError(string error) {
			for (int i = 0; i < videoRenderers.Length; i++) {
				if (videoRenderers [i].enabled) {
					videoRenderers [i].OnVideoCapturerError (error);
				}
			}
		}

		void OnTexture(Texture texture) {
			for (int i = 0; i < videoRenderers.Length; i++) {
				if (videoRenderers [i].enabled) {
					videoRenderers [i].OnTexture (texture);
				}
			}
		}
	}

}
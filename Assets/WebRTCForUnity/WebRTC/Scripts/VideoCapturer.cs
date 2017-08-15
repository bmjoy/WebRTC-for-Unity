using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;

namespace iBicha
{
	public class VideoCapturer {
		public enum CaptureSource
		{
			CameraFront,
			CameraBack,
			Screen, 
			RenderTexture
		}

		public static VideoCapturer CreateCapturer(CaptureSource source)
		{
			VideoCapturer cap;
			switch (source) {
			case CaptureSource.CameraBack:
				cap = new VideoCapturer (source);
				break;
			case CaptureSource.CameraFront:
				cap = new VideoCapturer (source);
				break;
			case CaptureSource.Screen:
				cap = new VideoCapturer (source);
				break;
			case CaptureSource.RenderTexture:
				throw new NotImplementedException ("CaptureSource.RenderTexture is not yet supported");
			default:
				throw new NotSupportedException ("CaptureSource not valid");
			}
			return cap;
		}

		public CaptureSource Source {
			get {
				return _source;
			}
		}
		public VideoCallback Callback {
			get {
				return _callback;
			}
		}

		private CaptureSource _source;
		private VideoCallback _callback;
		private AndroidJavaObject videoCapturer;
		private AndroidJavaObject videoTrack;
 
		private VideoCapturer(CaptureSource source) {
			this._source = source;
			this._callback = new VideoCallback ();
			this._callback.OnVideoCapturerStarted += OnVideoCapturerStarted;
		}

		public void StartCapture() {
			StopCapture ();
			switch (this._source) {
			case CaptureSource.CameraBack:
				ThreadUtils.RunOnUpdate (()=>{
					WebRTCAndroid.WebRTC_JavaClass.CallStatic("StartCameraCapture", false, _callback);
				});
				break;
			case CaptureSource.CameraFront:
				ThreadUtils.RunOnUpdate (()=>{
					WebRTCAndroid.WebRTC_JavaClass.CallStatic("StartCameraCapture", true, _callback);
				});
				break;
			case CaptureSource.Screen:
				ThreadUtils.RunOnUpdate (()=>{
					WebRTCAndroid.WebRTC_JavaClass.CallStatic("StartScreenCapture", _callback);
				});
				break;
			case CaptureSource.RenderTexture:
				throw new NotImplementedException ("CaptureSource.RenderTexture is not yet supported");
			default:
				throw new NotSupportedException ("CaptureSource not valid");
			}
		}

		public void StopCapture() {
			if (videoCapturer != null) {
				videoCapturer.Call ("stopCapture");
				videoCapturer.Call ("dispose");
				videoCapturer = null;
			}
			if (videoTrack != null) {
				videoTrack.Call ("dispose");
				videoTrack = null;
			}
		}


		void OnVideoCapturerStarted(AndroidJavaObject videoCapturer, AndroidJavaObject videoTrack) {
			this.videoCapturer = videoCapturer;
			this.videoTrack = videoTrack;
		}
	}

}

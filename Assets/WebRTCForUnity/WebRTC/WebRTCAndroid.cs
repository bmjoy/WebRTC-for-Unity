using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace iBicha
{
	public class WebRTCAndroid : WebRTC {
		[RuntimeInitializeOnLoadMethod]
		static void setUnityContext()
		{
			ThreadUtils.RunOnUpdate (() => {
				UnityEGLContext_JavaClass.CallStatic ("setUnityContext");
			});
		}

		public static void KillFrame(AndroidJavaObject i420Frame) {
			VideoRenderer_JavaClass.CallStatic ("renderFrameDone", i420Frame);
		}

		static AndroidJavaClass _WebRTC_JavaClass;
		static AndroidJavaClass _UnityEGLUtils_JavaClass;
		static AndroidJavaClass _VideoRenderer_JavaClass;

		public static AndroidJavaClass WebRTC_JavaClass {
			get
			{
				if(_WebRTC_JavaClass == null)
				{
					_WebRTC_JavaClass = new AndroidJavaClass("com.ibicha.webrtc.WebRTC");
				}
				return _WebRTC_JavaClass;
			}
		}

		public static AndroidJavaClass UnityEGLContext_JavaClass {
			get
			{
				if(_UnityEGLUtils_JavaClass == null)
				{
					_UnityEGLUtils_JavaClass = new AndroidJavaClass("com.ibicha.webrtc.UnityEGLUtils");
				}
				return _UnityEGLUtils_JavaClass;
			}
		}

		public static AndroidJavaClass VideoRenderer_JavaClass {
			get
			{
				if(_VideoRenderer_JavaClass == null)
				{
					_VideoRenderer_JavaClass = new AndroidJavaClass("org.webrtc.VideoRenderer");
				}
				return _VideoRenderer_JavaClass;
			}
		}

	}
}

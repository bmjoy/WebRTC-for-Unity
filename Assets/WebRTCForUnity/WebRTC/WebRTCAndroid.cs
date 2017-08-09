using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace iBicha
{
	public class WebRTCAndroid : WebRTC {
		[RuntimeInitializeOnLoadMethod]
		static void eglContextSet()
		{
			ThreadUtils.RunOnUpdate (() => {
				UnityEGLContext_JavaClass.CallStatic ("eglContextSet");
			});
		}

		public static void KillFrame(AndroidJavaObject i420Frame) {
			UnityEGLContext_JavaClass.CallStatic ("KillFrame", i420Frame);
		}

		public static void switchToUnityContext() {
			UnityEGLContext_JavaClass.CallStatic ("switchToUnityContext");
		}

		static AndroidJavaClass _WebRTC_JavaClass;
		static AndroidJavaClass _UnityEGLUtils_JavaClass;

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

	}
}

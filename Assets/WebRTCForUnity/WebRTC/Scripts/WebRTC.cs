using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;


namespace iBicha
{
	public abstract class WebRTC {

		private static WebRTC instance;
		public static WebRTC Instance {
			get {
				if (instance == null) {
					#if UNITY_ANDROID
					instance = new WebRTCAndroid();
					#endif
				}
				return instance;
			}
		}
	}
}

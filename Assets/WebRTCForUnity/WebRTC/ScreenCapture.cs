using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace iBicha
{
	public class ScreenCapture {

		public static void StartScreenCapture(ScreenCaptureCallback callback)
		{
			ThreadUtils.RunOnUpdate (()=>{
				WebRTCAndroid.WebRTC_JavaClass.CallStatic("StartScreenCapture", callback);
			});
		}



	}

}

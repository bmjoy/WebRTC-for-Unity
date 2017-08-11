using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using iBicha;

public class ScreenCaptureExample : MonoBehaviour {
	public TextureProxy textureProxy;
	// Use this for initialization
	public void StartScreenCapture () {
		ScreenCapture.StartScreenCapture (textureProxy.screenCaptureCallback);
	}


	public void StartCameraCapture () {
		CameraCapture.StartCameraCapture (true, textureProxy.screenCaptureCallback);
	}

}

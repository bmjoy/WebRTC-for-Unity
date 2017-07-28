using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using iBicha;

public class Test : MonoBehaviour {


	// Use this for initialization
	void Start () {
		SessionDescription sd = new SessionDescription ();
		sd.Username = "iBicha";
		sd.SessionId = 16513216;
		sd.VersionNumber = 153165621;
		sd.NetworkAddress = "127.0.0.1";
		sd.SessionName = "Session Name";
		sd.StartDate = System.DateTime.UtcNow;
		sd.EndDate = System.DateTime.UtcNow.AddHours(2);
		sd.Media.Add ("video something");
		sd.Media.Add ("audio something");
		print (sd.RawDescriptionString);
	}

}

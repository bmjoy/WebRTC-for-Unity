using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Text;
using System;

namespace iBicha
{
	public class SessionDescription {

		public enum DescriptionType 
		{
			OFFER,
			PRANSWER,
			ANSWER
		}

		public enum DescriptionKeys 
		{
			Version = 'v',
			Originator = 'o',
			SessionName = 's',
			SessionInformation = 'i',
			DescriptionURI = 'u',
			Emails = 'e',
			PhoneNumbers = 'p',
			ConnectionInformation = 'c',
			BandwidthInformation = 'b',
			SessionTime = 't',
			RepeatTimes = 'r',
			TimeZoneAdjustments = 'z',
			EncryptionKey = 'k',
			SessionAttribute = 'a',
			MediaDescription = 'm',
		}

		public SessionDescription() {
			rawDescriptionIsDirty = true;
			version = 0;
			username = "-";
			sessionName = "-";
			media = new ObservableList<string> ();
			media.Updated += SetDirty;
		}

		public  SessionDescription (string rawDescription) : this() {
			RawDescriptionString = rawDescription;
		}
			
		public DescriptionType Type {
			get {
				return type;
			}
			set {
				type = value;
			}
		}

		public string RawDescriptionString {
			get {
				if (rawDescriptionIsDirty) {
					RebuildRawDescription ();
				}
				return rawDescription;
			}
			set {
				rawDescription = value;
				ParseRawDescription ();
				rawDescriptionIsDirty = false;
			}
		}

		public int Version {
			get {
				return version;
			}
			set {
				version = value;
				rawDescriptionIsDirty = true;
			}
		}

		public string Username {
			get {
				return username;
			}
			set {
				username = value;
				rawDescriptionIsDirty = true;
			}
		}

		public int SessionId {
			get {
				return sessionId;
			}
			set {
				sessionId = value;
				rawDescriptionIsDirty = true;
			}
		}

		public int VersionNumber {
			get {
				return versionNumber;
			}
			set {
				versionNumber = value;
				rawDescriptionIsDirty = true;
			}
		}

		public string NetworkAddress {
			get {
				return networkAddress;
			}
			set {
				networkAddress = value;
				rawDescriptionIsDirty = true;
			}
		}

		public string SessionName {
			get {
				return sessionName;
			}
			set {
				sessionName = value;
				rawDescriptionIsDirty = true;
			}
		}


		public DateTime? StartDate {
			get {
				return startDate;
			}
			set {
				startDate = value;
				rawDescriptionIsDirty = true;
			}
		}

		public DateTime? EndDate {
			get {
				return endDate;
			}
			set {
				endDate = value;
				rawDescriptionIsDirty = true;
			}
		}

		public ObservableList<string> Media {
			get {
				return media;
			}
		}

		private string rawDescription;
		private bool rawDescriptionIsDirty;

		private DescriptionType type;

		//v
		private int version;
		//o
		private string username;
		private int sessionId;
		private int versionNumber;
		private string networkAddress;
		//s
		private string sessionName;

		private DateTime? startDate;
		private DateTime? endDate;

		private ObservableList<string> media;

		private void SetDirty() {
			rawDescriptionIsDirty = true;
		}

		private void RebuildRawDescription() {
			StringBuilder sb = new StringBuilder ();
			sb.Append (string.Format ("{0}={1}\n", (char)DescriptionKeys.Version, this.Version));
			sb.Append (string.Format ("{0}={1} {2} {3} IN IP4 {4}\n", (char)DescriptionKeys.Originator, this.Username,  this.SessionId, this.VersionNumber, this.NetworkAddress));
			sb.Append (string.Format ("{0}={1}\n", (char)DescriptionKeys.SessionName, this.SessionName));
			sb.Append (string.Format ("{0}={1} {2}\n", (char)DescriptionKeys.SessionTime, this.StartDate.ToUnixTimestamp(), this.EndDate.ToUnixTimestamp()));
			//TODO: rest of the attributes
			foreach (string item in media) {
				sb.Append (string.Format ("{0}={1}\n", (char)DescriptionKeys.MediaDescription, item));
			}
			rawDescription = sb.ToString ();
			rawDescriptionIsDirty = false;
		}

		private void ParseRawDescription() {
			throw new System.NotImplementedException ();
		}

		public override string ToString ()
		{
			return RawDescriptionString;
		}
	}
}

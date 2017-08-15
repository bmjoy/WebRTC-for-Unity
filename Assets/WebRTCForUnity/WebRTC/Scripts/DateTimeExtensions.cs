using System;
namespace iBicha
{
	public static class DateTimeExtensions {
		public static long ToUnixTimestamp(this DateTime? dateTime)
		{
			if (dateTime.HasValue) {
				return (long)(TimeZoneInfo.ConvertTimeToUtc(dateTime.Value) - 
					new DateTime(1970, 1, 1, 0, 0, 0, 0, System.DateTimeKind.Utc)).TotalSeconds;	
			}
			return 0;
		}
	}
}

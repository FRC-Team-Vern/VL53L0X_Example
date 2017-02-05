package org.usfirst.frc.team5461.robot;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogTimestamp {
	private final static long SOME_TIME_AFTER_1970 = 523980000000L;

	private static String _timestampString = null;

	// http://javarevisited.blogspot.com/2014/05/double-checked-locking-on-singleton-in-java.html
	public static String getTimestampString() {
		if (_timestampString == null) { // do a quick check (no overhead from synchonized)
			synchronized (LogTimestamp.class) {
				if (_timestampString == null) { // Double checked
					long now = System.currentTimeMillis();
					if (now > SOME_TIME_AFTER_1970) {
						SimpleDateFormat formatName = new SimpleDateFormat(
								"yyyyMMdd-HHmmss");
						_timestampString = formatName.format(new Date());
						String logMessage = String.format("timestamp for logs is %s\n", _timestampString);
						EventLogging.writeToDS(logMessage);
						System.out.print (logMessage);
					}
				}
			}
		}
		return _timestampString;
	}

}
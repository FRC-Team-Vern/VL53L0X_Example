package org.usfirst.frc.team5461.robot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.wpi.first.wpilibj.hal.ControlWord;
import edu.wpi.first.wpilibj.hal.HAL;

//import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary;
//import edu.wpi.first.wpilibj.communication.HALControlWord;

public class EventLogging {
	
	// make some levels that correspond to the different SLF4J logging 
	// methods. Have the mappings to the underlying j.u.l logging levels.
	public enum Level {
		TRACE(java.util.logging.Level.FINEST), //
		DEBUG(java.util.logging.Level.FINE), //
		INFO(java.util.logging.Level.INFO), //
		WARN(java.util.logging.Level.WARNING), //
		ERROR(java.util.logging.Level.SEVERE);

		java.util.logging.Level julLevel;

		Level(java.util.logging.Level _julLevel) {
			julLevel = _julLevel;
		}
	}
	
	/**
	 * Get an SLF4J logger for a class. Set the underlying j.u.l logger to the 
	 * desired level.
	 * 
	 * @param clazz class for the logger
	 * @param l Level that we want to log at
	 * @return
	 */
	static public org.slf4j.Logger getLogger(Class<?> clazz, Level l)
	{
		return getLogger(clazz.getName(), l);
	}

	/**
	 * Get an SLF4J logger for a name. Set the underlying j.u.l logger to the 
	 * desired level.
	 * 
	 * @param sClazz name for the logger
	 * @param l Level that we want to log at
	 * @return
	 */
	static public org.slf4j.Logger getLogger(String sClazz, Level l)
	{
		// set up the underlying logger to log at the level we want
		java.util.logging.Logger julLogger = java.util.logging.Logger
				.getLogger(sClazz);
		julLogger.setLevel(l.julLevel);

		// and return the SLF4J logger.
		org.slf4j.Logger rv = org.slf4j.LoggerFactory.getLogger(sClazz);
		return rv;
	}

	/**
	 * Write a message to the DriverStation. It also logs into the driver
	 * station log.
	 * @param message Message to log.
	 */
	public static final void writeToDS(String message)
	{
		final ControlWord controlWord = new ControlWord();
		if (controlWord.getDSAttached())
		{
			HAL.setErrorData(message);
		}
	}

	/**
	 * Create a String representation of an Exception.
	 * @param t
	 * @return
	 */
	public static final String exceptionToString(Throwable t)
	{
		final StackTraceElement[] stackTrace = t.getStackTrace();
		final StringBuilder message = new StringBuilder();
		final String separator = "===\n";
		final Throwable cause = t.getCause();

		message.append("Exception of type ").append(t.getClass().getName())
				.append('\n');
		message.append("Message: ").append(t.getMessage()).append('\n');
		message.append(separator);
		message.append("   ").append(stackTrace[0]).append('\n');

		for (int i = 1; i < stackTrace.length; i++)
		{
			message.append(" \t").append(stackTrace[i]).append('\n');
		}

		if (cause != null)
		{
			final StackTraceElement[] causeTrace = cause.getStackTrace();
			message.append(" \t\t").append("Caused by ")
					.append(cause.getClass().getName()).append('\n');
			message.append(" \t\t").append("Because: ")
					.append(cause.getMessage()).append('\n');
			message.append(" \t\t   ").append(causeTrace[0]).append('\n');
			message.append(" \t\t \t").append(causeTrace[2]).append('\n');
			message.append(" \t\t \t").append(causeTrace[3]);
		}

		return message.toString();
	}

	private static boolean setupDone = false;

	/**
	 * Set up a j.u.l logger that will start logging to a file (with a
	 * timestamped name). The logging to the file will not start until
	 * the system time is set.
	 * @param logDirectory Directory to create the logging file in
	 */
	public static void setup(File logDirectory)
	{
		if (!setupDone) // quickly check to see if we are initialized
		{
			synchronized (EventLogging.class) // check slowly and carefully
			{
				if (!setupDone)
				{
					Logger rootLogger = Logger.getLogger("");
					// get all the existing handlers
					Handler[] handlers = rootLogger.getHandlers();
					// and remove them
					for (Handler handler : handlers)
					{
						rootLogger.removeHandler(handler);
					}

					// add the handlers we want
					Handler h = new ConsoleHandler();
					h.setFormatter(new FormatterForFileHandler());
					h.setLevel(java.util.logging.Level.ALL);
					rootLogger.addHandler(h);

					h = new MyFileHandler(logDirectory);
					h.setFormatter(new FormatterForFileHandler());
					h.setLevel(java.util.logging.Level.ALL);
					rootLogger.addHandler(h);

					setupDone = true;
				}
			}
		}
	}

	static class MyFileHandler extends StreamHandler {
		File logDirectory;
		FileOutputStream fileOutputStream = null;
		private static final int MAX_NUM_LOG_FILES = 5;

		public MyFileHandler(File logDirectory) {
			super();
			this.logDirectory = logDirectory;
		}

		@Override
		public void publish(LogRecord arg0)
		{
			/*
			 * if the file is not open, see if we can open it.
			 */
			if (fileOutputStream == null) // quick check
			{
				synchronized (this) // take some overhead for the good check
				{
					if (fileOutputStream == null) // deliberate check
					{
						// we'll get a null here if the clock is not yet set
						String timestampString = LogTimestamp
								.getTimestampString();
						
						if (timestampString != null)
						{
							// Let's keep the number of log files we have small to protect memory.
							File logFile = generateNextLogFile(timestampString);
							try
							{
								fileOutputStream = new FileOutputStream(logFile);
								setOutputStream(fileOutputStream);
							} catch (IOException ex)
							{
								ex.printStackTrace(System.err);
							}

						}
					}
				}
			}

			// only log if we have a place to log to
			if (fileOutputStream != null)
			{
				super.publish(arg0);
				flush();
			}
		}
		
		private File generateNextLogFile(String timestampString) {
			// Find all existing log files to determine if any need to be deleted
			FilenameFilter fnf = new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					// Get last index for '.' char
					int lastIndex = name.lastIndexOf('.');
					if (lastIndex > 0) {
						// Get extension
						String str = name.substring(lastIndex);
						
						if (str.equals(".log")) {
							return true;
						}
					}
					return false;
				}
			};
			
		File[] currentLogDirectoryFiles = logDirectory.listFiles(fnf);
			
			// Find oldest log file.
			Map<String, File> logFilesMap = new HashMap<>();
			Pattern logPattern = Pattern.compile("^(\\d+-\\d+).log$");
			for (File file : currentLogDirectoryFiles) {
				String filename = file.getName();
				Matcher m = logPattern.matcher(filename);
				if (m.find()) {
					logFilesMap.put(m.group(1), file);
				} else {
					System.out.println("Could not match pattern: " + filename);
				}
			}
			
			// If number of log files is greater than the max index then determine the oldest and delete
			if (logFilesMap.size() >= MAX_NUM_LOG_FILES) {
				String oldestTimeStamp = logFilesMap.entrySet().stream()
						.min((kv1, kv2) -> kv1.getKey().compareTo(kv2.getKey())).get().getKey();
				boolean deleteLogFile = logFilesMap.get(oldestTimeStamp).delete();
				if (deleteLogFile) {
					System.out.println("Deleted log file: " + oldestTimeStamp + ".log");
				}
			}
			
			
			return new File(logDirectory, timestampString + ".log");
		}
	}

	static class FormatterForFileHandler extends java.util.logging.Formatter {
		//
		// Create a DateFormat to format the logger timestamp.
		//
		private final DateFormat df = new SimpleDateFormat(
				"yyyy/MM/dd hh:mm:ss.SSS");

		public String format(LogRecord record)
		{
			StringBuilder builder = new StringBuilder(1000);
			// DateFormat objects are not thread-safe....
			synchronized (df) 
			{
				builder.append(df.format(new Date(record.getMillis()))).append(",");
			}
			builder.append("[").append(record.getLoggerName())
					.append("] ");
			builder.append(record.getLevel()).append(",");
			builder.append(formatMessage(record));
			builder.append("\n");
			return builder.toString();
		}

		public String getHead(Handler h)
		{
			return super.getHead(h);
		}

		public String getTail(Handler h)
		{
			return super.getTail(h);
		}
	}

}

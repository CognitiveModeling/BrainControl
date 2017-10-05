/*******************************************************************************
 * Copyright (C) 2017 Cognitive Modeling Group, University of Tuebingen
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * You can contact us at:
 * University of Tuebingen
 * Department of Computer Science
 * Cognitive Modeling
 * Sand 14
 * 72076 Tübingen 
 * cm-sekretariat -at- inf.uni-tuebingen.de
 ******************************************************************************/
package marioWorld.engine;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import mario.main.Settings;
import marioWorld.utils.ResetStaticInterface;

/**
 * Provides centralized logging capability. This class differs from the functionality provided by Logger/LogManager in the way that this class automatically
 * creates log files; furthermore it won't log messages sent to loggers which have not been created before. The output files will be in your project's directory
 * (subdirectory 'logs').
 * 
 * Usage as follows: 1) Setup the logger (Logging.addLogger(YOUR_NAME_HERE)) 2) Use the Logging.log* methods to log your messages.
 * 
 * @author sebastian
 * 
 */
public class Logging implements ResetStaticInterface {
	private static Hashtable<String, Logger> loggers = new Hashtable<String, Logger>();

	/**
	 * Time for the names of the logfiles. Created once so files from the same run have the same time in the name.
	 */
	private static Date startTime = new Date();

	/**
	 * Formatter for the date used in the name of log files.
	 */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	/**
	 * Base path for the logging files.
	 */
	private static final String BASE_PATH = "logs";

	static {
		// create log directory
		File logPath = new File(BASE_PATH);
		if (Settings.DO_LOGGING) {
			logPath.mkdir();
		}
	}

	/**
	 * No need to ever create an object of this kind :).
	 */
	private Logging() {
		classesWithStaticStuff.add(this.getClass());
	}

	/**
	 * Adds a logger with a given name and sets its log level to the default (all).
	 * 
	 * @param name
	 */
	public static void addLogger(String name) {
		addLogger(name, Level.ALL);
	}

	/**
	 * Adds a logger with a given name and a given log level.
	 * 
	 * @param name
	 */
	public static void addLogger(String name, Level level) {
		if (name == null) {
			return;
		}

		if (loggers.containsKey(name)) {
			loggers.get(name).setLevel(level);
		} else {
			Logger logger = Logger.getLogger(name);
			logger.setLevel(level);

			// setup the logger to log to a file
			String date = DATE_FORMAT.format(startTime);
			String fileName = BASE_PATH + File.separatorChar + name + "-" + date + ".log";
			FileHandler handler;
			try {
				if (Settings.DO_LOGGING) {
					handler = new FileHandler(fileName);
					logger.addHandler(handler);
					FileFormatter formatter = new FileFormatter();
					handler.setFormatter(formatter);
				}
				loggers.put(name, logger);
			} catch (SecurityException e) {
				System.out.println("Failed to setup logger " + name + "!");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Failed to setup logger " + name + "!");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Deactivates console output for a given logger.
	 * 
	 * @param loggerName
	 */
	public static void deactivateConsoleOutput(String loggerName) {
		if (loggerName != null) {
			Logger logger = loggers.get(loggerName);
			if (logger != null) {
				logger.setUseParentHandlers(false);
			}
		}
	}

	/**
	 * Activates console output for a given logger.
	 * 
	 * @param loggerName
	 */
	public static void activateConsoleOutput(String loggerName) {
		if (loggerName != null) {
			Logger logger = loggers.get(loggerName);
			if (logger != null) {
				logger.setUseParentHandlers(true);
			}
		}
	}

	/**
	 * Sets the logging level of the specific logger.
	 * 
	 * @param loggerName
	 * @param level
	 */
	public static void setLogLevel(String loggerName, Level level) {
		if (loggerName != null) {
			Logger logger = loggers.get(loggerName);
			if (logger != null) {
				logger.setLevel(level);
			}
		}
	}

	/**
	 * Sets the logging level of the specific logger.
	 * 
	 * @param loggerName
	 * @param level
	 */
	public static Level getLogLevel(String loggerName) {
		if (loggerName != null) {
			Logger logger = loggers.get(loggerName);
			if (logger != null) {
				return logger.getLevel();
			}
		}
		return null;
	}

	/**
	 * Prints out the current stack trace to the specified logger. The log-level of stack traces is 'Severe' by default.
	 * 
	 * @param loggerName
	 */
	public static void logStackTrace(String loggerName) {
		String stackTrace = getStackTrace();
		logMessage(loggerName, stackTrace, Level.SEVERE, 1);
	}

	/**
	 * Prints out the current stack trace to the specified logger.
	 * 
	 * @param loggerName
	 * @param level
	 */
	public static void logStackTrace(String loggerName, Level level) {
		String stackTrace = getStackTrace();
		logMessage(loggerName, stackTrace, level, 1);
	}

	/**
	 * Return a string containing the stack trace -- excluding this method and its direct caller.
	 * 
	 * @return
	 */
	private static String getStackTrace() {
		Thread currentThread = Thread.currentThread();
		StackTraceElement[] trace = currentThread.getStackTrace();
		StringBuilder b = new StringBuilder();
		b.append("StackTrace:");
		// Stack:
		// [0] = currentThread.getStackTrace
		// [1] = Logging.getStackTrace
		// [2] = Logging.{CALLER}
		// -> start at three
		for (int i = 3; i < trace.length; i++) {
			StackTraceElement t = trace[i];
			b.append('\n');
			b.append(t.toString());
		}
		return b.toString();
	}

	/**
	 * Logs a message to the specified logger using the specified level.
	 * 
	 * @param loggerName
	 * @param message
	 */
	public static void logConfig(String loggerName, String message) {
		logMessage(loggerName, message, Level.CONFIG, 1);
	}

	/**
	 * Logs a message to the specified logger using the specified level.
	 * 
	 * @param loggerName
	 * @param message
	 */
	public static void logSevere(String loggerName, String message) {
		logMessage(loggerName, message, Level.SEVERE, 1);
	}

	/**
	 * Logs a message to the specified logger using the specified level.
	 * 
	 * @param loggerName
	 * @param message
	 */
	public static void logInfo(String loggerName, String message) {
		logMessage(loggerName, message, Level.INFO, 1);
	}

	/**
	 * Logs a message to the specified logger using the specified level.
	 * 
	 * @param loggerName
	 * @param message
	 */
	public static void logWarning(String loggerName, String message) {
		logMessage(loggerName, message, Level.WARNING, 1);
	}

	/**
	 * Logs a message to the specified logger using the specified level.
	 * 
	 * @param loggerName
	 * @param message
	 */
	public static void logFinest(String loggerName, String message) {
		logMessage(loggerName, message, Level.FINEST, 1);
	}

	/**
	 * Logs a message to the specified logger using the specified level.
	 * 
	 * @param loggerName
	 * @param message
	 */
	public static void logFiner(String loggerName, String message) {
		logMessage(loggerName, message, Level.FINER, 1);
	}

	/**
	 * Logs a message to the specified logger using the specified level.
	 * 
	 * @param loggerName
	 * @param message
	 */
	public static void logFine(String loggerName, String message) {
		logMessage(loggerName, message, Level.FINE, 1);
	}

	/**
	 * Logs a message to the logger with the given name using the default logging level (all).
	 * 
	 * @param loggerName
	 * @param message
	 */
	public static void log(String loggerName, String message) {
		logMessage(loggerName, message, Level.ALL, 1);
	}

	/**
	 * Logs throwing of exceptions.
	 * 
	 * @param loggerName
	 * @param thrown
	 */
	public static void logThrowing(String loggerName, Throwable thrown) {
		if (loggerName != null) {
			Logger logger = loggers.get(loggerName);
			if (logger != null) {
				Thread current = Thread.currentThread();
				int totalStackOffset = 2;
				StackTraceElement[] trace = current.getStackTrace();

				LogRecord logRecord = new LogRecord(Level.ALL, "Exception thrown!");

				if (trace.length >= totalStackOffset) {
					StackTraceElement caller = trace[totalStackOffset];
					logRecord.setSourceMethodName(caller.getMethodName());
					logRecord.setSourceClassName(caller.getClassName());
					int lineNumber = caller.getLineNumber();
					logRecord.setParameters(new Object[] { lineNumber });
					logRecord.setThrown(thrown);
				}
				logger.log(logRecord);
			}
		}
	}

	/**
	 * Logs a message to the logger with the given name. If there is no such logger, the log-message is ignored.
	 * 
	 * @param loggerName
	 * @param message
	 * @param level
	 */
	public static void log(String loggerName, String message, Level level) {
		logMessage(loggerName, message, level, 1);
	}

	/**
	 * Logs a message to the logger with the given name. If there is no such logger, the log-message is ignored.
	 * 
	 * @param loggerName
	 * @param message
	 * @param level
	 */
	private static void logMessage(String loggerName, String message, Level level, int stackOffset) {
		if (loggerName == null) {
			return;
		}
		Logger logger = loggers.get(loggerName);
		if (logger == null) {
			System.out.println("Tried to log message to " + loggerName + " which isn't available");
			return;
		} else {
			LogRecord logRecord = new LogRecord(level, message);

			// if (!(level.intValue() < logger.getLevel().intValue() ||
			// logger.getLevel().intValue() == Level.OFF.intValue())) {
			// System.out.print();//Use breakpoint & debug mode to quickly find
			// where a message was logged from
			// }

			// this method is called from another method -- and the logger would
			// normally log "logMessage" as its caller, which is rather
			// unhelpful
			// -> go to the call stack and get the relevant message!

			Thread current = Thread.currentThread();
			int totalStackOffset = 2 + stackOffset;
			StackTraceElement[] trace = current.getStackTrace();
			if (trace.length >= totalStackOffset) {
				StackTraceElement caller = trace[totalStackOffset];
				logRecord.setSourceMethodName(caller.getMethodName() + " (Logger: " + loggerName + ")");
				logRecord.setSourceClassName(caller.getClassName());
				logRecord.setLoggerName(loggerName);
				int lineNumber = caller.getLineNumber();
				logRecord.setParameters(new Object[] { lineNumber });
			}

			logger.log(logRecord);
		}
	}

	/**
	 * File formatter to pretty-print the log messages.
	 * 
	 * @author sebastian
	 */
	static class FileFormatter extends Formatter {

		private static final DateFormat LOG_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

		/**
		 * Appends a given string to a stringbuilder a specified number of times.
		 * 
		 * @param builder
		 * @param toAppend
		 * @param times
		 */
		private static final void appendTimes(StringBuilder builder, String toAppend, int times) {
			for (int i = 0; i < times; i++) {
				builder.append(toAppend);
			}
		}

		/**
		 * Recursive exception formatting.
		 * 
		 * @param thrown
		 * @param builder
		 * @param indent
		 */
		private static final void format(Throwable thrown, StringBuilder builder, int indent) {
			// prints "exception class: localized message"
			builder.append(thrown.toString());
			builder.append("\n");
			appendTimes(builder, "\t", indent);
			builder.append("stack trace:");
			StackTraceElement[] s = thrown.getStackTrace();
			for (int i = 0; i < s.length; i++) {
				builder.append("\n");
				appendTimes(builder, "\t", indent + 1);
				builder.append(s[i].toString());
			}

			if (thrown.getCause() != null) {
				builder.append("\n");
				appendTimes(builder, "\t", indent);
				builder.append("inner exception:");
				format(thrown, builder, indent + 1);
			}
		}

		/**
		 * Formats a given log record for logging.
		 */
		public String format(LogRecord record) {
			StringBuilder builder = new StringBuilder(1000);
			Date logTime = new Date(record.getMillis());
			builder.append(LOG_DATE_FORMAT.format(logTime));
			builder.append(" - ");
			builder.append("[").append(record.getSourceClassName());
			builder.append(".");
			builder.append(record.getSourceMethodName());
			Object[] params = record.getParameters();

			// we are in full control of this instance and we are passing
			// the line of the call from the source file as the first entry
			// in the parameter array
			if (params != null && params.length >= 1) {
				builder.append(":");
				builder.append(params[0]);
			}
			builder.append("] - ");
			builder.append("[").append(record.getLevel());
			builder.append("] - ");
			builder.append(formatMessage(record));
			if (record.getThrown() != null) {
				format(record.getThrown(), builder, 0);
			}
			builder.append("\n");
			return builder.toString();
		}

		public String getHead(Handler h) {
			return super.getHead(h);
		}

		public String getTail(Handler h) {
			return super.getTail(h);
		}
	}

	public static void deleteStaticAttributes() {
		loggers = null;
	}

	public static void resetStaticAttributes() {
		loggers = new Hashtable<String, Logger>();		
	}
}

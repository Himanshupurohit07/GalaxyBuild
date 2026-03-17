package com.acn.migration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class CustomLogger {

	private static final Map<String, Logger> loggerMap = new HashMap<>();

	public static Logger getLogger(Class<?> clazz, String logFilePath, String logLevelStr) throws IOException {
		String loggerName = clazz.getName() + "." + logFilePath;

		if (loggerMap.containsKey(loggerName)) {
			return loggerMap.get(loggerName);
		}

		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration config = context.getConfiguration();

		// Define the log format
		PatternLayout layout = PatternLayout.newBuilder().withPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p %c - %m%n")
				.withConfiguration(config).build();

		// Unique appender name
		String appenderName = "FileAppender_" + loggerName;

		// Create a FileAppender without deprecated withName()
		FileAppender.Builder<?> builder = FileAppender.newBuilder();
		builder.setConfiguration(config);
		builder.setName(appenderName);
		builder.setLayout(layout);
		builder.withFileName(logFilePath + ".log");
		builder.withAppend(true);
		builder.withBufferedIo(true);

		FileAppender appender = builder.build();
		appender.start();
		config.addAppender(appender);

		// Set up logger config
		Level logLevel = Level.toLevel(logLevelStr.toUpperCase(), Level.INFO);
		LoggerConfig loggerConfig = new LoggerConfig(loggerName, logLevel, false);
		loggerConfig.addAppender(appender, logLevel, null);

		config.addLogger(loggerName, loggerConfig);
		context.updateLoggers();

		Logger logger = LogManager.getLogger(loggerName);
		loggerMap.put(loggerName, logger);
		System.out.println(loggerMap);

		return logger;
	}

	public static Logger getLogger(String clazz, String logFilePath, String logLevelStr) throws IOException {
		String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());

		String loggerName = clazz + "." + logLevelStr;

		if (loggerMap.containsKey(loggerName)) {
			return loggerMap.get(loggerName);
		}

		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration config = context.getConfiguration();

		// Define the log format
		PatternLayout layout = PatternLayout.newBuilder().withPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p %c - %m%n")
				.withConfiguration(config).build();

		// Unique appender name
		String appenderName = "FileAppender_" + loggerName;

		// Create a FileAppender without deprecated withName()
		FileAppender.Builder<?> builder = FileAppender.newBuilder();
		builder.setConfiguration(config);
		builder.setName(appenderName);
		builder.setLayout(layout);
		builder.withFileName(logFilePath + "_" + logLevelStr + "_" + timeStamp + ".log");
		builder.withAppend(true);
		builder.withBufferedIo(true);

		FileAppender appender = builder.build();
		appender.start();
		config.addAppender(appender);

		// Set up logger config
		Level logLevel = Level.toLevel(logLevelStr.toUpperCase(), Level.INFO);
		LoggerConfig loggerConfig = new LoggerConfig(loggerName, logLevel, false);
		loggerConfig.addAppender(appender, logLevel, null);

		config.addLogger(loggerName, loggerConfig);
		context.updateLoggers();

		Logger logger = LogManager.getLogger(loggerName);
		loggerMap.put(loggerName, logger);
		System.out.println(loggerMap);

		return logger;
	}

}

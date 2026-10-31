package facade;

import logger.AsyncLogger;
import logger.Logger;
import logger.SyncLogger;
import model.LogConfig;
import model.LogLevel;
import sink.Sink;
import sink.StdoutSink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoggerLibrary {
    private static LoggerLibrary instance;
    private final Map<String, Logger> loggers = new ConcurrentHashMap<>();

    private LoggerLibrary() {
    }

    public static synchronized LoggerLibrary getInstance() {
        if (instance == null) {
            instance = new LoggerLibrary();
        }
        return instance;
    }

    public void init(LogConfig config) {
        String name = config.getLoggerName();
        if (loggers.containsKey(name)) {
            System.out.println("Logger already exists: " + name);
            return;
        }

        List<Sink> sinks = new ArrayList<>();

        if ("STDOUT".equalsIgnoreCase(config.getSinkType())) {
            sinks.add(new StdoutSink(config.getLogLevel()));
        }

        Logger logger;

        if ("ASYNC".equalsIgnoreCase(config.getLoggerType())) {
            logger = new AsyncLogger(config, sinks);
        } else {
            logger = new SyncLogger(config, sinks);
        }
        loggers.put(name, logger);
        System.out.println("Logger created: " + name + " type=" + config.getLoggerType());
    }

    public void log(String loggerName, String message, LogLevel level) {
        Logger logger = loggers.get(loggerName);
        if (logger == null) {
            System.out.println("No logger found with name: " + loggerName);
            return;
        }
        logger.log(message, level);
    }

    public void shutdownAll() {
        System.out.println("Shutting down all loggers...");
        for (Logger logger : loggers.values()) {
            logger.shutdown();
        }
        loggers.clear();
    }
}
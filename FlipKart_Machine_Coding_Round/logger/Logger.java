package logger;

import model.LogLevel;

public interface Logger {
    void log(String content, LogLevel level);

    void shutdown();
}
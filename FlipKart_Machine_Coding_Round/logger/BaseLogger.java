package logger;

import model.LogConfig;
import model.Message;
import sink.Sink;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class BaseLogger implements Logger {
    protected LogConfig config;
    protected List<Sink> sinks;

    protected BaseLogger(LogConfig config, List<Sink> sinks) {
        this.config = config;
        this.sinks = sinks;
    }

    protected void writeToSinks(Message msg) {
        if (msg.getLevel().getPriority() < config.getLogLevel().getPriority()) {
            return;
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(config.getTsFormat()));
        String formatted = timestamp + " [" + msg.getLevel() + "] " + msg.getContent();
        for (Sink sink : sinks) {
            if (msg.getLevel().getPriority() >= sink.getSinkLevel().getPriority()) {
                sink.write(formatted);
            }
        }
    }
}
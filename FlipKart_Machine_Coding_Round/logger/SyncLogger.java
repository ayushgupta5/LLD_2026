package logger;

import model.LogConfig;
import model.LogLevel;
import model.Message;
import sink.Sink;

import java.util.List;

public class SyncLogger extends BaseLogger {

    public SyncLogger(LogConfig config, List<Sink> sinks) {
        super(config, sinks);
    }

    @Override
    public synchronized void log(String content, LogLevel level) {
        Message msg = new Message(content, level);
        writeToSinks(msg);
    }

    @Override
    public void shutdown() {
        // nothing to clean for sync
    }
}
package logger;

import model.LogConfig;
import model.LogLevel;
import model.Message;
import sink.Sink;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AsyncLogger extends BaseLogger {
    private final BlockingQueue<Message> queue;
    private final Thread worker;
    private volatile boolean running = true;

    public AsyncLogger(LogConfig config, List<Sink> sinks) {
        super(config, sinks);
        this.queue = new LinkedBlockingQueue<>(config.getBufferSize());
        this.worker = new Thread(() -> {
            while (running || !queue.isEmpty()) {
                try {
                    Message msg = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (msg != null) {
                        writeToSinks(msg);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        this.worker.setDaemon(true);
        this.worker.start();
    }

    @Override
    public void log(String content, LogLevel level) {
        try {
            queue.put(new Message(content, level));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void shutdown() {
        running = false;
        try {
            worker.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
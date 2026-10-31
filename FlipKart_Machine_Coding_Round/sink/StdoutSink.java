package sink;

import model.LogLevel;

public class StdoutSink implements Sink {
    private final LogLevel sinkLevel;

    public StdoutSink(LogLevel sinkLevel) {
        this.sinkLevel = sinkLevel;
    }

    @Override
    public void write(String msg) {
        System.out.println(msg);
    }

    @Override
    public LogLevel getSinkLevel() {
        return sinkLevel;
    }

}
package sink;

import model.LogLevel;

public interface Sink {
    void write(String formattedMessage);

    LogLevel getSinkLevel();

}


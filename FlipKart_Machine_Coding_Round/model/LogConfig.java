package model;

public class LogConfig {

    private final String loggerName;
    private final LogLevel logLevel;
    private final String loggerType;
    private final int bufferSize;
    private final String sinkType;
    private final String tsFormat;

    private LogConfig(Builder builder) {
        this.loggerName = builder.loggerName;
        this.logLevel = builder.logLevel;
        this.loggerType = builder.loggerType;
        this.bufferSize = builder.bufferSize;
        this.sinkType = builder.sinkType;
        this.tsFormat = builder.tsFormat;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public String getLoggerType() {
        return loggerType;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public String getSinkType() {
        return sinkType;
    }

    public String getTsFormat() {
        return tsFormat;
    }

    public static class Builder {
        private String loggerName = "app-logger";
        private LogLevel logLevel = LogLevel.INFO;
        private String loggerType = "SYNC";
        private int bufferSize = 10;
        private String sinkType = "STDOUT";
        private String tsFormat = "dd-MM-yyyy-HH-mm-ss";

        public Builder loggerName(String loggerName) {
            this.loggerName = loggerName;
            return this;
        }

        public Builder logLevel(LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public Builder loggerType(String loggerType) {
            this.loggerType = loggerType;
            return this;
        }

        public Builder bufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder sinkType(String sinkType) {
            this.sinkType = sinkType;
            return this;
        }

        public LogConfig build() {
            return new LogConfig(this);
        }
    }
}
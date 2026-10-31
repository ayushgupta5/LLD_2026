package model;

public class Message {
    private final String content;
    private final LogLevel level;

    public Message(String content, LogLevel level) {
        this.content = content;
        this.level = level;
    }

    public String getContent() {
        return content;
    }

    public LogLevel getLevel() {
        return level;
    }
}
import facade.LoggerLibrary;
import model.LogConfig;
import model.LogLevel;

public class Driver {
    public static void main(String[] args) throws InterruptedException {
        LoggerLibrary lib = LoggerLibrary.getInstance();
        LogConfig syncConfig = new LogConfig.Builder().loggerName("sync-logger").logLevel(LogLevel.INFO).loggerType("SYNC").sinkType("STDOUT").build();
        LogConfig asyncConfig = new LogConfig.Builder().loggerName("async-logger").logLevel(LogLevel.DEBUG).loggerType("ASYNC").bufferSize(25).sinkType("STDOUT").build();
        lib.init(syncConfig);
        lib.init(asyncConfig);

        System.out.println("\nsync logger:");
        lib.log("sync-logger", "app started successfully", LogLevel.INFO);
        lib.log("sync-logger", "this is debug msg", LogLevel.DEBUG);
        lib.log("sync-logger", "db connection failed", LogLevel.ERROR);
        lib.log("sync-logger", "server is down", LogLevel.FATAL);

        System.out.println("\nasync logger:");
        lib.log("async-logger", "user logged in", LogLevel.DEBUG);
        lib.log("async-logger", "fetching user data", LogLevel.INFO);
        lib.log("async-logger", "response time high", LogLevel.WARN);
        lib.log("async-logger", "payment failed", LogLevel.ERROR);

        Thread.sleep(300);

        LoggerLibrary lib2 = LoggerLibrary.getInstance();
        lib2.init(syncConfig);
        lib2.init(asyncConfig);

        System.out.println("\nlib2_sync logger:");
        lib2.log("sync-logger", "app started successfully", LogLevel.INFO);
        lib2.log("sync-logger", "this is debug msg", LogLevel.DEBUG);
        lib2.log("sync-logger", "db connection failed", LogLevel.ERROR);
        lib2.log("sync-logger", "server is down", LogLevel.FATAL);

        System.out.println("\nlib2_async logger:");
        lib2.log("async-logger", "user logged in", LogLevel.DEBUG);
        lib2.log("async-logger", "fetching user data", LogLevel.INFO);
        lib2.log("async-logger", "response time high", LogLevel.WARN);
        lib2.log("async-logger", "payment failed", LogLevel.ERROR);


        lib.shutdownAll();

        System.out.println("\nsecond attempt : lib2_async logger:");
        lib2.log("async-logger", "user logged in", LogLevel.DEBUG);
        lib2.log("async-logger", "fetching user data", LogLevel.INFO);
        lib2.log("async-logger", "response time high", LogLevel.WARN);
        lib2.log("async-logger", "payment failed", LogLevel.ERROR);

        System.out.println("done.");
    }
}
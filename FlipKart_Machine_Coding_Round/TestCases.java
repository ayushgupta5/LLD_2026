import facade.LoggerLibrary;
import model.LogConfig;
import model.LogLevel;

public class TestCases {
    public static void main(String[] args) throws InterruptedException {
        LoggerLibrary lib = LoggerLibrary.getInstance();
        LogConfig syncConfig = new LogConfig.Builder().loggerName("sync-logger").logLevel(LogLevel.INFO).loggerType("SYNC").sinkType("STDOUT").build();
        LogConfig asyncConfig = new LogConfig.Builder().loggerName("async-logger").logLevel(LogLevel.DEBUG).loggerType("ASYNC").bufferSize(25).sinkType("STDOUT").build();
        lib.init(syncConfig);
        lib.init(asyncConfig);
        System.out.println("\nlevel filtering:");
        lib.log("sync-logger", "info msg", LogLevel.INFO);
        lib.log("sync-logger", "debug msg should drop", LogLevel.DEBUG);
        lib.log("sync-logger", "warn msg", LogLevel.WARN);
        lib.log("sync-logger", "error msg", LogLevel.ERROR);
        lib.log("sync-logger", "fatal msg", LogLevel.FATAL);
        System.out.println("\nasync all levels:");
        lib.log("async-logger", "debug msg", LogLevel.DEBUG);
        lib.log("async-logger", "info msg", LogLevel.INFO);
        lib.log("async-logger", "warn msg", LogLevel.WARN);
        lib.log("async-logger", "error msg", LogLevel.ERROR);
        lib.log("async-logger", "fatal msg", LogLevel.FATAL);
        Thread.sleep(300);
        System.out.println("\nwrong name:");
        lib.log("xyz-logger", "should not work", LogLevel.INFO);
        System.out.println("\nduplicate init:");
        lib.init(syncConfig);
        System.out.println("\nmulti thread:");
        Thread t1 = new Thread(() -> {
            for (int i = 1; i <= 5; i++)
                lib.log("async-logger", "thread1 msg " + i, LogLevel.INFO);
        });
        Thread t2 = new Thread(() -> {
            for (int i = 1; i <= 5; i++)
                lib.log("async-logger", "thread2 msg " + i, LogLevel.WARN);
        });
        Thread t3 = new Thread(() -> {
            for (int i = 1; i <= 5; i++)
                lib.log("async-logger", "thread3 msg " + i, LogLevel.ERROR);
        });
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
        Thread.sleep(500);
        lib.shutdownAll();
        System.out.println("\nafter shutdown:");
        lib.log("sync-logger", "should not print", LogLevel.INFO);
        lib.log("async-logger", "should also fail", LogLevel.ERROR);
        System.out.println("\nall tests done.");
    }
}

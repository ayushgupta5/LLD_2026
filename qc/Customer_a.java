// Customer.java
import java.util.concurrent.atomic.AtomicLong;

public class Customer_a {
    private static AtomicLong idCounter = new AtomicLong(1);

    private long customerId;
    private String name;

    public Customer_a(String name) {
        this.customerId = idCounter.getAndIncrement();
        this.name = name;
    }

    public long getCustomerId() { return customerId; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return String.format("Customer[ID=%d, Name=%s]", customerId, name);
    }
}
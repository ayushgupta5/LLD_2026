package src.models;

import java.time.LocalDateTime;

public class Customer {
    private long customerId;
    private String name;
    private String phone;
    private String email;

    public Customer(long customerId, String name) {
        this.customerId = customerId;
        this.name = name;
        LocalDateTime registeredAt = LocalDateTime.now();
    }

    public long getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return String.format("Customer[ID=%d, Name=%s, Phone=%s]",
                customerId, name, phone);
    }
}
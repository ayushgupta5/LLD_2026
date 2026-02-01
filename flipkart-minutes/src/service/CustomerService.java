package src.service;

import src.models.Customer;
import src.repository.CustomerRepository;
import src.util.FileUtil;

public class CustomerService {

    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private long customerIdCounter;

    public CustomerService(CustomerRepository customerRepository,
                           NotificationService notificationService) {
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
        this.customerIdCounter = loadLastCustomerId();
    }

    private long loadLastCustomerId() {
        return FileUtil.loadCounter("customer_counter.txt", 1L);
    }

    private void saveCustomerId(long id) {
        FileUtil.saveCounter("customer_counter.txt", id);
    }

    public synchronized Customer onboardCustomer(String name) {
        return onboardCustomer(name, null);
    }

    public synchronized Customer onboardCustomer(String name, String phone) {
        long customerId = ++customerIdCounter;
        saveCustomerId(customerId);

        Customer customer = new Customer(customerId, name);
        customer.setPhone(phone);

        customerRepository.save(customer);

        System.out.println("✓ Customer onboarded: " + customer);
        notificationService.logSystemEvent("New customer onboarded: " + name);

        return customer;
    }

    public Customer getCustomer(long customerId) {
        return customerRepository.findById(String.valueOf(customerId)).orElse(null);
    }
}
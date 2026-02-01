package src.repository;

import src.models.Customer;

import java.util.List;
import java.util.stream.Collectors;

public class CustomerRepository extends InMemoryRepository<Customer> {

    @Override
    protected String getEntityId(Customer entity) {
        return String.valueOf(entity.getCustomerId());
    }

    public Customer findByName(String name) {
        return findAll().stream().filter(customer -> customer.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Customer> findByNameContaining(String keyword) {
        return findAll().stream().filter(customer -> customer.getName().toLowerCase().contains(keyword.toLowerCase())).collect(Collectors.toList());
    }

    public Customer findByEmail(String email) {
        return findAll().stream().filter(customer -> email.equals(customer.getEmail())).findFirst().orElse(null);
    }

    public Customer findByPhone(String phone) {
        return findAll().stream().filter(customer -> phone.equals(customer.getPhone())).findFirst().orElse(null);
    }
}
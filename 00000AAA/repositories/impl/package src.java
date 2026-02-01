package src.enums;

public enum OrderStatus {
    PENDING,
    ASSIGNED,
    PICKED_UP,
    DELIVERED,
    CANCELLED
}

package src.enums;

public enum PartnerStatus {
    AVAILABLE,
    BUSY,
    OFFLINE
}

package src.models;

import java.time.LocalDateTime;
import java.io.Serializable;

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

    public long getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return String.format("Customer[ID=%d, Name=%s, Phone=%s]",
                customerId, name, phone);
    }
}

package src.models;

import java.time.LocalDateTime;
import java.io.Serializable;

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

    public long getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return String.format("Customer[ID=%d, Name=%s, Phone=%s]",
                customerId, name, phone);
    }
}

package src.models;

import src.enums.OrderStatus;

import java.time.LocalDateTime;

public class Order {
    private long orderId;
    private long customerId;
    private String itemName;
    private OrderStatus status;
    private Long assignedPartnerId;
    private LocalDateTime createdAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;

    public Order(long orderId, long customerId, String itemName) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.itemName = itemName;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public long getOrderId() { return orderId; }
    public long getCustomerId() { return customerId; }
    public String getItemName() { return itemName; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public Long getAssignedPartnerId() { return assignedPartnerId; }
    public void setAssignedPartnerId(Long partnerId) { this.assignedPartnerId = partnerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(LocalDateTime time) { this.pickedUpAt = time; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime time) { this.deliveredAt = time; }

    @Override
    public String toString() {
        return String.format("Order[ID=%d, Customer=%d, Item=%s, Status=%s, Partner=%s]",
                orderId, customerId, itemName, status, assignedPartnerId);
    }
}

package src.repository;

import src.models.Customer;
import java.util.List;
import java.util.stream.Collectors;

// SOLID: Single Responsibility - Only manages Customer files
public class CustomerRepository extends FileRepository<Customer> {

    private static final String CUSTOMER_DIR = "data/customers";

    public CustomerRepository() {
        super(CUSTOMER_DIR, Customer.class);
    }

    @Override
    protected String getEntityId(Customer entity) {
        return String.valueOf(entity.getCustomerId());
    }

    // Custom query methods
    public Customer findByName(String name) {
        return findAll().stream()
                .filter(customer -> customer.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public List<Customer> findByNameContaining(String keyword) {
        return findAll().stream()
                .filter(customer -> customer.getName().toLowerCase()
                        .contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Customer findByEmail(String email) {
        return findAll().stream()
                .filter(customer -> email.equals(customer.getEmail()))
                .findFirst()
                .orElse(null);
    }

    public Customer findByPhone(String phone) {
        return findAll().stream()
                .filter(customer -> phone.equals(customer.getPhone()))
                .findFirst()
                .orElse(null);
    }
}

package src.repository;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

// SOLID: Single Responsibility - Only handles file I/O
public abstract class FileRepository<T> implements IRepository<T> {

    protected final String directoryPath;
    protected final Class<T> entityClass;

    public FileRepository(String directoryPath, Class<T> entityClass) {
        this.directoryPath = directoryPath;
        this.entityClass = entityClass;
        createDirectoryIfNotExists();
    }

    private void createDirectoryIfNotExists() {
        try {
            Files.createDirectories(Paths.get(directoryPath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory: " + directoryPath, e);
        }
    }

    @Override
    public T save(T entity) {
        String id = getEntityId(entity);
        String filePath = getFilePath(id);

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath), java.nio.charset.StandardCharsets.UTF_8))) {
            String json = toJson(entity);
            writer.write(json);
            return entity;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save entity: " + id, e);
        }
    }

    @Override
    public Optional<T> findById(String id) {
        String filePathJson = getFilePath(id);
        Path p = Paths.get(filePathJson);

        if (!Files.exists(p)) {
            // No json file found
            return Optional.empty();
        }

        try (Reader reader = Files.newBufferedReader(p)) {
            T entity = fromJson(reader, entityClass);
            return Optional.ofNullable(entity);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read entity: " + id, e);
        }
    }

    @Override
    public List<T> findAll() {
        List<T> entities = new ArrayList<>();
        File directory = new File(directoryPath);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".json") || name.endsWith(".dat"));

        if (files != null) {
            for (File file : files) {
                try {
                    if (file.getName().endsWith(".json")) {
                        try (Reader reader = new FileReader(file)) {
                            T entity = fromJson(reader, entityClass);
                            if (entity != null) entities.add(entity);
                        }
                    } else if (file.getName().endsWith(".dat")) {
                        // Legacy: attempt to read Java-serialized file and migrate to JSON
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                            @SuppressWarnings("unchecked")
                            T entity = (T) ois.readObject();
                            if (entity != null) {
                                entities.add(entity);
                                // Migrate: write JSON file and rename .dat to .dat.bak
                                String baseName = file.getName().substring(0, file.getName().lastIndexOf('.'));
                                String jsonPath = directoryPath + File.separator + baseName + ".json";
                                try (Writer writer = new FileWriter(jsonPath)) {
                                    writer.write(toJson(entity));
                                } catch (IOException ex) {
                                    System.err.println("Warning: could not migrate .dat to .json for " + file.getName());
                                }
                                // keep the .dat as backup by renaming
                                try {
                                    Files.move(file.toPath(), file.toPath().resolveSibling(file.getName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException ex) {
                                    // ignore
                                }
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            System.err.println("Error reading file: " + file.getName());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error reading file: " + file.getName());
                }
            }
        }

        return entities;
    }

    @Override
    public void delete(String id) {
        String filePath = getFilePath(id);
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete entity: " + id, e);
        }
    }

    @Override
    public boolean exists(String id) {
        return new File(getFilePath(id)).exists();
    }

    protected String getFilePath(String id) {
        return directoryPath + File.separator + id + ".json";
    }

    // Simple reflection-based JSON serializer for interview-friendly code
    private String toJson(T entity) {
        StringBuilder sb = new StringBuilder();
        sb.append('{').append('\n');
        Field[] fields = entityClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            f.setAccessible(true);
            try {
                Object val = f.get(entity);
                sb.append("  \"").append(f.getName()).append("\": ");
                sb.append(toJsonValue(val));
                if (i < fields.length - 1) sb.append(',');
                sb.append('\n');
            } catch (IllegalAccessException e) {
                // ignore inaccessible field
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private String toJsonValue(Object val) {
        if (val == null) return "null";
        if (val instanceof String) return '"' + escape((String) val) + '"';
        if (val instanceof Number || val instanceof Boolean) return String.valueOf(val);
        if (val instanceof Enum) return '"' + ((Enum<?>) val).name() + '"';
        if (val instanceof LocalDateTime) return '"' + val.toString() + '"';
        // Fallback: call toString quoted
        return '"' + escape(val.toString()) + '"';
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    // Very small parser that expects the format produced by toJson
    private T fromJson(Reader reader, Class<T> clazz) {
        try {
            String content = readAll(reader).trim();
            if (content.isEmpty() || content.equals("{}")) return null;
            // remove leading { and trailing }
            if (content.startsWith("{")) content = content.substring(1);
            if (content.endsWith("}")) content = content.substring(0, content.length() - 1);

            T instance = clazz.getDeclaredConstructor().newInstance();
            String[] lines = content.split("\n");
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty()) continue;
                // remove trailing comma
                if (line.endsWith(",")) line = line.substring(0, line.length() - 1);
                // expect "key": value
                int colon = line.indexOf(':');
                if (colon < 0) continue;
                String keyPart = line.substring(0, colon).trim();
                String valuePart = line.substring(colon + 1).trim();
                // remove quotes from key
                String key = keyPart.replaceAll("^\"|\"$", "");

                Field field;
                try {
                    field = clazz.getDeclaredField(key);
                } catch (NoSuchFieldException e) {
                    continue; // unknown field
                }
                field.setAccessible(true);

                Object parsed = parseValue(valuePart, field.getType());
                field.set(instance, parsed);
            }
            return instance;
        } catch (Exception e) {
            // Parsing or instantiation error
            return null;
        }
    }

    private Object parseValue(String raw, Class<?> targetType) {
        if (raw.equals("null")) return null;
        if (targetType == String.class) return unquote(raw);
        if (targetType == long.class || targetType == Long.class) return Long.parseLong(raw);
        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(raw);
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(raw);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(raw);
        if (Enum.class.isAssignableFrom(targetType)) {
            String name = unquote(raw);
            @SuppressWarnings({"unchecked", "rawtypes"})
            Enum e = Enum.valueOf((Class<? extends Enum>) targetType, name);
            return e;
        }
        if (targetType == LocalDateTime.class) {
            String s = unquote(raw);
            return LocalDateTime.parse(s);
        }
        // Fallback: try to unquote
        return unquote(raw);
    }

    private String unquote(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private String readAll(Reader r) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[8192];
        int len;
        while ((len = r.read(buf)) != -1) sb.append(buf, 0, len);
        return sb.toString();
    }

    // Abstract method - each repository implements its own ID extraction
    protected abstract String getEntityId(T entity);
}

package src.repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class InMemoryRepository<T> implements IRepository<T> {

    protected final Map<String, T> storage = new ConcurrentHashMap<>();

    @Override
    public T save(T entity) {
        String id = getEntityId(entity);
        storage.put(id, entity);
        return entity;
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void delete(String id) {
        storage.remove(id);
    }

    @Override
    public boolean exists(String id) {
        return storage.containsKey(id);
    }

    protected abstract String getEntityId(T entity);
}

package src.repository;

import java.util.List;
import java.util.Optional;


public interface IRepository<T> {
    T save(T entity);
    Optional<T> findById(String id);
    List<T> findAll();
    void delete(String id);
    boolean exists(String id);
}

package src.repository;

import src.models.Order;
import src.enums.OrderStatus;

import java.util.List;
import java.util.stream.Collectors;

public class OrderRepository extends FileRepository<Order> {

    private static final String ORDER_DIR = "data/orders";

    public OrderRepository() {
        super(ORDER_DIR, Order.class);
    }

    @Override
    protected String getEntityId(Order entity) {
        return String.valueOf(entity.getOrderId());
    }

    // Custom query methods
    public List<Order> findByCustomerId(long customerId) {
        return findAll().stream()
                .filter(order -> order.getCustomerId() == customerId)
                .collect(Collectors.toList());
    }

    public List<Order> findByStatus(OrderStatus status) {
        return findAll().stream()
                .filter(order -> order.getStatus() == status)
                .collect(Collectors.toList());
    }
}

package src.repository;


import src.models.DeliveryPartner;
import src.enums.PartnerStatus;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

// SOLID: Single Responsibility - Only manages Partner files
public class PartnerRepository extends FileRepository<DeliveryPartner> {

    private static final String PARTNER_DIR = "data/partners";

    public PartnerRepository() {
        super(PARTNER_DIR, DeliveryPartner.class);
    }

    @Override
    protected String getEntityId(DeliveryPartner entity) {
        return String.valueOf(entity.getPartnerId());
    }

    // Custom query methods
    public List<DeliveryPartner> findByStatus(PartnerStatus status) {
        return findAll().stream()
                .filter(partner -> partner.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<DeliveryPartner> findAvailablePartners() {
        return findByStatus(PartnerStatus.AVAILABLE);
    }

    public DeliveryPartner findByPhone(String phone) {
        return findAll().stream()
                .filter(partner -> phone.equals(partner.getPhone()))
                .findFirst()
                .orElse(null);
    }

    public DeliveryPartner findByVehicleNumber(String vehicleNumber) {
        return findAll().stream()
                .filter(partner -> vehicleNumber.equals(partner.getVehicleNumber()))
                .findFirst()
                .orElse(null);
    }

    // Get top performers
    public List<DeliveryPartner> getTopPartnersByDeliveries(int limit) {
        return findAll().stream()
                .sorted(Comparator.comparingInt(DeliveryPartner::getTotalDeliveries).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<DeliveryPartner> getTopPartnersByRating(int limit) {
        return findAll().stream()
                .filter(partner -> partner.getRatingCount() > 0)
                .sorted(Comparator.comparingDouble(DeliveryPartner::getAverageRating).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Dashboard data
    public List<DeliveryPartner> getTopPartners(int limit) {
        return findAll().stream()
                .sorted((p1, p2) -> {
                    // Sort by deliveries first, then by rating
                    int deliveryCompare = Integer.compare(
                            p2.getTotalDeliveries(), p1.getTotalDeliveries());
                    if (deliveryCompare != 0) return deliveryCompare;
                    return Double.compare(p2.getAverageRating(), p1.getAverageRating());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
}

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

package src.service;

import src.repository.CustomerRepository;
import src.repository.PartnerRepository;

import java.time.format.DateTimeFormatter;

import src.models.Customer;
import src.models.DeliveryPartner;
import src.repository.CustomerRepository;
import src.repository.PartnerRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// SOLID: Single Responsibility - Only handles notifications
public class NotificationService {

    private final CustomerRepository customerRepository;
    private final PartnerRepository partnerRepository;
    private final DateTimeFormatter timeFormatter;

    public NotificationService(CustomerRepository customerRepository,
                               PartnerRepository partnerRepository) {
        this.customerRepository = customerRepository;
        this.partnerRepository = partnerRepository;
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    }

    // Simple constructor for basic usage
    public NotificationService() {
        this.customerRepository = null;
        this.partnerRepository = null;
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    }

    public void notifyCustomer(long customerId, String message) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String customerName = getCustomerName(customerId);

        System.out.println(String.format(
                "📱 [%s] CUSTOMER NOTIFICATION [%s - ID:%d]: %s",
                timestamp, customerName, customerId, message
        ));

        // In real system: send SMS, email, push notification
    }

    public void notifyPartner(long partnerId, String message) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String partnerName = getPartnerName(partnerId);

        System.out.println(String.format(
                "🚗 [%s] PARTNER NOTIFICATION [%s - ID:%d]: %s",
                timestamp, partnerName, partnerId, message
        ));

        // In real system: send SMS, app notification
    }

    public void notifyOrderCreated(long customerId, long orderId, String itemName) {
        notifyCustomer(customerId,
                String.format("Order #%d created for %s. Looking for delivery partner...",
                        orderId, itemName));
    }

    public void notifyOrderAssigned(long customerId, long orderId, long partnerId) {
        notifyCustomer(customerId,
                String.format("Order #%d assigned to delivery partner #%d",
                        orderId, partnerId));

        notifyPartner(partnerId,
                String.format("New order #%d assigned to you", orderId));
    }

    public void notifyOrderPickedUp(long customerId, long orderId, long partnerId) {
        notifyCustomer(customerId,
                String.format("Order #%d picked up by delivery partner. On the way!",
                        orderId));
    }

    public void notifyOrderDelivered(long customerId, long orderId) {
        notifyCustomer(customerId,
                String.format("Order #%d delivered successfully! Please rate your experience.",
                        orderId));
    }

    public void notifyOrderCancelled(long customerId, long orderId, String reason) {
        notifyCustomer(customerId,
                String.format("Order #%d has been cancelled. Reason: %s",
                        orderId, reason));
    }

    public void notifyPartnerFreed(long partnerId, long orderId, String reason) {
        notifyPartner(partnerId,
                String.format("Order #%d cancelled. %s. You are now available for new orders.",
                        orderId, reason));
    }

    // Helper methods
    private String getCustomerName(long customerId) {
        if (customerRepository != null) {
            return customerRepository.findById(String.valueOf(customerId))
                    .map(Customer::getName)
                    .orElse("Customer");
        }
        return "Customer";
    }

    private String getPartnerName(long partnerId) {
        if (partnerRepository != null) {
            return partnerRepository.findById(String.valueOf(partnerId))
                    .map(DeliveryPartner::getName)
                    .orElse("Partner");
        }
        return "Partner";
    }

    // System notifications
    public void logSystemEvent(String event) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        System.out.println(String.format("⚙️  [%s] SYSTEM: %s", timestamp, event));
    }

    public void logError(String error) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        System.err.println(String.format("❌ [%s] ERROR: %s", timestamp, error));
    }
}

package src.service;

import src.enums.OrderStatus;
import src.enums.PartnerStatus;
import src.models.*;
import src.repository.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

// SOLID: Single Responsibility - Only order business logic
public class OrderService {

    private final OrderRepository orderRepository;
    private final PartnerRepository partnerRepository;
    private final NotificationService notificationService;

    private final AtomicLong orderIdCounter;
    private final BlockingQueue<Long> pendingOrders;
    private final ExecutorService assignmentExecutor;

    // SOLID: Dependency Injection
    public OrderService(OrderRepository orderRepository,
                        PartnerRepository partnerRepository,
                        NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.partnerRepository = partnerRepository;
        this.notificationService = notificationService;

        this.orderIdCounter = new AtomicLong(loadLastOrderId());
        this.pendingOrders = new LinkedBlockingQueue<>();
        this.assignmentExecutor = Executors.newSingleThreadExecutor();

        startAutoAssignment();
    }

    private long loadLastOrderId() {
        // Load from metadata file or return default
        try {
            String content = Files.readString(Paths.get("data/metadata/order_counter.txt"));
            return Long.parseLong(content.trim());
        } catch (Exception e) {
            return 1000;
        }
    }

    private void saveOrderId(long id) {
        try {
            Files.createDirectories(Paths.get("data/metadata"));
            Files.writeString(Paths.get("data/metadata/order_counter.txt"),
                    String.valueOf(id));
        } catch (IOException e) {
            // Log error
        }
    }

    public Order createOrder(long customerId, String itemName) {
        long orderId = orderIdCounter.incrementAndGet();
        saveOrderId(orderId);

        Order order = new Order(orderId, customerId, itemName);
        orderRepository.save(order);

        pendingOrders.offer(orderId);
        notificationService.notifyCustomer(customerId, "Order created: " + orderId);

        return order;
    }

    public boolean cancelOrder(long orderId) {
        Optional<Order> optOrder = orderRepository.findById(String.valueOf(orderId));

        if (optOrder.isEmpty()) {
            return false;
        }

        Order order = optOrder.get();

        // Cannot cancel if picked up
        if (order.getStatus() == OrderStatus.PICKED_UP ||
                order.getStatus() == OrderStatus.DELIVERED) {
            return false;
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Free partner if assigned
        if (order.getAssignedPartnerId() != null) {
            partnerRepository.findById(String.valueOf(order.getAssignedPartnerId()))
                    .ifPresent(partner -> {
                        partner.setStatus(PartnerStatus.AVAILABLE);
                        partner.setCurrentOrderId(null);
                        partnerRepository.save(partner);
                    });
        }

        pendingOrders.remove(orderId);
        notificationService.notifyCustomer(order.getCustomerId(),
                "Order cancelled: " + orderId);

        return true;
    }

    private void startAutoAssignment() {
        assignmentExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Long orderId = pendingOrders.take();
                    assignOrderToPartner(orderId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private synchronized void assignOrderToPartner(long orderId) {
        Optional<Order> optOrder = orderRepository.findById(String.valueOf(orderId));

        if (optOrder.isEmpty() ||
                optOrder.get().getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        Order order = optOrder.get();

        // Find available partner
        Optional<DeliveryPartner> availablePartner = partnerRepository.findAll()
                .stream()
                .filter(p -> p.getStatus() == PartnerStatus.AVAILABLE)
                .findFirst();

        if (availablePartner.isPresent()) {
            DeliveryPartner partner = availablePartner.get();

            order.setStatus(OrderStatus.ASSIGNED);
            order.setAssignedPartnerId(partner.getPartnerId());
            orderRepository.save(order);

            partner.setStatus(PartnerStatus.BUSY);
            partner.setCurrentOrderId(orderId);
            partnerRepository.save(partner);

            notificationService.notifyPartner(partner.getPartnerId(),
                    "New order assigned: " + orderId);
        } else {
            // Put back in queue
            pendingOrders.offer(orderId);
        }
    }

    public Order getOrderStatus(long orderId) {
        return orderRepository.findById(String.valueOf(orderId)).orElse(null);
    }

    // Graceful shutdown: stop background assignment thread and persist counter
    public void shutdown() {
        // Persist current counter
        saveOrderId(orderIdCounter.get());

        // Stop executor
        assignmentExecutor.shutdownNow();
        try {
            if (!assignmentExecutor.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS)) {
                assignmentExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

package src.service;
import src.models.DeliveryPartner;
import src.enums.PartnerStatus;
import src.models.Order;
import src.enums.OrderStatus;
import src.repository.PartnerRepository;
import src.repository.OrderRepository;
import src.util.FileUtil;
import java.util.List;
import java.util.Optional;

// SOLID: Single Responsibility - Only partner business logic
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private long partnerIdCounter;

    // SOLID: Dependency Injection
    public PartnerService(PartnerRepository partnerRepository,
                          OrderRepository orderRepository,
                          NotificationService notificationService) {
        this.partnerRepository = partnerRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
        this.partnerIdCounter = loadLastPartnerId();
    }

    private long loadLastPartnerId() {
        return FileUtil.loadCounter("partner_counter.txt", 1L);
    }

    private void savePartnerId(long id) {
        FileUtil.saveCounter("partner_counter.txt", id);
    }

    // Onboard new partner
    public DeliveryPartner onboardPartner(String name) {
        return onboardPartner(name, null, null);
    }

    public synchronized DeliveryPartner onboardPartner(String name, String phone,
                                                       String vehicleNumber) {
        long partnerId = ++partnerIdCounter;
        savePartnerId(partnerId);

        DeliveryPartner partner = new DeliveryPartner(partnerId, name);
        partner.setPhone(phone);
        partner.setVehicleNumber(vehicleNumber);

        partnerRepository.save(partner);

        System.out.println("✓ Partner onboarded: " + partner);
        notificationService.logSystemEvent("New partner onboarded: " + name);

        return partner;
    }

    // Pick up order
    public synchronized boolean pickUpOrder(long partnerId, long orderId) {
        Optional<DeliveryPartner> optPartner =
                partnerRepository.findById(String.valueOf(partnerId));
        Optional<Order> optOrder =
                orderRepository.findById(String.valueOf(orderId));

        if (optPartner.isEmpty()) {
            notificationService.logError("Partner not found: " + partnerId);
            return false;
        }

        if (optOrder.isEmpty()) {
            notificationService.logError("Order not found: " + orderId);
            return false;
        }

        DeliveryPartner partner = optPartner.get();
        Order order = optOrder.get();

        // Validation checks
        if (order.getAssignedPartnerId() == null ||
                order.getAssignedPartnerId() != partnerId) {
            System.out.println("✗ Order not assigned to this partner");
            return false;
        }

        if (order.getStatus() != OrderStatus.ASSIGNED) {
            System.out.println("✗ Order cannot be picked up (status: " +
                    order.getStatus() + ")");
            return false;
        }

        if (partner.getStatus() != PartnerStatus.BUSY) {
            System.out.println("✗ Partner status invalid");
            return false;
        }

        // Pick up order
        order.setStatus(OrderStatus.PICKED_UP);
        order.setPickedUpAt(java.time.LocalDateTime.now());
        orderRepository.save(order);

        System.out.println("✓ Order picked up: " + orderId + " by Partner: " + partnerId);
        notificationService.notifyOrderPickedUp(order.getCustomerId(), orderId, partnerId);

        return true;
    }

    // Complete delivery
    public synchronized boolean completeOrder(long partnerId, long orderId) {
        return completeOrder(partnerId, orderId, null);
    }

    public synchronized boolean completeOrder(long partnerId, long orderId, Integer rating) {
        Optional<DeliveryPartner> optPartner =
                partnerRepository.findById(String.valueOf(partnerId));
        Optional<Order> optOrder =
                orderRepository.findById(String.valueOf(orderId));

        if (optPartner.isEmpty() || optOrder.isEmpty()) {
            return false;
        }

        DeliveryPartner partner = optPartner.get();
        Order order = optOrder.get();

        // Validation
        if (order.getStatus() != OrderStatus.PICKED_UP ||
                order.getAssignedPartnerId() != partnerId) {
            System.out.println("✗ Cannot complete order (not picked up by this partner)");
            return false;
        }

        // Complete order
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(java.time.LocalDateTime.now());
        orderRepository.save(order);

        // Update partner
        partner.setStatus(PartnerStatus.AVAILABLE);
        partner.setCurrentOrderId(null);
        partner.incrementDeliveries();

        if (rating != null && rating >= 1 && rating <= 5) {
            partner.addRating(rating);
            System.out.println("✓ Rating added: " + rating + " stars");
        }

        partnerRepository.save(partner);

        System.out.println("✓ Order delivered: " + orderId + " by Partner: " + partnerId);
        notificationService.notifyOrderDelivered(order.getCustomerId(), orderId);

        return true;
    }

    // Get partner status
    public DeliveryPartner getPartnerStatus(long partnerId) {
        return partnerRepository.findById(String.valueOf(partnerId)).orElse(null);
    }

    // Change partner status (online/offline)
    public synchronized boolean updatePartnerStatus(long partnerId, PartnerStatus status) {
        Optional<DeliveryPartner> optPartner =
                partnerRepository.findById(String.valueOf(partnerId));

        if (optPartner.isEmpty()) {
            return false;
        }

        DeliveryPartner partner = optPartner.get();

        // Cannot go offline if busy
        if (status == PartnerStatus.OFFLINE && partner.getStatus() == PartnerStatus.BUSY) {
            System.out.println("✗ Cannot go offline while delivering order");
            return false;
        }

        partner.setStatus(status);
        partnerRepository.save(partner);

        System.out.println("✓ Partner status updated: " + partnerId + " -> " + status);
        return true;
    }

    // Get all partners
    public List<DeliveryPartner> getAllPartners() {
        return partnerRepository.findAll();
    }

    // Get available partners
    public List<DeliveryPartner> getAvailablePartners() {
        return partnerRepository.findAvailablePartners();
    }

    // Dashboard: Top performers
    public void showTopPartners() {
        showTopPartners(10);
    }

    public void showTopPartners(int limit) {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("       TOP DELIVERY PARTNERS");
        System.out.println("═══════════════════════════════════════");

        List<DeliveryPartner> topPartners = partnerRepository.getTopPartners(limit);

        if (topPartners.isEmpty()) {
            System.out.println("No partners found.");
            return;
        }

        System.out.println(String.format("%-5s %-20s %-12s %-10s %-10s",
                "Rank", "Name", "Deliveries", "Rating", "Status"));
        System.out.println("───────────────────────────────────────");

        int rank = 1;
        for (DeliveryPartner partner : topPartners) {
            System.out.println(String.format("%-5d %-20s %-12d %-10.2f %-10s",
                    rank++,
                    partner.getName(),
                    partner.getTotalDeliveries(),
                    partner.getAverageRating(),
                    partner.getStatus()
            ));
        }

        System.out.println("═══════════════════════════════════════\n");
    }
}

package src.util;

import java.io.*;
import java.nio.file.*;

// SOLID: Single Responsibility - Only file utility operations
public class FileUtil {

    private static final String METADATA_DIR = "data/metadata";

    // Load counter from file
    public static long loadCounter(String filename, long defaultValue) {
        try {
            Path path = Paths.get(METADATA_DIR, filename);
            if (Files.exists(path)) {
                String content = Files.readString(path).trim();
                return Long.parseLong(content);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Warning: Could not load counter from " + filename +
                    ", using default: " + defaultValue);
        }
        return defaultValue;
    }

    // Save counter to file
    public static void saveCounter(String filename, long value) {
        try {
            Path metadataPath = Paths.get(METADATA_DIR);
            Files.createDirectories(metadataPath);

            Path filePath = Paths.get(METADATA_DIR, filename);
            Files.writeString(filePath, String.valueOf(value));
        } catch (IOException e) {
            System.err.println("Error: Could not save counter to " + filename);
            e.printStackTrace();
        }
    }

    // Ensure directory exists
    public static void ensureDirectoryExists(String directoryPath) {
        try {
            Files.createDirectories(Paths.get(directoryPath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory: " + directoryPath, e);
        }
    }

    // Delete file if exists
    public static boolean deleteFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            System.err.println("Error: Could not delete file: " + filePath);
            return false;
        }
    }

    // Check if file exists
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    // Read file content
    public static String readFile(String filePath) throws IOException {
        return Files.readString(Paths.get(filePath));
    }

    // Write file content
    public static void writeFile(String filePath, String content) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    // List all files in directory
    public static File[] listFiles(String directoryPath) {
        File directory = new File(directoryPath);
        return directory.listFiles();
    }

    // List JSON files in directory
    public static File[] listJsonFiles(String directoryPath) {
        File directory = new File(directoryPath);
        return directory.listFiles((dir, name) -> name.endsWith(".json"));
    }

    // Delete directory recursively
    public static void deleteDirectory(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((p1, p2) -> -p1.compareTo(p2)) // Reverse order for deletion
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            System.err.println("Could not delete: " + p);
                        }
                    });
        }
    }

    // Get file size
    public static long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            return -1;
        }
    }

    // Copy file
    public static void copyFile(String sourcePath, String destinationPath) throws IOException {
        Files.copy(Paths.get(sourcePath), Paths.get(destinationPath),
                StandardCopyOption.REPLACE_EXISTING);
    }
}

import src.repository.*;
import src.service.*;
import src.models.*;

public class Main {
    public static void main(String[] args) throws Exception {

        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║      FLIPKART MINUTES - IN-MEMORY SYSTEM       ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        // Initialize repositories
        CustomerRepository customerRepo = new CustomerRepository();
        PartnerRepository partnerRepo = new PartnerRepository();
        OrderRepository orderRepo = new OrderRepository();

        // Initialize services
        NotificationService notificationService = new NotificationService();

        CustomerService customerService = new CustomerService(
                customerRepo, notificationService);

        PartnerService partnerService = new PartnerService(
                partnerRepo, orderRepo, notificationService);

        OrderService orderService = new OrderService(
                orderRepo, partnerRepo, notificationService);

        // Test 1: Onboard customers and partners
        System.out.println("═══ TEST 1: ONBOARDING ═══");
        Customer c1 = customerService.onboardCustomer("Rahul Kumar", "9876543210");
        Customer c2 = customerService.onboardCustomer("Priya Sharma", "9876543211");

        DeliveryPartner p1 = partnerService.onboardPartner("Amit Singh", "9999888877", "123");
        DeliveryPartner p2 = partnerService.onboardPartner("Suresh Patel", "9999888878", "456");

        Thread.sleep(500);

        // Test 2: Create orders
        System.out.println("\n═══ TEST 2: CREATE ORDERS ═══");
        Order order1 = orderService.createOrder(c1.getCustomerId(), "MacBook Pro");
        Order order2 = orderService.createOrder(c2.getCustomerId(), "iPhone 15");

        Thread.sleep(1000);

        // Test 3: Check status
        System.out.println("\n═══ TEST 3: ORDER & PARTNER STATUS ═══");
        System.out.println(orderService.getOrderStatus(order1.getOrderId()));
        System.out.println(partnerService.getPartnerStatus(p1.getPartnerId()));

        // Test 4: Pickup and delivery
        System.out.println("\n═══ TEST 4: PICKUP & DELIVERY ═══");
        partnerService.pickUpOrder(p1.getPartnerId(), order1.getOrderId());
        Thread.sleep(500);
        partnerService.completeOrder(p1.getPartnerId(), order1.getOrderId(), 5);

        Thread.sleep(500);

        // Test 5: Cancel order before pickup
        System.out.println("\n═══ TEST 5: ORDER CANCELLATION ═══");
        Order order3 = orderService.createOrder(c1.getCustomerId(), "AirPods Pro");
        Thread.sleep(500);
        orderService.cancelOrder(order3.getOrderId());

        // Test 6: Try to cancel after pickup (should fail)
        System.out.println("\n═══ TEST 6: CANCEL AFTER PICKUP (SHOULD FAIL) ═══");
        partnerService.pickUpOrder(p2.getPartnerId(), order2.getOrderId());
        orderService.cancelOrder(order2.getOrderId());

        // Complete order 2
        partnerService.completeOrder(p2.getPartnerId(), order2.getOrderId(), 4);

        // Test 7: More orders than partners
        System.out.println("\n═══ TEST 7: QUEUE MANAGEMENT ═══");
        Order order4 = orderService.createOrder(c2.getCustomerId(), "Samsung TV");
        Order order5 = orderService.createOrder(c1.getCustomerId(), "PS5");

        Thread.sleep(1000);

        // Complete remaining orders
        partnerService.pickUpOrder(p1.getPartnerId(), order4.getOrderId());
        partnerService.completeOrder(p1.getPartnerId(), order4.getOrderId(), 5);

        Thread.sleep(500);

        partnerService.pickUpOrder(p2.getPartnerId(), order5.getOrderId());
        partnerService.completeOrder(p2.getPartnerId(), order5.getOrderId(), 3);

        // Test 8: Top partners dashboard
        System.out.println("\n═══ TEST 8: DASHBOARD ═══");
        partnerService.showTopPartners();

        // Cleanup
        orderService.shutdown();

        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║          DEMO COMPLETED SUCCESSFULLY           ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
    }
}
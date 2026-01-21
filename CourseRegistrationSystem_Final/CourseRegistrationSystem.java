import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// ==================== DOMAIN MODELS ====================

class User {
    private final String userId;
    private String name;
    private String email;
    private String passwordHash;
    private String phoneNumber;
    private String address;
    private final LocalDateTime createdAt;
    private UserStatus status;

    public User(String userId, String name, String email, String passwordHash, String phoneNumber, String address) {
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password cannot be null");
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.createdAt = LocalDateTime.now();
        this.status = UserStatus.ACTIVE;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
}

enum UserStatus {
    ACTIVE, INACTIVE, SUSPENDED
}

class Course {
    private final String courseId;
    private String title;
    private String description;
    private String instructorId;
    private double price;
    private CourseCategory category;
    private int maxCapacity;
    private int currentEnrollment;
    private CourseStatus status;

    public Course(String courseId, String title, String description, String instructorId,
                  double price, CourseCategory category, int maxCapacity) {
        this.courseId = Objects.requireNonNull(courseId, "Course ID cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = description;
        this.instructorId = Objects.requireNonNull(instructorId, "Instructor ID cannot be null");
        this.price = price;
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        this.maxCapacity = maxCapacity;
        this.currentEnrollment = 0;
        this.status = CourseStatus.ACTIVE;
    }

    public boolean hasCapacity() {
        return currentEnrollment < maxCapacity;
    }

    public void incrementEnrollment() {
        if (!hasCapacity()) {
            throw new IllegalStateException("Course is at full capacity");
        }
        currentEnrollment++;
    }

    public void decrementEnrollment() {
        if (currentEnrollment > 0) {
            currentEnrollment--;
        }
    }

    public String getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public CourseCategory getCategory() { return category; }
    public CourseStatus getStatus() { return status; }
    public int getCurrentEnrollment() { return currentEnrollment; }
    public int getMaxCapacity() { return maxCapacity; }
}

enum CourseCategory {
    PROGRAMMING, DATA_SCIENCE, WEB_DEVELOPMENT, MOBILE_DEVELOPMENT,
    DEVOPS, CLOUD_COMPUTING, CYBERSECURITY
}

enum CourseStatus {
    ACTIVE, INACTIVE, FULL, CANCELLED
}

class Enrollment {
    private final String enrollmentId;
    private final String userId;
    private final String courseId;
    private final LocalDateTime enrollmentDate;
    private EnrollmentStatus status;
    private double amountPaid;

    public Enrollment(String enrollmentId, String userId, String courseId) {
        this.enrollmentId = Objects.requireNonNull(enrollmentId);
        this.userId = Objects.requireNonNull(userId);
        this.courseId = Objects.requireNonNull(courseId);
        this.enrollmentDate = LocalDateTime.now();
        this.status = EnrollmentStatus.PENDING;
        this.amountPaid = 0.0;
    }

    public void complete(double amount) {
        this.status = EnrollmentStatus.COMPLETED;
        this.amountPaid = amount;
    }

    public void cancel() {
        this.status = EnrollmentStatus.CANCELLED;
    }

    public String getEnrollmentId() { return enrollmentId; }
    public String getUserId() { return userId; }
    public String getCourseId() { return courseId; }
    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public EnrollmentStatus getStatus() { return status; }
    public double getAmountPaid() { return amountPaid; }
}

enum EnrollmentStatus {
    PENDING, COMPLETED, CANCELLED, REFUNDED
}

class Payment {
    private final String paymentId;
    private final String userId;
    private final String courseId;
    private final double amount;
    private final PaymentMethod method;
    private final LocalDateTime paymentDate;
    private PaymentStatus status;

    public Payment(String paymentId, String userId, String courseId,
                   double amount, PaymentMethod method) {
        this.paymentId = Objects.requireNonNull(paymentId);
        this.userId = Objects.requireNonNull(userId);
        this.courseId = Objects.requireNonNull(courseId);
        this.amount = amount;
        this.method = Objects.requireNonNull(method);
        this.paymentDate = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }

    public void markSuccessful() { this.status = PaymentStatus.SUCCESS; }
    public void markFailed() { this.status = PaymentStatus.FAILED; }

    public String getPaymentId() { return paymentId; }
    public PaymentStatus getStatus() { return status; }
    public double getAmount() { return amount; }
    public String getUserId() { return userId; }
}

enum PaymentMethod {
    CREDIT_CARD, UPI, NET_BANKING, DEBIT_CARD, WALLET
}

enum PaymentStatus {
    PENDING, SUCCESS, FAILED, REFUNDED
}

// ==================== EXCEPTIONS ====================

class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) { super(message); }
}

class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(String message) { super(message); }
}

class EnrollmentException extends RuntimeException {
    public EnrollmentException(String message) { super(message); }
}

class PaymentException extends RuntimeException {
    public PaymentException(String message) { super(message); }
}

class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) { super(message); }
}

// ==================== REPOSITORIES (Data Access Layer) ====================

interface UserRepository {
    void save(User user);
    Optional<User> findById(String userId);
    Optional<User> findByEmail(String email);
    List<User> findAll();
}

class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, User> emailIndex = new ConcurrentHashMap<>();

    @Override
    public void save(User user) {
        users.put(user.getUserId(), user);
        emailIndex.put(user.getEmail(), user);
    }

    @Override
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(emailIndex.get(email));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
}

interface CourseRepository {
    void save(Course course);
    Optional<Course> findById(String courseId);
    List<Course> findByCategory(CourseCategory category);
    List<Course> findAll();
}

class InMemoryCourseRepository implements CourseRepository {
    private final Map<String, Course> courses = new ConcurrentHashMap<>();

    @Override
    public void save(Course course) {
        courses.put(course.getCourseId(), course);
    }

    @Override
    public Optional<Course> findById(String courseId) {
        return Optional.ofNullable(courses.get(courseId));
    }

    @Override
    public List<Course> findByCategory(CourseCategory category) {
        return courses.values().stream()
                .filter(c -> c.getCategory() == category)
                .collect(Collectors.toList());
    }

    @Override
    public List<Course> findAll() {
        return new ArrayList<>(courses.values());
    }
}

interface EnrollmentRepository {
    void save(Enrollment enrollment);
    Optional<Enrollment> findById(String enrollmentId);
    List<Enrollment> findByUserId(String userId);
    List<Enrollment> findByCourseId(String courseId);
    Optional<Enrollment> findByUserAndCourse(String userId, String courseId);
}

class InMemoryEnrollmentRepository implements EnrollmentRepository {
    private final Map<String, Enrollment> enrollments = new ConcurrentHashMap<>();

    @Override
    public void save(Enrollment enrollment) {
        enrollments.put(enrollment.getEnrollmentId(), enrollment);
    }

    @Override
    public Optional<Enrollment> findById(String enrollmentId) {
        return Optional.ofNullable(enrollments.get(enrollmentId));
    }

    @Override
    public List<Enrollment> findByUserId(String userId) {
        return enrollments.values().stream()
                .filter(e -> e.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Enrollment> findByCourseId(String courseId) {
        return enrollments.values().stream()
                .filter(e -> e.getCourseId().equals(courseId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Enrollment> findByUserAndCourse(String userId, String courseId) {
        return enrollments.values().stream()
                .filter(e -> e.getUserId().equals(userId) && e.getCourseId().equals(courseId))
                .findFirst();
    }
}

interface PaymentRepository {
    void save(Payment payment);
    Optional<Payment> findById(String paymentId);
    List<Payment> findByUserId(String userId);
}

class InMemoryPaymentRepository implements PaymentRepository {
    private final Map<String, Payment> payments = new ConcurrentHashMap<>();

    @Override
    public void save(Payment payment) {
        payments.put(payment.getPaymentId(), payment);
    }

    @Override
    public Optional<Payment> findById(String paymentId) {
        return Optional.ofNullable(payments.get(paymentId));
    }

    @Override
    public List<Payment> findByUserId(String userId) {
        return payments.values().stream()
                .filter(p -> p.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}

// ==================== SERVICES ====================

class AuthenticationService {
    private final UserRepository userRepository;
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>();

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!user.getPasswordHash().equals(hashPassword(password))) {
            throw new AuthenticationException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationException("User account is not active");
        }

        String sessionToken = UUID.randomUUID().toString();
        activeSessions.put(sessionToken, user.getUserId());
        return sessionToken;
    }

    public void logout(String sessionToken) {
        activeSessions.remove(sessionToken);
    }

    public Optional<String> validateSession(String sessionToken) {
        return Optional.ofNullable(activeSessions.get(sessionToken));
    }

    private String hashPassword(String password) {
        return password; // In production: use BCrypt or similar
    }
}

class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String name, String email, String password,
                             String phoneNumber, String address) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        String userId = UUID.randomUUID().toString();
        User user = new User(userId, name, email, password, phoneNumber, address);
        userRepository.save(user);
        return user;
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }
}

class CourseService {
    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course createCourse(String title, String description, String instructorId,
                               double price, CourseCategory category, int maxCapacity) {
        String courseId = UUID.randomUUID().toString();
        Course course = new Course(courseId, title, description, instructorId,
                price, category, maxCapacity);
        courseRepository.save(course);
        return course;
    }

    public List<Course> listCoursesByCategory(CourseCategory category) {
        return courseRepository.findByCategory(category);
    }

    public List<Course> searchCourses(String keyword) {
        return courseRepository.findAll().stream()
                .filter(c -> c.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        c.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Course getCourseById(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found: " + courseId));
    }
}

class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CourseRepository courseRepository,
                             UserRepository userRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public Enrollment enrollUserInCourse(String userId, String courseId) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Validate course exists and has capacity
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found"));

        if (!course.hasCapacity()) {
            throw new EnrollmentException("Course is at full capacity");
        }

        // Check if already enrolled
        if (enrollmentRepository.findByUserAndCourse(userId, courseId).isPresent()) {
            throw new EnrollmentException("User already enrolled in this course");
        }

        // Create enrollment
        String enrollmentId = UUID.randomUUID().toString();
        Enrollment enrollment = new Enrollment(enrollmentId, userId, courseId);
        enrollmentRepository.save(enrollment);

        // Update course capacity
        course.incrementEnrollment();
        courseRepository.save(course);

        return enrollment;
    }

    public void cancelEnrollment(String enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentException("Enrollment not found"));

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new EnrollmentException("Enrollment already cancelled");
        }

        enrollment.cancel();
        enrollmentRepository.save(enrollment);

        // Update course capacity
        Course course = courseRepository.findById(enrollment.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException("Course not found"));
        course.decrementEnrollment();
        courseRepository.save(course);
    }

    public List<Enrollment> getUserEnrollments(String userId) {
        return enrollmentRepository.findByUserId(userId);
    }
}

// ==================== PAYMENT STRATEGY ====================

interface PaymentProcessor {
    boolean process(Payment payment);
}

class CreditCardProcessor implements PaymentProcessor {
    @Override
    public boolean process(Payment payment) {
        System.out.println("Processing payment via Credit Card: $" + payment.getAmount());
        // Simulate payment processing
        return true;
    }
}

class UPIProcessor implements PaymentProcessor {
    @Override
    public boolean process(Payment payment) {
        System.out.println("Processing payment via UPI: ₹" + payment.getAmount());
        return true;
    }
}

class NetBankingProcessor implements PaymentProcessor {
    @Override
    public boolean process(Payment payment) {
        System.out.println("Processing payment via Net Banking: $" + payment.getAmount());
        return true;
    }
}

class PaymentService {
    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final Map<PaymentMethod, PaymentProcessor> processors;

    public PaymentService(PaymentRepository paymentRepository,
                          EnrollmentRepository enrollmentRepository) {
        this.paymentRepository = paymentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.processors = new HashMap<>();

        // Register payment processors
        processors.put(PaymentMethod.CREDIT_CARD, new CreditCardProcessor());
        processors.put(PaymentMethod.UPI, new UPIProcessor());
        processors.put(PaymentMethod.NET_BANKING, new NetBankingProcessor());
    }

    public Payment processPayment(String userId, String courseId,
                                  double amount, PaymentMethod method) {
        String paymentId = UUID.randomUUID().toString();
        Payment payment = new Payment(paymentId, userId, courseId, amount, method);

        PaymentProcessor processor = processors.get(method);
        if (processor == null) {
            throw new PaymentException("Unsupported payment method");
        }

        boolean success = processor.process(payment);

        if (success) {
            payment.markSuccessful();

            // Update enrollment
            Enrollment enrollment = enrollmentRepository
                    .findByUserAndCourse(userId, courseId)
                    .orElseThrow(() -> new EnrollmentException("Enrollment not found"));
            enrollment.complete(amount);
            enrollmentRepository.save(enrollment);
        } else {
            payment.markFailed();
        }

        paymentRepository.save(payment);
        return payment;
    }
}

// ==================== MAIN SYSTEM ====================

public class CourseRegistrationSystem {
    private final AuthenticationService authService;
    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final PaymentService paymentService;

    public CourseRegistrationSystem(AuthenticationService authService,
                                    UserService userService,
                                    CourseService courseService,
                                    EnrollmentService enrollmentService,
                                    PaymentService paymentService) {
        this.authService = authService;
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.paymentService = paymentService;
    }

    public static void main(String[] args) {
        // Initialize repositories
        UserRepository userRepo = new InMemoryUserRepository();
        CourseRepository courseRepo = new InMemoryCourseRepository();
        EnrollmentRepository enrollmentRepo = new InMemoryEnrollmentRepository();
        PaymentRepository paymentRepo = new InMemoryPaymentRepository();

        // Initialize services
        AuthenticationService authService = new AuthenticationService(userRepo);
        UserService userService = new UserService(userRepo);
        CourseService courseService = new CourseService(courseRepo);
        EnrollmentService enrollmentService = new EnrollmentService(
                enrollmentRepo, courseRepo, userRepo
        );
        PaymentService paymentService = new PaymentService(paymentRepo, enrollmentRepo);

        // Create system
        CourseRegistrationSystem system = new CourseRegistrationSystem(
                authService, userService, courseService, enrollmentService, paymentService
        );

        // Demo workflow
        try {
            System.out.println("=== Course Registration System Demo ===\n");

            // 1. Register a user
            System.out.println("1. Registering user...");
            User user = userService.registerUser(
                    "Ayush Kumar", "ayush@gmail.com", "securePassword123",
                    "+91-9876543210", "Delhi, India"
            );
            System.out.println("✓ User registered: " + user.getName() + "\n");

            // 2. Login
            System.out.println("2. Logging in...");
            String sessionToken = authService.login("ayush@gmail.com", "securePassword123");
            System.out.println("✓ Login successful. Session: " + sessionToken.substring(0, 8) + "...\n");

            // 3. Create courses
            System.out.println("3. Creating courses...");
            Course javaCourse = courseService.createCourse(
                    "Java Programming Masterclass",
                    "Complete Java course from basics to advanced",
                    "instructor-1",
                    99.99,
                    CourseCategory.PROGRAMMING,
                    50
            );
            Course pythonCourse = courseService.createCourse(
                    "Python for Data Science",
                    "Learn Python with focus on data science",
                    "instructor-2",
                    89.99,
                    CourseCategory.DATA_SCIENCE,
                    30
            );
            System.out.println("✓ Created: " + javaCourse.getTitle());
            System.out.println("✓ Created: " + pythonCourse.getTitle() + "\n");

            // 4. List courses by category
            System.out.println("4. Browsing Programming courses...");
            List<Course> programmingCourses = courseService.listCoursesByCategory(
                    CourseCategory.PROGRAMMING
            );
            programmingCourses.forEach(c ->
                    System.out.println("  - " + c.getTitle() + " ($" + c.getPrice() + ")")
            );
            System.out.println();

            // 5. Enroll in course
            System.out.println("5. Enrolling in Java course...");
            Enrollment enrollment = enrollmentService.enrollUserInCourse(
                    user.getUserId(), javaCourse.getCourseId()
            );
            System.out.println("✓ Enrollment created: " + enrollment.getEnrollmentId() + "\n");

            // 6. Process payment
            System.out.println("6. Processing payment...");
            Payment payment = paymentService.processPayment(
                    user.getUserId(),
                    javaCourse.getCourseId(),
                    javaCourse.getPrice(),
                    PaymentMethod.UPI
            );
            System.out.println("✓ Payment status: " + payment.getStatus() + "\n");

            // 7. View enrollment history
            System.out.println("7. Viewing enrollment history...");
            List<Enrollment> userEnrollments = enrollmentService.getUserEnrollments(
                    user.getUserId()
            );
            System.out.println("✓ Total enrollments: " + userEnrollments.size());
            userEnrollments.forEach(e -> {
                Course c = courseService.getCourseById(e.getCourseId());
                System.out.println("  - " + c.getTitle() + " (Status: " + e.getStatus() + ")");
            });
            System.out.println();

            // 8. Logout
            System.out.println("8. Logging out...");
            authService.logout(sessionToken);
            System.out.println("✓ Logout successful\n");

            System.out.println("=== Demo completed successfully! ===");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
import java.util.ArrayList;
import java.util.List;

class User {
    private int userID;
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
    private Enrollment enrollment;
    private List<Course>  courses;

    public User(int userID, String name, String email, String password, String phoneNumber, String address, Enrollment enrollment) {
        if (userID < 0 || name == null || email == null || password == null || phoneNumber == null || address == null) {
            throw new IllegalArgumentException("Invalid argument(s) provided to User constructor");
        }
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.enrollment = enrollment;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

class Course {
    private int courseID;
    private String title;
    private String description;
    private String instructor;
    private float price;
    private String category;

    public Course(int courseID, String title, String description, String instructor, float price, String category) {
        if (courseID < 0 || title == null || price <= 0 || instructor == null || category == null) {
            throw new IllegalArgumentException("Invalid argument(s) provided to Course constructor");
        }
        this.courseID = courseID;
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.price = price;
        this.category = category;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

class Payment {
    private int paymentID;
    private int userID;
    private int courseID;
    private float amount;
    private String paymentMethod;

    public Payment(int paymentID, int userID, int courseID, float amount, String paymentMethod) {
        if (paymentID < 0 || paymentID > 100) {
            throw new IllegalArgumentException("Invalid paymentID provided to Payment constructor");
        }
        this.paymentID = paymentID;
        this.userID = userID;
        this.courseID = courseID;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public int getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(int paymentID) {
        this.paymentID = paymentID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}

class Enrollment {
    private int enrollmentID;
    private int userID;
    private int courseID;
    private String enrollmentDate;

    public Enrollment(int enrollmentID, int userID, int courseID, String enrollmentDate) {
        if (enrollmentID < 0 || enrollmentID > 100) {
            throw new IllegalArgumentException("Invalid enrollmentID provided to Enrollment constructor");
        }
        this.enrollmentID = enrollmentID;
        this.userID = userID;
        this.courseID = courseID;
        this.enrollmentDate = enrollmentDate;
    }

    public int getEnrollmentID() {
        return enrollmentID;
    }

    public void setEnrollmentID(int enrollmentID) {
        this.enrollmentID = enrollmentID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
}

interface IUserAuthentication {
    Boolean login(String email, String password);

    Boolean logout(int userID);
}

class UserAuthentication implements IUserAuthentication {
    @Override
    public Boolean login(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            System.out.println("Invalid email or password");
            return false;
        }
        System.out.println("User logged in successfully");
        return true;
    }

    @Override
    public Boolean logout(int UserID) {
        if (UserID < 0 || UserID > 100) {
            System.out.println("Invalid UserID");
            return false;
        }
        System.out.println("User logged out successfully");
        return true;
    }
}

interface IUserRegistrationService {
    Boolean register();
}

class UserRegistrationService implements IUserRegistrationService {
    @Override
    public Boolean register() {
        System.out.println("User registered successfully");
        return true;
    }
}

interface ICourseService {
    List<Course> listCourses(List<Course> courses, String category);

    Course searchCourse(List<Course> courses, String courseTitle, String category);
}

class CourseService implements ICourseService {

    @Override
    public List<Course> listCourses(List<Course> courses, String category) {
        List<Course> courseList = new ArrayList<>();
        for (Course course : courses) {
            if (course.getCategory().equalsIgnoreCase(category)) {
                courseList.add(course);
            }
        }
        return courseList;
    }

    @Override
    public Course searchCourse(List<Course> courses, String courseTitle, String category) {
        for (Course course : courses) {
            if (course.getTitle().equalsIgnoreCase(courseTitle) && course.getCategory().equalsIgnoreCase(category)) {
                return course;
            }
        }
        return null;
    }
}

interface IEnrollmentService {
    Course enrollCourse(int userID, int courseID, String title, String description, String instructor, float price, String category);

    List<Enrollment> viewEnrollmentHistory(int userID, List<Enrollment> enrollments);
}

class EnrollmentService implements IEnrollmentService {
    private List<Enrollment> enrollments = new ArrayList<>();

    @Override
    public Course enrollCourse(int userID, int courseID, String title, String description, String instructor, float price, String category) {
        Enrollment enrollment = new Enrollment(enrollments.size() + 1, userID, courseID, java.time.LocalDate.now().toString());
        enrollments.add(enrollment);
        return new Course(courseID, title, description, instructor, price, category);
    }

    @Override
    public List<Enrollment> viewEnrollmentHistory(int userID, List<Enrollment> enrollments) {
        List<Enrollment> userEnrollments = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getUserID() == userID) {
                userEnrollments.add(enrollment);
            }
        }
        return userEnrollments;
    }
}

interface PaymentStrategy {
    void processPayment();
}

class CreditCardPayment implements PaymentStrategy {
    public void processPayment() {
        System.out.println("Payment Processed using Credit Card");
    }
}

class UPI implements PaymentStrategy {
    public void processPayment() {
        System.out.println("Payment Processed using UPI");
    }
}

class NetBanking implements PaymentStrategy {
    public void processPayment() {
        System.out.println("Payment Processed using Net Banking");
    }
}

class PaymentService {
    private PaymentStrategy paymentStrategy;

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public void processPayment() {
        paymentStrategy.processPayment();
    }
}


public class CourseRegistrationSystem {
    private final IUserAuthentication authService;
    private final ICourseService courseService;
    private final IEnrollmentService enrollmentService;
    private final PaymentService paymentService;

    // Constructor Injection - Dependency Inversion Principle
    public CourseRegistrationSystem(IUserAuthentication authService, ICourseService courseService, IEnrollmentService enrollmentService, PaymentService paymentService) {
        this.authService = authService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.paymentService = paymentService;
    }
    // complete the main function
    public static void main(String[] args) {
        IUserAuthentication authService = new UserAuthentication();
        ICourseService courseService = new CourseService();
        IEnrollmentService enrollmentService = new EnrollmentService();
        PaymentService paymentService = new PaymentService();

        CourseRegistrationSystem system = new CourseRegistrationSystem(authService, courseService, enrollmentService, paymentService);

        // Example usage
        system.authService.login("ayush@gmail.com", "password");
        List<Course> courses = new ArrayList<>();
        courses.add(new Course(1, "Java Programming", "Learn Java", "John Doe", 99.99f, "Programming"));
        courses.add(new Course(2, "Python Programming", "Learn Python", "Jane Doe", 89.99f, "Programming"));
        List<Course> programmingCourses = system.courseService.listCourses(courses, "Programming");
        for (Course course : programmingCourses) {
            System.out.println("Course: " + course.getTitle());
        }
        // Enroll in a course
        Course enrolledCourse = system.enrollmentService.enrollCourse(1, 1, "Java Programming", "Learn Java", "John Doe", 99.99f, "Programming");
        System.out.println("Enrolled in course: " + enrolledCourse.getTitle());
        // Process payment
        system.paymentService.setPaymentStrategy(new CreditCardPayment());
        system.paymentService.processPayment();
        // Logout
        system.authService.logout(1);
    }
}




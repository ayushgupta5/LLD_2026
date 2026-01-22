import java.util.*;

enum UserType{
    STUDENT,
    STAFF,
    ADMIN
}

class User {
    private int userID;
    private String name;
    private String email;
    private String passwordHash;
    private UserType userType;

    public User(int userID, String name, String email, String passwordHash, UserType userType) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.userType = userType;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
class Room {
    private int roomID;
    private List<User> users;
    private String roomNumber;
    private int capacity;
    private int occupiedBeds;

    public Room(int roomID, List<User> users, String roomNumber, int capacity, int occupiedBeds) {
        this.roomID = roomID;
        this.users = users;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.occupiedBeds = occupiedBeds;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getOccupiedBeds() {
        return occupiedBeds;
    }

    public void setOccupiedBeds(int occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
    }
}
class Fee {
    private int feeID;
    private int userID;
    private double amount;
    private Date dueDate;
    private boolean isPaid;

    public Fee(int feeID, int userID, double amount, Date dueDate, boolean isPaid) {
        this.feeID = feeID;
        this.userID = userID;
        this.amount = amount;
        this.dueDate = dueDate;
        this.isPaid = isPaid;
    }

    public int getFeeID() {
        return feeID;
    }

    public void setFeeID(int feeID) {
        this.feeID = feeID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }
}
class Maintenance {
    private int requestID;
    private int userID;
    private String description;
    private String status;
    private Date requestDate;

    public Maintenance(int requestID, int userID, String description, String status, Date requestDate) {
        this.requestID = requestID;
        this.userID = userID;
        this.description = description;
        this.status = status;
        this.requestDate = requestDate;
    }

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }
}
class Visitor {
    private int visitorID;
    private int userID;
    private String name;
    private Date entryTime;
    private Date exitTime;
    private String purpose;
    private String idProof;
    private String contactInfo;
    private String relationshipToStudent;

    public int getVisitorID() {
        return visitorID;
    }

    public void setVisitorID(int visitorID) {
        this.visitorID = visitorID;
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

    public Date getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Date entryTime) {
        this.entryTime = entryTime;
    }

    public Date getExitTime() {
        return exitTime;
    }

    public void setExitTime(Date exitTime) {
        this.exitTime = exitTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getIdProof() {
        return idProof;
    }

    public void setIdProof(String idProof) {
        this.idProof = idProof;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getRelationshipToStudent() {
        return relationshipToStudent;
    }

    public void setRelationshipToStudent(String relationshipToStudent) {
        this.relationshipToStudent = relationshipToStudent;
    }

    public Visitor(int visitorID, int userID, String name, Date entryTime, Date exitTime, String purpose, String idProof, String contactInfo, String relationshipToStudent) {
        this.visitorID = visitorID;
        this.userID = userID;
        this.name = name;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.purpose = purpose;
        this.idProof = idProof;
        this.contactInfo = contactInfo;
        this.relationshipToStudent = relationshipToStudent;
    }
}

interface IAuthService {
    boolean register(User user);
    User login(String email, String password);
    User logout(int userID);
    boolean recoverPassword(String email);
}
class AuthService implements IAuthService {

    @Override
    public boolean register(User user) {
        return false;
    }

    @Override
    public User login(String email, String password) {
        return null;
    }

    @Override
    public User logout(int userID) {
        return null;
    }

    @Override
    public boolean recoverPassword(String email) {
        return false;
    }
}

interface IRoomAllocationService {
    boolean allocateRoom(int userID, int roomID);
    boolean changeRoom(int userID, int newRoomID);
}
class RoomAllocationService implements IRoomAllocationService {
    public boolean allocateRoom(int userID, int roomID) {
        // Implement room allocation logic
        return true;
    }

    public boolean changeRoom(int userID, int newRoomID) {
        // Implement room change logic
        return true;
    }
}

interface IRoomChangeRequestService {
    boolean requestRoomChange(int userID, int preferredRoomType);
}
class RoomChangeRequestService implements IRoomChangeRequestService {
    public boolean requestRoomChange(int userID, int preferredRoomType) {
        // Implement room change request logic
        return true;
    }
}

interface IViewRoomAssignmentService{
    Room viewRoomAssignment(int userID);

}
class ViewRoomAssignmentService implements IViewRoomAssignmentService {
    public Room viewRoomAssignment(int userID) {
        // Implement view room assignment logic
        return null;
    }
}

interface IFeeManagementService {
    void TrackHostelFees(int userID);
}
class FeeManagementService implements IFeeManagementService {
    public void TrackHostelFees(int userID) {
        // Implement fee tracking logic
    }
}

interface PaymentStrategy {
    boolean pay(int userID, double amount);
}
class CreditCardPayment implements PaymentStrategy {
    public boolean pay(int userID, double amount) {
        // Implement credit card payment logic
        return true;
    }
}
class UPI implements PaymentStrategy {
    public boolean pay(int userID, double amount) {
        // Implement UPI payment logic
        return true;
    }
}
class NetBanking implements PaymentStrategy {
    public boolean pay(int userID, double amount) {
        // Implement net banking payment logic
        return true;
    }
}
class PaymentProcessor {
    private PaymentStrategy paymentStrategy;

    public PaymentProcessor(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public boolean processPayment(int userID, double amount) {
        return paymentStrategy.pay(userID, amount);
    }
}

interface IMaintenanceRequestService {
    boolean submitRequest(int userID, String description);
    List<Maintenance> trackRequests(int userID);
}
class MaintenanceRequestService implements IMaintenanceRequestService {
    public boolean submitRequest(int userID, String description) {
        // Implement maintenance request submission logic
        return true;
    }

    public List<Maintenance> trackRequests(int userID) {
        // Implement maintenance request tracking logic
        return new ArrayList<>();
    }
}

interface IVisitorManagementService {
    boolean registerVisitor(int userID, Visitor visitor);
    List<Visitor> trackVisitorActivity(int userID);
}
class VisitorManagementService implements IVisitorManagementService {
    public boolean registerVisitor(int userID, Visitor visitor) {
        // Implement visitor registration logic
        return true;
    }

    public List<Visitor> trackVisitorActivity(int userID) {
        // Implement visitor activity tracking logic
        return new ArrayList<>();
    }
}

interface IReportingService {
    List<Room> generateRoomOccupancyReport();
    List<Fee> generateFeeCollectionReport();
    List<Maintenance> generateMaintenanceRequestReport();
    List<Visitor> generateVisitorActivityReport();
}
class MaintenanceReportService implements IReportingService {
    public List<Room> generateRoomOccupancyReport() {
        // Implement room occupancy report generation logic
        return new ArrayList<>();
    }

    public List<Fee> generateFeeCollectionReport() {
        // Implement fee collection report generation logic
        return new ArrayList<>();
    }

    public List<Maintenance> generateMaintenanceRequestReport() {
        // Implement maintenance request report generation logic
        return new ArrayList<>();
    }

    public List<Visitor> generateVisitorActivityReport() {
        // Implement visitor activity report generation logic
        return new ArrayList<>();
    }
}

interface NotificationStrategy {
    void sendNotification(int userID, String message);
}
class EmailNotification implements NotificationStrategy {
    public void sendNotification(int userID, String message) {
        // Implement email notification logic
    }
}
class SMSNotification implements NotificationStrategy {
    public void sendNotification(int userID, String message) {
        // Implement SMS notification logic
    }
}
class PushInboxNotification implements NotificationStrategy {
    public void sendNotification(int userID, String message) {
        // Implement push inbox notification logic
    }
}
class NotificationFactory {
    public static NotificationStrategy getNotificationStrategy(String type) {
        switch (type) {
            case "EMAIL":
                return new EmailNotification();
            case "SMS":
                return new SMSNotification();
            case "PUSH_INBOX":
                return new PushInboxNotification();
            default:
                throw new IllegalArgumentException("Unknown notification type: " + type);
        }
    }
}

class HostelManagementFacade {
    private AuthService authService;
    private IRoomAllocationService roomAllocationService;
    private IRoomChangeRequestService roomChangeRequestService;
    private IViewRoomAssignmentService viewRoomAssignmentService;
    private IFeeManagementService feeManagementService;
    private IMaintenanceRequestService maintenanceRequestService;
    private IVisitorManagementService visitorManagementService;
    private IReportingService reportingService;

    public HostelManagementFacade(AuthService authService,
                                  IRoomAllocationService roomAllocationService,
                                  IRoomChangeRequestService roomChangeRequestService,
                                  IViewRoomAssignmentService viewRoomAssignmentService,
                                  IFeeManagementService feeManagementService,
                                  IMaintenanceRequestService maintenanceRequestService,
                                  IVisitorManagementService visitorManagementService,
                                  IReportingService reportingService) {
        this.authService = authService;
        this.roomAllocationService = roomAllocationService;
        this.roomChangeRequestService = roomChangeRequestService;
        this.viewRoomAssignmentService = viewRoomAssignmentService;
        this.feeManagementService = feeManagementService;
        this.maintenanceRequestService = maintenanceRequestService;
        this.visitorManagementService = visitorManagementService;
        this.reportingService = reportingService;
    }
    // Facade methods to interact with various services can be added here
    public boolean registerUser(User user) {
        return authService.register(user);
    }
    public User loginUser(String email, String password) {
        return authService.login(email, password);
    }
    public User logoutUser(int userID) {
        return authService.logout(userID);
    }
    public boolean recoverUserPassword(String email) {
        return authService.recoverPassword(email);
    }
    public boolean allocateRoomToUser(int userID, int roomID) {
        return roomAllocationService.allocateRoom(userID, roomID);
    }
    public boolean changeUserRoom(int userID, int newRoomID) {
        return roomAllocationService.changeRoom(userID, newRoomID);
    }
    public boolean requestUserRoomChange(int userID, int preferredRoomType) {
        return roomChangeRequestService.requestRoomChange(userID, preferredRoomType);
    }
    public Room viewUserRoomAssignment(int userID) {
        return viewRoomAssignmentService.viewRoomAssignment(userID);
    }
    public void trackUserHostelFees(int userID) {
        feeManagementService.TrackHostelFees(userID);
    }
    public boolean submitMaintenanceRequest(int userID, String description) {
        return maintenanceRequestService.submitRequest(userID, description);
    }
    public List<Maintenance> trackUserMaintenanceRequests(int userID) {
        return maintenanceRequestService.trackRequests(userID);
    }
    public boolean registerVisitorForUser(int userID, Visitor visitor) {
        return visitorManagementService.registerVisitor(userID, visitor);
    }
    public List<Visitor> trackUserVisitorActivity(int userID) {
        return visitorManagementService.trackVisitorActivity(userID);
    }
    public List<Room> generateRoomOccupancyReport() {
        return reportingService.generateRoomOccupancyReport();
    }
    public List<Fee> generateFeeCollectionReport() {
        return reportingService.generateFeeCollectionReport();
    }
    public List<Maintenance> generateMaintenanceRequestReport() {
        return reportingService.generateMaintenanceRequestReport();
    }
    public List<Visitor> generateVisitorActivityReport() {
        return reportingService.generateVisitorActivityReport();
    }


}

public class HostelManagementSystem {
    public static void main(String[] args) {
        HostelManagementFacade hostelManagementFacade = new HostelManagementFacade(
                new AuthService(),
                new RoomAllocationService(),
                new RoomChangeRequestService(),
                new ViewRoomAssignmentService(),
                new FeeManagementService(),
                new MaintenanceRequestService(),
                new VisitorManagementService(),
                new MaintenanceReportService()
        );
        User user = new User(1, "John Doe", "John@gmail.com", "hashed_password", UserType.STUDENT);
        hostelManagementFacade.registerUser(user);
        hostelManagementFacade.loginUser(user.getEmail(), user.getPasswordHash());
        hostelManagementFacade.allocateRoomToUser(1,1);
        hostelManagementFacade.changeUserRoom(1,2);
        hostelManagementFacade.requestUserRoomChange(1,1);
        hostelManagementFacade.viewUserRoomAssignment(1);
        hostelManagementFacade.trackUserHostelFees(1);
        hostelManagementFacade.submitMaintenanceRequest(1, "Leaky faucet in room");
        hostelManagementFacade.trackUserMaintenanceRequests(1);
        Visitor visitor = new Visitor(1,1,"Jane Doe",new Date(),null,"Visiting student","ID12345","123-456-7890","Sister");
        hostelManagementFacade.registerVisitorForUser(1, visitor);
        hostelManagementFacade.trackUserVisitorActivity(1);
        hostelManagementFacade.generateRoomOccupancyReport();
        hostelManagementFacade.generateFeeCollectionReport();
        hostelManagementFacade.generateMaintenanceRequestReport();
        hostelManagementFacade.generateVisitorActivityReport();

    }
}

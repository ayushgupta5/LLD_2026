/*
Functional Requirement:-
user should recive notification for any action
1. PushInbox, Email, SMS (strategy Pattern)

class
    1. User
    2. Notification(Email, PushInbox, SMS) /. strategy Pattern
    3. Action
    4. Payment
1. Drawdown
    params(userID, userName, date, drawdownAmount, transactionID)
2. Repayment
    params(userID, userName, date, repaymentAmount, transactionID)
3. AutoRepayment
    params(userID, userName, date, autoRepaymentAmount, transactionID)

*/
class User {
    private String userID;
    private String userName;
    private String userEmail;
    private String userPhoneNumber;

    public User(String userID, String userName, String userEmail, String userPhoneNumber) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }
}

class Action{
    private String actionID;
    private String actionType; // Drawdown, Repayment, AutoRepayment
    private String actionDate;

    public Action(String actionID, String actionType, String actionDate) {
        this.actionID = actionID;
        this.actionType = actionType;
        this.actionDate = actionDate;
    }

    public String getActionID() {
        return actionID;
    }

    public void setActionID(String actionID) {
        this.actionID = actionID;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionDate() {
        return actionDate;
    }

    public void setActionDate(String actionDate) {
        this.actionDate = actionDate;
    }
}

class Payment {
    private String paymentID;
    private double paymentAmount;
    private String PaymentMethod;

    public Payment(String paymentID, double paymentAmount, String paymentMethod) {
        this.paymentID = paymentID;
        this.paymentAmount = paymentAmount;
        PaymentMethod = paymentMethod;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }

    public double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getPaymentMethod() {
        return PaymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        PaymentMethod = paymentMethod;
    }
}

class NotificationContent {
    private String userID;
    private String transactionID;
    private double amount;
    private String date;

    public NotificationContent(String userID, String transactionID, double amount, String date) {
        this.userID = userID;
        this.transactionID = transactionID;
        this.amount = amount;
        this.date = date;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
class Notification {
    private String notificationID;
    private String notificationType;
    private String templateID;
    private NotificationContent notificationContent;

    public Notification(String notificationID, String notificationType, String templateID, NotificationContent notificationContent) {
        this.notificationID = notificationID;
        this.notificationType = notificationType;
        this.templateID = templateID;
        this.notificationContent = notificationContent;
    }

    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTemplateID() {
        return templateID;
    }

    public void setTemplateID(String templateID) {
        this.templateID = templateID;
    }

    public NotificationContent getNotificationContent() {
        return notificationContent;
    }

    public void setNotificationContent(NotificationContent notificationContent) {
        this.notificationContent = notificationContent;
    }
}

// strategy Pattern
interface NotificationStrategy {
    void sendNotification();
}

class Email implements NotificationStrategy {

    @Override
    public void sendNotification() {
        System.out.println("Sending Email Notification");
    }
}
class PushInbox implements NotificationStrategy {

    @Override
    public void sendNotification() {
        System.out.println("Sending Email Notification");
    }
}
class SMS implements NotificationStrategy {

    @Override
    public void sendNotification() {
        System.out.println("Sending Email Notification");
    }
}

class NotificationProcessor {
    NotificationStrategy notificationStrategy;
    void setNotificationStrategy(NotificationStrategy notificationStrategy) {
        this.notificationStrategy = notificationStrategy;
    }
    void sendNotification() {
        notificationStrategy.sendNotification();
    }
}

interface INotificationService {
    Boolean validateUser(String userID);
    Boolean validateTemplateID(String templateID);
    Boolean validateParams(NotificationContent notificationContent);
    String getTemplateID();
    String updateTemplateID();
}
class NotificationService implements  INotificationService {

    @Override
    public Boolean validateUser(String userID) {
        return null;
    }

    @Override
    public Boolean validateTemplateID(String templateID) {
        return null;
    }

    @Override
    public Boolean validateParams(NotificationContent notificationContent) {
        return null;
    }

    @Override
    public String getTemplateID() {
        return "";
    }

    @Override
    public String updateTemplateID() {
        return "";
    }
}

public class MSLLD1 {
}

/*
Drawdown --> templateID1 (param1, param2)
Repayment --> templateID2 (param1, param3)
AutoRepayment --> templateID3 (param5, param 6)

NotificationService {
    Req--> UserID, templareID
    validate user
    validate templateID

}
templateID --> UUID --> Whether this UUID(templateID) is exist in our DB or Not --> If Exist --> Its a Valid TemplateID
templateID1 --> (p1, p2, p3) p1=userName, p2->DrawdownAmount, p3->TransactionID
*/


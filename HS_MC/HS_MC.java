import java.util.List;

/*
class:-
User
Order
Restaurant
DeliveryPartner
Payment

Functionality:-
PlaceOrder();
Pay();
notification();
acceptOrder();
*/
class User {
    private String userID;
    private String userName;
    private String userPhoneNumber;
    private String userAddress;

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

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }
}

class FoodItem {
    private String foodItemID;
    private int foodItemCount;
    private double foodItemPrice;

}

class Order {
    private String orderID;
    private List<FoodItem> foodItemList;
    private double totalAmount;
    private double discount;
}

class Payment {
    private String paymentID;
    private String paymentMethod;
    private double amount;

}

class Restaurant {
    private String restaurantID;
    private String restaurantLocation;
    private String restaurantName;
}

class DeliveryPartner {
    private String deliveryPartnerID;
    private String deliveryPartnerName;
    private String deliveryPartnerPhoneNumber;
}

// Strategy Patter for Payment
// CASH + UPI + Credit card
interface PaymentStrategy {
    void pay(double amount);
}

class CASH implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Cash Payment Method is in Progress");
    }
}

class UPI implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Cash Payment Method is in Progress");
    }
}

class CreditCard implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Cash Payment Method is in Progress");
    }
}

class PaymentProcessor {
    private PaymentStrategy paymentStrategy;

    void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    void pay(double amount) {
        paymentStrategy.pay(amount);
    }
}


interface NotificationStrategy {
    void sendNotification();
}

class Email implements NotificationStrategy {
    @Override
    public void sendNotification() {
        System.out.println("Send Notification");
    }

    class PaymentProcessor {
        private PaymentStrategy paymentStrategy;

        void setPaymentStrategy(PaymentStrategy paymentStrategy) {
            this.paymentStrategy = paymentStrategy;
        }

        void pay(double amount) {
            paymentStrategy.pay(amount);
        }
    }
}

public class HS_MC {
    public static void main(String[] args) {

    }
}


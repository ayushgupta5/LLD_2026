/*
class:
User (Admin, Customer)
Payment
Item

functionality:;

selectItems();
dispenseItem();
refillItem();

payMent() //cash, card, wallet





collectCash();
returnChange();



isTransactionSuccess(String PaymentStatus);
validatePayment();
*/


import java.util.ArrayList;
import java.util.List;

interface IITemManager {
    List<Item> selectItems(List<Item> itemList);
    List<Item> dispenseItem();
    void refillItem(List<Item> refillItems, List<Item> items);
    boolean isOutOfStock(Item item);
}

class ITemManager implements IITemManager {

    @Override
    public List<Item> selectItems(List<Item> itemList) {
        List<Item> choosenItems=new ArrayList<>();
        for(Item item: itemList) {
            if(item.itemName.toLowerCase().equalsIgnoreCase("Lays Chips")
                    || item.itemName.toLowerCase().equalsIgnoreCase("Dairy Milk")
                    || item.itemName.toLowerCase().equalsIgnoreCase("KitKat")) {
                choosenItems.add(item);
            }
        }
        System.out.println("Selecting the Items by User");
        return choosenItems;

    }

    @Override
    public List<Item> dispenseItem() {
        System.out.println("Dispense Item is Going on");
        return List.of();
    }

    @Override
    public void refillItem(List<Item> refillItems, List<Item> items) {
        System.out.println("Refill Item is Started");
        for(Item refillItem : refillItems) {
            items.add(refillItem);
        }
        System.out.println("Refill Item is Completed");
    }

    @Override
    public boolean isOutOfStock(Item item) {
        System.out.println("Check Item is Available or Not");
        if (item.isAvailable == false) {
            System.out.println("Item is Not Available");
            return true;
        }
        System.out.println("Item is Not Available");
        return false;
    }
}

interface ICashManager {
    void collectCash();
    void returnChange();
}
class CashManager implements ICashManager {

    @Override
    public void collectCash( ) {
        System.out.println("Collect the Cash");
    }

    @Override
    public void returnChange() {
        System.out.println("Return The Change");
    }
}

interface IPaymentManager {
    boolean isTransactionSuccess(String PaymentStatus);
    boolean validatePayment();
}
class PaymentManager implements IPaymentManager {

    @Override
    public boolean isTransactionSuccess(String PaymentStatus) {
        return PaymentStatus == "SUCCESS";
    }

    @Override
    public boolean validatePayment() {
        return true;
    }
}


// strategy pattern
interface PaymentStrategy {
    boolean payment(int amount);
}
class Cash implements PaymentStrategy {

    @Override
    public boolean payment(int amount) {
        System.out.println("Payment is in Process with Cash");
        return true;
    }
}

class Card implements PaymentStrategy {

    @Override
    public boolean payment(int amount) {
        System.out.println("Payment is in Process with Card");
        return true;
    }
}

class Wallet implements PaymentStrategy {

    @Override
    public boolean payment(int amount) {
        System.out.println("Payment is in Process with Wallet");
        return true;
    }
}

class PaymentProcessor {
    PaymentStrategy paymentStrategy;
    void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    boolean payment(int amount) {
        if(paymentStrategy.payment(amount)) {
            return true;
        }
        return false;
    }
}


class User{
    public String userID;
    public String userName;
    public String createdAt;
    public String updatedAt;

    public User(String userID, String userName, String createdAt, String updatedAt) {
        this.userID = userID;
        this.userName = userName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
class Admin extends User {
    public Admin(String userID, String userName, String createdAt, String updatedAt) {
        super(userID, userName, createdAt, updatedAt);
    }
}

class Customer extends User {
    public Customer(String userID, String userName, String createdAt, String updatedAt) {
        super(userID, userName, createdAt, updatedAt);
    }
}
class Payment {
    public String paymentID;
    public String paymentMethod;
    public int amount;
    public String userID;
    public String status;

    public Payment(String paymentID, String paymentMethod, int amount, String userID, String status) {
        this.paymentID = paymentID;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.userID = userID;
        this.status = status;
    }
}
class Item {
    public String itemID;
    public String itemName;
    public boolean isAvailable;
    public int amount;

    public Item(String itemID, String itemName, boolean isAvailable, int amount) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.isAvailable = isAvailable;
        this.amount = amount;
    }
}
public class VendingMachine {
    public static void main(String[] args) {

        Admin admin = new Admin("1", "Admin", "13 Feb 2026", "13 Feb 2026");
        Item item = new Item("1", "Lays Chips", true, 200);

        ITemManager iTemManager = new ITemManager();
        List<Item> refillItem=new ArrayList<>();
        List<Item> items=new ArrayList<>();

        iTemManager.refillItem(refillItem, items);

        iTemManager.dispenseItem();






        Payment payment = new Payment("1", "Card", 500, "1", "SUCCESS");
        PaymentProcessor paymentProcessor = new PaymentProcessor();
        paymentProcessor.setPaymentStrategy(new Card());
        paymentProcessor.payment(payment.amount);

        PaymentManager paymentManager = new PaymentManager();
        paymentManager.isTransactionSuccess(payment.status);
    }
}

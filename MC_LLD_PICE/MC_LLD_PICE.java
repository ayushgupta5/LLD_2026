/*
Designing a Digital Wallet Service

Requirements
The digital wallet should allow users to create an account and manage their personal information.
Users should be able to add and remove payment methods, such as credit cards or bank accounts.
The digital wallet should support fund transfers between users and to external accounts.
The system should handle transaction history and provide a statement of transactions.
The digital wallet should support multiple currencies and perform currency conversions.
The system should ensure the security of user information and transactions.
The digital wallet should handle concurrent transactions and ensure data consistency.
The system should be scalable to handle a large number of users and transactions.
*/
/*
classes:-
wallet
users
payment
transaction
currency

functionalities:-
Register();
Login(); Logout();
ManagePersonalInformation();
updatePaymentMethod();
fundTransfer() == pay(amount);
showTransactionHistory();
CurrencyProcessor();// IDR, INR, SGD, MYR

*/

import java.util.HashMap;

interface IRegister {
    void register(String email, String userName);
}

interface IUserRepository {
    User saveUser(User user);
    User findUserByUserID(String userID);
}
class UserRepository implements IUserRepository {

    HashMap<String, User> userDB = new HashMap<>();

    public UserRepository(HashMap<String, User> userDB) {
        this.userDB = userDB;
    }

    @Override
    public User saveUser(User user) {
        userDB.put(user.getUserID(), user);
        return user;
    }

    @Override
    public User findUserByUserID(String userID) {
        User user = userDB.getOrDefault(userID, null);
        return user;
    }
}
class Register implements IRegister {

    @Override
    public void register(String email, String userName) {

    }
}
interface IManagePersonalInformation {

}

interface IFundTransfer {
    void fundTransfer(Payment payment);
}
class FundTransfer implements  IFundTransfer {

    @Override
    public void fundTransfer(Payment payment) {

        // lock will happen on walletID

        String srcWalledIDForLock = payment.getSrcWalletID();
        boolean lockSuccess = lock.lock(srcWalledIDForLock);
        if err {
            //handle the error
        }


        String srcWalletID = payment.getSrcWalletID();
        Wallet srcWallet = findWalletByWalletID(srcWalletID);
        double srcAmount = srcWallet.getWalletAmount();
        // srcAmount --> 100 --> dstWallet

        String DstWalletID = payment.getDstWalletID();
        Wallet dstWallet = findWalletByWalletID(srcWalletID);
        double dstAmount = dstWallet.getWalletAmount();



        boolean debit= false;
        boolean credit = false;

        srcAmount -= 100;
        debit = true;
        dstAmount += 100;
        if(credit == false) {
            // rollback
        }


        srcWallet = updateWalletByAmount(srcAmount);
        dstWallet = updateWalletByAmount(dstAmount);

        // if failure --> RLLBack
        boolean failed = false;
        if(failed == true) {
            srcAmount += 100;
            dstAmount -= 100;

            srcWallet = updateWalletByAmount(srcAmount);
            dstWallet = updateWalletByAmount(dstAmount);
        }



    }
}







class User {
    private String userID;
    private String userName;
    private String userEmail;
    private String userPhoneNumber;

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

    public User(String userID, String userName, String userEmail, String userPhoneNumber) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhoneNumber = userPhoneNumber;
    }
}

class Wallet {
    private String walletID;
    private double walletAmount;

    public Wallet(String walletID, double walletAmount) {
        this.walletID = walletID;
        this.walletAmount = walletAmount;
    }

    public String getWalletID() {
        return walletID;
    }

    public void setWalletID(String walletID) {
        this.walletID = walletID;
    }

    public double getWalletAmount() {
        return walletAmount;
    }

    public void setWalletAmount(double walletAmount) {
        this.walletAmount = walletAmount;
    }
}

class Payment {
    private String paymentID;
    private PaymentMethod paymentMethod;
    private double amount;
    private String transactionID;
    private Currency currency;
    private String srcWalletID;
    private String dstWalletID;

    public Payment(String paymentID, PaymentMethod paymentMethod, double amount, String transactionID, Currency currency, String srcWalletID, String dstWalletID) {
        this.paymentID = paymentID;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.transactionID = transactionID;
        this.currency = currency;
        this.srcWalletID = srcWalletID;
        this.dstWalletID = dstWalletID;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getSrcWalletID() {
        return srcWalletID;
    }

    public void setSrcWalletID(String srcWalletID) {
        this.srcWalletID = srcWalletID;
    }

    public String getDstWalletID() {
        return dstWalletID;
    }

    public void setDstWalletID(String dstWalletID) {
        this.dstWalletID = dstWalletID;
    }
}

enum CurrencyName {
    IDR, INR, MYR, SGD, USD
}
enum PaymentMethod {
    CASH, UPI, CREDIT_CARD
}
class Currency {
    private String currencyID;
    private CurrencyName currencyName;

    public Currency(String currencyID, CurrencyName currencyName) {
        this.currencyID = currencyID;
        this.currencyName = currencyName;
    }

    public String getCurrencyID() {
        return currencyID;
    }

    public void setCurrencyID(String currencyID) {
        this.currencyID = currencyID;
    }

    public CurrencyName getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(CurrencyName currencyName) {
        this.currencyName = currencyName;
    }
}




// Credit Card, UPI, CASH --> Strategy Pattern
interface PaymentStrategy {
    void pay(double amount);
}
class UPI implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("UPI Payment is in Progress with Amount: " + amount);
    }
}
class CASH implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("CASH Payment is in Progress with Amount: " + amount);
    }
}
class CreditCard implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("CreditCard Payment is in Progress with Amount: " + amount);
    }
}
class PaymentProcessor{
    private PaymentStrategy paymentStrategy;
    void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    void pay(double amount) {
        paymentStrategy.pay(amount);
    }
}
//void setDefaultPaymentMethod(Payment payment) {
//    if(payment.getAmount() > 100000) {
//        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
//    }
//    else if(payment.getAmount() < 100000) {
//        payment.setPaymentMethod(PaymentMethod.UPI);
//    }
//    else {
//        payment.setPaymentMethod(PaymentMethod.CASH);
//    }
//}
public class MC_LLD_PICE {
    public static void main(String[] args) {
        User user = new User("1", "Ayush", "ayush@gmail.com", "9999999");

        Payment payment = new Payment("1", PaymentMethod.CASH, 500, "xxyyy");
        PaymentProcessor paymentProcessor = new PaymentProcessor();

        if(payment.getPaymentMethod() == PaymentMethod.CASH) {
            paymentProcessor.setPaymentStrategy(new CASH());
        } else if(payment.getPaymentMethod() == PaymentMethod.CREDIT_CARD) {
            paymentProcessor.setPaymentStrategy(new CreditCard());
        } else if(payment.getPaymentMethod() == PaymentMethod.UPI) {
            paymentProcessor.setPaymentStrategy(new UPI());
        }
        paymentProcessor.pay(payment.getAmount());

        payment.setPaymentMethod(PaymentMethod.UPI);
        paymentProcessor.pay(payment.getAmount());


    }
}


/*
USER A.  USER_B
1000.     500
-500.     +500


*/

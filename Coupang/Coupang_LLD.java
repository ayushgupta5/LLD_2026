/*
Design a simplified expense sharing application. There can be multiple users using this application.
A user can share expense amongst any number of other users. Once an expense is created the users who are part of the expense should be notified.

The code should expose two functionalities :
1. Create expense - A user can create an expense with other users
2. Fetch balance amounts - For a given user, fetch amount being owed and owned to other users

Expectation is to have extensible and functionally correct code.

User
Balance
Expense
Notification

// Pattern
Observer Pattern
*/
class User {
    private int userID;
    private String userName;
    private String userEmail;
    private String userPhoneNumber;


    public User(int userID, String userName, String userEmail, String userPhoneNumber) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhoneNumber = userPhoneNumber;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
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
class Balance {

}
public class Coupang_LLD {
}

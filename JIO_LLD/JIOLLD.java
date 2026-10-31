/*
Core Requirements
1. record(userId)
Record an activity/action performed by a user.
2. getRate(userId)
Return how many actions the user performed in the last 1 minute.
3. Concurrency
Multiple threads may call record() and getRate() simultaneously.
4. No external persistence
The system must be in-memory only.

LLD Focus Areas
You are expected to design the following components:
CounterStore
Handles counters for each user.
TimeBucket
Stores per-second or per-window counts.
Aggregator
Computes a user’s rate for the last minute.

class:
User
Activity

func:
record(userID);
getRate(userID);

https://docs.google.com/document/d/1A3Gh6KTduWAgFRzAs60Ym3TWS9gQVAE9wiWpD4QHhdw/edit?tab=t.0

fox studio
brevo

*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class User {
    private String userID;
    private String userName;
    private String userEmail;
    private String typeOfActivity;
    private List<Activity> activities;
    private String createdAt;
    private String createdBy;
    private String updatedAt;
    private String updatedBy;
    private int counterStore;

    public User(String userID, String userName, String userEmail, String typeOfActivity, List<Activity> activities, String createdAt, String createdBy, String updatedAt, String updatedBy, int counterStore) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.typeOfActivity = typeOfActivity;
        this.activities = activities;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.counterStore = counterStore;
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

    public String getTypeOfActivity() {
        return typeOfActivity;
    }

    public void setTypeOfActivity(String typeOfActivity) {
        this.typeOfActivity = typeOfActivity;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public int getCounterStore() {
        return counterStore;
    }

    public void setCounterStore(int counterStore) {
        this.counterStore = counterStore;
    }
}

class Activity {
    private String activityID;
    private String createdAt;
    private String createdBy;
    private String updatedAt;
    private String updatedBy;

    public Activity(String activityID, String createdAt, String createdBy, String updatedAt, String updatedBy) {
        this.activityID = activityID;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}

interface IRecordManager{
    Activity record(User user);
    int getRate(String userID);
}

class RecordManager implements IRecordManager {

    HashMap<String, Activity> userActivityMap = new HashMap<>(); // userID, --> Activity
    HashMap<String, User> UserIDToUserMap = new HashMap<>(); // activityID --> User
    @Override
    public Activity record(User user) {
        Activity activity = new Activity("1", "14 Feb 2026", "Manual", "14 Feb 2026", "Manual");
        int  count = user.getCounterStore();
        user.setCounterStore(count+1);
        userActivityMap.put(user.getUserID(), activity);

        UserIDToUserMap.put(user.getUserID(), user);
        return activity;
    }

    @Override
    public int getRate(String userID) {
        User user = UserIDToUserMap.get(userID);
        return user.getCounterStore();
    }
}

public class JIOLLD {


}

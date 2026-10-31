/*
Stream Data Monitoring and Anomaly
Detection
You are given a continuous stream of data points representing measurements like CPU utilization, temprature
Your task is to design and implement a stream processing component that:
Monitors threshold for a registered user
Sends a notification to a registered user when the incoming data
crosses a configured threshold.
Detects anomalies
Identifies abnormal values based on recent data trends.
10, 40, 60, 90, 89, 230, 69, 80, 80
User 1, 50
User2, 70
*/
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

class User {
    private String name;
    private int threshold;
    private String userStatus;

    public User(String name, int threshold, String userStatus) {
        this.name = name;
        this.threshold = threshold;
        this.userStatus = userStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
}

interface INotify {
    void notifyUser();
}

class Notify implements INotify {

    @Override
    public void notifyUser() {
        System.out.println("Notify the user");
    }
}

interface IUserRegister {
    User register(String userName, int threshold);

    User UnRegister(User user);
}

class UserRegister implements IUserRegister {

    @Override
    public User register(String userName, int threshold) {
        User user = new User(userName, threshold, "ACTIBVE");
        return user;
    }

    @Override
    public User UnRegister(User user) {
        user.setUserStatus("INACTIVE");
        return user;
    }
}

class Config {
    private User user;
    private String want;

    public Config(User user, String want) {
        this.user = user;
        this.want = want;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getWant() {
        return want;
    }

    public void setWant(String want) {
        this.want = want;
    }
}

//void getLatestConfig () {
//
//}

public class LLD_MS {
    public static void main(String[] args) {
        User user1 = new User("User1", 50, "ACTIVE");
        User user2 = new User("User2", 70, "ACTIVE");

        UserRegister userRegister = new UserRegister();

        Notify notify = new Notify();

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        System.out.println("Read The Data from The Input");

        while (true) {
            int prevData = -1;
            boolean flag = false;
            Config config = getLatestConfig();
            // validate
            if(config == null) {
                continue;
            }
            User userVal = config.getUser();
            String want = config.getWant();
            if (want == "SUBSCRIBE") {
                users.add(userVal);
            } else if (want == "UNSUBSCRIBE") {
                userVal.setUserStatus("INACTIVE");
                users.erase(userVal);
            }

            int inputData = getInputData();



            if (inputData == -1) {
                break;
            }
            for (User user : users) {
                // check user is valid or not
                if (user.getUserStatus() == "INACTIVE") {
                    continue;
                }
                if (user.getThreshold() < inputData) {
                    notify.notifyUser();
                }
            }
            // figure out abnormal values
            if (abs(inputData - prevData) > 100) { // logic for Handling abnormal Values
                if (flag) {
                    // we found two consecutive abnormal datas
                    System.out.println(prevData);
                    System.out.println(inputData);
                }
                flag = true;
            }
            prevData = inputData;
        }
    }
}

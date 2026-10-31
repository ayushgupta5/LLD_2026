import java.util.ArrayList;
import java.util.List;

interface Observer {
    void update(String msg);
}
class User implements Observer {
    private String userName;

    public User(String userName) {
        this.userName = userName;
    }

    @Override
    public void update(String msg) {
        System.out.println(userName + "Received: " + msg);
    }
}
interface Subject {
    void subscribe(Observer observer);
    void unSubscribe(Observer observer);
    void notify(String msg);
}
class NotificationService implements Subject {
    List<Observer> observerList;

    public NotificationService() {
        this.observerList = new ArrayList<>();
    }

    @Override
    public void subscribe(Observer observer) {
        observerList.add(observer);
    }

    @Override
    public void unSubscribe(Observer observer) {
        observerList.remove(observer);
    }

    @Override
    public void notify(String msg) {
        for(Observer observer : observerList) {
            observer.update(msg);
        }
    }
}
public class ObserverPattern {
    public static void main(String[] args) {
        User aman = new User("Aman");
        User akash = new User("Akash");

        NotificationService notificationService = new NotificationService();
        notificationService.subscribe(aman);
        notificationService.subscribe(akash);
        notificationService.notify("Notification 1");

        notificationService.unSubscribe(akash);

        notificationService.notify("Notification 2");


    }
}

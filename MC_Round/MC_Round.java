/*
Online Food Ordering System
Design and implement an in-memory online food ordering system that can:
Add restaurants and menu items.
Register users and allow them to place orders.
Support basic order tracking (e.g., PENDING/PLACED/COMPLETED).
Work purely in-memory (no database), using appropriate OOPS principles and clean class structure.
Process commands from standard input and print outputs to the console.
✅ Functional Requirements
1. Register User

Command: REGISTER_USER <user_id> <user_name>

2. Add Restaurant

Command: ADD_RESTAURANT <restaurant_id> <restaurant_name>

3. Add Menu Item

Command: ADD_MENU_ITEM <restaurant_id> <item_id> <item_name> <price>

4. Place Order

Command: PLACE_ORDER <user_id> <restaurant_id> <item_id> <quantity>

✅ Output: Order ID (on success) or INVALID_USER/INVALID_RESTAURANT/INVALID_ITEM

5. Get Order Status

Command: GET_ORDER_STATUS <order_id>

✅ Output: Order status

6. Update Order Status

Command: UPDATE_ORDER_STATUS <order_id> <status> (status = PLACED | COMPLETED)

✅ Output: SUCCESS or INVALID_ORDER


class:-
class User {
    private String userID;
    private String userName;
    private String userEmail;
    private String userPhoneNumber;
}
class Item {
    private String itemID;
    private String itemName;
    private String itemDescription;
    private double amount;
}
class Menu {
    private String menuID;
    private List<Item> listOfItem;
}
class Restaurants {
    private String restaurantID;
    private String city;
    private String location;
    private List<Menu> listOfMenu;
}

Order (PENDING/PLACED/COMPLETED)

functionalities:-
User registerUser(String userID, String userName);
Restaurant addRestaurant(String restaurantID, String restaurantName);
Menu addMenuItems(String restaurantID, String itemID, String itemName, double amount);
Order placeOrders(String userID, String restaurantID, String itemID, int quantity);
String getOrderStatus(String orderID);
String updateOrderStatue(String orderID, String status) // (status = PLACED | COMPLETED)
orderTracking(String orderID);


Non-functional / Design Requirements:

In-memory only (no external databases)

Proper OOP structure: use of classes, encapsulation, services, separation of concerns

Clean and readable code, modular design, good naming conventions

Handle all corner/edge cases



 Input Format
Each line of input is a command. Process and execute them in sequence.

 Output Format
Only commands that require output should print to System.out.

Each output should be on a separate line.





? Example Commands
REGISTER_USER U1 Alice

ADD_RESTAURANT R1 PizzaHut

ADD_MENU_ITEM R1 I1 Margherita 300

PLACE_ORDER U1 R1 I1 2

GET_ORDER_STATUS O1

UPDATE_ORDER_STATUS O1 COMPLETED

GET_ORDER_STATUS O1



Sample input

REGISTER_USER U1 John
ADD_RESTAURANT R1 Domino
ADD_MENU_ITEM R1 I1 VegPizza 250
PLACE_ORDER U1 R1 I1 1
GET_ORDER_STATUS O1
UPDATE_ORDER_STATUS O1 COMPLETED
GET_ORDER_STATUS O1
Sample output

O1
PENDING
SUCCESS
COMPLETED
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

class Item {
    private String itemID;
    private String itemName;
    private String itemDescription;
    private double amount;
    private int itemQuantity;

    public Item(String itemID, String itemName, String itemDescription, double amount) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.amount = amount;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}

class Menu {
    private String menuID;
    private List<Item> listOfItem;

    public Menu(String menuID, List<Item> listOfItem) {
        this.menuID = menuID;
        this.listOfItem = listOfItem;
    }

    public String getMenuID() {
        return menuID;
    }

    public void setMenuID(String menuID) {
        this.menuID = menuID;
    }
}

class Restaurant {
    private String restaurantID;
    private String restaurantName;
    private String city;
    private String location;

    public Restaurant(String restaurantID, String restaurantName, String city, String location) {
        this.restaurantID = restaurantID;
        this.restaurantName = restaurantName;
        this.city = city;
        this.location = location;
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

enum OrderStatus {
    PENDING,
    PLACED,
    COMPLETED
}

class Order {
    private String orderID;
    private String userID;
    private String restaurantID;
    private String itemID;
    private int quantity;
    private OrderStatus orderStatus;

    // String userID, String restaurantID, String itemID, int quantity


    public Order(String orderID, String userID, String restaurantID, String itemID, int quantity, OrderStatus orderStatus) {
        this.orderID = orderID;
        this.userID = userID;
        this.restaurantID = restaurantID;
        this.itemID = itemID;
        this.quantity = quantity;
        this.orderStatus = orderStatus;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}

class Payment {
    private String paymentID;
    private String paymentMethod;
    private double amount;
    private String orderID;

    public Payment(String paymentID, String paymentMethod, double amount, String orderID) {
        this.paymentID = paymentID;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.orderID = orderID;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }
}

// Strategy Pattern for Payment (CASH/UPI/CREDIT_CARD)
interface PaymentStrategy {
    void pay(double amount);
}

class UPI implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("UPI Payment is in Progress with this amount value" + amount);
    }
}

class CreditCard implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("CreditCard Payment is in Progress with this amount value" + amount);
    }
}

class Cash implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Cash Payment is in Progress with this amount value" + amount);
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

class Notification {
    private String notificationID;
    private String notificationType; // Email, PushInbox, SMS, WhatsApp
    private String Description;
}

// Strategy pattern for sending the notification(SMS,
interface NotificationStrategy {
    void sendNotification(String description);
}

class Email implements NotificationStrategy {
    @Override
    public void sendNotification(String description) {
        System.out.println("Sending Email Notification with given Description" + description);
    }
}

class PushInbox implements NotificationStrategy {
    @Override
    public void sendNotification(String description) {
        System.out.println("Sending PushInbox Notification with given Description" + description);
    }
}

class SMS implements NotificationStrategy {
    @Override
    public void sendNotification(String description) {
        System.out.println("Sending SMS Notification with given Description" + description);
    }
}

class WhatsApp implements NotificationStrategy {
    @Override
    public void sendNotification(String description) {
        System.out.println("Sending WhatsApp Notification with given Description" + description);
    }
}

class NotificationProcessor {
    private NotificationStrategy notificationStrategy;

    void setPaymentStrategy(NotificationStrategy notificationStrategy) {
        this.notificationStrategy = notificationStrategy;
    }

    void sendNotification(String description) {
        notificationStrategy.sendNotification(description);
    }
}

/*

functionalities:-
User registerUser(String userID, String userName);
Restaurant addRestaurant(String restaurantID, String restaurantName);
Menu addMenuItems(String restaurantID, String itemID, String itemName, double amount);
Order placeOrders(String userID, String restaurantID, String itemID, int quantity);
String getOrderStatus(String orderID);
String updateOrderStatue(String orderID, String status) // (status = PLACED | COMPLETED)
orderTracking(String orderID);


 */

interface IRegistration {
    void register(String userID, String userName);
}

interface IUserRepository {
    User save(User user);

    User findByUserID(String userID);
}

class Registration implements IRegistration {

    private IUserRepository userRepository;

    public Registration(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void register(String userID, String userName) {
        User user = new User(userID, userName, "ayush@gmail.com", "999");
        userRepository.save(user);
        System.out.println("User Registration");
    }
}

class UserRepository implements IUserRepository {
    private Map<String, User> userMap;

    public UserRepository(Map<String, User> userMap) {
        this.userMap = userMap;
    }

    @Override
    public User save(User user) {
        userMap.put(user.getUserID(), user);
        return user;
    }

    @Override
    public User findByUserID(String userID) {
        return userMap.getOrDefault(userID, null);
    }
}

interface IAuth {
    void login(String userEmail);

    void logOut();
}

class Auth implements IAuth {

    @Override
    public void login(String userEmail) {
        System.out.println("Login User with give UserEmail" + userEmail);
    }

    @Override
    public void logOut() {
        System.out.println("Logging Out");
    }
}

interface IRestaurantsRepository {
    Restaurant save(Restaurant restaurant);

    Restaurant findByRestaurantID(String restaurantID);
}

class RestaurantRepository implements IRestaurantsRepository {
    private Map<String, Restaurant> restaurantMap;

    public RestaurantRepository(Map<String, Restaurant> restaurantMap) {
        this.restaurantMap = restaurantMap;
    }

    @Override
    public Restaurant save(Restaurant restaurant) {
        restaurantMap.put(restaurant.getRestaurantID(), restaurant);
        return restaurant;
    }

    @Override
    public Restaurant findByRestaurantID(String restaurantID) {
        return restaurantMap.getOrDefault(restaurantID, null);
    }
}

interface IAddRestaurants {
    Restaurant addRestaurant(String restaurantID, String restaurantName);
}

class AddRestaurants implements IAddRestaurants {

    private RestaurantRepository restaurantRepository;

    public AddRestaurants(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public Restaurant addRestaurant(String restaurantID, String restaurantName) {
        Restaurant restaurant = new Restaurant(restaurantID, "CityName", "location", restaurantName);
        restaurantRepository.save(restaurant);
        System.out.println("Restaurant is added with given restaurantID and restaurantName" + restaurantID + " " + restaurantName);
        return restaurant;
    }
}

interface IMenuRepository {
    Menu saveMenu(Menu menu);

    Menu findByMenuID(String menuID);
}

class MenuRepository implements IMenuRepository {
    private Map<String, Menu> menuMap;

    public MenuRepository(Map<String, Menu> menuMap) {
        this.menuMap = menuMap;
    }

    @Override
    public Menu saveMenu(Menu menu) {
        menuMap.put(menu.getMenuID(), menu);
        return menu;
    }

    @Override
    public Menu findByMenuID(String menuID) {
        return menuMap.getOrDefault(menuID, null);
    }
}

interface IMenuItemService {
    Menu addMenuItems(String restaurantID, String itemID, String itemName, double amount);
}

class MenuItemService implements IMenuItemService {
    private IMenuRepository menuRepository;

    public MenuItemService(IMenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Override
    public Menu addMenuItems(String restaurantID, String itemID, String itemName, double amount) {
        Item item1 = new Item("1", itemName, "Item Description", amount);
        List<Item> items = new ArrayList<>();
        items.add(item1);

        Menu menu = new Menu("1", items);

        menuRepository.saveMenu(menu);
        System.out.println("MenuItems are added with given Info " + "restaurantID: " + restaurantID + "itemID : " + itemID + "itemName: " + "amount: " + amount);
        return menu;
    }
}


interface IOrderRepository {
    Order saveOrder(Order order);

    Order findByOrderID(String orderID);
}

class OrderRepository implements IOrderRepository {
    private Map<String, Order> orderMap;

    public OrderRepository(Map<String, Order> orderMap) {
        this.orderMap = orderMap;
    }

    @Override
    public Order saveOrder(Order order) {
        orderMap.put(order.getOrderID(), order);
        System.out.println("Order is saved ith given Order Info : " + order.getOrderID());
        return order;
    }

    @Override
    public Order findByOrderID(String orderID) {
        return orderMap.getOrDefault(orderID, null);
    }
}

interface IItemRepository {
    Item saveItem(Item item);

    Item findByItemID(String itemID);
}

class ItemRepository implements IItemRepository {
    private Map<String, Item> itemMap;

    public ItemRepository(Map<String, Item> itemMap) {
        this.itemMap = itemMap;
    }

    @Override
    public Item saveItem(Item item) {
        itemMap.put(item.getItemID(), item);
        return item;
    }

    @Override
    public Item findByItemID(String itemID) {
        return itemMap.getOrDefault(itemID, null);
    }
}


interface IOrderService {
    Order placeOrders(String userID, String restaurantID, String itemID, int quantity);

    String getOrderStatus(String orderID);

    String updateOrderStatus(String orderID, OrderStatus status); // (status = PLACED | COMPLETED)
}

class OrderService implements IOrderService {

    private IOrderRepository orderRepository;
    private IUserRepository userRepository;
    private IRestaurantsRepository restaurantsRepository;
    private IItemRepository itemRepository;

    public OrderService(IOrderRepository orderRepository, IUserRepository userRepository, IRestaurantsRepository restaurantsRepository, IItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.restaurantsRepository = restaurantsRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public Order placeOrders(String userID, String restaurantID, String itemID, int quantity) {
        User user = userRepository.findByUserID(userID);
        Restaurant restaurant = restaurantsRepository.findByRestaurantID(restaurantID);
        Item item = itemRepository.findByItemID(itemID);
        Order order = new Order("1", userID, restaurantID, itemID, quantity, OrderStatus.PENDING);
        System.out.println("Order is Placed with status : " + order.getOrderStatus());
        return order;
    }

    @Override
    public String getOrderStatus(String orderID) {
        Order order = orderRepository.findByOrderID(orderID);
        OrderStatus orderStatus = order.getOrderStatus();
        System.out.println("Getting the Order Status : " + orderStatus.toString());
        return orderStatus.toString();
    }

    @Override
    public String updateOrderStatus(String orderID, OrderStatus orderStatus) {
        Order order = orderRepository.findByOrderID(orderID);
        order.setOrderStatus(orderStatus);
        System.out.println("Updating the Order Status : " + orderStatus.toString());
        return orderStatus.toString();
    }
}

interface ITrackService {
    String orderTracking(String orderID);
}

class TrackService implements ITrackService {
    private IOrderRepository orderRepository;

    public TrackService(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public String orderTracking(String orderID) {
        Order order = orderRepository.findByOrderID(orderID);
        System.out.println("Tracking the Order with given OrderID: " + orderID + " OrderStatus" + order.getOrderStatus().toString());
        return order.getOrderStatus().toString();
    }
}


public class MC_Round {
    public static void main(String[] args) {
        //Registration
        User user = new User("1", "Ayush", "ayush@gmail.com", "999999");
        Map<String, User> userMap = new HashMap<>();
        IUserRepository userRepository = new UserRepository(userMap);
        IRegistration registration = new Registration(userRepository);
        registration.register(user.getUserID(), user.getUserName());

        // AUTH
        IAuth auth = new Auth();
        auth.login(user.getUserEmail());

        Restaurant restaurant = new Restaurant("1", "restaurantName", "BLR", "Belendur");

        Map<String, Restaurant> restaurantMap = new HashMap<>();
        IRestaurantsRepository restaurantsRepository = new RestaurantRepository(restaurantMap);
        IAddRestaurants addRestaurants = new AddRestaurants((RestaurantRepository) restaurantsRepository);
        addRestaurants.addRestaurant(restaurant.getRestaurantID(), restaurant.getRestaurantName());

        Item dosa = new Item("1", "Dosa", "Dosa Description", 50);
        Item badaPaav = new Item("2", "BadaPaav", "Description of Bada Paav", 100);

        List<Item> listOfItems = new ArrayList<>();
        listOfItems.add(dosa);
        listOfItems.add(badaPaav);

        Menu menu = new Menu("1", listOfItems);

        Map<String, Menu> menuMap = new HashMap<>();
        IMenuRepository menuRepository = new MenuRepository(menuMap);
        IMenuItemService menuItemService = new MenuItemService(menuRepository);
        menuItemService.addMenuItems(restaurant.getRestaurantID(), dosa.getItemID(), dosa.getItemName(), dosa.getAmount());
        menuItemService.addMenuItems(restaurant.getRestaurantID(), badaPaav.getItemID(), badaPaav.getItemName(), badaPaav.getAmount());

        Order orderOfDosa = new Order("1", user.getUserID(), restaurant.getRestaurantID(), dosa.getItemID(), 2, OrderStatus.PENDING);
        Order orderOfBadaPaav = new Order("1", user.getUserID(), restaurant.getRestaurantID(), badaPaav.getItemID(), 3, OrderStatus.PENDING);

        Map<String, Order> orderMap = new HashMap<>();
        IOrderRepository orderRepository = new OrderRepository(orderMap);
        orderRepository.saveOrder(orderOfDosa);
        orderRepository.saveOrder(orderOfBadaPaav);
        Map<String, Item> itemMap = new HashMap<>();
        ItemRepository itemRepository = new ItemRepository(itemMap);

        IOrderService orderService = new OrderService(orderRepository, userRepository, restaurantsRepository, itemRepository);
        orderService.placeOrders(user.getUserID(), restaurant.getRestaurantID(), dosa.getItemID(), orderOfDosa.getQuantity());
        orderService.placeOrders(user.getUserID(), restaurant.getRestaurantID(), badaPaav.getItemID(), orderOfBadaPaav.getQuantity());

        orderService.getOrderStatus(orderOfDosa.getOrderID());
        orderService.updateOrderStatus(orderOfDosa.getOrderID(), OrderStatus.COMPLETED);

        orderService.getOrderStatus(orderOfBadaPaav.getOrderID());
        orderService.updateOrderStatus(orderOfBadaPaav.getOrderID(), OrderStatus.COMPLETED);

        ITrackService trackService = new TrackService(orderRepository);
        trackService.orderTracking(orderOfDosa.getOrderID());
        trackService.orderTracking(orderOfBadaPaav.getOrderID());
        auth.logOut();
    }
}

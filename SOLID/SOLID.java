/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    SOLID PRINCIPLES - COMPLETE GUIDE                        ║
 * ║                    (Hinglish mein Easy Explanation)                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  S - Single Responsibility Principle  → Ek kaam, ek class                   ║
 * ║  O - Open/Closed Principle            → Extension YES, Modification NO      ║
 * ║  L - Liskov Substitution Principle    → Child = Parent (behavior same)      ║
 * ║  I - Interface Segregation Principle  → Chhote interfaces, bade nahi        ║
 * ║  D - Dependency Inversion Principle   → Interface par depend karo           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

// ╔═══════════════════════════════════════════════════════════════════════════════╗
// ║  S - SINGLE RESPONSIBILITY PRINCIPLE (SRP)                                   ║
// ╚═══════════════════════════════════════════════════════════════════════════════╝
/*
 * 📚 PRINCIPLE KYA HAI?
 * ---------------------
 * "Ek class ka sirf EK hi kaam hona chahiye"
 *
 * 🤔 SIMPLE EXAMPLE SE SAMJHO:
 * ---------------------------
 * Socho ek Chef hai jo:
 *   - Khana banata hai ✅
 *   - Waiter ka kaam bhi karta hai ❌
 *   - Billing bhi karta hai ❌
 *   - Safai bhi karta hai ❌
 *
 * Ye galat hai! Chef ka sirf ek kaam hai - khana banana.
 * Agar Chef ko billing bhi karni pade, toh kaam complicated ho jayega.
 *
 * 🎯 FAYDA KYA HAI?
 * -----------------
 * 1. Code samajhna easy hota hai
 * 2. Testing easy hoti hai
 * 3. Bugs dhundhna easy hota hai
 * 4. Code maintain karna easy hota hai
 */

// ═══════════════════════════════════════════════════════════════════
// ❌ GALAT TARIKA (BAD EXAMPLE) - Multiple Responsibilities
// ═══════════════════��═══════════════════════════════════════════════
/*
 * Neeche waali class GALAT hai kyunki:
 * Ek hi class mein 3 alag-alag kaam ho rahe hain!
 *
 *    ┌─────────────────────────────────────┐
 *    │         EmployeeBad Class           │
 *    │   ┌───────────────────────────┐     │
 *    │   │ 1. Salary Calculate ❌    │     │
 *    │   │ 2. Database Save ❌       │     │
 *    │   │ 3. Report Generate ❌     │     │
 *    │   └───────────────────────────┘     │
 *    │   (Bahut zyada responsibility!)     │
 *    └─────────────────────────────────────┘
 */
class EmployeeBad {
    private String name;
    private double salary;

    // ❌ Responsibility 1: Employee data manage karna
    public void calculateSalary() {
        // Salary calculation logic
        // Problem: Agar tax rules change ho, toh ye class change hogi
    }

    // ❌ Responsibility 2: Database operations
    public void saveToDatabase() {
        // Database save logic
        // Problem: Agar MySQL se MongoDB migrate karein, toh ye class change hogi
    }

    // ❌ Responsibility 3: Report generation
    public void generateReport() {
        // Report generation logic
        // Problem: Agar PDF format change ho, toh ye class change hogi
    }

    /*
     * 🚫 IS APPROACH KI PROBLEMS:
     * ---------------------------
     * 1. Agar database logic change ho → Employee class change karni padegi
     * 2. Agar report format change ho → Employee class change karni padegi
     * 3. Agar salary calculation change ho → Employee class change karni padegi
     *
     * Matlab: Ek class ko 3 alag reasons se change karna pad sakta hai!
     * Ye GALAT hai! Isse code fragile (kamzor) ho jata hai.
     */
}

// ═══════════════════════════════════════════════════════════════════
// ✅ SAHI TARIKA (GOOD EXAMPLE) - Single Responsibility
// ═══════════════════════════════════════════════════════════════════
/*
 * Ab har class ka SIRF EK kaam hai:
 *
 *    ┌────────────────┐    ┌──────────────────┐    ┌─────────────────┐
 *    │   Employee     │    │ SalaryCalculator │    │EmployeeRepository│
 *    │  (Data only)   │    │ (Salary only)    │    │ (Database only) │
 *    └────────────────┘    └──────────────────┘    └─────────────────┘
 *           │                      │                       │
 *    Sirf employee      Sirf salary            Sirf database
 *    ki info store      calculate kare         operations kare
 *
 *    ┌──────────────────────┐
 *    │EmployeeReportGenerator│
 *    │   (Reports only)      │
 *    └───────────────────────┘
 *           │
 *    Sirf reports
 *    generate kare
 */

// 📦 Class 1: Employee - Sirf employee ka data store karna
class Employee {
    private String name;    // Employee ka naam
    private double salary;  // Employee ki salary

    // Constructor - Object banate waqt naam aur salary set karo
    public Employee(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }

    // Getter methods - Sirf data access karne ke liye
    // Koi logic nahi, sirf data return karna
    public String getName() {
        return name;
    }

    public double getSalary() {
        return salary;
    }

    // 💡 Note: Is class ka sirf EK kaam hai - Employee data hold karna
    // Agar employee ke fields change ho, tabhi ye class change hogi
}

// 📦 Class 2: SalaryCalculator - Sirf salary related calculations
class SalaryCalculator {

    // Annual salary calculate karo (monthly * 12)
    public double calculateAnnualSalary(Employee emp) {
        return emp.getSalary() * 12;
    }

    // Tax calculate karo (20% of monthly salary)
    public double calculateTax(Employee emp) {
        return emp.getSalary() * 0.2;
    }

    // Bonus calculate karo (10% of annual salary)
    public double calculateBonus(Employee emp) {
        return calculateAnnualSalary(emp) * 0.10;
    }

    // 💡 Note: Is class ka sirf EK kaam hai - Salary calculations
    // Agar tax rules change ho, sirf ye class change hogi
}

// 📦 Class 3: EmployeeRepository - Sirf database operations
class EmployeeRepository {

    // Employee ko database mein save karo
    public void saveEmployee(Employee emp) {
        System.out.println("✅ Employee '" + emp.getName() + "' saved to database");
        // Real database logic yahan hoga (JDBC, Hibernate, etc.)
    }

    // Employee ko database se delete karo
    public void deleteEmployee(Employee emp) {
        System.out.println("🗑️ Employee '" + emp.getName() + "' deleted from database");
    }

    // Employee ko update karo
    public void updateEmployee(Employee emp) {
        System.out.println("🔄 Employee '" + emp.getName() + "' updated in database");
    }

    // 💡 Note: Is class ka sirf EK kaam hai - Database operations
    // Agar database change ho (MySQL → MongoDB), sirf ye class change hogi
}

// 📦 Class 4: EmployeeReportGenerator - Sirf report generation
class EmployeeReportGenerator {

    // PDF report generate karo
    public void generatePDF(Employee emp) {
        System.out.println("📄 Generating PDF report for: " + emp.getName());
        System.out.println("   - Name: " + emp.getName());
        System.out.println("   - Salary: ₹" + emp.getSalary());
    }

    // Excel report generate karo
    public void generateExcel(Employee emp) {
        System.out.println("📊 Generating Excel report for: " + emp.getName());
    }

    // 💡 Note: Is class ka sirf EK kaam hai - Reports generate karna
    // Agar report format change ho, sirf ye class change hogi
}

// ╔═══════════════════════════════════════════════════════════════════════════════╗
// ║  O - OPEN/CLOSED PRINCIPLE (OCP)                                             ║
// ╚═══════════════════════════════════════════════════════════════════════════════╝
/*
 * 📚 PRINCIPLE KYA HAI?
 * ---------------------
 * "Classes extension ke liye OPEN, modification ke liye CLOSED honi chahiye"
 *
 * 🤔 SIMPLE EXAMPLE SE SAMJHO:
 * ---------------------------
 * Socho ek Electric Board hai:
 *   - Naya plug add karna → Easy hai (Extension) ✅
 *   - Board todna nahi padta → No Modification ✅
 *
 * Matlab: Naye features add karne ke liye purana code CHANGE nahi karna chahiye
 *
 * 🎯 FAYDA KYA HAI?
 * -----------------
 * 1. Purana tested code safe rahega
 * 2. Naye features easily add ho sakte hain
 * 3. Bugs kam aate hain
 */

// ═══════════════════════════════════════════════════════════════════
// ❌ GALAT TARIKA (BAD EXAMPLE) - Har naye shape ke liye code modify karna
// ═══════════════════════════════════════════════════════════════════
/*
 * Problem dekho:
 *
 *    if (Circle)     → Circle ka area calculate karo
 *    else if (Rect)  → Rectangle ka area calculate karo
 *    else if (???)   → NAI SHAPE KE LIYE PHIR SE CODE CHANGE! ❌
 *
 * Har baar naya shape add karne ke liye purana code modify karna padega!
 */
class AreaCalculatorBad {
    public double calculateArea(Object shape) {
        // ❌ Har naye shape ke liye if-else add karna padega
        if (shape instanceof CircleShape) {
            CircleShape circle = (CircleShape) shape;
            return Math.PI * circle.getRadius() * circle.getRadius();
        } else if (shape instanceof RectangleShape) {
            RectangleShape rect = (RectangleShape) shape;
            return rect.getLength() * rect.getWidth();
        }
        // ❌ Triangle add karni ho toh phir se if-else add karna padega!
        // Ye MODIFICATION hai - galat approach
        return 0;
    }
}

// Helper classes for bad example (sirf demonstration ke liye)
class CircleShape {
    private double radius;
    public CircleShape(double r) { this.radius = r; }
    public double getRadius() { return radius; }
}

class RectangleShape {
    private double length, width;
    public RectangleShape(double l, double w) { this.length = l; this.width = w; }
    public double getLength() { return length; }
    public double getWidth() { return width; }
}

// ═══════════════════════════════════════════════════════════════════
// ✅ SAHI TARIKA (GOOD EXAMPLE) - Interface use karke extensible banana
// ═══════════════════════════════════════════════════════════════════
/*
 * Solution dekho:
 *
 *    ┌─────────────────────────────────────────────┐
 *    │           Shape Interface                   │
 *    │      (Common contract/rule define karo)     │
 *    │         double calculateArea()              │
 *    └─────────────────────────────────────────────┘
 *                        │
 *        ┌───────────────┼───────────────┐
 *        │               │               │
 *        ▼               ▼               ▼
 *   ┌─────────┐    ┌─────────┐    ┌─────────┐
 *   │ Circle  │    │Rectangle│    │Triangle │  ← Naya shape add karna easy!
 *   └─────────┘    └─────────┘    └─────────┘
 *
 * Ab AreaCalculator ka code KABHI change nahi hoga!
 */

// 🔷 Step 1: Interface banao - ye common contract hai
interface Shape {
    // Har shape ko ye method implement karna padega
    double calculateArea();
}

// 🔷 Step 2: Har shape apna implementation de
// Circle class
class Circle implements Shape {
    private double radius;  // Circle ka radius

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double calculateArea() {
        // Circle ka area formula: π × r²
        return Math.PI * radius * radius;
    }

    // Getter for radius (helpful for display)
    public double getRadius() {
        return radius;
    }
}

// Rectangle class
class Rectangle implements Shape {
    private double length;  // Rectangle ki length
    private double width;   // Rectangle ki width

    public Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }

    @Override
    public double calculateArea() {
        // Rectangle ka area formula: length × width
        return length * width;
    }
}

// 🆕 Triangle class - NAYA shape add karna kitna easy hai!
// Purane code ko touch nahi karna pada!
class Triangle implements Shape {
    private double base;    // Triangle ka base
    private double height;  // Triangle ki height

    public Triangle(double base, double height) {
        this.base = base;
        this.height = height;
    }

    @Override
    public double calculateArea() {
        // Triangle ka area formula: ½ × base × height
        return 0.5 * base * height;
    }
}

// 🆕 Pentagon bhi add kar sakte hain - NO MODIFICATION needed!
class Pentagon implements Shape {
    private double side;

    public Pentagon(double side) {
        this.side = side;
    }

    @Override
    public double calculateArea() {
        // Pentagon ka area formula (approximate)
        return 1.72 * side * side;
    }
}

// 🔷 Step 3: AreaCalculator - Ye code KABHI change nahi hoga
class AreaCalculator {
    // Koi bhi Shape pass karo, area mil jayega
    // Naya shape add karne par ye code change nahi hoga! ✅
    public double calculateArea(Shape shape) {
        return shape.calculateArea();
    }

    // Multiple shapes ka total area
    public double calculateTotalArea(Shape[] shapes) {
        double total = 0;
        for (Shape shape : shapes) {
            total += shape.calculateArea();
        }
        return total;
    }
}

// ╔═══════════════════════════════════════════════════════════════════════════════╗
// ║  L - LISKOV SUBSTITUTION PRINCIPLE (LSP)                                     ║
// ╚═══════════════════════════════════════════════════════════════════════════════╝
/*
 * 📚 PRINCIPLE KYA HAI?
 * ---------------------
 * "Child class ko Parent class ki jagah use kar sakte hain,
 *  aur program ka behavior same rahna chahiye"
 *
 * 🤔 SIMPLE EXAMPLE SE SAMJHO:
 * ---------------------------
 * Socho tumhare paas hai:
 *   - Remote Control (Parent) → TV on/off karta hai
 *   - Smart Remote (Child) → TV on/off + Voice control
 *
 * Agar Smart Remote, normal remote ki jagah use karo:
 *   - TV on/off hona chahiye ✅ (Same behavior)
 *   - Voice control extra feature hai ✅
 *   - But TV on/off FAIL ho jaye ❌ (Ye GALAT hai!)
 *
 * 🎯 FAYDA KYA HAI?
 * -----------------
 * 1. Code predictable hota hai
 * 2. Polymorphism sahi se kaam karta hai
 * 3. Bugs kam aate hain
 */

// ═══════════════════════════════════════════════════════════════════
// ❌ GALAT TARIKA (BAD EXAMPLE) - Square Rectangle extend karta hai
// ═══════════════════════════════════════════════════════════════════
/*
 * Problem: Square "IS A" Rectangle lagta hai mathematically,
 * But programming mein ye galat hai!
 *
 *    Rectangle:
 *    ┌──────────────┐     Width = 5
 *    │              │     Height = 4
 *    │              │     Area = 5 × 4 = 20
 *    └──────────────┘
 *
 *    Square (sab sides equal):
 *    ┌──────┐               Side = 5
 *    │      │               setWidth(5) → Width=5, Height=5
 *    │      │               setHeight(4) → Width=4, Height=4 ← PROBLEM!
 *    └──────┘               Area = 4 × 4 = 16 (Expected: 20)
 *
 *    Ye UNEXPECTED behavior hai! LSP VIOLATION!
 */
class RectangleBad {
    protected int width;
    protected int height;

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getArea() {
        return width * height;
    }
}

// ❌ Square extends Rectangle - GALAT approach!
class SquareBad extends RectangleBad {

    @Override
    public void setWidth(int width) {
        // Problem: Square mein width aur height SAME hone chahiye
        this.width = width;
        this.height = width; // ❌ Unexpected! Parent ki contract tod raha hai
    }

    @Override
    public void setHeight(int height) {
        this.width = height;  // ❌ Unexpected!
        this.height = height;
    }
}

// Test class - Ye fail ho jayega Square ke saath
class TestLSPBad {
    public void testRectangle(RectangleBad rect) {
        rect.setWidth(5);
        rect.setHeight(4);

        int area = rect.getArea();

        // Expected area = 5 × 4 = 20
        // ✅ Rectangle pass kiya → Area = 20 (CORRECT)
        // ❌ Square pass kiya → Area = 4 × 4 = 16 (WRONG!)

        // Ye LSP violation hai! Child class ne Parent ka behavior break kiya!
        System.out.println("Area: " + area + " (Expected: 20)");
    }
}

// ═══════════════════════════════════════════════════════════════════
// ✅ SAHI TARIKA (GOOD EXAMPLE) - Separate abstractions banana
// ═══════════════════════════════════════════════════════════════════
/*
 * Solution: Rectangle aur Square ko separate rakho!
 *
 *    ┌─────────────────────────────────────┐
 *    │         ShapeWithArea Interface     │
 *    │            int getArea()            │
 *    └─────────────────────────────────────┘
 *                      │
 *          ┌───────────┴───────────┐
 *          │                       │
 *          ▼                       ▼
 *    ┌─────────────┐        ┌─────────────┐
 *    │RectangleGood│        │ SquareGood  │
 *    │ width,height│        │    side     │
 *    └─────────────┘        └─────────────┘
 *
 *    Ab dono independently kaam karenge! ✅
 */

// Common interface for shapes with area
interface ShapeWithArea {
    int getArea();
}

// Rectangle - apni width aur height hai
class RectangleGood implements ShapeWithArea {
    private int width;
    private int height;

    public RectangleGood(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getArea() {
        return width * height;
    }
}

// Square - apna alag side hai, Rectangle se INDEPENDENT
class SquareGood implements ShapeWithArea {
    private int side;

    public SquareGood(int side) {
        this.side = side;
    }

    public void setSide(int side) {
        this.side = side;  // Sirf ek method, koi confusion nahi
    }

    @Override
    public int getArea() {
        return side * side;
    }
}

// 💡 Ab dono shapes independently kaam karenge
// Koi unexpected behavior nahi hoga!

// ╔═══════════════════════════════════════════════════════════════════════════════╗
// ║  I - INTERFACE SEGREGATION PRINCIPLE (ISP)                                   ║
// ╚═══════════════════════════════════════════════════════════════════════════════╝
/*
 * 📚 PRINCIPLE KYA HAI?
 * ---------------------
 * "Clients ko unnecessary methods implement karne par force NAHI karna chahiye"
 * "Bade interface ko chhote-chhote specific interfaces mein todo"
 *
 * 🤔 SIMPLE EXAMPLE SE SAMJHO:
 * ---------------------------
 * Socho ek "All-in-One Machine" hai:
 *   - Print karta hai
 *   - Scan karta hai
 *   - Fax karta hai
 *   - Copy karta hai
 *
 * Ab agar tumhe SIRF printer chahiye:
 *   ❌ Scan, Fax, Copy features bhi lene padenge (Wasteful!)
 *   ✅ Better: Sirf Printer interface lo
 *
 * 🎯 FAYDA KYA HAI?
 * -----------------
 * 1. Classes lightweight hoti hain
 * 2. Sirf jo chahiye wo implement karo
 * 3. Code maintainable hota hai
 */

// ═══════════════════════════════════════════════════════════════════
// ❌ GALAT TARIKA (BAD EXAMPLE) - Ek bada "Fat" interface
// ═══════════════════════════════════════════════════════════════════
/*
 *    ┌─────────────────────────────────────────────┐
 *    │           WorkerBad Interface               │
 *    │   ┌───────────────────────────────────┐     │
 *    │   │  work()  - Kaam karo              │     │
 *    │   │  eat()   - Khana khao             │     │
 *    │   │  sleep() - So jao                 │     │
 *    │   └───────────────────────────────────┘     │
 *    │   (Sab kuch ek jagah - GALAT!)              │
 *    └─────────────────────────────────────────────┘
 *
 *    Problem: Robot ko eat() aur sleep() ki zarurat NAHI hai
 *    But implement karna padega! ❌
 */
interface WorkerBad {
    void work();    // Kaam karo
    void eat();     // Khana khao
    void sleep();   // So jao
}

// Human worker ke liye sab methods useful hain - THEEK hai
class HumanWorkerBad implements WorkerBad {
    @Override
    public void work() {
        System.out.println("👨‍💼 Human is working");
    }

    @Override
    public void eat() {
        System.out.println("🍽️ Human is eating");
    }

    @Override
    public void sleep() {
        System.out.println("😴 Human is sleeping");
    }
}

// ❌ Problem: Robot ko eat() aur sleep() ki ZARURAT NAHI
// But interface force kar raha hai implement karne ke liye!
class RobotWorkerBad implements WorkerBad {
    @Override
    public void work() {
        System.out.println("🤖 Robot is working");
    }

    @Override
    public void eat() {
        // ❌ Robot nahi khata - UNNECESSARY method!
        throw new UnsupportedOperationException("Robot doesn't eat!");
    }

    @Override
    public void sleep() {
        // ❌ Robot nahi sota - UNNECESSARY method!
        throw new UnsupportedOperationException("Robot doesn't sleep!");
    }
}

// ═══════════════════════════════════════════════════════════════════
// ✅ SAHI TARIKA (GOOD EXAMPLE) - Chhote aur specific interfaces
// ═══════════════════════════════════════════════════════════════════
/*
 * Solution: Bade interface ko todo chhote-chhote mein!
 *
 *    ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
 *    │  Workable    │   │   Eatable    │   │  Sleepable   │
 *    │   work()     │   │    eat()     │   │   sleep()    │
 *    └──────────────┘   └──────────────┘   └──────────────┘
 *           │                  │                  │
 *           │                  │                  │
 *    ┌──────┴──────────────────┴──────────────────┴──────┐
 *    │                  HumanWorker                       │
 *    │            implements ALL THREE ✅                 │
 *    └───────────────────────────────────────────────────┘
 *
 *    ┌──────────────┐
 *    │ RobotWorker  │
 *    │  implements  │
 *    │  Workable    │  ← Sirf jo chahiye wo lo! ✅
 *    │    ONLY!     │
 *    └──────────────┘
 */

// Chhota Interface 1: Sirf kaam ke liye
interface Workable {
    void work();
}

// Chhota Interface 2: Sirf khana khane ke liye
interface Eatable {
    void eat();
}

// Chhota Interface 3: Sirf sone ke liye
interface Sleepable {
    void sleep();
}

// ✅ Human worker - Jo chahiye wo interfaces implement karo
class HumanWorker implements Workable, Eatable, Sleepable {
    @Override
    public void work() {
        System.out.println("👨‍💼 Human is working hard in office");
    }

    @Override
    public void eat() {
        System.out.println("🍽️ Human is eating lunch");
    }

    @Override
    public void sleep() {
        System.out.println("😴 Human is sleeping at night");
    }
}

// ✅ Robot worker - Sirf Workable implement karo (Eat aur Sleep ki zarurat nahi!)
class RobotWorker implements Workable {
    @Override
    public void work() {
        System.out.println("🤖 Robot is working 24/7 without break");
    }
    // ✅ Eat aur sleep implement karne ki ZARURAT NAHI!
    // Clean code, no unnecessary methods!
}

// ✅ Manager - Sirf Eatable aur Sleepable implement karo (mazak hai! 😄)
class Manager implements Eatable, Sleepable {
    @Override
    public void eat() {
        System.out.println("🍕 Manager is having a business lunch");
    }

    @Override
    public void sleep() {
        System.out.println("💤 Manager is taking a power nap");
    }
    // Workable implement nahi kiya - kyunki manager kaam nahi karta! 😜
}

// ╔═══════════════════════════════════════════════════════════════════════════════╗
// ║  D - DEPENDENCY INVERSION PRINCIPLE (DIP)                                    ║
// ╚═══════════════════════════════════════════════════════════════════════════════╝
/*
 * 📚 PRINCIPLE KYA HAI?
 * ---------------------
 * "High-level modules ko low-level modules par DIRECTLY depend nahi karna chahiye"
 * "Dono ko abstractions (interfaces) par depend karna chahiye"
 *
 * 🤔 SIMPLE EXAMPLE SE SAMJHO:
 * ---------------------------
 * Socho ek Mobile Charger hai:
 *
 *   ❌ GALAT: Charger directly phone ke andar connected ho
 *      (Agar phone change karo, charger bhi change karna padega!)
 *
 *   ✅ SAHI: Charger USB port se connect ho (Interface)
 *      (Koi bhi phone connect kar sakte ho!)
 *
 * USB Port = Interface (Abstraction)
 * Charger = High-level module
 * Phone = Low-level module
 *
 * 🎯 FAYDA KYA HAI?
 * -----------------
 * 1. Loose coupling - Components easily replace ho sakte hain
 * 2. Testing easy hoti hai (Mock objects use kar sakte hain)
 * 3. Code flexible aur maintainable hota hai
 */

// ═══════════════════════════════════════════════════════════════════
// ❌ GALAT TARIKA (BAD EXAMPLE) - Direct dependency (Tight Coupling)
// ═══════════════════════════════════════════════════════════════════
/*
 *    ┌─────────────────────────────────────────────┐
 *    │           EmailServiceBad                   │
 *    │  ┌───────────────────────────────────────┐  │
 *    │  │  MySQLDatabaseBad database = new...   │  │  ← TIGHT COUPLING!
 *    │  │  (Directly MySQL par depend hai)      │  │
 *    │  └───────────────────────────────────────┘  │
 *    └─────────────────────────────────────────────┘
 *
 *    Problem: Agar MongoDB use karna ho, toh EmailServiceBad
 *    ka code change karna padega! ❌
 */

// Low-level module (Concrete class)
class MySQLDatabaseBad {
    public void save(String data) {
        System.out.println("💾 Saving to MySQL: " + data);
    }
}

// ❌ High-level module DIRECTLY low-level par depend hai
class EmailServiceBad {
    // ❌ Direct dependency on MySQLDatabaseBad (Concrete class)
    private MySQLDatabaseBad database;

    public EmailServiceBad() {
        // ❌ TIGHT COUPLING! Object yahan create ho raha hai
        this.database = new MySQLDatabaseBad();
    }

    public void sendEmail(String message) {
        System.out.println("📧 Sending email: " + message);
        database.save(message);
    }

    /*
     * 🚫 IS APPROACH KI PROBLEMS:
     * ---------------------------
     * 1. Agar MongoDB use karna ho → EmailServiceBad change karna padega
     * 2. Testing mushkil hai (Real database lagega test mein bhi)
     * 3. Code tightly coupled hai
     */
}

// ═══════════════════════════════════════════════════════════════════
// ✅ SAHI TARIKA (GOOD EXAMPLE) - Interface-based dependency (Loose Coupling)
// ═══════════════════════════════════════════════════════════════════
/*
 * Solution: Interface use karo!
 *
 *    ┌─────────────────────────────────────────────┐
 *    │              Database Interface             │  ← ABSTRACTION
 *    │               void save(data)               │
 *    └─────────────────────────────────────────────┘
 *                         │
 *         ┌───────────────┼───────────────┐
 *         │               │               │
 *         ▼               ▼               ▼
 *    ┌─────────┐    ┌─────────┐    ┌──────────┐
 *    │  MySQL  │    │ MongoDB │    │PostgreSQL│  ← Implementations
 *    └─────────┘    └─────────┘    └──────────┘
 *
 *    ┌─────────────────────────────────────────────┐
 *    │            EmailService                     │
 *    │  ┌───────────────────────────────────────┐  │
 *    │  │  Database database (Interface)        │  │  ← LOOSE COUPLING!
 *    │  │  (Koi bhi database inject kar sakte)  │  │
 *    │  └───────────────────────────────────────┘  │
 *    └─────────────────────────────────────────────┘
 */

// 🔷 Step 1: Interface banao (Abstraction layer)
interface Database {
    void save(String data);  // Contract define karo
    void delete(String data);
}

// 🔷 Step 2: Different implementations banao
// MySQL Database implementation
class MySQLDatabase implements Database {
    @Override
    public void save(String data) {
        System.out.println("💾 [MySQL] Saving data: " + data);
    }

    @Override
    public void delete(String data) {
        System.out.println("🗑️ [MySQL] Deleting data: " + data);
    }
}

// MongoDB Database implementation
class MongoDBDatabase implements Database {
    @Override
    public void save(String data) {
        System.out.println("🍃 [MongoDB] Saving document: " + data);
    }

    @Override
    public void delete(String data) {
        System.out.println("🗑️ [MongoDB] Deleting document: " + data);
    }
}

// PostgreSQL Database implementation
class PostgreSQLDatabase implements Database {
    @Override
    public void save(String data) {
        System.out.println("🐘 [PostgreSQL] Saving record: " + data);
    }

    @Override
    public void delete(String data) {
        System.out.println("🗑️ [PostgreSQL] Deleting record: " + data);
    }
}

// 🔷 Step 3: High-level module ab INTERFACE par depend karta hai
class EmailService {
    // ✅ Interface par depend hai, concrete class par NAHI
    private Database database;

    // ✅ CONSTRUCTOR INJECTION - Database bahar se inject hota hai
    // Ye "Dependency Injection" ka example hai
    public EmailService(Database database) {
        this.database = database;
    }

    public void sendEmail(String message) {
        System.out.println("📧 Sending email: " + message);
        database.save("Email: " + message);  // Koi bhi database use ho sakta hai!
    }

    /*
     * ✅ IS APPROACH KE FAYDE:
     * ------------------------
     * 1. Database easily change ho sakta hai (MySQL → MongoDB)
     * 2. Testing easy hai (Mock database inject kar sakte hain)
     * 3. Code loosely coupled hai
     */
}

// 🔷 Step 4: Notification service bhi same pattern follow karti hai
class NotificationService {
    private Database database;

    // Constructor injection
    public NotificationService(Database database) {
        this.database = database;
    }

    public void sendNotification(String notification) {
        System.out.println("🔔 Sending notification: " + notification);
        database.save("Notification: " + notification);
    }

    public void sendPushNotification(String title, String body) {
        System.out.println("📱 Push: " + title + " - " + body);
        database.save("Push: " + title);
    }
}

// ╔═══════════════════════════════════════════════════════════════════════════════╗
// ║  MAIN CLASS - SAB PRINCIPLES KO TEST KARNE KE LIYE                           ║
// ╚═══════════════════════════════════════════════════════════════════════════════╝
/*
 * 💻 Is class ko run karo aur dekho sab SOLID principles kaise kaam karte hain!
 *
 * Command to run:
 *   javac SOLID.java
 *   java SOLIDPrinciplesDemo
 */
class SOLIDPrinciplesDemo {
    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           🎯 SOLID PRINCIPLES - LIVE DEMO 🎯                ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // ═══════════════════════════════════════════════════════════════
        // 1️⃣ SINGLE RESPONSIBILITY PRINCIPLE (SRP)
        // ═══════════════════════════════════════════════════════════════
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│  1️⃣  SINGLE RESPONSIBILITY PRINCIPLE                        │");
        System.out.println("│      'Ek class ka sirf ek hi kaam hona chahiye'             │");
        System.out.println("└──────────────────────────────────────────────────────────────┘");

        // Har class ka alag kaam hai
        Employee emp = new Employee("Rahul Kumar", 50000);
        SalaryCalculator calculator = new SalaryCalculator();
        EmployeeRepository repo = new EmployeeRepository();
        EmployeeReportGenerator reportGen = new EmployeeReportGenerator();

        System.out.println("📋 Employee: " + emp.getName());
        System.out.println("💰 Monthly Salary: ₹" + emp.getSalary());
        System.out.println("💵 Annual Salary: ₹" + calculator.calculateAnnualSalary(emp));
        System.out.println("📊 Tax (20%): ₹" + calculator.calculateTax(emp));
        System.out.println("🎁 Bonus (10%): ₹" + calculator.calculateBonus(emp));
        repo.saveEmployee(emp);
        reportGen.generatePDF(emp);
        System.out.println();

        // ═══════════════════════════════════════════════════════════════
        // 2️⃣ OPEN/CLOSED PRINCIPLE (OCP)
        // ═══════════════════════════════════════════════════════════════
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│  2️⃣  OPEN/CLOSED PRINCIPLE                                  │");
        System.out.println("│      'Extension ke liye OPEN, Modification ke liye CLOSED' │");
        System.out.println("└──────────────────────────────────────────────────────────────┘");

        AreaCalculator areaCalc = new AreaCalculator();

        // Different shapes - sab Shape interface implement karte hain
        Shape circle = new Circle(5);
        Shape rectangle = new Rectangle(4, 6);
        Shape triangle = new Triangle(3, 4);
        Shape pentagon = new Pentagon(3);

        System.out.println("⭕ Circle (radius=5) Area: " + String.format("%.2f", areaCalc.calculateArea(circle)));
        System.out.println("▭  Rectangle (4×6) Area: " + String.format("%.2f", areaCalc.calculateArea(rectangle)));
        System.out.println("△  Triangle (base=3, height=4) Area: " + String.format("%.2f", areaCalc.calculateArea(triangle)));
        System.out.println("⬠  Pentagon (side=3) Area: " + String.format("%.2f", areaCalc.calculateArea(pentagon)));

        // Multiple shapes ka total area
        Shape[] allShapes = {circle, rectangle, triangle, pentagon};
        System.out.println("📐 Total Area of all shapes: " + String.format("%.2f", areaCalc.calculateTotalArea(allShapes)));
        System.out.println();

        // ═══════════════════════════════════════════════════════════════
        // 3️⃣ LISKOV SUBSTITUTION PRINCIPLE (LSP)
        // ═══════════════════════════════════════════════════════════════
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│  3️⃣  LISKOV SUBSTITUTION PRINCIPLE                          │");
        System.out.println("│      'Child class Parent ki jagah use ho sake'              │");
        System.out.println("└──────────────────────────────────────────────────────────────┘");

        // Dono ShapeWithArea interface implement karte hain
        ShapeWithArea rectGood = new RectangleGood(5, 4);
        ShapeWithArea squareGood = new SquareGood(5);

        System.out.println("▭  Rectangle (5×4) Area: " + rectGood.getArea());
        System.out.println("▢  Square (side=5) Area: " + squareGood.getArea());

        // LSP Bad Example (Warning!)
        System.out.println("\n⚠️  LSP Violation Example:");
        TestLSPBad testBad = new TestLSPBad();
        System.out.print("   Rectangle: ");
        testBad.testRectangle(new RectangleBad());
        System.out.print("   Square (PROBLEM!): ");
        testBad.testRectangle(new SquareBad());  // Ye galat result dega!
        System.out.println();

        // ═══════════════════════════════════════════════════════════════
        // 4️⃣ INTERFACE SEGREGATION PRINCIPLE (ISP)
        // ═══════════════════════════════════════════════════════════════
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│  4️⃣  INTERFACE SEGREGATION PRINCIPLE                        │");
        System.out.println("│      'Chhote specific interfaces use karo'                  │");
        System.out.println("└──────────────────────────────────────────────────────────────┘");

        HumanWorker human = new HumanWorker();
        RobotWorker robot = new RobotWorker();
        Manager manager = new Manager();

        System.out.println("👨‍💼 Human Worker (implements Workable, Eatable, Sleepable):");
        human.work();
        human.eat();
        human.sleep();

        System.out.println("\n🤖 Robot Worker (implements ONLY Workable):");
        robot.work();
        System.out.println("   (Robot ko eat/sleep ki zarurat nahi!)");

        System.out.println("\n👔 Manager (implements Eatable, Sleepable - No Workable! 😜):");
        manager.eat();
        manager.sleep();
        System.out.println();

        // ═══════════════════════════════════════════════════════════════
        // 5️⃣ DEPENDENCY INVERSION PRINCIPLE (DIP)
        // ═══════════════════════════════════════════════════════════════
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│  5️⃣  DEPENDENCY INVERSION PRINCIPLE                         │");
        System.out.println("│      'Interface par depend karo, concrete class par nahi'   │");
        System.out.println("└──────────────────────────────────────────────────────────────┘");

        // Same EmailService, different databases!
        System.out.println("📧 EmailService with MySQL:");
        Database mysqlDB = new MySQLDatabase();
        EmailService emailMySQL = new EmailService(mysqlDB);
        emailMySQL.sendEmail("Hello from MySQL setup!");

        System.out.println("\n📧 EmailService with MongoDB (NO CODE CHANGE!):");
        Database mongoDB = new MongoDBDatabase();
        EmailService emailMongo = new EmailService(mongoDB);
        emailMongo.sendEmail("Hello from MongoDB setup!");

        System.out.println("\n🔔 NotificationService with PostgreSQL:");
        Database postgresDB = new PostgreSQLDatabase();
        NotificationService notifService = new NotificationService(postgresDB);
        notifService.sendNotification("Welcome to SOLID Principles!");
        notifService.sendPushNotification("New Message", "You have learned DIP!");

        // ═══════════════════════════════════════════════════════════════
        // SUMMARY
        // ═══════════════════════════════════════════════════════════════
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    📝 QUICK SUMMARY                          ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  S - Single Responsibility  → Ek class = Ek kaam            ║");
        System.out.println("║  O - Open/Closed           → Extend karo, modify mat karo   ║");
        System.out.println("║  L - Liskov Substitution   → Child = Parent replacement     ║");
        System.out.println("║  I - Interface Segregation → Chhote interfaces better       ║");
        System.out.println("║  D - Dependency Inversion  → Interface par depend karo      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println("\n🎉 Congratulations! Aapne SOLID Principles seekh liye! 🎉");
    }
}
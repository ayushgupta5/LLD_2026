/*

Design a simple Student Management system backend using Spring Boot where the system should allow creating a student, fetching all students, fetching a student by ID, and optionally filtering students by name using query parameters.
Explain how you would structure the project (packages and layers), design the database schema, authentication, logging etc. You will also write high-level code snippets to support your explanation.

Functional Requirements:-
Clasess:-

DB Schema
Student
    studentID;
    studentName;
    studentEmail;
    studentPhoneNumber;
    studentClass;
    studentSchoolName;

    // Getter and setter and If needed Constructore as well

class className {
}

CRUD Operations:-
POST /student/create --> creating a student
GET /student/fetch --> fetching all students[]
GET /student/fetch/{student_id} --> fetching a student by ID
    param {student_name} --> Optional
GET /student/fetch/

Packages:-
    Model
        class Student

    Controller
        Actual Logic (CRUD)
            fetchByID() {
                // actual Logic of fetching the student from the DB
            }

    RepositoryLayer
        dataBase Schema

    View
        studentController.fetchByID();


DB Schema
Student Table
    student_id;
    student_name;
    student_email;
    student_phone_number;
    student_class;
    student_school_name;
    created_at;
    created_by;
    updated_at;
    updated_by;


Course
    course_id;
    course_name;
    created_at;
    created_by;
    updated_at;
    updated_by;



LLD Part:-

Create
Update
delete
Fetch

Enroll()
Notification()


Fetch 10Th Marksheet:-
Millions of User
HLD:-
Fetching the info

write
Read:

It will be a Read Heavy

write:read --> 1:15

client --> LB --> School Service (3-5) --> Redis Cache (LRU, LFU Cache Eviction Policy)--> DB (Sql) + (Read Replica)

DB Read Replica

Marksheet:
enrollment_id(Indexing)
DOB (indexing)
Name (indexing)

Name
Father'sName
Mother's Name

Marks {
    PCMEH
}

Grafana DashBoard
whats is the value of the Following
1. CPU Utilization
2. Memory Utilization
3. Erroe %
4. DB Load Error %

1. Vertical Scaling (Ram --> 1 GB --> 4 GB | CPU --> 500m --> 2000m) --> It Has a Limit
2. Horizontal Scaling (minReplica:3 and maxReplica: 5, CPU Utilization: 60)


*/

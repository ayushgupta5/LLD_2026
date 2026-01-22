import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

enum Status {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
abstract class Task {
    private int taskID;
    private  int userID;
    private String description;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
class SequentialTask extends Task {
    private int sequenceNumber;
    private List<Task> subTasks;
}
class ParallelTask extends Task {
    private List<Task> parallelTasks;
}
class WorkflowEngine {
    private HashMap<String, Workflow> workflowDatabase = new HashMap<>();
    ExecutorService executorService = Executors.newFixedThreadPool(10);
}
interface IWorkflowEngineService {
    void executeWorkflow(Workflow workflow);
    void getWorkflowStatus(int workflowID);
}
class WorkflowEngineService implements  IWorkflowEngineService {

    @Override
    public void executeWorkflow(Workflow workflow) {

    }

    @Override
    public void getWorkflowStatus(int workflowID) {

    }
}
class Workflow {
    private int workflowID;
    private int userID;
    private String name;
    private List<Task> tasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
interface ITaskManager {
    void createTask(Task task);
    Task getTask(int taskID);
    void updateTaskStatus(int taskID, Status newStatus);
    void deleteTask(int taskID);
}
class TaskManager implements ITaskManager {
    private HashMap<Integer, Task> taskDatabase = new HashMap<>();

    @Override
    public void createTask(Task task) {

    }

    @Override
    public Task getTask(int taskID) {
        return null;
    }

    @Override
    public void updateTaskStatus(int taskID, Status newStatus) {

    }

    @Override
    public void deleteTask(int taskID) {

    }
}

class DataContext {
    private HashMap<String, Object> data = new HashMap<>();

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }
}
public class WorkflowManagementInterface {
    public static void main(String[] args) {
        System.out.println("Workflow Management Interface");
    }
}

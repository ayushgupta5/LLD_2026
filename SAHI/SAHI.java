import java.util.*;

/*
We have a list of tasks.
Some of the tasks can be dependent on each other.
It is guaranteed to be a Directly Acyclic Graph.
We need to execute all the tasks as fast as possible (serial and parallel)
A task should not be executed if it’s dependencies are not executed yet
A task should not be executed if any of it’s dependencies have failed.
A task can have some logic on using responses of it’s dependencies.
For example:
Task1: Read Input
Task2: Process Input and do some data manipulation
dependency on Task1
Task3: Take input from Task2 and call Service A
dependency on Task2
Task4: Take input from Task2 and call Service B
dependency on Task2
Task5: Create Response after merging response from Task3 and Task4
dependency on Task3, Task4
 */
class Task {
    String id;
    List<String> dependencies;

    public Task(String id, List<String> dependencies) {
        this.id = id;
        this.dependencies = dependencies;
    }
    void run () {
        System.out.println("Running Task");
    }
}
class Executor {
    private Map<String, List<String>> adj = new HashMap<>();
    private Map<String, Integer> indegree = new HashMap<>();
    private Map<String, Task> taskDB = new HashMap<>();
    public Executor (List<Task> tasks) {
        for(Task task : tasks) {
            taskDB.put(task.id, task);
            indegree.put(task.id, 0);
        }
        for(Task task : tasks) {
            for(String list : task.dependencies) {
                indegree.put(task.id, indegree.get(task.id) + 1);
            }
        }
    }
    // Topological Sort
    public void execute() {
        Queue<String> queue = new LinkedList<>();
        for(String node : indegree.keySet()) {
            if(indegree.get(node) == 0) {
                queue.add(node);
            }
        }
        while(!queue.isEmpty()) {
            String node= queue.poll();

            System.out.println(node);

            for(String adjNode: adj.getOrDefault(node, null)) {
                indegree.put(adjNode, indegree.get(adjNode) - 1);
                if (indegree.get(adjNode) == 0) {
                    queue.add(adjNode);
                }
            }
        }
    }
}
public class SAHI {
}

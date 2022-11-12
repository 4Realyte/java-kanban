import java.util.HashMap;

public class TaskManager {
    HashMap<Integer, Task> tasks;
    HashMap<Integer, Epic> epics;
    HashMap<Integer, SubTask> subTasks;

    public TaskManager() {
        subTasks = new HashMap<>();
        epics = new HashMap<>();
        tasks = new HashMap<>();
    }

    public void clearAllTasks() {
        if (!tasks.isEmpty() || !subTasks.isEmpty() || !epics.isEmpty()) {
            tasks.clear();
            subTasks.clear();
            epics.clear();
        }
    }

    public void addTask(Task task) {
        if (task.getClass() == Task.class) {
            tasks.put(task.getId(), task);
        } else if (task.getClass() == SubTask.class) {
            subTasks.put(task.getId(), (SubTask) task);
        } else if (task.getClass() == Epic.class) {
            epics.put(task.getId(), (Epic) task);
        } else {
            System.out.println("Задач такого типа пока нет");
        }
    }

    public Epic getEpicById(Integer id) {
        Epic epic = null;
        if (epics.containsKey(id)) {
            epic = epics.get(id);
        } else {
            System.out.println("Задачи с таким ID нет в списке");
        }
        return epic;
    }

    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public HashMap<Integer, SubTask> getSubTasks() {
        return subTasks;
    }

}

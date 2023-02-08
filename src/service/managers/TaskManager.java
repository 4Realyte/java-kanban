package service.managers;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public interface TaskManager {
    HashMap<Integer, SubTask> getAllEpicSubTasks(Epic epic);

    void removeTask(Task task);

    void removeSubTask(SubTask subTask);

    void removeEpic(Epic epic);

    void updateTask(Task task);

    void setStatus(Task task, TaskStatus status);

    <T extends Task> T createTask(T task);

    void clearAllTasks();

    void clearAllEpics();

    void clearAllSubTasks();

    List<Task> getHistory();

    Epic getEpicById(int id);

    Task getTaskById(int id);

    SubTask getSubTaskById(int id);

    HashMap<Integer, Task> getTasks();

    HashMap<Integer, Epic> getEpics();

    HashMap<Integer, SubTask> getSubTasks();
    HistoryManager getHistoryManager();
    TreeSet<Task> getPrioritizedTasks();
}

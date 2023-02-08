package service.managers;

import model.*;
import service.managers.supportServices.ManagerCreateException;
import service.utilites.Managers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    HashMap<Integer, Task> tasks;
    HashMap<Integer, Epic> epics;
    HashMap<Integer, SubTask> subTasks;
    TreeSet<Task> tasksSet;
    protected static AtomicInteger idCounter;

    HistoryManager historyManager = Managers.getDefaultHistory();

    public InMemoryTaskManager() {
        subTasks = new HashMap<>();
        epics = new HashMap<>();
        tasks = new HashMap<>();
        idCounter = new AtomicInteger(0);
        tasksSet = new TreeSet<>(Comparator.comparing(Task::getStartTime,
                Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Task::getId));
    }

    @Override
    public HashMap<Integer, SubTask> getAllEpicSubTasks(Epic epic) {
        return epic.getEpicSubs();
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) {
            System.out.println("Задачи с таким ID не существует");
        } else {
            switch (task.getType()) {
                case TASK:
                    Task taskFromMap = tasks.get(task.getId());
                    if (!taskFromMap.equals(task)) {
                        tasks.put(task.getId(), task);
                    } else {
                        System.out.println("Задачи идентичны, нечего обновлять");
                    }
                    break;
                case SUBTASK:
                    SubTask subTask = (SubTask) task;
                    SubTask subTaskFromMap = subTasks.get(subTask.getId());
                    if (!subTaskFromMap.equals(subTask)) {
                        subTasks.put(subTask.getId(), subTask);
                        updateTask(epics.get(subTask.getEpicId()));
                    } else {
                        System.out.println("Подзадачи идентичны, нечего обновлять");
                    }
                    break;
                case EPIC:
                    Epic epic = (Epic) task;
                    tasksSet.remove(epic);
                    if (epic.getEpicSubs().isEmpty() || subTasks.isEmpty()) {
                        epic.setStatus(TaskStatus.NEW);
                    } else {
                        int doneCounter = 0;
                        int newCounter = 0;
                        for (SubTask sub : epic.getEpicSubs().values()) {
                            if (sub.getStatus() == TaskStatus.DONE) {
                                doneCounter++;
                            } else if (sub.getStatus() == TaskStatus.NEW) {
                                newCounter++;
                            }
                        }
                        if (doneCounter == epic.getEpicSubs().size()) {
                            epic.setStatus(TaskStatus.DONE);
                        } else if (newCounter == epic.getEpicSubs().size()) {
                            epic.setStatus(TaskStatus.NEW);
                        } else {
                            epic.setStatus(TaskStatus.IN_PROGRESS);
                        }
                    }
                    epic.initilizeTime();
                    tasksSet.add(epic);
                    epics.put(epic.getId(), epic);
            }
        }
    }

    private boolean validateTimeIntersection(Task task) {
        if (task.getStartTime() != null) {
            return getPrioritizedTasks()
                    .stream()
                    .filter(task1 -> task1.getStartTime() != null)
                    .anyMatch(task1 -> task1.getEndTime().isAfter(task.getStartTime())
                            && task1.getEndTime().isBefore(task.getEndTime()));
        }
        return false;
    }

    public TreeSet<Task> getPrioritizedTasks() {
        return tasksSet;
    }

    @Override
    public <T extends Task> T createTask(T task) {
        if (validateTimeIntersection(task)) {
            throw new ManagerCreateException("Задачи пересекаются по времени");
        }
        switch (task.getType()) {
            case TASK:
                task.setId(idCounter.incrementAndGet());
                tasks.put(task.getId(), task);
                tasksSet.add(task);
                return task;
            case SUBTASK:
                SubTask subTask = (SubTask) task;
                subTask.setId(idCounter.incrementAndGet());
                subTasks.put(subTask.getId(), subTask);
                tasksSet.add(subTask);
                if (epics.containsKey(subTask.getEpicId())) {
                    Epic epic = epics.get(subTask.getEpicId());
                    epic.getEpicSubs().put(subTask.getId(), subTask);
                    epic.initilizeTime();
                }
                return (T) subTask;
            case EPIC:
                Epic epic = (Epic) task;
                epic.setId(idCounter.incrementAndGet());
                epics.put(epic.getId(), epic);
                tasksSet.add(epic);
                return (T) epic;
            default:
                return null;
        }
    }

    public <T extends Task> T createTaskFromSource(T task) {
        if (validateTimeIntersection(task)) {
            throw new ManagerCreateException("Задачи пересекаются по времени");
        }
        switch (task.getType()) {
            case TASK:
                tasks.put(task.getId(), task);
                tasksSet.add(task);
                return task;
            case SUBTASK:
                SubTask subTask = (SubTask) task;
                subTasks.put(subTask.getId(), subTask);
                tasksSet.add(subTask);
                if (epics.containsKey(subTask.getEpicId())) {
                    Epic epic = epics.get(subTask.getEpicId());
                    epic.getEpicSubs().put(subTask.getId(), subTask);
                    epic.initilizeTime();
                }
                return (T) subTask;
            case EPIC:
                Epic epic = (Epic) task;
                epics.put(epic.getId(), epic);
                tasksSet.add(epic);
                return (T) epic;
            default:
                return null;
        }
    }

    @Override
    public void removeTask(Task task) {
        if (tasks.containsValue(task)) {
            tasks.remove(task.getId());
            historyManager.remove(task.getId());
            tasksSet.remove(task);
            idCounter.decrementAndGet();
        }
    }

    @Override
    public void removeSubTask(SubTask subTask) {
        if (subTasks.containsValue(subTask)) {
            subTasks.remove(subTask.getId());
            historyManager.remove(subTask.getId());
            Epic epic = epics.get(subTask.getEpicId());
            epic.getEpicSubs().remove(subTask.getId());
            tasksSet.remove(subTask);
            idCounter.decrementAndGet();
            updateTask(epic);
        }
    }

    @Override
    public void removeEpic(Epic epic) {
        if (epics.containsValue(epic)) {
            epic.getEpicSubs().values()
                    .stream()
                    .collect(Collectors.toList())
                    .forEach(subTask -> removeSubTask(subTask));

            historyManager.remove(epic.getId());
            epics.remove(epic.getId());
            tasksSet.remove(epic);
            idCounter.decrementAndGet();
        }
    }

    @Override
    public void setStatus(Task task, TaskStatus status) {
        tasksSet.remove(task);
        task.setStatus(status);
        tasksSet.add(task);
        if (task.getType() == TasksType.SUBTASK) {
            SubTask subTask = (SubTask) task;
            updateTask(epics.get(subTask.getEpicId()));
        }
    }


    @Override
    public void clearAllTasks() {
        if (!tasks.isEmpty()) {
            tasks.values()
                    .stream()
                    .collect(Collectors.toList())
                    .stream()
                    .forEach(task -> removeTask(task));
        }
    }

    @Override
    public void clearAllEpics() {
        if (!epics.isEmpty()) {
            epics.values()
                    .stream()
                    .collect(Collectors.toList())
                    .stream()
                    .forEach(epic -> removeEpic(epic));
        }
    }

    @Override
    public void clearAllSubTasks() {
        if (!subTasks.isEmpty()) {
            epics.values().stream()
                    .forEach(epic -> epic.getEpicSubs().values().stream()
                            .collect(Collectors.toList())
                            .forEach(subTask -> removeSubTask(subTask)));
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = null;
        if (epics.containsKey(id)) {
            epic = epics.get(id);
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = null;
        if (tasks.containsKey(id)) {
            task = tasks.get(id);
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public SubTask getSubTaskById(int id) {
        SubTask subTask = null;
        if (subTasks.containsKey(id)) {
            subTask = subTasks.get(id);
            historyManager.add(subTask);
        }
        return subTask;
    }

    @Override
    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    @Override
    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    @Override
    public HashMap<Integer, SubTask> getSubTasks() {
        return subTasks;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }
}

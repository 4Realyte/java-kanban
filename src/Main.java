import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();
        Epic epic = new Epic("Помыть руки", "Тщательно");
        Epic epic2 = new Epic("Помытьcя", "Тщательно");
        SubTask subTask = new SubTask("Что-то сделать", "как-то",epic);
        SubTask subTask2 = new SubTask("Что-то купить", "как-то",epic2);
        manager.addTask(epic);
        manager.addTask(epic2);
        manager.addTask(subTask);
        manager.addTask(subTask2);

        System.out.println(epic.subTasksId);
        System.out.println();

        HashMap<Integer, Epic> epics = manager.getEpics();
        for (Epic value : epics.values()) {
            System.out.println(value);
        }
        System.out.println();
        HashMap<Integer, SubTask> subTasks = manager.getSubTasks();
        for (SubTask value : subTasks.values()) {
            System.out.println(value);
        }
        System.out.println();
        System.out.println(manager.getEpicById(2));

        System.out.println(epic.getId());
        System.out.println(subTask.getId());
        System.out.println(epic);
        System.out.println(subTask);
    }
}

import model.Epic;
import model.SubTask;
import model.Task;
import service.webAPI.HttpTaskServer;
import service.webAPI.KVServer;
import service.managers.HttpTaskManager;
import service.utilites.Managers;

import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
        KVServer kvServer = new KVServer();
        kvServer.start();
        HttpTaskManager httpManager = (HttpTaskManager) Managers.getDefault();
        HttpTaskServer taskServer = new HttpTaskServer(httpManager);
        httpManager.createTask(new Epic("Сделать ТЗ", "На отлично!"));
        httpManager.createTask(new SubTask("Прочитать теорию", "Вдумчиво", 1));
        httpManager.createTask(new SubTask("Приступить к ТЗ", "Тщательно", 1));
        httpManager.createTask(new SubTask("Приступить к работе", "Тщательно", 1));
        httpManager.createTask(new Task("task", "description"));
        /*TaskManager loadedManager = httpManager.load();
        System.out.println(httpManager.getSubTasks());
        System.out.println(loadedManager.getSubTasks());*/
    }
}

package service.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.SubTask;
import model.Task;
import service.webAPI.KVTaskClient;
import service.utilites.Managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HttpTaskManager extends FileBackedTasksManager {
    private final KVTaskClient client;
    private final Gson gson;

    public HttpTaskManager(String urlOfServer) {
        super(null);
        client = new KVTaskClient(urlOfServer);
        gson = Managers.getDefaultGson();
    }

    public HttpTaskManager(KVTaskClient client) {
        super(null);
        this.client = client;
        gson = Managers.getDefaultGson();
    }

    public KVTaskClient getClient() {
        return client;
    }

    public Gson getGson() {
        return gson;
    }

    public static HttpTaskManager loadFromServer(KVTaskClient client, Gson gson) {
        HttpTaskManager loadedManager = new HttpTaskManager(client);
        List<Epic> epicList = gson.fromJson(client.load("epics"), new TypeToken<ArrayList<Epic>>() {
        }.getType());
        epicList = epicList == null ? Collections.emptyList() : epicList;
        epicList.forEach(loadedManager::createTaskFromSource);
        List<SubTask> subTasks = gson.fromJson(client.load("subtasks"), new TypeToken<ArrayList<SubTask>>() {
        }.getType());
        subTasks = subTasks == null ? Collections.emptyList() : subTasks;
        subTasks.forEach(loadedManager::createTaskFromSource);
        List<Task> tasks = gson.fromJson(client.load("tasks"), new TypeToken<ArrayList<Task>>() {
        }.getType());
        tasks = tasks == null ? Collections.emptyList() : tasks;
        tasks.forEach(loadedManager::createTaskFromSource);
        List<Integer> historyIdList = gson.fromJson(client.load("history"), new TypeToken<ArrayList<Integer>>() {
        }.getType());
        historyIdList = historyIdList == null ? Collections.emptyList() : historyIdList;

        for (Integer id : historyIdList) {
            if (loadedManager.getTasks().containsKey(id)) {
                loadedManager.getTaskById(id);
            } else if (loadedManager.getEpics().containsKey(id)) {
                loadedManager.getEpicById(id);
            } else if (loadedManager.getSubTasks().containsKey(id)) {
                loadedManager.getSubTaskById(id);
            }
        }
        return loadedManager;
    }

    @Override
    public void save() {
        String jsonTasks = gson.toJson(new ArrayList<>(getTasks().values()));
        client.put("tasks", jsonTasks);
        String jsonEpics = gson.toJson(new ArrayList<>(getEpics().values()));
        client.put("epics", jsonEpics);
        String jsonSubtasks = gson.toJson(new ArrayList<>(getSubTasks().values()));
        client.put("subtasks", jsonSubtasks);
        String jsonHistory = gson.toJson(getHistory().stream().map(Task::getId).collect(Collectors.toList()));
        client.put("history", jsonHistory);
    }
}

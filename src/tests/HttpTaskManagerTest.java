import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.managers.HttpTaskManager;
import service.webAPI.HttpTaskServer;
import service.webAPI.KVServer;
import service.webAPI.KVTaskClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {
    KVServer kvServer;
    HttpTaskServer taskServer;
    String urlOfServer = "http://localhost:8078/";
    Gson gson;

    @BeforeEach
    void beforeEach() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        manager = new HttpTaskManager(urlOfServer);
        taskServer = new HttpTaskServer(manager);
        gson = manager.getGson();
    }

    @AfterEach
    void stopServers() {
        kvServer.stop();
        taskServer.stop();
    }

    @Test
    void loadFromServer_loadedManagerTasksAndHistoryShouldBeEqualsManagerTasksAndHistory() {
        manager.createTask(new Epic("epic", "description"));
        manager.createTask(new SubTask("subtask", "description", 1, "12.01.2023 18:43", 500));
        manager.getEpicById(1);
        manager.getSubTaskById(2);
        HttpTaskManager loadedManager = HttpTaskManager.loadFromServer(manager.getClient(),
                gson);
        assertEquals(manager.getEpics(), loadedManager.getEpics());
        assertEquals(manager.getSubTasks(), loadedManager.getSubTasks());
        assertEquals(manager.getTasks(), loadedManager.getTasks());
        assertEquals(manager.getHistory(), loadedManager.getHistory());
    }

    @Test
    void loadFromServer_WhenTasksAreEmpty() {
        HttpTaskManager loadedManager = HttpTaskManager.loadFromServer(manager.getClient(),
                gson);
        assertEquals(manager.getEpics(), loadedManager.getEpics());
        assertEquals(manager.getSubTasks(), loadedManager.getSubTasks());
        assertEquals(manager.getTasks(), loadedManager.getTasks());
        assertEquals(manager.getHistory(), loadedManager.getHistory());
    }

    @Test
    void loadFromServer_EpicWithoutSubs() {
        manager.createTask(new Epic("epic", "description"));
        manager.getEpicById(1);
        HttpTaskManager loadedManager = HttpTaskManager.loadFromServer(manager.getClient(),
                gson);
        assertEquals(manager.getEpics(), loadedManager.getEpics());
        assertEquals(manager.getSubTasks(), loadedManager.getSubTasks());
        assertEquals(manager.getTasks(), loadedManager.getTasks());
        assertEquals(manager.getHistory(), loadedManager.getHistory());
    }

    @Test
    void loadFromServer_TasksWithDurationStartAndEndTime() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask = manager.createTask(new SubTask("subtask", "description",
                1, "21.01.2023 17:43", 357));
        HttpTaskManager loadedManager = HttpTaskManager.loadFromServer(manager.getClient(),
                gson);
        assertEquals(manager.getEpics(), loadedManager.getEpics());
        assertEquals(manager.getSubTasks(), loadedManager.getSubTasks());
        assertEquals(manager.getTasks(), loadedManager.getTasks());
        assertEquals(manager.getHistory(), loadedManager.getHistory());
    }

    @Test
    void loadFromServer_SomeSubsWithDurationSomeWithout() {
        manager.createTask(new Epic("Epic", "description"));
        manager.createTask(new SubTask("subtask", "description",
                1, "21.01.2023 17:43", 357));
        manager.createTask(new SubTask("subtask2", "description2", 1));

        HttpTaskManager loadedManager = HttpTaskManager.loadFromServer(manager.getClient(),
                gson);
        assertEquals(manager.getEpics(), loadedManager.getEpics());
        assertEquals(manager.getSubTasks(), loadedManager.getSubTasks());
        assertEquals(manager.getTasks(), loadedManager.getTasks());
        assertEquals(manager.getHistory(), loadedManager.getHistory());
    }

    @Test
    void save_shouldSaveTasksOnServer() {
        Epic epic = manager.createTaskFromSource(new Epic("Epic", "description"));
        manager.save();
        KVTaskClient client = manager.getClient();
        ArrayList<Epic> epics = gson.fromJson(client.load("epics"), new TypeToken<ArrayList<Epic>>() {
        }.getType());
        assertEquals(epic, epics.get(0));
    }

    @Test
    void save_clientShouldReturnEmptyStringWhenSaveCalledAndNoTasksWasCreated() {
        manager.save();
        KVTaskClient client = manager.getClient();
        assertEquals("[]", client.load("epics"));
        assertEquals("[]", client.load("subtasks"));
        assertEquals("[]", client.load("tasks"));
    }

    @Test
    void endPoint_GetPriorityTasksTest() {
        manager.createTask(new Epic("Epic", "description"));
        String priority = gson.toJson(manager.getPrioritizedTasks());
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:8080/tasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(priority, response.body());
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void endPoint_GetTasks() {
        manager.createTask(new Task("task", "description"));
        String tasks = gson.toJson(new ArrayList<>(manager.getTasks().values()));
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:8080/tasks/task");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(tasks, response.body());
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void endPoint_GetTaskByID_WithCorrectIncorrectAndNotExistingID() throws IllegalArgumentException,
            IOException, InterruptedException {
        manager.createTask(new Task("task", "description"));
        manager.createTask(new Task("task1", "description"));
        String task = gson.toJson(manager.getTaskById(2));
        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create("http://localhost:8080/tasks/task?id=2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(task, response.body());
        assertEquals(200, response.statusCode());

        uri = URI.create("http://localhost:8080/tasks/task?id=3");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Задачи с таким ID не существует", response.body());
        assertEquals(404, response.statusCode());

        uri = URI.create("http://localhost:8080/tasks/task?id=ssa");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Некорректный идентификатор задачи", response.body());
        assertEquals(400, response.statusCode());
    }

    @Test
    void endPoint_GetSubtasks() {
        manager.createTask(new Epic("epic", "description"));
        manager.createTask(new SubTask("subtask", "description", 1));
        String subs = gson.toJson(new ArrayList<>(manager.getSubTasks().values()));
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:8080/tasks/subtask");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(subs, response.body());
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void endPoint_GetSubtaskByID_WithCorrectIncorrectAndNotExistingID() throws IllegalArgumentException,
            IOException, InterruptedException {
        manager.createTask(new Epic("epic", "description"));
        manager.createTask(new SubTask("subtask", "description", 1));
        String sub = gson.toJson(manager.getSubTaskById(2));
        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create("http://localhost:8080/tasks/subtask?id=2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(sub, response.body());
        assertEquals(200, response.statusCode());

        uri = URI.create("http://localhost:8080/tasks/subtask?id=3");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Подзадачи с таким ID не существует", response.body());
        assertEquals(404, response.statusCode());

        uri = URI.create("http://localhost:8080/tasks/subtask?id=ssa");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Некорректный идентификатор задачи", response.body());
        assertEquals(400, response.statusCode());
    }

    @Test
    void endPoint_GetEpics() {
        manager.createTask(new Epic("epic", "description"));
        manager.createTask(new SubTask("subtask", "description", 1));
        String epics = gson.toJson(new ArrayList<>(manager.getEpics().values()));
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:8080/tasks/epic");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(epics, response.body());
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void endPoint_GetEpicByID_WithCorrectIncorrectAndNotExistingID() throws IllegalArgumentException,
            IOException, InterruptedException {
        manager.createTask(new Epic("epic", "description"));
        manager.createTask(new SubTask("subtask", "description", 1));
        String epic = gson.toJson(manager.getEpicById(1));
        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create("http://localhost:8080/tasks/epic?id=1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(epic, response.body());
        assertEquals(200, response.statusCode());

        uri = URI.create("http://localhost:8080/tasks/epic?id=3");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Эпика с таким ID не существует", response.body());
        assertEquals(404, response.statusCode());

        uri = URI.create("http://localhost:8080/tasks/epic?id=ssa");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Некорректный идентификатор задачи", response.body());
        assertEquals(400, response.statusCode());
    }

    @Test
    void endPoint_GetHistory() {
        manager.createTask(new Epic("epic", "description"));
        manager.createTask(new SubTask("subtask", "description", 1));
        String history = gson.toJson(manager.getHistory());
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:8080/tasks/history");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(history, response.body());
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void endPoint_PostTask() {
        Task task = new Task("task", "description");
        task.setId(1);
        String JsonTask = gson.toJson(task);
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:8080/tasks/task");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(JsonTask))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals("Задача успешно создана", response.body());
            assertEquals(201, response.statusCode());
            assertEquals(manager.getTaskById(1), task);
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void endPoint_PostEpic() {
        Epic epic = new Epic("epic", "description");
        epic.setId(1);
        String JsonTask = gson.toJson(epic);
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:8080/tasks/epic");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(JsonTask))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals("Задача успешно создана", response.body());
            assertEquals(201, response.statusCode());
            assertEquals(manager.getEpicById(1), epic);
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void endPoint_PostSubtask() {
        Epic epic = manager.createTask(new Epic("epic", "description"));
        SubTask subTask = new SubTask("sub", "descrip", 1);
        subTask.setId(2);
        String JsonTask = gson.toJson(subTask);
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:8080/tasks/subtask");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(JsonTask))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals("Задача успешно создана", response.body());
            assertEquals(201, response.statusCode());
            assertEquals(manager.getSubTaskById(2), subTask);
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void endPoint_PostWithIncorrectJson() throws IllegalArgumentException,
            IOException, InterruptedException {
        String json = gson.toJson("sdsd");
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Получен некорректный JSON = required type JsonObject",
                response.body());
        assertEquals(400, response.statusCode());

        Task task = new Task(null, "description");
        json = gson.toJson(task);
        request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Получен некорректный JSON = Имя и описание задачи не могут быть пустыми",
                response.body());

        task = new Task("task", "description");
        task.setType(null);
        json = gson.toJson(task);
        request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Получен некорректный JSON = Тип задачи не может быть пустым",
                response.body());
    }

    @Test
    void endPoint_deleteAllEpics() {
        manager.createTask(new Epic("epic", "description"));
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:8080/tasks/epic");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals("Эпики успешно удалены!", response.body());
            assertTrue(manager.getEpics().isEmpty());
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void endPoint_deleteEpicByID_WithCorrectIncorrectAndNotExistingID() throws IllegalArgumentException,
            IOException, InterruptedException {
        manager.createTask(new Epic("epic", "description"));
        manager.createTask(new SubTask("subtask", "description", 1));
        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create("http://localhost:8080/tasks/epic?id=1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Эпик успешно удалён", response.body());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getEpics().isEmpty());
        assertTrue(manager.getSubTasks().isEmpty());

        uri = URI.create("http://localhost:8080/tasks/epic?id=3");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Эпика с таким ID не существует", response.body());
        assertEquals(404, response.statusCode());

        uri = URI.create("http://localhost:8080/tasks/epic?id=ssa");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Некорректный идентификатор задачи", response.body());
        assertEquals(400, response.statusCode());
    }

    @Test
    void endPoint_deleteAllTasks() {
        manager.createTask(new Task("task", "description"));
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:8080/tasks/task");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals("Задачи успешно удалены!", response.body());
            assertTrue(manager.getTasks().isEmpty());
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void endPoint_deleteTaskByID_WithCorrectIncorrectAndNotExistingID() throws IllegalArgumentException,
            IOException, InterruptedException {
        manager.createTask(new Task("task", "description"));
        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create("http://localhost:8080/tasks/task?id=1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Задача успешно удалена", response.body());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getTasks().isEmpty());

        uri = URI.create("http://localhost:8080/tasks/task?id=3");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Задачи с таким ID не существует", response.body());
        assertEquals(404, response.statusCode());

        uri = URI.create("http://localhost:8080/tasks/task?id=ssa");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Некорректный идентификатор задачи", response.body());
        assertEquals(400, response.statusCode());
    }

    @Test
    void endPoint_deleteAllSubtasks() {
        Epic epic = manager.createTask(new Epic("epic", "description"));
        manager.createTask(new SubTask("subtask", "description", 1));
        manager.createTask(new SubTask("subtask2", "description", 1));
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:8080/tasks/subtask");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertTrue(manager.getSubTasks().isEmpty());
            assertTrue(epic.getEpicSubs().isEmpty());
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void endPoint_deleteSubtaskByID_WithCorrectIncorrectAndNotExistingID() throws IllegalArgumentException,
            IOException, InterruptedException {
        Epic epic = manager.createTask(new Epic("epic", "description"));
        manager.createTask(new SubTask("subtask", "description", 1));
        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create("http://localhost:8080/tasks/subtask?id=2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertTrue(manager.getSubTasks().isEmpty());
        assertTrue(epic.getEpicSubs().isEmpty());
        assertEquals(200, response.statusCode());

        uri = URI.create("http://localhost:8080/tasks/subtask?id=3");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Подзадачи с таким ID не существует", response.body());
        assertEquals(404, response.statusCode());

        uri = URI.create("http://localhost:8080/tasks/subtask?id=ssa");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Некорректный идентификатор задачи", response.body());
        assertEquals(400, response.statusCode());
    }

    @Test
    void endpoint_Unknown() throws IllegalArgumentException,
            IOException, InterruptedException {
        Epic epic = manager.createTask(new Epic("epic", "description"));
        manager.createTask(new SubTask("subtask", "description", 1));
        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create("http://localhost:8080/tasks/set");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Такого эндпоинта не существует", response.body());
        assertEquals(404, response.statusCode());

        uri = URI.create("http://localhost:8080/tasks/some");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Такого эндпоинта не существует", response.body());
        assertEquals(404, response.statusCode());

        String json = gson.toJson(epic);
        uri = URI.create("http://localhost:8080/tasks/set");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Такого эндпоинта не существует", response.body());
        assertEquals(404, response.statusCode());
    }
}
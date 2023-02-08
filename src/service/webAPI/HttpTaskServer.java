package service.webAPI;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.Epic;
import model.SubTask;
import model.Task;
import model.TasksType;
import service.managers.TaskManager;
import service.utilites.Managers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class HttpTaskServer {
    private final TaskManager taskManager;
    private static final int PORT = 8080;
    private final Gson gson;
    private final HttpServer server;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        gson = Managers.getDefaultGson();
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TaskHandler());
        server.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public void stop() {
        server.stop(0);
    }

    class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Endpoint endpoint = getEndpoint(exchange);
            switch (endpoint) {
                case GET_PRIORITY_TASKS:
                    handleGetPriorityTasks(exchange);
                    break;
                case GET_TASKS:
                    handleGetTasks(exchange);
                    break;
                case GET_TASK_BY_ID:
                    handleGetTaskByID(exchange);
                    break;
                case GET_SUBTASKS:
                    handleGetSubtasks(exchange);
                    break;
                case GET_SUBTASK_BY_ID:
                    handleGetSubtaskByID(exchange);
                    break;
                case GET_EPICS:
                    handleGetEpics(exchange);
                    break;
                case GET_EPIC_BY_ID:
                    handleGetEpicByID(exchange);
                    break;
                case GET_HISTORY:
                    handleGetHistory(exchange);
                    break;
                case POST_TASK:
                case POST_SUBTASK:
                case POST_EPIC:
                    handlePostTask(exchange);
                    break;
                case DELETE_ALL_EPICS:
                    handleDeleteAllEpics(exchange);
                    break;
                case DELETE_EPIC_BY_ID:
                    handleDeleteEpicByID(exchange);
                    break;
                case DELETE_ALL_SUBTASKS:
                    handleDeleteAllSubtasks(exchange);
                    break;
                case DELETE_SUBTASK_BY_ID:
                    handleDeleteSubtaskByID(exchange);
                    break;
                case DELETE_ALL_TASKS:
                    handleDeleteAllTasks(exchange);
                    break;
                case DELETE_TASK_BY_ID:
                    handleDeleteTaskByID(exchange);
                    break;
                case UNKNOWN:
                    writeResponse(exchange, "Такого эндпоинта не существует", 404);
            }
        }

        private void handleGetPriorityTasks(HttpExchange h) throws IOException {
            String priorityTasks = gson.toJson(taskManager.getPrioritizedTasks());
            writeResponse(h, priorityTasks, 200);
        }

        private void handleDeleteTaskByID(HttpExchange h) throws IOException {
            int id = validateQuery(h);
            if(id == -1) return;
            Task task = taskManager.getTasks().get(id);
            if (task != null) {
                taskManager.removeTask(task);
                writeResponse(h, "Задача успешно удалена", 200);
            } else {
                writeResponse(h, "Задачи с таким ID не существует", 404);
            }
        }

        private void handleDeleteAllTasks(HttpExchange h) throws IOException {
            taskManager.clearAllTasks();
            writeResponse(h, "Задачи успешно удалены!", 200);
        }

        private void handleDeleteSubtaskByID(HttpExchange h) throws IOException {
            int id = validateQuery(h);
            if(id == -1) return;
            SubTask subtask = taskManager.getSubTasks().get(id);
            if (subtask != null) {
                taskManager.removeSubTask(subtask);
                writeResponse(h, "Подзадача успешно удалена", 200);
            } else {
                writeResponse(h, "Подзадачи с таким ID не существует", 404);
            }
        }

        private void handleDeleteAllSubtasks(HttpExchange h) throws IOException {
            taskManager.clearAllSubTasks();
            writeResponse(h, "Подзадачи успешно удалены!", 200);
        }

        private void handleDeleteEpicByID(HttpExchange h) throws IOException {
            int id = validateQuery(h);
            if(id == -1) return;
            Epic epic = taskManager.getEpics().get(id);
            if (epic != null) {
                taskManager.removeEpic(epic);
                writeResponse(h, "Эпик успешно удалён", 200);
            } else {
                writeResponse(h, "Эпика с таким ID не существует", 404);
            }
        }

        private void handleDeleteAllEpics(HttpExchange h) throws IOException {
            taskManager.clearAllEpics();
            writeResponse(h, "Эпики успешно удалены!", 200);
        }

        private TasksType validatePostBody(String body) throws JsonSyntaxException {
            try {
                JsonElement jsonElem = JsonParser.parseString(body);
                // обработка, если не объект Json
                if (!jsonElem.isJsonObject()) {
                    throw new JsonSyntaxException("required type JsonObject");
                }
                JsonElement typeElement = jsonElem.getAsJsonObject().get("type");
                // обработка, если TaskType отсутствует
                if (typeElement == null) {
                    throw new JsonSyntaxException("Тип задачи не может быть пустым");
                }
                TasksType type = TasksType.valueOf(typeElement.getAsString());
                return type;
            } catch (IllegalArgumentException ex) {
                throw new JsonSyntaxException("Тип задачи указан некорретно");
            }
        }

        private void handlePostTask(HttpExchange h) throws IOException {
            InputStream input = h.getRequestBody();
            String body = new String(input.readAllBytes());
            try {
                TasksType type = validatePostBody(body);
                switch (type) {
                    case TASK:
                        Task task = gson.fromJson(body, Task.class);
                        // обработка, если клиент передал пустую таску
                        if (task == null) {
                            throw new JsonSyntaxException("Задача пуста");
                            // если клиент не указал обязательные поля
                        } else if (task.getName() == null || task.getDescription() == null) {
                            throw new JsonSyntaxException("Имя и описание задачи не могут быть пустыми");
                        }
                        taskManager.createTask(task);
                        break;
                    case SUBTASK:
                        SubTask subTask = gson.fromJson(body, SubTask.class);
                        if (subTask == null) {
                            throw new JsonSyntaxException("Подзадача пуста");
                        } else if (subTask.getName() == null
                                || subTask.getDescription() == null
                                || subTask.getEpicId() == 0) {
                            throw new JsonSyntaxException("Имя, описание, ID эпика подзадачи не могут быть пустыми");
                        }
                        taskManager.createTask(subTask);
                        break;
                    case EPIC:
                        Epic epic = gson.fromJson(body, Epic.class);
                        if (epic == null) {
                            throw new JsonSyntaxException("Эпик пуст");
                        } else if (epic.getName() == null || epic.getDescription() == null) {
                            throw new JsonSyntaxException("Имя, описание эпика не могут быть пустыми");
                        }
                        taskManager.createTask(epic);
                }
            } catch (JsonSyntaxException ex) {
                writeResponse(h, "Получен некорректный JSON = " + ex.getMessage(), 400);
                return;
            }
            writeResponse(h, "Задача успешно создана", 201);
        }

        private void handleGetHistory(HttpExchange h) throws IOException {
            String history = gson.toJson(taskManager.getHistory());
            writeResponse(h, history, 200);
        }

        private int validateQuery(HttpExchange h) throws IOException {
            String query = h.getRequestURI().getQuery();
            int id = -1;
            try {
                id = Integer.parseInt(query.substring(query.indexOf("=") + 1));
            } catch (NumberFormatException ex) {
                writeResponse(h, "Некорректный идентификатор задачи", 400);
            }
            return id;
        }

        private void handleGetEpicByID(HttpExchange h) throws IOException {
            int id = validateQuery(h);
            if(id == -1) return;
            Epic epic = taskManager.getEpicById(id);
            if (epic != null) {
                String JsonEpic = gson.toJson(epic);
                writeResponse(h, JsonEpic, 200);
            } else {
                writeResponse(h, "Эпика с таким ID не существует", 404);
            }
        }

        private void handleGetEpics(HttpExchange h) throws IOException {
            String JsonEpics = gson.toJson(new ArrayList<>(taskManager.getEpics().values()));
            writeResponse(h, JsonEpics, 200);
        }

        private void handleGetSubtaskByID(HttpExchange h) throws IOException {
            int id = validateQuery(h);
            if(id == -1) return;
            SubTask subtask = taskManager.getSubTaskById(id);
            if (subtask != null) {
                String JsonSubtask = gson.toJson(subtask);
                writeResponse(h, JsonSubtask, 200);
            } else {
                writeResponse(h, "Подзадачи с таким ID не существует", 404);
            }
        }

        private void handleGetSubtasks(HttpExchange h) throws IOException {
            String JsonSubs = gson.toJson(new ArrayList<>(taskManager.getSubTasks().values()));
            writeResponse(h, JsonSubs, 200);
        }

        private void handleGetTasks(HttpExchange h) throws IOException {
            String JsonTasks = gson.toJson(new ArrayList<>(taskManager.getTasks().values()));
            writeResponse(h, JsonTasks, 200);
        }

        private void handleGetTaskByID(HttpExchange h) throws IOException {
            int id = validateQuery(h);
            if(id == -1) return;
            Task task = taskManager.getTaskById(id);
            if (task != null) {
                String JsonTask = gson.toJson(task);
                writeResponse(h, JsonTask, 200);
            } else {
                writeResponse(h, "Задачи с таким ID не существует", 404);
            }
        }

        private void writeResponse(HttpExchange exchange,
                                   String responseString,
                                   int responseCode) throws IOException {
            if (responseString.isBlank()) {
                exchange.sendResponseHeaders(responseCode, 0);
            } else {
                byte[] bytes = responseString.getBytes();
                exchange.sendResponseHeaders(responseCode, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
            exchange.close();
        }

        private Endpoint getEndpoint(HttpExchange exchange) {
            String requestPath = exchange.getRequestURI().getPath();
            String[] pathParts = requestPath.split("/");
            String requestMethod = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery() == null ? "" : exchange.getRequestURI().getQuery();

            switch (requestMethod) {
                case "GET":
                    if (pathParts.length == 2) {
                        return Endpoint.GET_PRIORITY_TASKS;
                    }
                    switch (pathParts[2]) {
                        case "task":
                            if (!query.isBlank()) {
                                return Endpoint.GET_TASK_BY_ID;
                            } else {
                                return Endpoint.GET_TASKS;
                            }
                        case "subtask":
                            if (!query.isBlank()) {
                                return Endpoint.GET_SUBTASK_BY_ID;
                            } else {
                                return Endpoint.GET_SUBTASKS;
                            }
                        case "epic":
                            if (!query.isBlank()) {
                                return Endpoint.GET_EPIC_BY_ID;
                            } else {
                                return Endpoint.GET_EPICS;
                            }
                        case "history":
                            return Endpoint.GET_HISTORY;
                    }
                case "POST":
                    switch (pathParts[2]) {
                        case "task":
                            return Endpoint.POST_TASK;
                        case "subtask":
                            return Endpoint.POST_SUBTASK;
                        case "epic":
                            return Endpoint.POST_EPIC;
                    }
                case "DELETE":
                    switch (pathParts[2]) {
                        case "task":
                            if (query.isBlank()) {
                                return Endpoint.DELETE_ALL_TASKS;
                            } else {
                                return Endpoint.DELETE_TASK_BY_ID;
                            }
                        case "subtask":
                            if (query.isBlank()) {
                                return Endpoint.DELETE_ALL_SUBTASKS;
                            } else {
                                return Endpoint.DELETE_SUBTASK_BY_ID;
                            }
                        case "epic":
                            if (query.isBlank()) {
                                return Endpoint.DELETE_ALL_EPICS;
                            } else {
                                return Endpoint.DELETE_EPIC_BY_ID;
                            }
                    }
                default:
                    return Endpoint.UNKNOWN;
            }
        }
    }
}

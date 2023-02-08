package service.managers;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TasksType;
import service.managers.supportServices.ManagerSaveException;
import service.utilites.Managers;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private File fileToSave;

    public FileBackedTasksManager(String fileToSave) {
        if(fileToSave != null) {
            this.fileToSave = new File(fileToSave).getAbsoluteFile();
        }
    }

    public File getFileToSave() {
        return fileToSave;
    }

    public String toString(Task task) {
        StringJoiner joiner = new StringJoiner(",");
        if (task.getType() == TasksType.SUBTASK) {
            SubTask sub = (SubTask) task;
            joiner.add(String.valueOf(sub.getId()));
            joiner.add(sub.getType().toString());
            joiner.add(sub.getName());
            joiner.add(sub.getStatus().toString());
            joiner.add(sub.getDescription());
            joiner.add(String.valueOf(sub.getEpicId()));
        } else {
            joiner.add(String.valueOf(task.getId()));
            joiner.add(task.getType().toString());
            joiner.add(task.getName());
            joiner.add(task.getStatus().toString());
            joiner.add(task.getDescription());
        }
        if (task.getStartTime() != null) {
            joiner.add(task.getStartTime().format(Managers.DATE_TIME_FORMATTER));
            joiner.add(task.getEndTime().format(Managers.DATE_TIME_FORMATTER));
            joiner.add(String.valueOf(task.getDuration().toMinutes()));
        }
        return joiner.toString();
    }

    public static String historyToString(HistoryManager manager) {
        StringJoiner joiner = new StringJoiner(",");
        for (Task task : manager.getHistory()) {
            joiner.add(String.valueOf(task.getId()));
        }
        return joiner.toString();
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
            writer.write("id,type,name,status,description,epic,startTime,endTime,duration");
            writer.newLine();
            if (!tasks.isEmpty()) {
                for (Task task : tasks.values()) {
                    writer.write(toString(task));
                    writer.newLine();
                }
            }
            if (!epics.isEmpty()) {
                for (Epic epic : epics.values()) {
                    writer.write(toString(epic));
                    writer.newLine();
                }
            }
            if (!subTasks.isEmpty()) {
                for (SubTask sub : subTasks.values()) {
                    writer.write(toString(sub));
                    writer.newLine();
                }
            }
            if (!historyManager.getHistory().isEmpty()) {
                writer.newLine();
                writer.write(historyToString(historyManager));
            }
        } catch (IOException ex) {
            throw new ManagerSaveException("Произошла ошибка записи в файл");
        }
    }

    @Override
    public <T extends Task> T createTask(T task) {
        T created = super.createTask(task);
        save();
        return created;
    }

    @Override
    public void removeTask(Task task) {
        super.removeTask(task);
        save();
    }

    @Override
    public void removeSubTask(SubTask subTask) {
        super.removeSubTask(subTask);
        save();
    }

    @Override
    public void removeEpic(Epic epic) {
        super.removeEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public SubTask getSubTaskById(int id) {
        SubTask subTask = super.getSubTaskById(id);
        save();
        return subTask;
    }

    public static List<Integer> historyFromString(String value) {
        return Arrays.stream(value.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public static Task fromString(String value) {
        String[] taskAttributes = value.split(",");
        switch (TasksType.valueOf(taskAttributes[1])) {
            case TASK:
                Task task = new Task(taskAttributes);
                return task;
            case EPIC:
                Epic epic = new Epic(taskAttributes);
                return epic;
            case SUBTASK:
                SubTask subTask = new SubTask(taskAttributes);
                return subTask;
        }
        return null;
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager manager = new FileBackedTasksManager(file.getAbsolutePath());
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            while (reader.ready()) {
                String taskAsString = reader.readLine();
                if (!taskAsString.isBlank()) {
                    manager.createTaskFromSource(fromString(taskAsString));
                } else {
                    List<Integer> historyIdList = historyFromString(reader.readLine());
                    for (Integer id : historyIdList) {
                        if (manager.getTasks().containsKey(id)) {
                            manager.getTaskById(id);
                        } else if (manager.getEpics().containsKey(id)) {
                            manager.getEpicById(id);
                        } else if (manager.getSubTasks().containsKey(id)) {
                            manager.getSubTaskById(id);
                        }
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return manager;
    }
}

package model;

import service.utilites.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private int id;
    private TaskStatus status;
    private TasksType type;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
        type = TasksType.TASK;
    }

    public Task(String name, String description, String startTime, int duration) {
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
        type = TasksType.TASK;
        this.startTime = LocalDateTime.parse(startTime, Managers.DATE_TIME_FORMATTER);
        this.duration = Duration.ofMinutes(duration);
    }

    public Task(String[] attributes) {
        this.id = Integer.parseInt(attributes[0]);
        this.type = TasksType.TASK;
        this.name = attributes[2];
        this.status = TaskStatus.valueOf(attributes[3]);
        this.description = attributes[4];
        if (attributes.length == 8) {
            this.startTime = LocalDateTime.parse(attributes[5], Managers.DATE_TIME_FORMATTER);
            this.duration = Duration.ofMinutes(Long.parseLong(attributes[7]));
        }
    }


    public TasksType getType() {
        return type;
    }

    public void setType(TasksType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "model.Task{"
                + "name='" + name + '\''
                + ", description='"
                + description + '\''
                + ", id=" + id
                + ", status=" + status
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && name.equals(task.name)
                && description.equals(task.description)
                && Objects.equals(status, task.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, status);
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getEndTime() {
        LocalDateTime endTime = null;
        if (startTime != null) {
            endTime = startTime.plus(duration);
        }
        return endTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }
}

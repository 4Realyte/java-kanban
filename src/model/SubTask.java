package model;

import service.utilites.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class SubTask extends Task {
    private int epicId;

    public SubTask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
        setType(TasksType.SUBTASK);
    }

    public SubTask(String name, String description, int epicId, String startTime, int duration) {
        super(name, description, startTime, duration);
        this.epicId = epicId;
        setType(TasksType.SUBTASK);

    }

    public SubTask(String[] attributes) {
        super(attributes);
        this.epicId = Integer.parseInt(attributes[5]);
        setType(TasksType.SUBTASK);
        if (attributes.length == 9) {
            this.startTime = LocalDateTime.parse(attributes[6], Managers.DATE_TIME_FORMATTER);
            this.duration = Duration.ofMinutes(Long.parseLong(attributes[8]));
            getEndTime();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubTask subTask = (SubTask) o;
        return this.getId() == subTask.getId() && this.getName().equals(subTask.getName())
                && this.getDescription().equals(subTask.getDescription())
                && this.getStatus().equals(subTask.getStatus()) && this.getEpicId() == subTask.getEpicId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getName(), this.getDescription(),
                this.getId(), this.getStatus(), this.getEpicId());
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "model.SubTask{"
                + "name='" + this.getName() + '\''
                + ", description='" + this.getDescription() + '\''
                + ", id=" + this.getId()
                + ", status=" + this.getStatus()
                + '}';
    }
}

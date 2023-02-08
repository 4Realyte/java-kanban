package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;

public class Epic extends Task {
    private HashMap<Integer, SubTask> epicSubs = new HashMap<>();
    private LocalDateTime endTime;


    public Epic(String name, String description) {
        super(name, description);
        setType(TasksType.EPIC);
    }

    public Epic(String[] attributes) {
        super(attributes);
        setType(TasksType.EPIC);
        getEndTime();
    }


    public HashMap<Integer, SubTask> getEpicSubs() {
        return epicSubs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Epic epic = (Epic) o;
        return this.getId() == epic.getId() && this.getName().equals(epic.getName())
                && this.getDescription().equals(epic.getDescription())
                && this.getStatus().equals(epic.getStatus()) && this.getEpicSubs().equals(epic.getEpicSubs());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getName(), this.getDescription(),
                this.getId(), this.getStatus(), this.getEpicSubs());
    }

    @Override
    public String toString() {
        return "model.Epic{"
                + "name='" + this.getName() + '\''
                + ", description='" + this.getDescription() + '\''
                + ", id=" + this.getId()
                + ", status=" + this.getStatus()
                + '}';
    }

    @Override
    public LocalDateTime getStartTime() {
        LocalDateTime startTime = null;
        if (!epicSubs.isEmpty()) {
            SubTask sub = epicSubs.values().iterator().next();
            startTime = sub.getStartTime();
            for (SubTask value : epicSubs.values()) {
                LocalDateTime anotherStartTime = value.getStartTime();
                if (anotherStartTime != null) {
                    if (startTime == null ? true : anotherStartTime.isBefore(startTime)) {
                        startTime = anotherStartTime;
                    }
                }
            }
        }
        this.startTime = startTime;

        return startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        LocalDateTime endTime = null;
        if (!epicSubs.isEmpty()) {
            SubTask sub = epicSubs.values().iterator().next();
            endTime = sub.getEndTime();
            for (SubTask value : epicSubs.values()) {
                LocalDateTime anotherEndTime = value.getEndTime();
                if (anotherEndTime != null) {
                    if (endTime == null ? true : anotherEndTime.isAfter(endTime)) {
                        endTime = anotherEndTime;
                    }
                }
            }
        }
        this.endTime = endTime;

        return endTime;
    }

    public void initilizeTime() {
        getStartTime();
        getEndTime();
        getDuration();
    }

    @Override
    public Duration getDuration() {
        if (endTime != null && startTime != null) {
            duration = Duration.between(startTime, endTime);
        }
        return duration;
    }
}

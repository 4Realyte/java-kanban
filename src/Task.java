import java.util.HashMap;
import java.util.Objects;

public class Task {
     String name;
    protected String description;
    protected int id;
    protected static int idCounter = 1;
    protected String status;
    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.id = idCounter;
        idCounter++;
        this.status = "NEW";
    }

    @Override
    public String toString() {
        return "Task{"
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
                && status.equals(task.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, status);
    }

    public String getStatus() {
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

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }
}

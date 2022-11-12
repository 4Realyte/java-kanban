import java.util.HashMap;

public class SubTask extends Task{
    private int epicId;
    public SubTask(String name, String description, Epic epic) {
        super(name, description);
        this.epicId = epic.id;
        epic.subTasksId.add(this.id);

//        subTasksList.put(this.getId(),this);
    }

    @Override
    public String toString() {
        return "SubTask{"
                + "name='" + this.getName() + '\''
                + ", description='" + this.getDescription() + '\''
                + ", id=" + this.getId()
                + ", status=" + this.getStatus()
                + '}';
    }
}

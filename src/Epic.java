import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {
    ArrayList<Integer> subTasksId;
//    HashMap<Integer, SubTask> epicSubs; - раскомментить

    public Epic(String name, String description) {
        super(name, description);
//        epicSubs = new HashMap<>();
        subTasksId = new ArrayList<>();
    }

    public ArrayList<Integer> getSubTasksId() {
        return subTasksId;
    }

    @Override

    public String toString() {
        String result = "Epic{"
                + "name='" + this.getName() + '\''
                + ", description='" + this.getDescription() + '\''
                + ", id=" + this.getId()
                + ", status=" + this.getStatus();

        if(subTasksId.isEmpty()){
            result = result +", подзадачи отсутствуют"+'}';
        }else{
            result = result + ", subTasksId=" + this.subTasksId + '}';
        }
        return result;
    }
}

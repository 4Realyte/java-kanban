

import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.managers.FileBackedTasksManager;
import service.managers.supportServices.ManagerSaveException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {
    @BeforeEach
    void beforeEach() {
        manager = new FileBackedTasksManager("src/tests/tasksTest.csv");
    }

    @Test
    void toString_shouldReturnEpicAsStringWhenSubtasksAbsent() {
        Epic epic = manager.createTask(new Epic("epic", "description"));
        assertEquals("1,EPIC,epic,NEW,description", manager.toString(epic));
    }

    @Test
    void toString_shouldReturnEpicAsStringWithSubTasks() {
        Epic epic = manager.createTask(new Epic("epic", "description"));
        SubTask sub = manager.createTask(new SubTask("subtask", "description", 1));
        assertEquals("1,EPIC,epic,NEW,description", manager.toString(epic));
        assertEquals("2,SUBTASK,subtask,NEW,description,1", manager.toString(sub));
    }

    @Test
    void toString_shouldReturnDurationBetweenSubsStartAndEndTime() {
        Epic epic = manager.createTask(new Epic("epic", "description"));
        SubTask sub = manager.createTask(new SubTask("subtask", "description", 1,
                "21.01.2023 17:33", 360));
        SubTask sub2 = manager.createTask(new SubTask("subtask2", "description", 1,
                "23.01.2023 18:45", 5000));
        assertEquals(7952, epic.getDuration().toMinutes());
    }

    @Test
    void toString_shouldReturnNullDurationWhenEpicDoesNotHaveSubtasks() {
        Epic epic = manager.createTask(new Epic("epic", "description"));
        assertNull(epic.getDuration());
    }

    @Test
    void save_shouldReturnHeadingStringWhenTasksWereNotCreated() {
        manager.save();
        List<String> fileList = null;
        try {
            fileList = Files.readAllLines(manager.getFileToSave().toPath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        assertEquals("[id,type,name,status,description,epic,startTime,endTime,duration]", fileList.toString());
    }

    @Test
    void save_shouldReturnHeadingAndEpicAsStringWithEmptyHistory() {
        Epic epic = manager.createTask(new Epic("epic", "description"));
        manager.save();
        List<String> fileList = null;
        try {
            fileList = Files.readAllLines(manager.getFileToSave().toPath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        assertEquals("id,type,name,status,description,epic,startTime,endTime,duration", fileList.get(0));
        assertEquals("1,EPIC,epic,NEW,description", fileList.get(1));
    }

    @Test
    void save_shouldReturnHeadingEpicAsStringEmptyLineAndHistory() {
        Epic epic = manager.createTask(new Epic("epic", "description"));
        manager.getEpicById(1);
        List<String> fileList = null;
        try {
            fileList = Files.readAllLines(manager.getFileToSave().toPath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        assertEquals("id,type,name,status,description,epic,startTime,endTime,duration", fileList.get(0));
        assertEquals("1,EPIC,epic,NEW,description", fileList.get(1));
        assertEquals("", fileList.get(2));
        assertEquals("1", fileList.get(3));
    }

    @Test
    void save_shouldThrowManagerSaveException() {
        manager = new FileBackedTasksManager("src/");
        final ManagerSaveException ex = assertThrows(ManagerSaveException.class, manager::save);
        assertEquals("Произошла ошибка записи в файл", ex.getMessage());
    }

    @Test
    void loadFromFile_loadedManagerTasksAndHistoryShouldBeEqualsManagerTasksAndHistory() {
        manager.createTask(new Epic("epic", "description"));
        manager.createTask(new SubTask("subtask", "description", 1));
        manager.getEpicById(1);
        manager.getSubTaskById(2);

        FileBackedTasksManager backedManager = FileBackedTasksManager.loadFromFile(manager.getFileToSave());
        assertEquals(manager.getEpics(), backedManager.getEpics());
        assertEquals(manager.getSubTasks(), backedManager.getSubTasks());
        assertEquals(manager.getHistory(), backedManager.getHistory());
    }

    @Test
    void loadFromFile_loadedManagerTasksSetShouldBeEqualsManagerTasksSet() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:43", 357));

        FileBackedTasksManager backedManager = FileBackedTasksManager.loadFromFile(manager.getFileToSave());
        assertEquals(manager.getPrioritizedTasks().toString(), backedManager.getPrioritizedTasks().toString());
    }

    @Test
    void loadFromFile_WhenTasksAreEmpty() {
        // очищаем историю, сохранненую в в предыдущем файле
        manager.save();
        FileBackedTasksManager backedManager = FileBackedTasksManager.loadFromFile(manager.getFileToSave());
        assertEquals(manager.getEpics(), backedManager.getEpics());
        assertEquals(manager.getSubTasks(), backedManager.getSubTasks());
        assertEquals(manager.getHistory(), backedManager.getHistory());
    }

    @Test
    void loadFromFile_EpicWithoutSubs() {
        manager.createTask(new Epic("epic", "description"));
        manager.getEpicById(1);
        FileBackedTasksManager backedManager = FileBackedTasksManager.loadFromFile(manager.getFileToSave());
        assertEquals(manager.getEpics(), backedManager.getEpics());
        assertEquals(manager.getSubTasks(), backedManager.getSubTasks());
        assertEquals(manager.getHistory(), backedManager.getHistory());
    }

    @Test
    void loadFromFile_EpicWithoutHistory() {
        manager.createTask(new Epic("epic", "description"));
        FileBackedTasksManager backedManager = FileBackedTasksManager.loadFromFile(manager.getFileToSave());
        assertEquals(manager.getEpics(), backedManager.getEpics());
        assertEquals(manager.getSubTasks(), backedManager.getSubTasks());
        assertEquals(manager.getHistory(), backedManager.getHistory());
    }

    @Test
    void loadFromFile_TasksWithDurationStartAndEndTime() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask = manager.createTask(new SubTask("subtask", "description",
                1, "21.01.2023 17:43", 357));
        FileBackedTasksManager backedManager = FileBackedTasksManager.loadFromFile(manager.getFileToSave());
        assertEquals(manager.getEpics(), backedManager.getEpics());
        assertEquals(manager.getSubTasks(), backedManager.getSubTasks());
        assertEquals(manager.getHistory(), backedManager.getHistory());
    }

    @Test
    void loadFromFile_SomeSubsWithDurationSomeWithout() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask = manager.createTask(new SubTask("subtask", "description",
                1, "21.01.2023 17:43", 357));
        manager.createTask(new SubTask("subtask2", "description2", 1));
        FileBackedTasksManager backedManager = FileBackedTasksManager.loadFromFile(manager.getFileToSave());
        assertEquals(manager.getEpics(), backedManager.getEpics());
        assertEquals(manager.getSubTasks(), backedManager.getSubTasks());
        assertEquals(manager.getHistory(), backedManager.getHistory());
    }

    @Test
    void loadFromFile_WithTasksDurationAndHistory() {
        manager.save();
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask = manager.createTask(new SubTask("subtask", "description",
                1, "21.01.2023 17:43", 357));
        manager.createTask(new SubTask("subtask2", "description2", 1));
        manager.createTask(new Task("Task", "task", "19.01.2023 17:41", 315));

        manager.getEpicById(1);
        manager.getSubTaskById(2);
        manager.getTaskById(4);
        FileBackedTasksManager backedManager = FileBackedTasksManager.loadFromFile(manager.getFileToSave());
        assertEquals(manager.getEpics(), backedManager.getEpics());
        assertEquals(manager.getSubTasks(), backedManager.getSubTasks());
        assertEquals(manager.getHistory(), backedManager.getHistory());
    }
}
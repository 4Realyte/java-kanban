

import model.Epic;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.managers.HistoryManager;
import service.managers.InMemoryTaskManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryHistoryManagerTest {
    HistoryManager historyManager;
    InMemoryTaskManager manager;
    @BeforeEach
    void beforeEach() {
        manager = new InMemoryTaskManager();
        historyManager = manager.getHistoryManager();
    }

    @Test
    void add_shouldAddTaskToEndOfHistory() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        Epic epic2 = manager.createTask(new Epic("Epic2", "description2"));

        historyManager.add(epic1);
        historyManager.add(epic2);
        assertEquals(epic2, historyManager.getHistory().get(1));
    }

    @Test
    void add_shouldAddTaskWithoutDuplicates() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));

        historyManager.add(epic1);
        historyManager.add(epic1);
        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void remove_shouldRemoveTaskFromHistory() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        historyManager.add(epic1);
        assertEquals(1, historyManager.getHistory().size());
        historyManager.remove(epic1.getId());
        assertEquals(0, historyManager.getHistory().size());
    }

    @Test
    void remove_shouldRemoveTaskFromEndOfHistory() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        Epic epic2 = manager.createTask(new Epic("Epic2", "description2"));
        Epic epic3 = manager.createTask(new Epic("Epic3", "description3"));
        historyManager.add(epic1);
        historyManager.add(epic2);
        historyManager.add(epic3);
        assertEquals(epic3, historyManager.getHistory().get(2));
        historyManager.remove(epic3.getId());
        assertEquals(epic2, historyManager.getHistory().get(1));
    }

    @Test
    void remove_shouldRemoveTaskFromMiddleOfHistory() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        Epic epic2 = manager.createTask(new Epic("Epic2", "description2"));
        Epic epic3 = manager.createTask(new Epic("Epic3", "description3"));
        historyManager.add(epic1);
        historyManager.add(epic2);
        historyManager.add(epic3);
        assertEquals(epic2, historyManager.getHistory().get(1));
        historyManager.remove(epic2.getId());
        assertEquals(epic3, historyManager.getHistory().get(1));
    }

    @Test
    void remove_shouldRemoveTaskFromStartOfHistory() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        Epic epic2 = manager.createTask(new Epic("Epic2", "description2"));
        Epic epic3 = manager.createTask(new Epic("Epic3", "description3"));
        historyManager.add(epic1);
        historyManager.add(epic2);
        historyManager.add(epic3);
        assertEquals(epic1, historyManager.getHistory().get(0));
        historyManager.remove(epic1.getId());
        assertEquals(epic2, historyManager.getHistory().get(0));
    }
    @Test
    void getHistory_shouldReturnHistoryOfHistoryManager() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        manager.getEpicById(1);
        final List<Task> history = manager.getHistory();
        assertNotNull(history);
        assertEquals(1, history.size());
    }

    @Test
    void getHistory_shouldReturnEmptyHistoryListWhenNoOneOfGetTasksMethodWasCalled() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        final List<Task> history = manager.getHistory();
        assertNotNull(history);
        assertEquals(0, history.size());
    }

    @Test
    void getHistory_shouldReturnHistoryWithoutDuplicates() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        manager.getEpicById(1);
        manager.getEpicById(1);
        final List<Task> history = manager.getHistory();
        assertNotNull(history);
        assertEquals(1, history.size());
    }
}
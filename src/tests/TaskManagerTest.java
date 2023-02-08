
import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;
import service.managers.TaskManager;
import service.managers.supportServices.ManagerCreateException;
import service.utilites.Managers;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    T manager;

    @Test
    public void shouldEpicStatusBeNewWhenSubtasksIsEmpty() {
        manager.createTask(new Epic("Сделать ТЗ", "На отлично!"));
        Epic epic = manager.getEpicById(1);
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    public void shouldEpicStatusBeNewWhenAllSubtasksStatusIsNew() {
        manager.createTask(new Epic("Сделать ТЗ", "На отлично!"));
        manager.createTask(new SubTask("Прочитать теорию", "Вдумчиво", 1));
        manager.createTask(new SubTask("Приступить к ТЗ", "Тщательно", 1));
        manager.createTask(new SubTask("Приступить к работе", "Тщательно", 1));
        Epic epic = manager.getEpicById(1);
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    public void shouldEpicStatusBeDoneWhenAllSubtasksStatusIsDone() {
        manager.createTask(new Epic("Сделать ТЗ", "На отлично!"));
        manager.createTask(new SubTask("Прочитать теорию", "Вдумчиво", 1));
        manager.createTask(new SubTask("Приступить к ТЗ", "Тщательно", 1));
        SubTask subOne = manager.getSubTaskById(2);
        SubTask subTwo = manager.getSubTaskById(3);
        manager.setStatus(subOne, TaskStatus.DONE);
        manager.setStatus(subTwo, TaskStatus.DONE);

        Epic epic = manager.getEpicById(1);
        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    public void shouldEpicStatusBeInProgressWhenSomeSubtasksAreDONEAndSomeAreNEW() {
        manager.createTask(new Epic("Сделать ТЗ", "На отлично!"));
        manager.createTask(new SubTask("Прочитать теорию", "Вдумчиво", 1));
        manager.createTask(new SubTask("Приступить к ТЗ", "Тщательно", 1));
        SubTask subOne = manager.getSubTaskById(2);
        manager.setStatus(subOne, TaskStatus.DONE);

        Epic epic = manager.getEpicById(1);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    public void shouldEpicStatusBeInProgressWhenAllSubtasksAreInProgress() {
        manager.createTask(new Epic("Сделать ТЗ", "На отлично!"));
        manager.createTask(new SubTask("Прочитать теорию", "Вдумчиво", 1));
        manager.createTask(new SubTask("Приступить к ТЗ", "Тщательно", 1));
        SubTask subOne = manager.getSubTaskById(2);
        SubTask subTwo = manager.getSubTaskById(3);
        manager.setStatus(subOne, TaskStatus.IN_PROGRESS);
        manager.setStatus(subTwo, TaskStatus.IN_PROGRESS);

        Epic epic = manager.getEpicById(1);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void getAllEpicSubTasks_shouldReturnMapOfEpicSubtasksWhenIsNotEmpty() {
        manager.createTask(new Epic("epic", "description"));
        manager.createTask(new SubTask("subtask", "description", 1));

        HashMap<Integer, SubTask> subtasksMap = manager.getAllEpicSubTasks(manager.getEpicById(1));
        for (Map.Entry<Integer, SubTask> subTaskEntry : subtasksMap.entrySet()) {
            assertEquals(2, subTaskEntry.getKey(), "Подзадачи с таким ID не существует");
            assertEquals(manager.getSubTaskById(2),
                    subTaskEntry.getValue(), "Подзадачи в мапе эпика и менеджера отличаются");
        }
    }

    @Test
    void getAllEpicSubTasks_shouldReturnTrueWhenEpicSubtasksIsEmpty() {
        Epic epic = manager.createTask(new Epic("epic", "description"));
        assertTrue(manager.getAllEpicSubTasks(epic).isEmpty());
    }

    @Test
    void getAllEpicSubTasks_shouldReturnTrueWhenEpicSubtasksIdIsIncorrect() {
        Epic epic = manager.createTask(new Epic("epic", "description"));
        SubTask subTask = manager.createTask(new SubTask("subtask", "description", 2));
        assertTrue(manager.getAllEpicSubTasks(epic).isEmpty());
    }

    // TASK
    @Test
    void removeTask_shouldReturnTrueWhenTaskRemovedCorrectly() {
        Task task = manager.createTask(new Task("task", "description"));
        manager.removeTask(task);
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void removeTask_shouldReturnTrueWhenTryToRemoveTaskWhenItWasNotCreated() {
        manager.removeTask(manager.getTaskById(1));
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void removeTask_shouldReturnFalseWhenTryToRemoveTaskWithIncorrectID() {
        manager.createTask(new Task("task", "description"));
        manager.removeTask(manager.getTaskById(2));
        assertFalse(manager.getTasks().isEmpty());
    }

    // SUBTASK
    @Test
    void removeSubtask_shouldReturnTrueWhenSubTaskRemovedCorrectly() {
        Epic epic = manager.createTask(new Epic("task", "description"));
        SubTask subTask = manager.createTask(new SubTask("subtask", "description", 1));

        manager.removeSubTask(subTask);

        assertTrue(manager.getAllEpicSubTasks(epic).isEmpty());
    }

    @Test
    void removeSubtask_shouldReturnTrueWhenTryToRemoveSubTaskWhenItWasNotCreated() {
        manager.createTask(new Epic("task", "description"));
        manager.removeTask(manager.getSubTaskById(2));
        assertTrue(manager.getSubTasks().isEmpty());
    }

    @Test
    void removeSubtask_shouldReturnFalseWhenTryToRemoveSubTaskWithIncorrectID() {
        manager.createTask(new Epic("task", "description"));
        SubTask subTask = manager.createTask(new SubTask("subtask", "description", 1));
        manager.removeTask(subTask);
        assertFalse(manager.getSubTasks().isEmpty());
    }

    @Test
    void SubtaskHasEpic_epicIdShouldBeEqualsSubTasksEpicId() {
        Epic epic = manager.createTask(new Epic("task", "description"));
        SubTask subTask = manager.createTask(new SubTask("subtask", "description", 1));
        assertEquals(1, subTask.getEpicId());
    }

    // EPIC
    @Test
    void removeEpic_shouldReturnTrueWhenEpicRemovedCorrectly() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        manager.removeEpic(epic);
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    void removeEpic_shouldReturnTrueWhenTryToRemoveEpicWhenItWasNotCreated() {
        manager.createTask(new SubTask("subtask", "description", 1));
        manager.removeEpic(manager.getEpicById(1));
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    void removeEpic_shouldReturnFalseWhenTryToRemoveEpicWithIncorrectID() {
        manager.createTask(new Epic("Epic", "description"));
        manager.removeEpic(manager.getEpicById(2));
        assertFalse(manager.getEpics().isEmpty());
    }

    @Test
    void removeEpic_shouldCalculateEpicStatusCorrectlyWhenSubtaskIsRemoved() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subTask = manager.createTask(new SubTask("subtask", "description", 1));
        manager.setStatus(subTask, TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Статус эпика не обновился");

        manager.removeSubTask(subTask);
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void updateTask_shouldUpdateTaskSubstaskEpicWhenItWasChanged() {
        Task task = manager.createTask(new Task("Task", "description"));
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask = manager.createTask(new SubTask("subtask", "description", 2));
        task.setName("TaskOne");
        subtask.setDescription("DES");
        epic.setName("Epic12");
        manager.updateTask(task);
        manager.updateTask(epic);
        manager.updateTask(subtask);

        for (Map.Entry<Integer, Epic> epicEntry : manager.getEpics().entrySet()) {
            assertEquals(2, epicEntry.getKey());
            assertEquals(epic, epicEntry.getValue());
        }
        for (SubTask subtask1 : manager.getSubTasks().values()) {
            assertEquals("DES", subtask1.getDescription());
        }
        for (Task value : manager.getTasks().values()) {
            assertEquals("TaskOne", value.getName());
        }
    }

    @Test
    void setStatus_shouldSetStatusAndUpdateIt() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subTask = manager.createTask(new SubTask("subtask", "description", 1));
        assertEquals(TaskStatus.NEW, epic.getStatus());
        manager.setStatus(subTask, TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void setStatus_shouldUpdateStatusInMapOfTasks() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        manager.setStatus(epic, TaskStatus.IN_PROGRESS);
        assertEquals(epic, manager.getEpicById(1));
    }

    @Test
    void createTask_shouldCreateTasksEpicsSubtasksCorrectly() {
        Task task = manager.createTask(new Task("Task", "description"));
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask = manager.createTask(new SubTask("subtask", "description", 2));

        assertNotNull(task);
        assertEquals("Task", task.getName());
        assertEquals("description", task.getDescription());
        assertNotNull(subtask);
        assertEquals("subtask", subtask.getName());
        assertEquals("description", subtask.getDescription());
        assertEquals(2, subtask.getEpicId());
        assertNotNull(epic);
        assertEquals("Epic", epic.getName());
        assertEquals("description", epic.getDescription());
        assertEquals(manager.getSubTasks(), epic.getEpicSubs());

        assertEquals(task, manager.getTaskById(1));
        assertEquals(epic, manager.getEpicById(2));
        assertEquals(subtask, manager.getSubTaskById(3));
    }

    @Test
    void createTask_shouldEpicSubtasksBeEmptyWhenSubtaskCreatedWithWrongId() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask = manager.createTask(new SubTask("subtask", "description", 3));
        assertTrue(epic.getEpicSubs().isEmpty());
    }

    @Test
    void createTask_calculateDuration() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:43", 357));
        SubTask subtask2 = manager.createTask(new SubTask("subtask2", "description",
                1, "23.01.2023 17:16", 78));
        SubTask subtask3 = manager.createTask(new SubTask("subtask3", "description",
                1, "27.01.2023 18:01", 78));
        assertEquals(8736, epic.getDuration().toMinutes());
    }

    @Test
    void createTask_shouldThrowCreateExceptionWhenTryToCreateTaskThatHasTimeIntersection() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:43", 357));
        ManagerCreateException ex = assertThrows(ManagerCreateException.class, () -> manager.createTask(
                new SubTask("subtask1", "description",
                        1, "21.01.2023 19:20", 357)));
        assertEquals("Задачи пересекаются по времени", ex.getMessage());
    }

    @Test
    void createTask_shouldThrowCreateExceptionBorderCase() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:00", 60));
        ManagerCreateException ex = assertThrows(ManagerCreateException.class, () -> manager.createTask(
                new SubTask("subtask1", "description",
                        1, "21.01.2023 17:59", 357)));
        assertEquals("Задачи пересекаются по времени", ex.getMessage());
    }

    @Test
    void createTask_shouldNotThrowCreateExceptionWhenTryToCreateTaskWithNoTimeIntersect() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:00", 60));
        assertDoesNotThrow(() -> manager.createTask(
                new SubTask("subtask1", "description",
                        1, "21.01.2023 18:00", 357)));
    }

    @Test
    void clearAllTasks_shouldMapOfTasksBeEmptyWhenClearAllTasksIsCalled() {
        Task task1 = manager.createTask(new Task("Task1", "description1"));
        Task task2 = manager.createTask(new Task("Task2", "description2"));
        Task task3 = manager.createTask(new Task("Task3", "description3"));
        manager.clearAllTasks();
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void clearAllTasks_shouldMapOfTasksBeEmptyWhenClearAllTasksCalledAndNoneOfTasksWasCreated() {
        manager.clearAllTasks();
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void clearAllEpics_shouldMapOfEpicsBeEmptyWhenClearAllEpicsIsCalled() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        Epic epic2 = manager.createTask(new Epic("Epic2", "description2"));
        Epic epic3 = manager.createTask(new Epic("Epic3", "description3"));
        manager.clearAllEpics();
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    void clearAllEpics_shouldMapOfEpicsBeEmptyWhenClearAllEpicsCalledAndNoneOfEpicsWasCreated() {
        manager.clearAllEpics();
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    void clearAllEpics_shouldMapOfSubtasksBeEmptyWhenClearAllEpicsCalled() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask", "description", 1));
        SubTask subtask2 = manager.createTask(new SubTask("subtask", "description", 1));
        SubTask subtask3 = manager.createTask(new SubTask("subtask", "description", 1));
        manager.clearAllEpics();
        assertTrue(manager.getSubTasks().isEmpty());
    }

    //SUBTASK
    @Test
    void clearAllSubTasks_shouldMapOfSubtasksBeEmptyWhenClearAllSubTasksIsCalled() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask", "description", 1));
        SubTask subtask2 = manager.createTask(new SubTask("subtask", "description", 1));
        SubTask subtask3 = manager.createTask(new SubTask("subtask", "description", 1));
        manager.clearAllSubTasks();
        assertTrue(manager.getSubTasks().isEmpty());
    }

    @Test
    void clearAllSubTasks_shouldMapOfSubTasksBeEmptyWhenClearAllSubtasksCalledAndNoneOfSubTasksWasCreated() {
        manager.clearAllSubTasks();
        assertTrue(manager.getSubTasks().isEmpty());
    }

    @Test
    void clearAllSubTasks_shouldEpicStatusCalculateToNewWhenClearAllSubTasksIsCalled() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask", "description", 1));
        SubTask subtask2 = manager.createTask(new SubTask("subtask", "description", 1));
        SubTask subtask3 = manager.createTask(new SubTask("subtask", "description", 1));
        manager.setStatus(subtask1, TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, epic1.getStatus());

        manager.clearAllSubTasks();
        assertEquals(TaskStatus.NEW, epic1.getStatus());
    }

    @Test
    void getEpicById_shouldReturnEpicByItsId() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        assertEquals(epic1, manager.getEpicById(1));
    }

    @Test
    void getEpicById_shouldReturnNullWhenEpicWasNotCreated() {
        assertNull(manager.getEpicById(1));
    }

    @Test
    void getEpicById_shouldReturnNullWhenEpicIdIsIncorrect() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        assertNull(manager.getEpicById(2));
    }


    @Test
    void getTaskById_shouldReturnTaskByItsId() {
        Task task = manager.createTask(new Task("task", "description"));
        assertEquals(task, manager.getTaskById(1));
    }

    @Test
    void getTaskById_shouldReturnNullWhenTaskWasNotCreated() {
        assertNull(manager.getTaskById(1));
    }

    @Test
    void getTaskById_shouldReturnNullWhenTaskIdIsIncorrect() {
        Task task = manager.createTask(new Task("task", "description"));
        assertNull(manager.getEpicById(2));
    }

    @Test
    void getSubTaskById_shouldReturnSubTaskByItsId() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        SubTask subtask = manager.createTask(new SubTask("subtask", "description", 1));
        assertEquals(subtask, manager.getSubTaskById(2));
    }

    @Test
    void getSubTaskById_shouldReturnNullWhenSubTaskWasNotCreated() {
        assertNull(manager.getSubTaskById(2));
    }

    @Test
    void getSubTaskById_shouldReturnNullWhenSubTaskIdIsIncorrect() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        SubTask subtask = manager.createTask(new SubTask("subtask", "description", 1));
        assertNull(manager.getSubTaskById(3));
    }

    @Test
    void getTasks_shouldReturnAllTasksFromMap() {
        Task task1 = manager.createTask(new Task("task1", "description"));
        Task task2 = manager.createTask(new Task("task2", "description"));
        Task task3 = manager.createTask(new Task("task3", "description"));
        assertEquals(task1, manager.getTasks().get(1));
        assertEquals(task2, manager.getTasks().get(2));
        assertEquals(task3, manager.getTasks().get(3));
    }

    @Test
    void getTasks_shouldReturnEmptyMapWhenTasksWereNotCreated() {
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void getTasks_shouldReturnNullWhenTasksIdIsIncorrect() {
        Task task1 = manager.createTask(new Task("task1", "description"));
        Task task2 = manager.createTask(new Task("task2", "description"));
        assertNull(manager.getTasks().get(3));
    }

    @Test
    void getEpics_shouldReturnAllEpicsFromMap() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        Epic epic2 = manager.createTask(new Epic("Epic2", "description1"));
        assertEquals(epic1, manager.getEpics().get(1));
        assertEquals(epic2, manager.getEpics().get(2));
    }

    @Test
    void getEpics_shouldReturnEmptyMapWhenEpicsWereNotCreated() {
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    void getEpics_shouldReturnNullWhenEpicIdIsIncorrect() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        Epic epic2 = manager.createTask(new Epic("Epic2", "description1"));
        assertNull(manager.getEpics().get(3));
    }

    @Test
    void getSubTasks_shouldReturnAllSubsFromMap() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description", 1));
        SubTask subtask2 = manager.createTask(new SubTask("subtask2", "description", 1));
        assertEquals(subtask1, manager.getSubTasks().get(2));
        assertEquals(subtask2, manager.getSubTasks().get(3));
    }

    @Test
    void getSubtasks_shouldReturnEmptyMapWhenSubsWereNotCreated() {
        assertTrue(manager.getSubTasks().isEmpty());
    }

    @Test
    void getSubtasks_shouldReturnNullWhenSubtaskIdIsIncorrect() {
        Epic epic1 = manager.createTask(new Epic("Epic1", "description1"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description", 1));
        assertNull(manager.getSubTasks().get(3));
    }

    @Test
    void getStartTime_StartTimeOfEpicAndSubShouldBeEqualsWhenSubTaskIsSingle() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:43", 357));

        assertEquals(epic.getStartTime(), subtask1.getStartTime());
    }

    @Test
    void getStartTime_StartTimeOfEpicAndSubShouldNoTBeEqualsWhenSubTasksAreNotSingle() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:43", 357));
        SubTask subtask2 = manager.createTask(new SubTask("subtask2", "description",
                1, "23.01.2023 17:16", 78));

        assertNotEquals(epic.getStartTime(), subtask2.getStartTime());
    }

    @Test
    void getStartTime_shouldReturnNullWhenEpicDoesNotHaveSubtasks() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        assertNull(epic.getStartTime());
    }

    @Test
    void getEndTime_EndTimeOfEpicAndSubShouldBeEqualsWhenSubTaskIsSingle() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:43", 357));
        assertEquals(epic.getEndTime(), subtask1.getEndTime());
    }

    @Test
    void getEndTime_EndTimeOfEpicAndSubShouldNotBeEqualsWhenSubTasksAreNotSingle() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:43", 357));
        SubTask subtask2 = manager.createTask(new SubTask("subtask2", "description",
                1, "23.01.2023 17:16", 78));
        assertNotEquals(epic.getEndTime(), subtask1.getEndTime());
        assertEquals("23.01.2023 18:34", epic.getEndTime().format(Managers.DATE_TIME_FORMATTER));
    }

    @Test
    void getEndTime_shouldReturnNullWhenEpicDoesNotHaveSubtasksOrSubsDoesNotHaveTime() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description", 1));
        assertNull(epic.getEndTime());
    }

    @Test
    void getEndTime_EpicEndTimeShouldUpdateToBeNullWhenSubWithTimeWasRemoved() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description", 1));
        SubTask subtask2 = manager.createTask(new SubTask("subtask2", "description",
                1, "23.01.2023 17:16", 78));
        assertNotNull(epic.getEndTime());
        manager.removeSubTask(subtask2);
        assertNull(epic.getEndTime());

    }

    @Test
    void getDuration_shouldReturnNullWhenEpicDoesNotHaveSubtasksOrSubsDoesNotHaveTime() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description", 1));
        assertNull(epic.getDuration());
    }

    @Test
    void getDuration_DurationOfEpicAndSubShouldNotBeEqualsWhenSubTasksAreNotSingle() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:43", 357));
        SubTask subtask2 = manager.createTask(new SubTask("subtask2", "description",
                1, "23.01.2023 17:16", 78));
        assertNotEquals(epic.getDuration(), subtask1.getDuration());
        assertEquals(2931, epic.getDuration().toMinutes());
    }

    @Test
    void getDuration_DurationOfEpicAndSubShouldBeEqualsWhenSubTaskIsSingle() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:43", 357));
        assertEquals(epic.getDuration(), subtask1.getDuration());
    }

    @Test
    void getPrioritizedTasks_shouldReturnSortedSubTasks() {
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:43", 357));
        SubTask subtask2 = manager.createTask(new SubTask("subtask2", "description", 1));
        assertEquals("[model.SubTask{name='subtask1', " +
                "description='description', id=1, status=NEW}, " +
                "model.SubTask{name='subtask2', " +
                "description='description', id=2, status=NEW}]", manager.getPrioritizedTasks().toString());
    }

    @Test
    void getPrioritizedTasks_shouldReturnTrueWhenNoTaskWasCreated() {
        assertTrue(manager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void getPrioritizedTasks_shouldReturnSetWithoutRemovedSubTask() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:43", 357));
        SubTask subtask2 = manager.createTask(new SubTask("subtask2", "description", 1));
        manager.removeSubTask(subtask2);
        assertEquals(2, manager.getPrioritizedTasks().size());
    }

    @Test
    void getPrioritizedTasks_shouldReturnEmptySetWhenEpicIsRemoved() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:43", 357));
        SubTask subtask2 = manager.createTask(new SubTask("subtask2", "description", 1));
        manager.removeEpic(epic);
        assertEquals(0, manager.getPrioritizedTasks().size());
    }

    @Test
    void getPrioritizedTasks_shouldReturnTheSameSubtaskAndEpicWhenItWasChanged() {
        Epic epic = manager.createTask(new Epic("Epic", "description"));
        SubTask subtask1 = manager.createTask(new SubTask("subtask1", "description",
                1, "21.01.2023 17:43", 357));
        manager.setStatus(subtask1, TaskStatus.IN_PROGRESS);
        for (Task task : manager.getPrioritizedTasks()) {
            if (task.getClass() == SubTask.class) {
                SubTask sub = (SubTask) task;
                assertEquals(subtask1, sub);
            } else {
                Epic epic1 = (Epic) task;
                assertEquals(epic, epic1);
                assertEquals(TaskStatus.IN_PROGRESS, epic1.getStatus());
            }
        }
    }
}
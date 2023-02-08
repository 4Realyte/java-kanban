

import org.junit.jupiter.api.BeforeEach;
import service.managers.InMemoryTaskManager;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    void beforeEach() {
        manager = new InMemoryTaskManager();
    }
}

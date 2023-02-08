package service.managers;

import model.Task;
import service.managers.supportServices.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final CustomLinkedList historyWatchList = new CustomLinkedList();

    class CustomLinkedList {
        private Node head;
        private Node tail;
        private int size = 0;

        private HashMap<Integer, Node> map = new HashMap<>();

        public int size() {
            return this.size;
        }

        public Node checkNodeInMap(int id) {
            if (map.containsKey(id)) {
                return map.get(id);
            }
            return null;
        }

        public void linkLast(Task element) {
            final Node oldTail = tail;
            final Node newNode = new Node(element, null, tail);
            Task task = element;
            map.put(task.getId(), newNode);
            tail = newNode;
            if (oldTail == null) {
                head = newNode;
            } else {
                oldTail.setNext(newNode);
            }
            size++;
        }

        public Task removeNode(Node node) {
            final Task element = node.getData();
            final Node next = node.getNext();
            final Node prev = node.getPrev();
            map.remove(element.getId());

            if (next == null) {
                tail = prev;
            } else {
                next.setPrev(prev);
                node.setNext(null);
            }
            if (prev == null) {
                head = next;
            } else {
                prev.setNext(next);
                node.setPrev(null);
            }
            node.setData(null);
            size--;

            return element;
        }

        public ArrayList<Task> getTasks() {
            Node curHead = head;
            ArrayList<Task> taskList = new ArrayList<>();
            while (curHead != null) {
                taskList.add(curHead.getData());
                curHead = curHead.getNext();
            }
            return taskList;
        }
    }

    @Override
    public void add(Task task) {
        Node node = historyWatchList.checkNodeInMap(task.getId());
        if (node != null) {
            historyWatchList.removeNode(node);
        }
        historyWatchList.linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node node = historyWatchList.checkNodeInMap(id);
        if (node != null) {
            historyWatchList.removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyWatchList.getTasks();
    }
}

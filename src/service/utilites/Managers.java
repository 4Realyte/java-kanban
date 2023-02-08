package service.utilites;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import service.managers.*;

import java.time.format.DateTimeFormatter;

public  class Managers {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private Managers() {
    }

    public static TaskManager getDefault() {
        return new HttpTaskManager("http://localhost:8078/");
    }
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
    public static Gson getDefaultGson(){
        return new GsonBuilder()
                //.setPrettyPrinting()
                .create();
    }
}

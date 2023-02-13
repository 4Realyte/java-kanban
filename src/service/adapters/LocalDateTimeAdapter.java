package service.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import service.utilites.Managers;

import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        if (localDateTime != null) {
            jsonWriter.value(localDateTime.format(Managers.DATE_TIME_FORMATTER));
        } else {
            jsonWriter.value((String) null);
        }
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        String jsonDate = jsonReader.nextString();
        return jsonDate == null ? null : LocalDateTime.parse(jsonDate, Managers.DATE_TIME_FORMATTER);
    }
}

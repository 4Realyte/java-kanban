package service.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        if (duration != null) {
            jsonWriter.value(duration.toMinutes());
        } else {
            jsonWriter.value((Number) null);
        }
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        Long duration = jsonReader.nextLong();
        return duration == null ? null : Duration.ofMinutes(duration);
    }
}

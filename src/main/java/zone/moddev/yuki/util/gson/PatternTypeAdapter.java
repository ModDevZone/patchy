package zone.moddev.yuki.util.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.regex.Pattern;

public class PatternTypeAdapter extends TypeAdapter<Pattern> {

    @Override
    public void write(final JsonWriter out, final Pattern value) throws IOException {
        out.value(value.toString());
    }

    @Override
    public Pattern read(final JsonReader in) throws IOException {
        return Pattern.compile(in.nextString());
    }
}
package property.abolish.archery.utilities;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OSM {
    public static JsonElement httpGetAsJson(String url) throws IOException {
        final HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
        urlConnection.setRequestMethod("GET");

        InputStream inputStream;
        try {
            inputStream = urlConnection.getInputStream();
        } catch (IOException e) {
            inputStream = urlConnection.getErrorStream();
        }

        try (final InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(streamReader);
        }
    }
}

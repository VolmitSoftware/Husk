package art.arcane.husk.core;

import art.arcane.volmlib.util.config.ConfigJson;
import art.arcane.volmlib.util.io.AtomicFileIO;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class HuskRuntime implements AutoCloseable {
    private final RuntimeEnvironment environment;
    private volatile Settings settings;

    public HuskRuntime(RuntimeEnvironment environment) {
        this.environment = Objects.requireNonNull(environment, "environment");
    }

    public synchronized void start() throws IOException {
        if (settings != null) {
            throw new IllegalStateException(BuildIdentity.NAME + " is already running");
        }
        if (!BuildIdentity.MINECRAFT_VERSIONS.contains(environment.minecraftVersion())) {
            throw new IOException("Unsupported Minecraft version: " + environment.minecraftVersion());
        }

        Path config = environment.dataDirectory().resolve("config.json");
        Settings loaded;
        if (Files.exists(config)) {
            loaded = readSettings(config);
        } else {
            loaded = new Settings("Hello from " + BuildIdentity.NAME);
            AtomicFileIO.writeString(config, ConfigJson.toJson(loaded, true) + System.lineSeparator());
        }
        settings = loaded;
    }

    @Override
    public synchronized void close() {
        settings = null;
    }

    public String status() {
        Settings activeSettings = settings;
        if (activeSettings == null) {
            throw new IllegalStateException(BuildIdentity.NAME + " is not running");
        }
        return BuildIdentity.NAME + " " + BuildIdentity.VERSION + " | " + environment.platform()
                + " | Minecraft " + environment.minecraftVersion() + " | " + activeSettings.statusMessage();
    }

    public boolean isRunning() {
        return settings != null;
    }

    private Settings readSettings(Path config) throws IOException {
        try {
            String content = Files.readString(config);
            validateJson(content);
            Settings loaded = ConfigJson.fromJson(content, Settings.class);
            if (loaded == null || loaded.statusMessage() == null || loaded.statusMessage().isBlank()) {
                throw new IOException("statusMessage must be a nonblank string");
            }
            return loaded;
        } catch (IOException | JsonParseException | IllegalStateException exception) {
            throw new IOException("Cannot load " + config + ": " + exception.getMessage(), exception);
        }
    }

    private void validateJson(String content) throws IOException {
        try (JsonReader reader = new JsonReader(new StringReader(content))) {
            reader.setLenient(false);
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("statusMessage") && reader.peek() != JsonToken.STRING) {
                    throw new IOException("statusMessage must be a string");
                }
                reader.skipValue();
            }
            reader.endObject();
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                throw new IOException("Unexpected content after configuration object");
            }
        }
    }

    private record Settings(String statusMessage) {
    }
}

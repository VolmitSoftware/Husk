package art.arcane.husk.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HuskRuntimeTest {
    @TempDir
    Path directory;

    @Test
    void createsDefaultConfigurationAndStarts() throws IOException {
        Path dataDirectory = directory.resolve("nested/data");
        HuskRuntime runtime = new HuskRuntime(new RuntimeEnvironment(Platform.BUKKIT, "26.2", dataDirectory));

        assertFalse(runtime.isRunning());
        runtime.start();

        assertAll(
                () -> assertTrue(runtime.isRunning()),
                () -> assertTrue(runtime.status().contains("Hello from " + BuildIdentity.NAME)),
                () -> assertTrue(Files.readString(dataDirectory.resolve("config.json")).contains("Hello from " + BuildIdentity.NAME))
        );
    }

    @ParameterizedTest
    @EnumSource(Platform.class)
    void reportsPlatformAndBuildIdentity(Platform platform) throws IOException {
        HuskRuntime runtime = new HuskRuntime(new RuntimeEnvironment(platform, "26.2", directory));
        runtime.start();

        String status = runtime.status();

        assertAll(
                () -> assertTrue(status.contains(BuildIdentity.NAME)),
                () -> assertTrue(status.contains(BuildIdentity.VERSION)),
                () -> assertTrue(status.contains(platform.name())),
                () -> assertTrue(status.contains("26.2"))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.20.1", "1.21.1", "26.1.2", "26.2", "26.3"})
    void acceptsSupportedMinecraftVersions(String minecraftVersion) throws IOException {
        HuskRuntime runtime = new HuskRuntime(new RuntimeEnvironment(Platform.FABRIC, minecraftVersion, directory));

        runtime.start();

        assertTrue(runtime.isRunning());
        assertTrue(runtime.status().contains(minecraftVersion));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.20", "1.20.2", "1.21", "1.21.2", "26.1", "26.1.1", "26.2.1", "26.3.1", "26.4", "1.21.11"})
    void rejectsUnsupportedMinecraftBeforeWriting(String minecraftVersion) {
        Path dataDirectory = directory.resolve("unsupported");
        HuskRuntime runtime = new HuskRuntime(new RuntimeEnvironment(Platform.FORGE, minecraftVersion, dataDirectory));

        IOException failure = assertThrows(IOException.class, runtime::start);

        assertAll(
                () -> assertTrue(failure.getMessage().contains(minecraftVersion)),
                () -> assertFalse(runtime.isRunning()),
                () -> assertFalse(Files.exists(dataDirectory))
        );
    }

    @Test
    void preservesExistingSettingsAndCachesStatus() throws IOException {
        Path config = directory.resolve("config.json");
        String original = "{\n  \"statusMessage\": \"Configured message\"\n}\n";
        Files.writeString(config, original);
        HuskRuntime runtime = new HuskRuntime(new RuntimeEnvironment(Platform.NEOFORGE, "26.3", directory));

        runtime.start();

        assertEquals(original, Files.readString(config));
        Files.delete(config);
        assertTrue(runtime.status().contains("Configured message"));
        assertFalse(Files.exists(config));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "null",
            "{}",
            "{\"statusMessage\":null}",
            "{\"statusMessage\":\"\"}",
            "{\"statusMessage\":\"   \"}",
            "{\"statusMessage\":42}",
            "{\"statusMessage\":true}",
            "{\"statusMessage\":[]}",
            "{\"statusMessage\":\"broken\"",
            "{statusMessage:\"unquoted key\"}",
            "{\"statusMessage\":'single quotes'}",
            "{\"statusMessage\":\"valid\"} trailing",
            "{\"statusMessage\":\"valid\",}",
            "// comment\n{\"statusMessage\":\"valid\"}",
            "[]",
            ""
    })
    void rejectsInvalidConfigWithoutOverwriting(String content) throws IOException {
        Path config = directory.resolve("config.json");
        Files.writeString(config, content);
        HuskRuntime runtime = new HuskRuntime(new RuntimeEnvironment(Platform.BUKKIT, "26.2", directory));

        IOException failure = assertThrows(IOException.class, runtime::start);

        assertAll(
                () -> assertFalse(runtime.isRunning()),
                () -> assertEquals(content, Files.readString(config)),
                () -> assertTrue(failure.getMessage().contains("config.json"))
        );
    }

    @Test
    void refusesSecondStartWithoutDiscardingRunningSettings() throws IOException {
        HuskRuntime runtime = new HuskRuntime(new RuntimeEnvironment(Platform.FABRIC, "26.2", directory));
        runtime.start();
        String status = runtime.status();

        assertThrows(IllegalStateException.class, runtime::start);

        assertTrue(runtime.isRunning());
        assertEquals(status, runtime.status());
    }

    @Test
    void closeIsIdempotentAndRestartReadsCurrentConfiguration() throws IOException {
        HuskRuntime runtime = new HuskRuntime(new RuntimeEnvironment(Platform.FORGE, "26.2", directory));
        runtime.close();
        runtime.start();

        runtime.close();
        runtime.close();

        assertFalse(runtime.isRunning());
        assertThrows(IllegalStateException.class, runtime::status);
        Files.writeString(directory.resolve("config.json"), "{\"statusMessage\":\"Restarted\"}");
        runtime.start();
        assertTrue(runtime.isRunning());
        assertTrue(runtime.status().contains("Restarted"));
    }

    @Test
    void canStartAfterConfigurationFailureIsCorrected() throws IOException {
        Path config = directory.resolve("config.json");
        Files.writeString(config, "{}");
        HuskRuntime runtime = new HuskRuntime(new RuntimeEnvironment(Platform.NEOFORGE, "26.2", directory));
        assertThrows(IOException.class, runtime::start);
        Files.writeString(config, "{\"statusMessage\":\"Recovered\"}");

        runtime.start();

        assertTrue(runtime.isRunning());
        assertTrue(runtime.status().contains("Recovered"));
    }

    @Test
    void filesystemFailureLeavesRuntimeStopped() throws IOException {
        Path dataDirectory = directory.resolve("file");
        Files.writeString(dataDirectory, "existing file");
        HuskRuntime runtime = new HuskRuntime(new RuntimeEnvironment(Platform.BUKKIT, "26.2", dataDirectory));

        assertThrows(IOException.class, runtime::start);

        assertFalse(runtime.isRunning());
        assertEquals("existing file", Files.readString(dataDirectory));
    }

    @Test
    void environmentRejectsMissingValues() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new RuntimeEnvironment(null, "26.2", directory)),
                () -> assertThrows(NullPointerException.class, () -> new RuntimeEnvironment(Platform.BUKKIT, null, directory)),
                () -> assertThrows(NullPointerException.class, () -> new RuntimeEnvironment(Platform.BUKKIT, "26.2", null)),
                () -> assertThrows(IllegalArgumentException.class, () -> new RuntimeEnvironment(Platform.BUKKIT, " ", directory)),
                () -> assertThrows(NullPointerException.class, () -> new HuskRuntime(null))
        );
    }
}

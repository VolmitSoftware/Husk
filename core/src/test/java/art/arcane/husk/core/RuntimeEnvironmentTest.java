package art.arcane.husk.core;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RuntimeEnvironmentTest {
    @TempDir
    Path directory;

    @ParameterizedTest
    @CsvSource({
            "26.2-R0.1-SNAPSHOT,26.2",
            "26.1.2.build.74-stable,26.1.2",
            "26.3.build.9-alpha,26.3",
            "26.3-R0.1-SNAPSHOT,26.3",
            "26.2,26.2"
    })
    void extractsMinecraftReleaseFromBukkitServerVersion(String serverVersion, String minecraftVersion) {
        RuntimeEnvironment environment = RuntimeEnvironment.bukkit(serverVersion, directory);

        assertAll(
                () -> assertEquals(Platform.BUKKIT, environment.platform()),
                () -> assertEquals(minecraftVersion, environment.minecraftVersion()),
                () -> assertEquals(directory, environment.dataDirectory())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Paper 26.2", " 26.2", "unknown", "-26.2"})
    void rejectsServerVersionWithoutLeadingRelease(String serverVersion) {
        assertThrows(IllegalArgumentException.class, () -> RuntimeEnvironment.bukkit(serverVersion, directory));
    }
}

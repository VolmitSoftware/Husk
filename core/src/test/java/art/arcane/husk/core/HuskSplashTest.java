package art.arcane.husk.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HuskSplashTest {
    @TempDir
    Path directory;

    @ParameterizedTest
    @EnumSource(Platform.class)
    void rendersStartupIdentityForEachLoader(Platform platform) {
        RuntimeEnvironment environment = new RuntimeEnvironment(platform, "26.3", directory);

        String splash = HuskSplash.render(environment, "Example server (MC: 26.3)", true);

        assertAll(
                () -> assertTrue(splash.contains(BuildIdentity.NAME)),
                () -> assertTrue(splash.contains("Version: " + BuildIdentity.VERSION)),
                () -> assertTrue(splash.contains("Platform: " + platform.name())),
                () -> assertTrue(splash.contains("Minecraft: 26.3")),
                () -> assertTrue(splash.contains("MC Support: 1.20.1, 1.21.1, 26.1.2, 26.2, 26.3")),
                () -> assertTrue(splash.contains("Server: Example server")),
                () -> assertFalse(splash.contains("(MC:")),
                () -> assertTrue(splash.contains("Java: " + Runtime.version().feature())),
                () -> assertTrue(splash.contains("Date: " + LocalDate.now())),
                () -> assertTrue(splash.contains("Startup: READY")),
                () -> assertEquals(6L, splash.lines().filter(line -> !line.isBlank()).count()),
                () -> assertFalse(splash.contains("\u001b")),
                () -> assertFalse(splash.contains("\u00a7"))
        );
    }

    @Test
    void failureBannerDoesNotReportReady() {
        RuntimeEnvironment environment = new RuntimeEnvironment(Platform.BUKKIT, "26.3", directory);

        String splash = HuskSplash.render(environment, "Example server", false);

        assertTrue(splash.contains("Startup: FAILED"));
        assertFalse(splash.contains("Startup: READY"));
    }
}

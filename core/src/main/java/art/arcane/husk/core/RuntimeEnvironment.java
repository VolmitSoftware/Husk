package art.arcane.husk.core;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record RuntimeEnvironment(Platform platform, String minecraftVersion, Path dataDirectory) {
    private static final Pattern BUKKIT_VERSION = Pattern.compile("^[0-9]+(?:\\.[0-9]+)*");

    public RuntimeEnvironment {
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(minecraftVersion, "minecraftVersion");
        Objects.requireNonNull(dataDirectory, "dataDirectory");
        if (minecraftVersion.isBlank()) {
            throw new IllegalArgumentException("Minecraft version cannot be blank");
        }
    }

    public static RuntimeEnvironment bukkit(String bukkitVersion, Path dataDirectory) {
        Matcher matcher = BUKKIT_VERSION.matcher(Objects.requireNonNull(bukkitVersion, "bukkitVersion"));
        if (!matcher.find()) {
            throw new IllegalArgumentException("Bukkit version has no Minecraft release: " + bukkitVersion);
        }
        return new RuntimeEnvironment(Platform.BUKKIT, matcher.group(), dataDirectory);
    }
}

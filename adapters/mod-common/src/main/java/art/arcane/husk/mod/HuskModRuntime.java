package art.arcane.husk.mod;

import art.arcane.husk.core.BuildIdentity;
import art.arcane.husk.core.HuskRuntime;
import art.arcane.husk.core.HuskSplash;
import art.arcane.husk.core.Platform;
import art.arcane.husk.core.RuntimeEnvironment;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;

public final class HuskModRuntime {
    private static final Logger LOGGER = LoggerFactory.getLogger(BuildIdentity.ID);

    private final Platform platform;
    private final Path dataDirectory;
    private HuskRuntime runtime;

    public HuskModRuntime(Platform platform, Path configDirectory) {
        this.platform = Objects.requireNonNull(platform, "platform");
        this.dataDirectory = Objects.requireNonNull(configDirectory, "configDirectory").resolve(BuildIdentity.ID);
    }

    public void start() {
        if (runtime != null) {
            throw new IllegalStateException(BuildIdentity.NAME + " is already running");
        }
        RuntimeEnvironment environment = new RuntimeEnvironment(
            platform, SharedConstants.getCurrentVersion().name(), dataDirectory);
        HuskRuntime started = new HuskRuntime(environment);
        try {
            started.start();
            runtime = started;
            LOGGER.info(HuskSplash.render(environment,
                platform + " / Minecraft " + environment.minecraftVersion(), true));
        } catch (IOException exception) {
            LOGGER.error("Could not start " + BuildIdentity.NAME, exception);
            throw new UncheckedIOException("Could not start " + BuildIdentity.NAME, exception);
        } catch (RuntimeException exception) {
            LOGGER.error("Could not start " + BuildIdentity.NAME, exception);
            throw exception;
        }
    }

    public void stop() {
        HuskRuntime active = runtime;
        runtime = null;
        if (active == null) {
            return;
        }
        try {
            active.close();
            LOGGER.info("{} stopped", BuildIdentity.NAME);
        } catch (RuntimeException exception) {
            LOGGER.error("Could not stop " + BuildIdentity.NAME, exception);
            throw exception;
        }
    }

    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(BuildIdentity.ID)
            .executes(context -> status(context.getSource()))
            .then(Commands.literal("status").executes(context -> status(context.getSource()))));
    }

    private int status(CommandSourceStack source) {
        HuskRuntime active = runtime;
        if (active == null || !active.isRunning()) {
            source.sendFailure(Component.literal(BuildIdentity.NAME + " is not running."));
            return 0;
        }
        String message = active.status();
        source.sendSuccess(() -> Component.literal(message), false);
        return 1;
    }
}

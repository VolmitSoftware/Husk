package art.arcane.husk.neoforge;

import art.arcane.husk.core.BuildIdentity;
import art.arcane.husk.core.Platform;
import art.arcane.husk.mod.HuskModRuntime;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(BuildIdentity.ID)
public final class HuskNeoForge {
    private final HuskModRuntime runtime = new HuskModRuntime(Platform.NEOFORGE, FMLPaths.CONFIGDIR.get());

    public HuskNeoForge() {
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::start);
        NeoForge.EVENT_BUS.addListener(this::stop);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        runtime.registerCommands(event.getDispatcher());
    }

    private void start(ServerStartedEvent event) {
        runtime.start();
    }

    private void stop(ServerStoppingEvent event) {
        runtime.stop();
    }
}

package art.arcane.husk.fabric;

import art.arcane.husk.core.Platform;
import art.arcane.husk.mod.HuskModRuntime;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

public final class HuskFabric implements ModInitializer {
    private final HuskModRuntime runtime = new HuskModRuntime(
        Platform.FABRIC, FabricLoader.getInstance().getConfigDir());

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            runtime.registerCommands(dispatcher));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> runtime.start());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> runtime.stop());
    }
}

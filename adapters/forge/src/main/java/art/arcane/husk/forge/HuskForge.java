package art.arcane.husk.forge;

import art.arcane.husk.core.BuildIdentity;
import art.arcane.husk.core.Platform;
import art.arcane.husk.mod.HuskModRuntime;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(BuildIdentity.ID)
public final class HuskForge {
    private final HuskModRuntime runtime = new HuskModRuntime(Platform.FORGE, FMLPaths.CONFIGDIR.get());

    public HuskForge(FMLJavaModLoadingContext context) {
        RegisterCommandsEvent.BUS.addListener(event -> runtime.registerCommands(event.getDispatcher()));
        ServerStartedEvent.BUS.addListener(event -> runtime.start());
        ServerStoppingEvent.BUS.addListener(event -> runtime.stop());
    }
}

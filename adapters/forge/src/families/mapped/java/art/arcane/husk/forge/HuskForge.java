package art.arcane.husk.forge;

import art.arcane.husk.core.BuildIdentity;
import art.arcane.husk.core.Platform;
import art.arcane.husk.mod.HuskModRuntime;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(BuildIdentity.ID)
public final class HuskForge {
    private final HuskModRuntime runtime = new HuskModRuntime(Platform.FORGE, FMLPaths.CONFIGDIR.get());

    public HuskForge() {
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> runtime.registerCommands(event.getDispatcher()));
        MinecraftForge.EVENT_BUS.addListener((ServerStartedEvent event) -> runtime.start());
        MinecraftForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> runtime.stop());
    }
}

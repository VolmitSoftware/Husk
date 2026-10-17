package art.arcane.husk.mod;

import net.minecraft.SharedConstants;

final class MinecraftVersion {
    private MinecraftVersion() {
    }

    static String current() {
        return SharedConstants.getCurrentVersion().getName();
    }
}

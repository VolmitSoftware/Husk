package art.arcane.husk.core;

import art.arcane.volmlib.util.plugin.SplashScreenSupport;

import java.util.List;
import java.util.Objects;

public final class HuskSplash {
    private static final List<String> LOGO = List.of(
            "██╗  ██╗██╗   ██╗███████╗██╗  ██╗",
            "██║  ██║██║   ██║██╔════╝██║ ██╔╝",
            "███████║██║   ██║███████╗█████╔╝ ",
            "██╔══██║██║   ██║╚════██║██╔═██╗ ",
            "██║  ██║╚██████╔╝███████║██║  ██╗",
            "╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝"
    );

    private HuskSplash() {
    }

    public static String render(RuntimeEnvironment environment, String serverDescription, boolean ready) {
        Objects.requireNonNull(environment, "environment");
        String server = SplashScreenSupport.serverVersionWithoutMcSuffix(
                Objects.requireNonNull(serverDescription, "serverDescription"));
        List<String> supported = BuildIdentity.MINECRAFT_VERSIONS;
        String supportRange = String.join(", ", supported);
        String[] details = {
                "Platform: " + environment.platform() + " | Minecraft: " + environment.minecraftVersion(),
                BuildIdentity.NAME + ", Plugin & Mod Template ["
                        + SplashScreenSupport.releaseTrain(BuildIdentity.VERSION) + " RELEASE]",
                "Version: " + BuildIdentity.VERSION,
                "By: Volmit Software (Arcane Arts) | VolmitSoftware.com | Startup: " + (ready ? "READY" : "FAILED"),
                "Server: " + server + " | MC Support: " + supportRange,
                "Java: " + SplashScreenSupport.javaMajorVersion() + " | Date: " + SplashScreenSupport.startupDate()
        };
        StringBuilder output = new StringBuilder("\n");
        for (int row = 0; row < LOGO.size(); row++) {
            output.append(LOGO.get(row)).append("   ").append(details[row]).append('\n');
        }
        return output.toString();
    }
}

# Changelog

## x.x.x

### Added

- Shared runtime with typed atomic configuration and a status command across Spigot, Paper, Folia, Fabric, Forge, and NeoForge.
- Minecraft 26.1.2, 26.2, and 26.3 build targets with independent mod-loader builds, version source overrides, and local VolmLib resolution.
- Volmit startup splash and platform/version artifact names.
- Configurable project identity with isolated packaged Java namespaces for derived mods.
- Full distribution builds, configuration tests, artifact checks, and a reusable gameplay command scenario.
- Minecraft 1.20.1 and 1.21.1 targets with mandatory Forge support, mapped Fabric/Forge artifacts, and NeoForge 1.21.1 support.
- Shared Forge/NeoForge 1.20.1 artifact and grouped source overrides for older Minecraft APIs.

### Changed

- Compile the shared runtime and Bukkit adapter for Java 17, with mod adapters targeting Java 17, 21, or 25 for their Minecraft release.
- List exact supported releases in the loader splash and collect all fifteen distributions with the existing platform/version naming convention.

### Fixed

- Prevent shaded jars from declaring nonexistent multi-release entries that stop older Forge loaders from starting.

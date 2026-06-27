# CLAUDE.md

Dont add any comment to the code

Dont try to compile the project, just ask the user to do it

Change the version on plugin.yml to SNAPSHOT-26w{WEEK_OF_YEAR} and a letter for the version, eg `SNAPSHOT-26w24a` for the first build of week 24, `SNAPSHOT-26w24b` for the second build of week 24, etc.

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Pendulum is a PaperMC plugin for a Spanish-language Minecraft survival/event server (Paper API `1.21.11`, Java 21). Code, comments, command output, and config keys are predominantly in Spanish — match that convention when editing.

## Build & Run

```bash
# One-time setup: paper-nms needs the remapped NMS artifact installed locally
mvn ca.bkaw:paper-nms-maven-plugin:1.4.10:init

# Build (defaultGoal is "clean package")
mvn
```

- The shade plugin relocates `com.zaxxer.hikari` → `org.pendulum.libs.hikari` and `org.postgresql` → `org.pendulum.libs.postgresql`. Any direct JDBC driver class reference must use the relocated name (see `DatabaseManager.setDriverClassName("org.pendulum.libs.postgresql.Driver")`).
- `maven-jar-plugin` is configured to write the built jar **directly into a hardcoded server path** (`C:\Users\PC\Proyectos\pendulum_server\plugins`) and sets `paperweight-mappings-namespace: mojang`. There is no separate test/lint setup; iteration is build → reload server.
- There are no unit tests in this repo.

## NMS / paper-nms

Custom mob AI uses Mojang-mapped NMS directly (`net.minecraft.*` imports, e.g. `NMSEntityUtils`, the `chargebase` mob classes). This depends on `paper-nms` (scope `provided`) and the `mojang` mappings namespace. Because mappings are version-pinned, bumping the Minecraft version requires updating `paper.api.version` / `paper.nms.version` in `pom.xml`, re-running `paper-nms:init`, and reviewing all `net.minecraft.*` usage.

Note: Maven groupId is `org.pendulum` but the Java package root is `org.delta`, and the main class is `org.delta.pendulum` (lowercase) — declared in `src/main/resources/plugin.yml`.

## Architecture

**Bootstrap.** `pendulum#onEnable` (`org.delta.pendulum`) is the composition root: it loads config, connects the DB, constructs all managers, then delegates listener wiring to `EventRegistry#registerAll` and command wiring to `registerCommands`. The plugin is designed to **degrade gracefully without a database** — a failed DB connection logs a warning and continues; feature code must tolerate `DatabaseManager` being unconnected.

**Configuration.** `settings.yml` (resource, copied to the data folder on first run) holds DB credentials, world settings, op list, and the full reto/castigo (challenge/punishment) definitions. `PendulumSettings` is a singleton parsed from that file in `load()`; it builds typed `Reto` subclasses (`RetoItem`, `RetoLogro`, `RetoMobs`, `RetoMinar`) from the `reto.retos` map list. Bukkit's own `config.yml` is also reloaded but `settings.yml` is the primary config.

**Commands.** A single `/pendulum` (alias `/pdl`) command. `PendulumCommand` dispatches `args[0]` to `SubCommand` implementations registered in `registerSubCommands()`. To add a command, implement `SubCommand` and add it there; `requiresPermission()` is gated by `pendulum.admin` OR membership in the `permisos` list from settings. `CommandCompletion` provides tab completion.

**Listeners.** All `Listener`s are registered centrally in `EventRegistry`, grouped by feature (Player, ChargeBase, Perks, TeamChest, Bingo, Spawns, Items, World Generation). Add new listeners there rather than registering them ad hoc.

**Persistence.** `DatabaseManager` owns a HikariCP pool to PostgreSQL (Supabase, `sslmode=require`) and exposes repositories (`players()`, `teams()`, plus bingo/death repositories under `database/repositories`). Per-player transient state (e.g. lives) is stored on the player's `PersistentDataContainer` via `NamespacedKey`, not the DB — see `LifeManager`.

### Feature subsystems (under `org.delta`)

- **death / lives** (`managers/death`, `listeners/death`, `listeners/player`) — 3-lives system (`LifeManager`), death messages, clock/pillar/chest death mechanics.
- **reto** (`managers/reto`, `libs/reto`, `listeners/player/RetoListener`) — timed team challenges with rewards/punishments driven by `PendulumSettings`.
- **bingo** (`managers/bingo`, `listeners/bingo`, `database/repositories/Bingo*`) — bingo cards with collect/kill/mine progress tracking, scoreboard sync to DB via `BingoSyncManager`.
- **chargebase** (`managers/chargebase`, `customs/mobs/chargebase`, `listeners/chargebase`) — base-defense waves of custom NMS mobs in role classes (atacante, defensor, healer, controlador, hibrido), each with basico/avanzado tiers and a dedicated behavior listener.
- **perks** (`managers/perks`, `listeners/perks`) — player perks; `BasePerkListener` is the shared base, concrete perks under `listeners/perks/impl`.
- **customs** (`customs/mobs`, `customs/craftings`) — custom mobs implement the `CustomMob` interface and are created through the `MobRegistry` factory map (key → `BiFunction<pendulum, Location, CustomMob>`); custom recipes via `CustomCrafting` / `CustomCraftingRegistry`.
- **worldgen** (`worldgen`) — `StructurePopulator` (a `BlockPopulator` added to NORMAL worlds) places JSON-defined structures (`JsonStructure`, `StructureDef`) with loot tables; `PendingEntitySpawner` handles deferred entity spawns from generated structures.
- **builders / libs** (`libs/builders`, `libs`) — `ItemBuilder`, `MobBuilder`, `CustomRecipeBuilder` fluent helpers; `MessageUtils` (color codes + `sendConsole`), `Icons` (Adventure `Component` glyphs).

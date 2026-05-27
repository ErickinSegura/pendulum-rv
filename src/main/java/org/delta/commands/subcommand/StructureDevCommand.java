package org.delta.commands.subcommand;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.worldgen.StructureDef;
import org.delta.worldgen.StructurePopulator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class StructureDevCommand implements SubCommand, Listener {

    private static final Material WAND_MATERIAL = Material.BLAZE_ROD;
    private static final String WAND_NAME = ChatColor.GOLD + "" + ChatColor.BOLD + "Structure Wand";

    private static final Map<UUID, Location> pos1Map = new HashMap<>();
    private static final Map<UUID, Location> pos2Map = new HashMap<>();

    private final StructurePopulator populator;
    private final File outputFolder;

    public StructureDevCommand(StructurePopulator populator, File pluginDataFolder) {
        this.populator = populator;
        this.outputFolder = new File(pluginDataFolder, "structure_exports");
        if (!this.outputFolder.exists()) this.outputFolder.mkdirs();
    }

    @Override
    public String getName() { return "structdev"; }

    @Override
    public boolean requiresPermission() { return false; }

    @Override
    public void execute(Player player, String[] args) {
        if (!isAdmin(player)) {
            player.sendMessage(MessageUtils.color("&cNo tienes permisos para usar structdev."));
            return;
        }

        if (args.length < 2) { showUsage(player); return; }


        switch (args[1].toLowerCase()) {
            case "wand"  -> giveWand(player);
            case "scan"  -> handleScan(player, args);
            case "spawn" -> handleSpawn(player, args);
            case "list"  -> handleList(player);
            default      -> showUsage(player);
        }
    }

    @Override
    public void showUsage(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        player.sendMessage(MessageUtils.color("&c&l⚠ Sintaxis incorrecta"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7Usos disponibles:"));
        player.sendMessage(MessageUtils.color("&8▪ &e/pendulum structdev wand &8- &7Obtener wand de selección"));
        player.sendMessage(MessageUtils.color("&8▪ &e/pendulum structdev scan <id> &8- &7Exportar selección a código Java"));
        player.sendMessage(MessageUtils.color("&8▪ &e/pendulum structdev spawn <id> &8- &7Spawnear estructura en tus pies"));
        player.sendMessage(MessageUtils.color("&8▪ &e/pendulum structdev list &8- &7Listar estructuras registradas"));
        player.sendMessage("");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (!isAdmin(player)) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isWand(item)) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        Location loc = event.getClickedBlock().getLocation();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            pos1Map.put(player.getUniqueId(), loc.clone());
            player.sendMessage(MessageUtils.color(
                    "&a✔ Pos1: &f(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")"
            ));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 1.2f);
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            pos2Map.put(player.getUniqueId(), loc.clone());
            player.sendMessage(MessageUtils.color(
                    "&b✔ Pos2: &f(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")"
            ));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 1.5f);
        }
    }

    private void giveWand(Player player) {
        ItemStack wand = new ItemStack(WAND_MATERIAL);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(WAND_NAME);
            wand.setItemMeta(meta);
        }
        player.getInventory().addItem(wand);
        player.sendMessage(MessageUtils.color("&6✦ Structure Wand entregado."));
        player.sendMessage(MessageUtils.color("&7  Click &aizquierdo &7→ Pos1  |  Click &bderecho &7→ Pos2"));
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.2f);
    }

    private void handleScan(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(MessageUtils.color("&cUso: &e/pendulum structdev scan <id>"));
            return;
        }

        UUID uuid = player.getUniqueId();


        if (!pos1Map.containsKey(uuid) || !pos2Map.containsKey(uuid)) {
            player.sendMessage(MessageUtils.color("&cPrimero selecciona dos esquinas con el wand."));
            player.sendMessage(MessageUtils.color("&7Usa &e/pendulum structdev wand &7para obtenerlo."));
            return;
        }

        String id = args[2].toLowerCase().replace(" ", "_");
        Location p1 = pos1Map.get(uuid);
        Location p2 = pos2Map.get(uuid);


        if (!p1.getWorld().equals(p2.getWorld())) {
            player.sendMessage(MessageUtils.color("&cLas dos posiciones deben estar en el mismo mundo."));
            return;
        }

        player.sendMessage(MessageUtils.color("&7Escaneando región..."));

        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int minY = Math.min(p1.getBlockY(), p2.getBlockY());
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX());
        int maxY = Math.max(p1.getBlockY(), p2.getBlockY());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());

        World world = p1.getWorld();
        List<String> entries = new ArrayList<>();
        int blockCount = 0;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.AIR
                            || block.getType() == Material.CAVE_AIR
                            || block.getType() == Material.VOID_AIR) continue;

                    entries.add(String.format("            .block(%d, %d, %d, Material.%s)",
                            x - minX, y - minY, z - minZ, block.getType().name()));
                    blockCount++;
                }
            }
        }



        if (blockCount == 0) {
            player.sendMessage(MessageUtils.color("&cNo se encontraron bloques sólidos en la selección."));
            return;
        }

        StringBuilder code = new StringBuilder();
        code.append("new StructureDef.Builder(\"").append(id).append("\")\n");
        for (String entry : entries) code.append(entry).append("\n");
        code.append("    .build()");

        File outFile = new File(outputFolder, id + ".txt");
        try (FileWriter fw = new FileWriter(outFile)) {
            fw.write(code.toString());
        } catch (IOException e) {
            player.sendMessage(MessageUtils.color("&cError al guardar el archivo: " + e.getMessage()));
            return;
        }

        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&a&l✔ Estructura exportada!"));
        player.sendMessage(MessageUtils.color("&8└ &7ID: &e" + id));
        player.sendMessage(MessageUtils.color("&8└ &7Bloques: &e" + blockCount));
        player.sendMessage(MessageUtils.color("&8└ &7Archivo: &e" + outFile.getPath()));
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }

    private void handleSpawn(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(MessageUtils.color("&cUso: &e/pendulum structdev spawn <id>"));
            return;
        }

        String id = args[2].toLowerCase();
        StructureDef structure = populator.getStructures().stream()
                .filter(s -> s.getId().equalsIgnoreCase(id))
                .findFirst().orElse(null);

        if (structure == null) {
            player.sendMessage(MessageUtils.color("&cNo existe estructura con id: &e" + id));
            return;
        }

        Location origin = player.getLocation().getBlock().getLocation();
        int placed = 0;
        for (StructureDef.BlockEntry entry : structure.getBlocks()) {
            player.getWorld().getBlockAt(
                    origin.getBlockX() + entry.relX(),
                    origin.getBlockY() + entry.relY(),
                    origin.getBlockZ() + entry.relZ()
            ).setType(entry.material());
            placed++;
        }


        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&a&l✔ Estructura spawneada!"));
        player.sendMessage(MessageUtils.color("&8└ &7ID: &e" + id + " &8│ &7Bloques: &e" + placed));
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.4f, 1.5f);
    }

    private void handleList(Player player) {
        List<StructureDef> structures = populator.getStructures();
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lESTRUCTURAS REGISTRADAS&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");
        if (structures.isEmpty()) {
            player.sendMessage(MessageUtils.color("&cNo hay estructuras registradas."));
        } else {
            for (StructureDef s : structures) {
                player.sendMessage(MessageUtils.color(
                        "&8▪ &e" + s.getId()
                                + " &8│ &7" + s.getBlocks().size() + " bloques"
                                + " &8│ &7max " + s.getMaxRelX() + "x" + s.getMaxRelZ()
                ));
            }
        }
        player.sendMessage("");
    }

    private boolean isWand(ItemStack item) {
        if (item == null || item.getType() != WAND_MATERIAL || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && WAND_NAME.equals(meta.getDisplayName());
    }

    private boolean isAdmin(Player player) {
        return player.hasPermission("pendulum.admin") ||
                Arrays.asList(PendulumSettings.getInstance().getOp()).contains(player.getName());
    }
}
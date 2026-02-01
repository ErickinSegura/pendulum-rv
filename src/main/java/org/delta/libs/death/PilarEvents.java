package org.delta.libs.death;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.delta.libs.builders.ItemBuilder;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class PilarEvents {
    public static void placeDeathPilar(Player player, Location location, PlayerDeathEvent event) {
        if (location == null || event == null) return;

        location.getBlock().setType(Material.END_ROD);

        location.add(0, 1, 0);
        Block headBlock = location.getBlock();
        headBlock.setType(Material.PLAYER_HEAD);
        Skull skull = (Skull) headBlock.getState();
        skull.setOwnerProfile(player.getPlayerProfile());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String deathTime = dateFormat.format(new Date());

        String deathLocation = String.format("Mundo: %s, X: %d, Y: %d, Z: %d",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());

        ItemStack skullItem = skullGenerator(deathTime, deathLocation);

        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
        skullMeta.setOwnerProfile(player.getPlayerProfile());
        skullItem.setItemMeta(skullMeta);

        skull.update(true);

        location.add(0, -2, 0);
        setRandomBaseBlock(location.getBlock());
    }

    private static ItemStack skullGenerator (String deathTime, String deathLocation) {


        return new ItemBuilder(Material.PLAYER_HEAD)
                .setLore(Arrays.asList(
                        ItemBuilder.format("&7Murió el: &f" + deathTime),
                        ItemBuilder.format("&7Ubicación: &f" + deathLocation)
                ))
                .build();
    }

    private static void setRandomBaseBlock(Block block) {
        double random = Math.random();
        if (random < 0.4) {
            block.setType(Material.GOLD_BLOCK);
        } else if (random < 0.7) {
            block.setType(Material.EMERALD_BLOCK);
        } else if (random < 0.9) {
            block.setType(Material.DIAMOND_BLOCK);
        } else {
            block.setType(Material.NETHERITE_BLOCK);
        }
    }
}
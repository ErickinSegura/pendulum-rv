package org.delta.listeners.worldgen;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.delta.worldgen.StructurePopulator;

public class WorldGenListener implements Listener {

    private final StructurePopulator structurePopulator;

    public WorldGenListener(StructurePopulator populator) {
        this.structurePopulator = populator;
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL
                && world.getEnvironment() != World.Environment.THE_END) return;

        world.getPopulators().add(structurePopulator);
    }
}
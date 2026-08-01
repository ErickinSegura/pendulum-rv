package org.delta.managers.event.impl;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.delta.libs.MessageUtils;
import org.delta.managers.event.ServerEvent;
import org.delta.pendulum;

public class InauguracionEvent implements ServerEvent {

    private static final double RADIO = 20.0;
    private static final long DIA_TICKS = 6000L;
    private static final int EFECTO_TICKS = 200;

    private final pendulum plugin;

    public InauguracionEvent(pendulum plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "inauguracion";
    }

    @Override
    public String getDisplayName() {
        return "Inauguración";
    }

    @Override
    public boolean restrictsMovement() {
        return true;
    }

    @Override
    public double getMovementRadius() {
        return RADIO;
    }

    @Override
    public boolean cancelsDamage() {
        return true;
    }

    @Override
    public void activate(Location anchor) {
        for (World world : Bukkit.getWorlds()) {
            world.setDifficulty(Difficulty.PEACEFUL);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        }
        if (anchor.getWorld() != null) {
            anchor.getWorld().setTime(DIA_TICKS);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            prepararJugador(player, anchor);
        }

        anunciar(
                "&8&m                                                    ",
                "&d&l≫ &f&lINAUGURACIÓN &d&l≪",
                "&7El evento de &d&lInauguración &7ha comenzado.",
                "&7Permanece cerca de la ceremonia.",
                "&8&m                                                    "
        );
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.0f);
        }
    }

    @Override
    public void deactivate() {
        for (World world : Bukkit.getWorlds()) {
            world.setDifficulty(Difficulty.HARD);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ptl resettime *");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGameMode(GameMode.SURVIVAL);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, EFECTO_TICKS, 1, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, EFECTO_TICKS, 0, false, true));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
        }

        anunciar(
                "&8&m                                                    ",
                "&d&l≫ &f&lINAUGURACIÓN &d&l≪",
                "&7¡El evento ha finalizado! &aEl mundo es libre.",
                "&8&m                                                    "
        );
    }

    @Override
    public void onPlayerJoin(Player player, Location anchor) {
        prepararJugador(player, anchor);
    }

    private void prepararJugador(Player player, Location anchor) {
        player.setGameMode(GameMode.ADVENTURE);
        if (anchor.getWorld() != null && fueraDeRadio(player.getLocation(), anchor)) {
            player.teleport(anchor);
        }
    }

    private boolean fueraDeRadio(Location loc, Location anchor) {
        if (loc.getWorld() == null || !loc.getWorld().equals(anchor.getWorld())) return true;
        double dx = loc.getX() - anchor.getX();
        double dz = loc.getZ() - anchor.getZ();
        return Math.sqrt(dx * dx + dz * dz) > RADIO;
    }

    private void anunciar(String... lineas) {
        Bukkit.getServer().broadcast(MessageUtils.color(""));
        for (String linea : lineas) {
            Bukkit.getServer().broadcast(MessageUtils.color(linea));
        }
        Bukkit.getServer().broadcast(MessageUtils.color(""));
    }
}

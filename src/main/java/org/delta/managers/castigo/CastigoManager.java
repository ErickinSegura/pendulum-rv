package org.delta.managers.castigo;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.database.repositories.CastigoRepository;
import org.delta.database.repositories.CastigoRepository.CastigoActivo;
import org.delta.libs.MessageUtils;
import org.delta.libs.castigo.Castigo;
import org.delta.pendulum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CastigoManager {

    private final pendulum plugin;
    private final CastigoRepository repo;
    private final Map<UUID, CastigoActivo> activos = new ConcurrentHashMap<>();

    private final NamespacedKey corazonesKey;
    private final NamespacedKey barreraKey;

    public CastigoManager(pendulum plugin) {
        this.plugin = plugin;
        this.repo = plugin.getDatabaseManager().castigos();
        this.corazonesKey = new NamespacedKey(plugin, "castigo_corazones");
        this.barreraKey = new NamespacedKey(plugin, "castigo_barrera");
    }

    public void cargarDesdeDB() {
        activos.clear();
        activos.putAll(repo.cargarActivos());
        for (Player online : Bukkit.getOnlinePlayers()) {
            reaplicar(online);
        }
    }

    public Castigo getCastigo(UUID uuid) {
        CastigoActivo activo = activos.get(uuid);
        return activo != null ? activo.getCastigo() : null;
    }

    public boolean esBarrera(ItemStack item) {
        if (item == null || item.getType() != Material.BARRIER) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer()
                .has(barreraKey, PersistentDataType.BYTE);
    }

    public void aplicar(UUID uuid, String nombre, Castigo castigo) {
        CastigoActivo activo = new CastigoActivo(castigo, false);
        activos.put(uuid, activo);
        repo.guardar(uuid.toString(), nombre, castigo, false)
                .exceptionally(e -> {
                    plugin.getLogger().warning("[Castigo] Error al guardar castigo de " + nombre + ": " + e.getMessage());
                    return null;
                });

        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            reaplicar(online);
            online.sendMessage(MessageUtils.color("&8[&cCastigo&8] &7Recibiste un castigo: &c" + castigo.getDescripcion()));
        }
    }

    public void reaplicar(Player player) {
        CastigoActivo activo = activos.get(player.getUniqueId());
        if (activo == null) return;

        Castigo castigo = activo.getCastigo();
        switch (castigo.getTipo()) {
            case REDUCIR_CORAZONES -> aplicarReducirCorazones(player, castigo.getCantidad());
            case BLOQUEAR_SLOTS -> aplicarBloquearSlots(player, castigo.getCantidad());
            case PROHIBIR_ITEM -> {
            }
            case PERDER_ITEMS -> {
                if (!activo.isAplicado()) {
                    aplicarPerderItems(player, castigo.getMaterial(), castigo.getCantidad());
                    activo.setAplicado(true);
                    repo.marcarAplicado(player.getUniqueId().toString());
                }
            }
        }
    }

    public void quitar(Player player) {
        CastigoActivo activo = activos.get(player.getUniqueId());
        if (activo == null) return;

        switch (activo.getCastigo().getTipo()) {
            case REDUCIR_CORAZONES -> quitarReducirCorazones(player);
            case BLOQUEAR_SLOTS -> quitarBloquearSlots(player);
            default -> {
            }
        }
    }

    public java.util.concurrent.CompletableFuture<Void> levantarTodos() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            quitar(online);
        }
        activos.clear();
        return repo.eliminarTodos()
                .exceptionally(e -> {
                    plugin.getLogger().warning("[Castigo] Error al limpiar castigos: " + e.getMessage());
                    return null;
                });
    }

    private void aplicarReducirCorazones(Player player, int corazones) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;

        attr.getModifiers().stream()
                .filter(m -> m.getKey().equals(corazonesKey))
                .toList()
                .forEach(attr::removeModifier);

        double reduccion = Math.min(corazones * 2.0, 18.0);
        attr.addModifier(new AttributeModifier(corazonesKey, -reduccion, Operation.ADD_NUMBER));

        if (player.getHealth() > attr.getValue()) {
            player.setHealth(attr.getValue());
        }
    }

    private void quitarReducirCorazones(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;
        attr.getModifiers().stream()
                .filter(m -> m.getKey().equals(corazonesKey))
                .toList()
                .forEach(attr::removeModifier);
    }

    private void aplicarBloquearSlots(Player player, int cantidad) {
        PlayerInventory inv = player.getInventory();
        int bloqueados = Math.min(cantidad, 27);
        for (int i = 0; i < bloqueados; i++) {
            int slot = 9 + i;
            ItemStack actual = inv.getItem(slot);
            if (esBarrera(actual)) continue;
            if (actual != null && actual.getType() != Material.AIR) {
                inv.setItem(slot, null);
                for (ItemStack sobrante : inv.addItem(actual).values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), sobrante);
                }
            }
            inv.setItem(slot, crearBarrera());
        }
    }

    private void quitarBloquearSlots(Player player) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            if (esBarrera(inv.getItem(i))) {
                inv.setItem(i, null);
            }
        }
    }

    private void aplicarPerderItems(Player player, Material material, int cantidad) {
        if (material == null) return;
        int restante = cantidad;
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize() && restante > 0; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == material) {
                if (item.getAmount() <= restante) {
                    restante -= item.getAmount();
                    inv.setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - restante);
                    restante = 0;
                }
            }
        }
    }

    public void limpiarBarrerasDeDrops(List<ItemStack> drops) {
        drops.removeIf(this::esBarrera);
    }

    private ItemStack crearBarrera() {
        ItemStack barrera = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrera.getItemMeta();
        meta.displayName(MessageUtils.color("&c&lSlot bloqueado &8(castigo)"));
        meta.getPersistentDataContainer().set(barreraKey, PersistentDataType.BYTE, (byte) 1);
        barrera.setItemMeta(meta);
        return barrera;
    }
}

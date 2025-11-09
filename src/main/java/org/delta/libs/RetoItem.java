package org.delta.libs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RetoItem extends Reto {
    private final Material material;
    private final int cantidad;

    public RetoItem(String titulo, Material material, int cantidad) {
        super(titulo, "Consigue " + cantidad + " de " + material.toString().toLowerCase().replace("_", " "), TipoReto.ITEM);
        this.material = material;
        this.cantidad = cantidad;
    }

    @Override
    public boolean verificarCompletado(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count >= cantidad;
    }

    @Override
    public String obtenerProgreso(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count + "/" + cantidad;
    }

    @Override
    public void inicializar(Player player) {
        // No necesita inicialización especial
    }

    public Material getMaterial() {
        return material;
    }

    public int getCantidad() {
        return cantidad;
    }
}
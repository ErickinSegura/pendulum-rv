package org.delta.libs.castigo;

import org.bukkit.Material;

public class Castigo {
    private final TipoCastigo tipo;
    private final String descripcion;
    private final Material material;
    private final int cantidad;

    public Castigo(TipoCastigo tipo, String descripcion, Material material, int cantidad) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.material = material;
        this.cantidad = cantidad;
    }

    public TipoCastigo getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Material getMaterial() {
        return material;
    }

    public int getCantidad() {
        return cantidad;
    }
}

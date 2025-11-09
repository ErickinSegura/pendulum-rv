package org.delta.libs;

import org.bukkit.entity.Player;

public abstract class Reto {
    protected String titulo;
    protected String descripcion;
    protected TipoReto tipo;

    public enum TipoReto {
        ITEM,
        LOGRO,
        MATAR_MOBS,
        MINAR_BLOQUES,
        CRAFTEAR,
        VIAJAR_DISTANCIA,
        CUSTOM
    }

    public Reto(String titulo, String descripcion, TipoReto tipo) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipo = tipo;
    }

    public abstract boolean verificarCompletado(Player player);
    public abstract String obtenerProgreso(Player player);
    public abstract void inicializar(Player player);

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public TipoReto getTipo() {
        return tipo;
    }
}
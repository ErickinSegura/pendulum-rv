package org.delta.libs.rango;

public enum Rango {
    ADMIN("&6", "A ", "&6&lAdministrador\n&7Encargado de dirigir y mantener el servidor."),
    MODERADOR("&a", "M ", "&a&lModerador\n&7Vela por el orden y las reglas del servidor."),
    INVITADO("&e", "+ ", "&e&lInvitado\n&7Jugador invitado especial al evento."),
    MIEMBRO("&b", "M ", "&b&lMiembro\n&7Parte de la comunidad de Pendulum.");

    private final String color;
    private final String etiqueta;
    private final String descripcion;

    Rango(String color, String etiqueta, String descripcion) {
        this.color = color;
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }

    public String getColor() {
        return color;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

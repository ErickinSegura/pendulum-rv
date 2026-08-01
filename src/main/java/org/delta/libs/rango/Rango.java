package org.delta.libs.rango;

public enum Rango {
    ADMIN("", "℈", "&6&lAdministrador\n&7Su señor y salvador."),
    MODERADOR("", "℞", "&a&lModerador\n&7Chalán del admin."),
    INVITADO("", "℗", "&e&lInvitado +++++\n&7Alex"),
    MIEMBRO("", "℥", "&b&lMiembro\n&7Ese eres tú.");

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

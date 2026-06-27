package org.delta.customs.items.modifier;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.delta.libs.builders.ItemBuilder;

public class ModifierLiviano extends StackeableArmorModifier {

    private static final NamespacedKey KEY = new NamespacedKey("delta", "modifier_liviano");
    private static final String LORE = ItemBuilder.format("&fLiviano");

    @Override
    public String getKey() {
        return "liviano_modifier";
    }

    @Override
    public String getDisplayName() {
        return "Liviano";
    }

    @Override
    protected NamespacedKey key() {
        return KEY;
    }

    @Override
    protected Attribute attribute() {
        return Attribute.MOVEMENT_SPEED;
    }

    @Override
    protected Operation operation() {
        return Operation.ADD_SCALAR;
    }

    @Override
    protected double bonusPerStack() {
        return 0.05;
    }

    @Override
    protected String loreLine() {
        return LORE;
    }
}

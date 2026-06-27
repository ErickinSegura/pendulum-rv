package org.delta.customs.items.modifier;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.delta.libs.builders.ItemBuilder;

import java.util.Set;

public class ModifierTemple extends StackeableArmorModifier {

    private static final NamespacedKey KEY = new NamespacedKey("delta", "modifier_temple");
    private static final String LORE = ItemBuilder.format("&c❤ Temple");

    @Override
    public String getKey() {
        return "temple_modifier";
    }

    @Override
    public String getDisplayName() {
        return "Temple";
    }

    @Override
    public Set<String> incompatibleWith() {
        return Set.of("unbreakable_modifier");
    }

    @Override
    protected NamespacedKey key() {
        return KEY;
    }

    @Override
    protected Attribute attribute() {
        return Attribute.MAX_HEALTH;
    }

    @Override
    protected Operation operation() {
        return Operation.ADD_NUMBER;
    }

    @Override
    protected double bonusPerStack() {
        return 2.0;
    }

    @Override
    protected String loreLine() {
        return LORE;
    }
}

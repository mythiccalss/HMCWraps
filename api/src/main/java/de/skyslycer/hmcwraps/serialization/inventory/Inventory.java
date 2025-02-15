package de.skyslycer.hmcwraps.serialization.inventory;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class Inventory {

    private ShortcutSettings shortcut;
    private String title;
    private Type type;
    private int rows;
    private int targetItemSlot;
    private Map<Integer, InventoryItem> items;
    private @Nullable HashMap<String, HashMap<String, List<String>>> actions;

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }

    public int getTargetItemSlot() {
        return targetItemSlot;
    }

    public Map<Integer, InventoryItem> getItems() {
        return items;
    }

    public Type getType() {
        return type;
    }

    public ShortcutSettings getShortcut() {
        return shortcut;
    }

    @Nullable
    public HashMap<String, HashMap<String, List<String>>> getActions() {
        return actions;
    }

    public enum Type {

        PAGINATED,
        SCROLLING

    }

}

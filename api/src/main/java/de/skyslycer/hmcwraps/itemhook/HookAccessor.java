package de.skyslycer.hmcwraps.itemhook;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class HookAccessor {

    private final Set<ItemHook> hooks;

    public HookAccessor(Set<ItemHook> hooks) {
        this.hooks = hooks;
    }

    /**
     * Get an item stack based on the input.
     *
     * @param id The input
     * @return The item stack
     */
    @Nullable
    public ItemStack getItemFromHook(String id) {
        var possible = hooks.stream().filter(it -> id.startsWith(it.getPrefix())).findFirst();
        if (possible.isEmpty()) {
            return ItemHook.defaultHook.get(id);
        } else {
            return possible.get().get(id.replace(possible.get().getPrefix(), ""));
        }
    }

    /**
     * Get the model id corresponding to the input.
     *
     * @param id The input
     * @return The model id, may return -1 when none is available
     */
    public int getModelIdFromHook(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException ignored) {
            var possible = hooks.stream().filter(it -> id.startsWith(it.getPrefix())).findFirst();
            return possible.map(itemHook -> itemHook.getModelId(id.replace(possible.get().getPrefix(), ""))).orElse(-1);
        }
    }

    /**
     * Get the color corresponding to the input.
     *
     * @param id The input
     * @return The color, may return null when none is available
     */
    @Nullable
    public Color getColorFromHook(String id) {
        var possible = hooks.stream().filter(it -> id.startsWith(it.getPrefix())).findFirst();
        return possible.map(itemHook -> itemHook.getColor(id.replace(possible.get().getPrefix(), ""))).orElse(null);
    }

}

package de.skyslycer.hmcwraps.itemhook;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.Nullable;


public class ItemsAdderItemHook implements ItemHook {

    @Override
    public String getPrefix() {
        return "itemsadder:";
    }

    @Override
    public ItemStack get(String id) {
        final CustomStack stack = CustomStack.getInstance(id);
        if (stack == null) {
            return null;
        }
        return stack.getItemStack().clone();
    }

    @Override
    public int getModelId(String id) {
        var stack = get(id);
        if (stack != null && stack.getItemMeta().hasCustomModelData()) {
            return stack.getItemMeta().getCustomModelData();
        }
        return -1;
    }

    @Override
    @Nullable
    public Color getColor(String id) {
        var stack = get(id);
        if (stack != null && stack.getItemMeta() instanceof LeatherArmorMeta meta) {
            return meta.getColor();
        }
        return null;
    }

}

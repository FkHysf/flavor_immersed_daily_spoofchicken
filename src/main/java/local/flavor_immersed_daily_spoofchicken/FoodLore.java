package local.flavor_immersed_daily_spoofchicken;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Shared translatable flavor text, safe on both client and dedicated server. */
public final class FoodLore {
    private FoodLore() {}
    public static void append(ItemStack stack, List<Component> tooltip) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String key = "tooltip." + id.getNamespace() + "." + id.getPath();
        tooltip.add(Component.translatable(key + ".short").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable(key + ".detail").withStyle(ChatFormatting.GRAY));
    }
}

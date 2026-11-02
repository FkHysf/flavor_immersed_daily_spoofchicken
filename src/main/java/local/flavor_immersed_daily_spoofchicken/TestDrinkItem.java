package local.flavor_immersed_daily_spoofchicken;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

/** Preview ingredient drinks have no status effects or container remainders. */
public final class TestDrinkItem extends LoreFoodItem {
    public TestDrinkItem(Properties properties) { super(properties); }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
}

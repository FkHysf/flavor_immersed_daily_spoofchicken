package local.flavor_immersed_daily_spoofchicken;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.*;
@Mod(PreviewMod.MODID)
public final class PreviewMod {
    public static final String MODID = "flavor_immersed_daily_spoofchicken";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final List<DeferredBlock<ChickenBlock>> DISHES = new ArrayList<>();
    public static final List<DeferredItem<BlockItem>> DISH_ITEMS = new ArrayList<>();
    public static final List<DeferredItem<Item>> INGREDIENT_ITEMS = new ArrayList<>();
    private static DeferredItem<Item> ingredient(String name, int nutrition, boolean drink) {
        DeferredItem<Item> item = ITEMS.register(name, () -> {
            Item.Properties properties = new Item.Properties().food(new FoodProperties.Builder()
                .nutrition(nutrition).saturationModifier(0.2F).alwaysEdible().build());
            return drink ? new TestDrinkItem(properties) : new LoreFoodItem(properties);
        });
        INGREDIENT_ITEMS.add(item);
        return item;
    }
    public static final DeferredItem<Item> TARO_PASTE = ingredient("taro_paste", 4, false);
    public static final DeferredItem<Item> FISH_MINT_MILK_TEA = ingredient("fish_mint_milk_tea", 3, true);
    public static final DeferredItem<Item> WANG_LAO_JI = ingredient("wang_lao_ji", 1, true);
    public static final DeferredItem<Item> HUOXIANG_ZHENGQI_WATER = ingredient("huoxiang_zhengqi_water", 1, true);
    public static final DeferredItem<Item> POPPING_CANDY = ingredient("popping_candy", 1, false);
    private static final String[] NAMES = {
        "mango_white_cut_chicken",
        "pineapple_white_cut_chicken",
        "durian_white_cut_chicken",
        "peanut_white_cut_chicken",
        "salad_white_cut_chicken",
        "mayonnaise_white_cut_chicken",
        "ketchup_white_cut_chicken",
        "chili_white_cut_chicken",
        "honey_white_cut_chicken",
        "matcha_white_cut_chicken",
        "taro_white_cut_chicken",
        "yogurt_white_cut_chicken",
        "peach_oolong_white_cut_chicken",
        "sea_salt_cheese_white_cut_chicken",
        "fish_mint_tea_white_cut_chicken",
        "bubble_tea_white_cut_chicken",
        "mango_pomelo_white_cut_chicken",
        "cola_white_cut_chicken",
        "sprite_white_cut_chicken",
        "orange_white_cut_chicken",
        "pineapple_beer_white_cut_chicken",
        "lemon_tea_white_cut_chicken",
        "plum_white_cut_chicken",
        "herbal_tea_white_cut_chicken",
        "wangzai_milk_white_cut_chicken",
        "stinky_tofu_white_cut_chicken",
        "huoxiang_white_cut_chicken",
        "chocolate_white_cut_chicken",
        "popping_candy_white_cut_chicken",
        "fermented_tofu_white_cut_chicken"
    };
    static {
        for (String name : NAMES) {
            DeferredBlock<ChickenBlock> block = BLOCKS.registerBlock(name, ChickenBlock::new,
                BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.3F).sound(SoundType.WOOD).noOcclusion().noCollission());
            DISHES.add(block);
            DISH_ITEMS.add(ITEMS.register(name, () -> new LoreBlockItem(block.get(), new Item.Properties())));
        }
        TABS.register("preview", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.flavor_immersed_daily_spoofchicken.preview"))
            .icon(() -> new ItemStack(DISH_ITEMS.getFirst().get()))
            .displayItems((parameters, output) -> {
                DISH_ITEMS.forEach(item -> output.accept(item.get()));
                INGREDIENT_ITEMS.forEach(item -> output.accept(item.get()));
            })
            .build());
    }
    public PreviewMod(IEventBus modBus) {
        BLOCKS.register(modBus); ITEMS.register(modBus); TABS.register(modBus);
    }
}

package local.flavor_immersed_daily_spoofchicken;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PreviewMod.MODID)
@PrefixGameTestTemplate(false)
public final class PreviewGameTests {
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void platesRemainIdleWithoutTickersOrSpontaneousDrops(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        var block = PreviewMod.DISHES.getFirst().get();
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, block);
        var state = helper.getBlockState(pos);
        helper.assertTrue(!state.isRandomlyTicking(), "Plate must not request random ticks");
        helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(pos)) == null, "Plate must not create block entity");
        int bowls = bowlsNear(helper, pos);
        helper.runAfterDelay(60, () -> {
            helper.assertTrue(helper.getBlockState(pos).equals(state), "Idle plate must keep its state");
            helper.assertTrue(bowlsNear(helper, pos) == bowls, "Idle plate must not produce entities");
            helper.succeed();
        });
    }

    private static int bowlsNear(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
            new net.minecraft.world.phys.AABB(helper.absolutePos(pos)).inflate(1.5)).stream()
            .filter(entity -> entity.getItem().is(Items.BOWL)).mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    @GameTest(template = "empty")
    public static void staleDuplicateAndDeniedInteractionsDoNotDropBowls(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        BlockPos abs = helper.absolutePos(pos);
        var block = PreviewMod.DISHES.getFirst().get();
        var lastBite = block.defaultBlockState().setValue(ChickenBlock.BITES, 2);
        Player survival = helper.makeMockPlayer(GameType.SURVIVAL);
        survival.getAbilities().mayBuild = true;
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, lastBite);
        var hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
        int before = bowlsNear(helper, pos);
        Player spectator = helper.makeMockPlayer(GameType.SPECTATOR);
        block.useWithoutItem(lastBite, helper.getLevel(), abs, spectator, hit);
        Player adventure = helper.makeMockPlayer(GameType.ADVENTURE);
        adventure.getAbilities().mayBuild = false;
        block.useWithoutItem(lastBite, helper.getLevel(), abs, adventure, hit);
        helper.assertBlockProperty(pos, ChickenBlock.BITES, 2);
        helper.assertTrue(bowlsNear(helper, pos) == before, "Denied players must not create bowls");
        block.useWithoutItem(lastBite, helper.getLevel(), abs, survival, hit);
        helper.assertTrue(bowlsNear(helper, pos) == before + 1, "Final bite must create one bowl");
        block.useItemOn(new ItemStack(Items.STICK), lastBite, helper.getLevel(), abs, survival, InteractionHand.OFF_HAND, hit);
        block.useWithoutItem(lastBite, helper.getLevel(), abs, survival, hit);
        helper.assertTrue(bowlsNear(helper, pos) == before + 1, "Stale duplicate calls must not create another bowl");
        helper.setBlock(pos, block);
        var initial = helper.getBlockState(pos);
        block.useWithoutItem(initial, helper.getLevel(), abs, survival, hit);
        block.useWithoutItem(initial, helper.getLevel(), abs, survival, hit);
        helper.assertBlockProperty(pos, ChickenBlock.BITES, 1);
        helper.setBlock(pos, Blocks.AIR);
        helper.assertTrue(bowlsNear(helper, pos) == before + 1, "Breaking an unfinished plate must not create a bowl");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void slabSupportAndSupportRemovalDoNotDropBowls(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        var block = PreviewMod.DISHES.getFirst().get();
        helper.setBlock(pos.below(), Blocks.OAK_SLAB);
        helper.setBlock(pos, block);
        helper.assertTrue(block.defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(pos)), "Slab must support plate");
        int before = bowlsNear(helper, pos);
        helper.setBlock(pos.below(), Blocks.AIR);
        helper.assertBlockPresent(Blocks.AIR, pos);
        helper.assertTrue(bowlsNear(helper, pos) == before, "Removing support must not grant an uneaten bowl");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fiveIngredientCraftsMatchAndRejectMissingInputs(GameTestHelper helper) {
        var level = helper.getLevel();
        for (var ingredientItem : PreviewMod.INGREDIENT_ITEMS) {
            var item = ingredientItem.get();
            String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
            var id = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(PreviewMod.MODID, "ingredients/" + path);
            var recipe = (net.minecraft.world.item.crafting.CraftingRecipe) level.getRecipeManager().byKey(id).orElseThrow().value();
            var slots = new java.util.ArrayList<ItemStack>();
            for (var input : recipe.getIngredients()) {
                helper.assertTrue(!input.isEmpty(), "Unknown crafting ingredient: " + id);
                slots.add(input.getItems()[0].copyWithCount(1));
            }
            java.util.Collections.reverse(slots);
            while (slots.size() < 9) slots.add(ItemStack.EMPTY);
            var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, slots);
            var matched = level.getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, level);
            helper.assertTrue(matched.isPresent() && matched.get().id().equals(id), "Wrong crafting match: " + id);
            var result = recipe.assemble(input, level.registryAccess());
            helper.assertTrue(result.is(item) && result.getCount() == 1, "Wrong crafting result: " + id);
            slots.set(0, ItemStack.EMPTY);
            helper.assertTrue(!recipe.matches(net.minecraft.world.item.crafting.CraftingInput.of(3, 3, slots), level),
                "Craft must reject missing materials: " + id);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void allThirtyFiveItemsExposeTwoTranslatedLoreLines(GameTestHelper helper) {
        var items = new java.util.ArrayList<net.minecraft.world.item.Item>();
        PreviewMod.DISH_ITEMS.forEach(item -> items.add(item.get()));
        PreviewMod.INGREDIENT_ITEMS.forEach(item -> items.add(item.get()));
        helper.assertTrue(items.size() == 35, "Expected thirty-five items");
        for (var item : items) {
            var lines = new java.util.ArrayList<net.minecraft.network.chat.Component>();
            item.appendHoverText(new ItemStack(item), net.minecraft.world.item.Item.TooltipContext.EMPTY,
                lines, net.minecraft.world.item.TooltipFlag.NORMAL);
            helper.assertTrue(lines.size() == 2, "Each item must have two lore lines");
            for (var line : lines) {
                helper.assertTrue(line.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents,
                    "Lore must be translatable");
                var content = (net.minecraft.network.chat.contents.TranslatableContents) line.getContents();
                helper.assertTrue(content.getKey().startsWith("tooltip." + PreviewMod.MODID + "."), "Stale tooltip namespace");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void allThirtyRecipesCookInFlavorPot(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var pot = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
            net.minecraft.resources.ResourceLocation.parse("fidworkblock:pot"));
        helper.assertTrue(pot != Blocks.AIR, "Bundled Flavor pot must be registered");
        BlockPos local = new BlockPos(2, 1, 2);
        BlockPos absolute = helper.absolutePos(local);
        Class<?> cookingClass = Class.forName("com.fidtest.block.entity.AbstractCookingBlockEntity");
        var tick = cookingClass.getMethod("serverTick", net.minecraft.world.level.Level.class,
            BlockPos.class, BlockState.class, cookingClass);
        for (var dish : PreviewMod.DISH_ITEMS) {
            var item = dish.get();
            var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
            var recipeId = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                PreviewMod.MODID, "pot/" + id.getPath());
            var holder = level.getRecipeManager().byKey(recipeId).orElseThrow();
            var recipe = holder.value();
            helper.assertTrue(recipe.getResultItem(level.registryAccess()).is(item), "Wrong result: " + id);
            helper.assertTrue(recipe.getIngredients().size() <= 6, "Too many ingredients: " + id);
            helper.setBlock(local, Blocks.AIR);
            helper.setBlock(local.below(), Blocks.STONE);
            helper.setBlock(local, pot);
            var be = level.getBlockEntity(absolute);
            helper.assertTrue(be instanceof net.minecraft.world.Container, "Pot inventory missing");
            var inventory = (net.minecraft.world.Container) be;
            int slot = 0;
            for (var ingredient : recipe.getIngredients()) {
                helper.assertTrue(!ingredient.isEmpty(), "Unknown ingredient: " + id);
                inventory.setItem(slot++, ingredient.getItems()[0].copyWithCount(1));
            }
            for (int n = 0; n < 125; n++) tick.invoke(null, level, absolute, level.getBlockState(absolute), be);
            helper.assertTrue(inventory.getItem(7).is(item) && inventory.getItem(7).getCount() == 1,
                "Pot failed to produce exactly one correct dish: " + id);
            for (int n = 0; n < 6; n++) helper.assertTrue(inventory.getItem(n).isEmpty(), "Ingredient not consumed: " + id);
            helper.assertTrue(inventory.getItem(6).isEmpty(), "Recipe must not require extra bowl: " + id);
            inventory.clearContent();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fiveNewIngredientsAreEdibleAndDrinksAnimate(GameTestHelper helper) {
        helper.assertTrue(PreviewMod.INGREDIENT_ITEMS.size() == 5, "Expected five new ingredients");
        for (var item : PreviewMod.INGREDIENT_ITEMS) {
            ItemStack stack = item.toStack();
            helper.assertTrue(stack.has(net.minecraft.core.component.DataComponents.FOOD), "Missing food properties");
            boolean drink = item.get() instanceof TestDrinkItem;
            helper.assertTrue(stack.getUseAnimation() == (drink ? net.minecraft.world.item.UseAnim.DRINK : net.minecraft.world.item.UseAnim.EAT),
                "Wrong use animation");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyFlavorPlacesAndEatsThreeTimes(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getAbilities().mayBuild = true;
        BlockPos local = new BlockPos(2, 1, 2);
        BlockPos absolute = helper.absolutePos(local);
        for (int index = 0; index < PreviewMod.DISHES.size(); index++) {
            int bowlsBefore = bowlsNear(helper, local);
            helper.setBlock(local.below(), Blocks.STONE);
            helper.setBlock(local, Blocks.AIR);
            ItemStack stack = new ItemStack(PreviewMod.DISH_ITEMS.get(index).get(), 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            BlockPos floor = absolute.below();
            BlockHitResult topHit = new BlockHitResult(Vec3.atCenterOf(floor).add(0, 0.5, 0), Direction.UP, floor, false);
            stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, topHit));
            helper.assertBlockPresent(PreviewMod.DISHES.get(index).get(), local);
            helper.assertTrue(stack.getCount() == 1, "Placement must consume one item in survival");
            helper.assertBlockProperty(local, ChickenBlock.BITES, 0);
            for (int bite = 1; bite <= 3; bite++) {
                // Include empty-hand and held-item interactions, at full hunger.
                player.getFoodData().setFoodLevel(20);
                player.setItemInHand(InteractionHand.MAIN_HAND, bite % 2 == 0 ? new ItemStack(Items.STICK) : ItemStack.EMPTY);
                helper.useBlock(local, player);
                if (bite < 3) helper.assertBlockProperty(local, ChickenBlock.BITES, bite);
                else helper.assertBlockPresent(Blocks.AIR, local);
                helper.assertTrue(bowlsNear(helper, local) == bowlsBefore + (bite == 3 ? 1 : 0),
                    "Expected exactly one bowl only on third bite, flavor " + index);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void facingAndUnsupportedPlacement(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getAbilities().mayBuild = true;
        BlockPos local = new BlockPos(2, 1, 2);
        BlockPos absolute = helper.absolutePos(local);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            helper.setBlock(local.below(), Blocks.STONE);
            helper.setBlock(local, Blocks.AIR);
            player.setYRot(facing.toYRot());
            ItemStack stack = new ItemStack(PreviewMod.DISH_ITEMS.getFirst().get(), 1);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            BlockHitResult topHit = new BlockHitResult(Vec3.atCenterOf(absolute.below()).add(0, 0.5, 0), Direction.UP, absolute.below(), false);
            stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, topHit));
            helper.assertBlockProperty(local, ChickenBlock.FACING, facing.getOpposite());
        }
        helper.setBlock(local.below(), Blocks.AIR);
        helper.assertBlockPresent(Blocks.AIR, local);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void creativeAlsoEatsAndStatePersists(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        player.getAbilities().mayBuild = true;
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos.below(), Blocks.STONE);
        BlockState state = PreviewMod.DISHES.getFirst().get().defaultBlockState().setValue(ChickenBlock.BITES, 1).setValue(ChickenBlock.FACING, Direction.EAST);
        BlockState decoded = net.minecraft.nbt.NbtUtils.readBlockState(helper.getLevel().holderLookup(net.minecraft.core.registries.Registries.BLOCK), net.minecraft.nbt.NbtUtils.writeBlockState(state));
        helper.assertTrue(decoded.equals(state), "Bites and facing must survive serialization");
        helper.setBlock(pos, decoded);
        int bowlsBefore = bowlsNear(helper, pos);
        player.getFoodData().setFoodLevel(20);
        helper.useBlock(pos, player);
        helper.assertBlockProperty(pos, ChickenBlock.BITES, 2);
        helper.useBlock(pos, player);
        helper.assertBlockPresent(Blocks.AIR, pos);
        helper.assertTrue(bowlsNear(helper, pos) == bowlsBefore + 1, "Creative final bite must also leave exactly one bowl");
        helper.succeed();
    }
}

package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.abstracts.TickingBlock;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.interfaces.ICardHolder;
import me.ddggdd135.slimeae.api.interfaces.IMECraftDevice;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.recipes.CraftingOperation;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class LargeMolecularAssembler extends TickingBlock
        // 如果不是TickingBlock的话 玩家不打开一次方块就没法自动合成 奇怪的bug
        implements IMECraftDevice, MachineProcessHolder<CraftingOperation>, InventoryBlock, ICardHolder {

    private final MachineProcessor<CraftingOperation> processor = new MachineProcessor<>(this);

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    protected void tick(
            @Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull SlimefunBlockData slimefunBlockData) {}

    public LargeMolecularAssembler(
            ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this, item.getDisplayName());
        addItemHandler(onBlockBreak());
    }

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
        BlockMenu menu = slimefunBlockData.getBlockMenu();
        if (menu == null) return;
        tickCards(block, SlimefunItem.getById(slimefunBlockData.getSfId()), slimefunBlockData);
        CraftingOperation operation = processor.getOperation(block);
        if (operation == null) {
            for (int slot : getCraftingInputSlots()) {
                menu.replaceExistingItem(slot, MenuItems.EMPTY);
            }
            menu.replaceExistingItem(getProgressSlot(), ChestMenuUtils.getBackground());
            menu.replaceExistingItem(getOutputSlot(), MenuItems.EMPTY);
            return;
        }

        ItemStack[] input = operation.getRecipe().getInput();
        for (int i = 0; i < input.length; i++) {
            ItemStack itemStack = input[i];
            if (itemStack == null || itemStack.getType().isAir()) continue;
            ItemUtils.setSettingItem(menu.getInventory(), getCraftingInputSlots()[i], itemStack);
        }

        if (isFinished(block)) {
            menu.replaceExistingItem(getProgressSlot(), ChestMenuUtils.getBackground());
            return;
        }

        operation.addProgress(1);

        int progress = operation.getProgress();
        int maxProgress = operation.getTotalTicks();

        menu.replaceExistingItem(
                getProgressSlot(),
                new CustomItemStack(
                        Material.GREEN_STAINED_GLASS_PANE,
                        "&a进度: &e" + progress + "&7/&e" + maxProgress,
                        "&7" + (int) ((progress / (double) maxProgress) * 100) + "%"));

        ItemStack[] output = operation.getRecipe().getOutput();
        if (output.length > 0) {
            ItemStack displayItem = output[0].clone();
            menu.replaceExistingItem(getOutputSlot(), displayItem);
        }
    }

    @Override
    public boolean isSupport(@Nonnull Block block, @Nonnull CraftingRecipe recipe) {
        return SlimefunItem.getById(
                                StorageCacheUtils.getBlock(block.getLocation()).getSfId())
                        instanceof LargeMolecularAssembler
                && recipe.getCraftType() == CraftType.LARGE;
    }

    @Override
    public boolean canStartCrafting(@Nonnull Block block, @Nonnull CraftingRecipe recipe) {
        return isSupport(block, recipe) && processor.getOperation(block) == null;
    }

    @Override
    public void startCrafting(@Nonnull Block block, @Nonnull CraftingRecipe recipe) {
        processor.startOperation(block, new CraftingOperation(4, recipe));
    }

    @Override
    public boolean isFinished(@Nonnull Block block) {
        CraftingOperation craftingOperation = processor.getOperation(block);
        if (craftingOperation == null) return false;
        return craftingOperation.isFinished();
    }

    @Nullable @Override
    public CraftingRecipe getFinishedCraftingRecipe(@Nonnull Block block) {
        if (!isFinished(block)) return null;
        return processor.getOperation(block).getRecipe();
    }

    @Override
    public void finishCrafting(@Nonnull Block block) {
        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
        if (menu != null) {
            menu.replaceExistingItem(getProgressSlot(), ChestMenuUtils.getBackground());
            menu.replaceExistingItem(getOutputSlot(), null);
        }
        processor.endOperation(block);
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Nonnull
    @Override
    public MachineProcessor<CraftingOperation> getMachineProcessor() {
        return processor;
    }

    @Override
    public int[] getInputSlots() {
        return new int[0];
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    @Override
    public int[] getCardSlots() {
        return new int[] {27, 36, 45};
    }

    public int getProgressSlot() {
        return 53;
    }

    public int[] getCraftingInputSlots() {
        return new int[] {
            1, 2, 3, 4, 5, 6,
            10, 11, 12, 13, 14, 15,
            19, 20, 21, 22, 23, 24,
            28, 29, 30, 31, 32, 33,
            37, 38, 39, 40, 41, 42,
            46, 47, 48, 49, 50, 51
        };
    }

    public int getOutputSlot() {
        return 8;
    }

    public int[] getBlackBorderSlots() {
        return new int[] {0, 9, 18};
    }

    public int[] getBlueBorderSlots() {
        return new int[] {17, 26, 35, 43, 44, 52};
    }

    public int[] getOrangeBorderSlots() {
        return new int[] {7, 16, 25, 34};
    }

    @Override
    public void init(@Nonnull BlockMenuPreset preset) {
        for (int slot : getBlackBorderSlots()) {
            preset.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int slot : getOrangeBorderSlots()) {
            preset.addItem(slot, ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int slot : getBlueBorderSlots()) {
            preset.addItem(slot, ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int slot : getCraftingInputSlots()) {
            preset.addItem(slot, MenuItems.EMPTY, ChestMenuUtils.getEmptyClickHandler());
        }

        preset.addItem(getProgressSlot(), ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());

        preset.addItem(getOutputSlot(), MenuItems.EMPTY, (player, i, itemStack, clickAction) -> false);
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        for (int slot : getCardSlots()) {
            if (menu.getItemInSlot(slot) == null
                    || menu.getItemInSlot(slot).getType().isAir()) {
                menu.replaceExistingItem(slot, MenuItems.CARD);
            }
            menu.addMenuClickHandler(slot, ItemUtils.getCardSlotClickHandler(block));
        }
    }

    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());
                if (blockMenu == null) return;

                for (int slot : getCardSlots()) {
                    ItemStack itemStack = blockMenu.getItemInSlot(slot);
                    if (itemStack != null
                            && itemStack.getType() != Material.AIR
                            && !(SlimefunUtils.isItemSimilar(itemStack, MenuItems.CARD, true, false))) {
                        b.getWorld().dropItemNaturally(b.getLocation(), itemStack);
                    }
                }

                CraftingOperation operation = processor.getOperation(b);
                if (operation == null) return;
                processor.endOperation(b);

                for (ItemStack itemStack : operation.getRecipe().getInput()) {
                    b.getWorld().dropItemNaturally(b.getLocation(), itemStack);
                }
            }
        };
    }

    @Override
    public boolean isGlobal(Block block) {
        return true;
    }
}

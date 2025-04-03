package me.ddggdd135.slimeae.core.slimefun.cards;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.abstracts.Card;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.slimefun.MEInterface;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.inventory.ItemStack;

public class RedstoneCard extends Card {
    public RedstoneCard(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void onTick(Block block, SlimefunItem item, SlimefunBlockData data) {
        BlockMenu blockMenu = data.getBlockMenu();
        if (item instanceof MEInterface meInterface) {
            for (int slot : meInterface.getItemSlots()) {
                int settingSlot = slot - 9;
                ItemStack setting = ItemUtils.getSettingItem(blockMenu.getInventory(), settingSlot);
                ItemStack itemStack = blockMenu.getItemInSlot(slot);

                if (setting == null
                        || setting.getType().isAir()
                        || SlimefunUtils.isItemSimilar(setting, MenuItems.SETTING, true, false)) {
                    continue;
                }

                if (itemStack == null || itemStack.getType().isAir() || itemStack.getAmount() < setting.getAmount()) {
                    updateBlock(block, 0);
                    return;
                }
            }

            updateBlock(block, 15);
        }
    }

    private void updateBlock(@Nonnull Block block, int power) {
        Bukkit.getScheduler().runTask(SlimeAEPlugin.getInstance(), () -> {
            BlockState blockState = PaperLib.getBlockState(block, false).getState();
            AnaloguePowerable analoguePowerable = (AnaloguePowerable) blockState.getBlockData();
            analoguePowerable.setPower(power);
            blockState.setBlockData(analoguePowerable);
            blockState.update(true);

            for (BlockFace blockFace : IMEObject.Valid_Faces) {
                Block target = block.getRelative(blockFace);
                PaperLib.getBlockState(target, false).getState().update(true);
            }
        });
    }
}

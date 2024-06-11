package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.abstracts.TickingBlock;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.interfaces.IMECraftDevice;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.recipes.CraftingOperation;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class MolecularAssembler extends TickingBlock
        implements IMECraftDevice, MachineProcessHolder<CraftingOperation> {
    private final MachineProcessor<CraftingOperation> processor = new MachineProcessor<>(this);

    public MolecularAssembler(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        CraftingOperation craftingOperation = processor.getOperation(block);
        if (craftingOperation == null) return;
        craftingOperation.addProgress(1);
    }

    @Override
    public boolean isSupport(@Nonnull Block block, @Nonnull CraftingRecipe recipe) {
        return SlimefunItem.getById(
                                StorageCacheUtils.getBlock(block.getLocation()).getSfId())
                        instanceof MolecularAssembler
                && recipe.getCraftType() == CraftType.CRAFTING_TABLE;
    }

    @Override
    public boolean canStartCrafting(@Nonnull Block block, @Nonnull CraftingRecipe recipe) {
        return isSupport(block, recipe) && processor.getOperation(block) == null;
    }

    @Override
    public void startCrafting(@Nonnull Block block, @Nonnull CraftingRecipe recipe) {
        processor.startOperation(block, new CraftingOperation(2, recipe));
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
        processor.endOperation(block);
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Nonnull
    @Override
    public MachineProcessor<CraftingOperation> getMachineProcessor() {
        return processor;
    }
}

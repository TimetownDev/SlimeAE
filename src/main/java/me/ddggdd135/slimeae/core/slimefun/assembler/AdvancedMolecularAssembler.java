package me.ddggdd135.slimeae.core.slimefun.assembler;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.abstracts.Card;
import me.ddggdd135.slimeae.api.interfaces.ICardHolder;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class AdvancedMolecularAssembler extends MolecularAssembler {
    private int defaultSpeed;

    public AdvancedMolecularAssembler(
            ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int defaultSpeed) {
        super(itemGroup, item, recipeType, recipe);
        this.defaultSpeed = defaultSpeed;
    }

    @Override
    public int getSpeed(@Nonnull Block block) {
        Card accelerationCard = (Card) SlimefunItem.getByItem(SlimeAEItems.ACCELERATION_CARD);
        SlimefunBlockData data = StorageCacheUtils.getBlock(block.getLocation());

        BlockMenu menu = data.getBlockMenu();
        if (menu == null) return 0;

        Map<Card, Integer> amount = cache.get(block.getLocation());
        if (amount == null) {
            ICardHolder.updateCache(block, this, data);
            amount = cache.get(block.getLocation());
        }

        return (int) ((amount.getOrDefault(accelerationCard, 0) / 1.5 + 1) * defaultSpeed);
    }
}

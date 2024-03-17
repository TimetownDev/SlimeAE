package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.interfaces.IMEController;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class MEController extends SlimefunItem implements IMEController<MEController> {
    public MEController(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler(new BlockTicker() {
            @Override
            public boolean isSynchronized() {
                return true;
            }
            @Override
            public void tick(Block block, SlimefunItem item, SlimefunBlockData data) {
                NetworkInfo info = SlimeAEPlugin.getNetworkData().refreshNetwork(block.getLocation());
                if (info != null) {
                    info.getChildren().forEach(x -> {
                        SlimefunBlockData blockData = Slimefun.getDatabaseManager().getBlockDataController().getBlockData(x);
                        ((IMEObject<?>)SlimefunItem.getById(blockData.getSfId())).onNetworkUpdate(x.getBlock(), info);
                    });
                    ItemStack[] items = info.getStorage().tryTakeItem(new ItemRequest[] {new ItemRequest(new ItemStack(Material.COBBLESTONE), 1)});
                    if (items.length != 0)
                        block.getWorld().dropItemNaturally(block.getLocation().add(BlockFace.UP.getDirection()), items[0]);
                }
            }
        });
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {

    }
}

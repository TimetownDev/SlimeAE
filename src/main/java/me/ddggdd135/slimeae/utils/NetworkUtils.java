package me.ddggdd135.slimeae.utils;

import static me.ddggdd135.slimeae.api.interfaces.IMEObject.Valid_Faces;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.AutoCraftingSession;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.interfaces.*;
import me.ddggdd135.slimeae.core.NetworkInfo;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class NetworkUtils {
    public static void scan(Block block, Set<Location> blocks) {
        Stack<Location> stack = new Stack<>();
        stack.push(block.getLocation());
        while (!stack.empty()) {
            Location next = stack.pop();
            for (BlockFace blockFace : Valid_Faces) {
                Location testLocation = next.clone().add(blockFace.getDirection());
                if (blocks.contains(testLocation)) continue;
                if (SlimeAEPlugin.getNetworkData().AllNetworkBlocks.containsKey(testLocation)) {
                    blocks.add(testLocation);
                    stack.push(testLocation);
                } else {
                    SlimefunBlockData blockData = StorageCacheUtils.getBlock(testLocation);
                    if (blockData == null) {
                        continue;
                    }
                    SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
                    if (slimefunItem instanceof IMEObject IMEObject) {
                        blocks.add(testLocation);
                        SlimeAEPlugin.getNetworkData().AllNetworkBlocks.put(testLocation, IMEObject);

                        if (slimefunItem instanceof IMEController IMEController) {
                            SlimeAEPlugin.getNetworkData().AllControllers.put(testLocation, IMEController);
                        }

                        if (slimefunItem instanceof IMEStorageObject IMEStorageObject) {
                            SlimeAEPlugin.getNetworkData().AllStorageObjects.put(testLocation, IMEStorageObject);
                        }

                        if (slimefunItem instanceof IMECraftHolder IMECraftHolder) {
                            SlimeAEPlugin.getNetworkData().AllCraftHolders.put(testLocation, IMECraftHolder);
                        }

                        stack.push(testLocation);
                    }
                }
            }
        }
    }

    public static Set<Location> scan(Block block) {
        Set<Location> result = new HashSet<>();
        scan(block, result);
        return result;
    }

    public static void doCraft(@Nonnull NetworkInfo networkInfo, @Nonnull ItemStack itemStack, int amount) {
        // 检查是否已有相同的合成任务
        boolean hasExistingTask = false;
        for (AutoCraftingSession session : networkInfo.getCraftingSessions()) {
            // 检查配方的所有输出物品
            for (ItemStack output : session.getRecipe().getOutput()) {
                if (SlimefunUtils.isItemSimilar(output, itemStack, true, false)) {
                    hasExistingTask = true;
                    break;
                }
            }
            if (hasExistingTask) break;
        }

        // 如果没有现有任务且未达到最大任务数，则创建新的合成任务
        if (!hasExistingTask && networkInfo.getCraftingSessions().size() < NetworkInfo.getMaxCraftingSessions()) {
            CraftingRecipe recipe = networkInfo.getRecipeFor(itemStack);
            if (recipe != null) {
                // 检查配方输出是否包含目标物品
                boolean hasMatchingOutput = false;
                for (ItemStack output : recipe.getOutput()) {
                    if (SlimefunUtils.isItemSimilar(output, itemStack, true, false)) {
                        hasMatchingOutput = true;
                        break;
                    }
                }

                if (hasMatchingOutput) {
                    try {
                        // 创建并启动新的合成任务
                        AutoCraftingSession session = new AutoCraftingSession(networkInfo, recipe, amount);
                        session.start();
                    } catch (Exception e) {
                        // 忽略合成失败的情况，等待下一次尝试
                    }
                }
            }
        }
    }
}

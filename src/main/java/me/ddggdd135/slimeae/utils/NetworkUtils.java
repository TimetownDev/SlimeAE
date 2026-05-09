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
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.AutoCraftingTask;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.interfaces.*;
import me.ddggdd135.slimeae.core.NetworkInfo;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class NetworkUtils {
    private static boolean loadNetworkObject(@Nonnull Location location) {
        if (!location.isChunkLoaded()) return false;
        SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
        if (blockData == null) {
            removeNetworkObject(location);
            return false;
        }
        SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
        if (!(slimefunItem instanceof IMEObject imeObject)) {
            removeNetworkObject(location);
            return false;
        }
        SlimeAEPlugin.getNetworkData().AllNetworkBlocks.put(location, imeObject);
        if (slimefunItem instanceof IMEController controller) {
            SlimeAEPlugin.getNetworkData().AllControllers.put(location, controller);
        } else {
            SlimeAEPlugin.getNetworkData().AllControllers.remove(location);
        }
        if (slimefunItem instanceof IMEStorageObject storageObject) {
            SlimeAEPlugin.getNetworkData().AllStorageObjects.put(location, storageObject);
        } else {
            SlimeAEPlugin.getNetworkData().AllStorageObjects.remove(location);
        }
        if (slimefunItem instanceof IMECraftHolder craftHolder) {
            SlimeAEPlugin.getNetworkData().AllCraftHolders.put(location, craftHolder);
        } else {
            SlimeAEPlugin.getNetworkData().AllCraftHolders.remove(location);
        }
        return true;
    }

    private static void removeNetworkObject(@Nonnull Location location) {
        SlimeAEPlugin.getNetworkData().AllNetworkBlocks.remove(location);
        SlimeAEPlugin.getNetworkData().AllControllers.remove(location);
        SlimeAEPlugin.getNetworkData().AllStorageObjects.remove(location);
        SlimeAEPlugin.getNetworkData().AllCraftHolders.remove(location);
    }

    public static void scan(Block block, Set<Location> blocks) {
        Stack<Location> stack = new Stack<>();
        stack.push(block.getLocation());
        while (!stack.empty()) {
            Location next = stack.pop();
            for (BlockFace blockFace : Valid_Faces) {
                Location testLocation = next.clone().add(blockFace.getDirection());
                if (blocks.contains(testLocation)) continue;
                if (SlimeAEPlugin.getNetworkData().AllNetworkBlocks.containsKey(testLocation)) {
                    if (!loadNetworkObject(testLocation)) continue;
                    blocks.add(testLocation);
                    stack.push(testLocation);
                } else {
                    if (loadNetworkObject(testLocation)) {
                        blocks.add(testLocation);
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

    @Nullable public static NetworkInfo findNetworkByBFS(Location start) {
        Set<Location> visited = new HashSet<>();
        Stack<Location> stack = new Stack<>();
        stack.push(start);
        visited.add(start);
        while (!stack.empty()) {
            Location next = stack.pop();
            for (BlockFace blockFace : Valid_Faces) {
                Location testLocation = next.clone().add(blockFace.getDirection());
                if (visited.contains(testLocation)) continue;
                if (!SlimeAEPlugin.getNetworkData().AllNetworkBlocks.containsKey(testLocation)) continue;
                if (!loadNetworkObject(testLocation)) continue;
                visited.add(testLocation);
                NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(testLocation);
                if (info != null) return info;
                stack.push(testLocation);
            }
        }
        return null;
    }

    public static void doCraft(@Nonnull NetworkInfo networkInfo, @Nonnull ItemStack itemStack, long amount) {
        // 检查是否已有相同的合成任务
        boolean hasExistingTask = false;
        for (AutoCraftingTask task : networkInfo.getAutoCraftingSessions()) {
            // 检查配方的所有输出物品
            for (ItemStack output : task.getRecipe().getOutput()) {
                if (SlimefunUtils.isItemSimilar(output, itemStack, true, false)) {
                    hasExistingTask = true;
                    break;
                }
            }
            if (hasExistingTask) break;
        }

        // 如果没有现有任务且未达到最大任务数，则创建新的合成任务
        if (!hasExistingTask && networkInfo.getAutoCraftingSessions().size() < NetworkInfo.getMaxCraftingSessions()) {
            CraftingRecipe recipe = networkInfo.getRecipeFor(itemStack);
            if (recipe != null) {
                // 检查配方输出是否包含目标物品
                boolean hasMatchingOutput = false;
                int onceAmount = 1;

                for (ItemStack output : recipe.getOutput()) {
                    if (SlimefunUtils.isItemSimilar(output, itemStack, true, false)) {
                        hasMatchingOutput = true;
                        onceAmount = output.getAmount();
                        break;
                    }
                }

                if (hasMatchingOutput) {
                    try {
                        // 创建并启动新的合成任务
                        AutoCraftingTask task = new AutoCraftingTask(networkInfo, recipe, amount / onceAmount);
                        if (!task.start()) task.dispose();
                    } catch (Exception e) {
                        // 忽略合成失败的情况，等待下一次尝试
                    }
                }
            }
        }
    }
}

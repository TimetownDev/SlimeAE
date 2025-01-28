package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.abstracts.Card;
import me.ddggdd135.slimeae.core.AutoCraftingSession;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class CraftingCard extends Card {

    private static final Map<Location, Long> cooldowns = new ConcurrentHashMap<>();
    private static int cooldownTicks;

    public CraftingCard(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void onTick(Block block, SlimefunItem item, SlimefunBlockData data) {
        // ME接口
        if (item instanceof MEInterface) {
            BlockMenu blockMenu = data.getBlockMenu();
            if (blockMenu == null) return;

            NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
            if (networkInfo == null) return;

            // 检查CD
            Location loc = block.getLocation();
            long currentTick = Bukkit.getCurrentTick();
            Long lastUsage = cooldowns.get(loc);

            if (lastUsage != null && currentTick - lastUsage < cooldownTicks) {
                return;
            }

            for (int slot : MEInterface.ITEM_SLOTS) {
                int settingSlot = slot - 9;
                ItemStack setting = ItemUtils.getSettingItem(blockMenu.getInventory(), settingSlot);
                ItemStack itemStack = blockMenu.getItemInSlot(slot);

                if (SlimefunUtils.isItemSimilar(setting, MenuItems.Setting, true, false)) {
                    continue;
                }

                // 检查槽位中的物品数量是否已达到设定值
                if (itemStack != null && !itemStack.getType().isAir() && itemStack.getAmount() >= setting.getAmount()) {
                    continue;
                }

                // 获取需要合成的数量
                int neededAmount = setting.getAmount()
                        - (itemStack == null || itemStack.getType().isAir() ? 0 : itemStack.getAmount());

                // 检查是否已有相同的合成任务
                boolean hasExistingTask = false;
                for (AutoCraftingSession session : networkInfo.getCraftingSessions()) {
                    // 检查配方的所有输出物品
                    for (ItemStack output : session.getRecipe().getOutput()) {
                        if (SlimefunUtils.isItemSimilar(output, setting, true, false)) {
                            hasExistingTask = true;
                            break;
                        }
                    }
                    if (hasExistingTask) break;
                }

                // 如果没有现有任务且未达到最大任务数，则创建新的合成任务
                if (!hasExistingTask
                        && networkInfo.getCraftingSessions().size() < NetworkInfo.getMaxCraftingSessions()) {
                    CraftingRecipe recipe = networkInfo.getRecipeFor(setting);
                    if (recipe != null) {
                        // 检查配方输出是否包含目标物品
                        boolean hasMatchingOutput = false;
                        for (ItemStack output : recipe.getOutput()) {
                            if (SlimefunUtils.isItemSimilar(output, setting, true, false)) {
                                hasMatchingOutput = true;
                                break;
                            }
                        }

                        if (hasMatchingOutput) {
                            try {
                                // 创建并启动新的合成任务
                                AutoCraftingSession session =
                                        new AutoCraftingSession(networkInfo, recipe, neededAmount);
                                session.start();
                            } catch (Exception e) {
                                // 忽略合成失败的情况，等待下一次尝试
                            }
                        }
                    }
                }
            }

            // 无论是否进行了合成，都更新CD
            cooldowns.put(loc, currentTick);
        }
    }

    /**
     * 清理不再使用的CD缓存
     * @param location 要清理的位置
     */
    public static void clearCooldown(Location location) {
        cooldowns.remove(location);
    }

    /**
     * 重新加载配置
     */
    public static void reloadConfig() {
        cooldownTicks = SlimeAEPlugin.getInstance().getConfig().getInt("auto-crafting.crafting-card-cooldown", 200);
    }
}

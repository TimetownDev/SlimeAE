package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.abstracts.Card;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.slimefun.buses.MEExportBus;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.NetworkUtils;
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

        // ME接口
        if (item instanceof MEInterface meInterface) {
            for (int slot : meInterface.getItemSlots()) {
                int settingSlot = slot - 9;
                ItemStack setting = ItemUtils.getSettingItem(blockMenu.getInventory(), settingSlot);
                ItemStack itemStack = blockMenu.getItemInSlot(slot);

                if (setting == null || setting.getType().isAir()) {
                    continue;
                }

                // 检查槽位中的物品数量是否已达到设定值
                if (itemStack != null && !itemStack.getType().isAir() && itemStack.getAmount() >= setting.getAmount()) {
                    continue;
                }

                // 获取需要合成的数量
                int neededAmount = setting.getAmount()
                        - (itemStack == null || itemStack.getType().isAir() ? 0 : itemStack.getAmount());

                NetworkUtils.doCraft(networkInfo, setting, neededAmount);
            }

            cooldowns.put(loc, currentTick);
        }
        // ME输出总线
        if (item instanceof MEExportBus meExportBus) {
            for (int slot : meExportBus.getSettingSlots()) {
                ItemStack setting = ItemUtils.getSettingItem(blockMenu.getInventory(), slot);
                if (setting == null || setting.getType().isAir()) {
                    continue;
                }
                if (!networkInfo.getStorage().contains(new ItemRequest(setting, setting.getAmount()))) {
                    NetworkUtils.doCraft(networkInfo, setting, setting.getAmount());
                }
            }
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

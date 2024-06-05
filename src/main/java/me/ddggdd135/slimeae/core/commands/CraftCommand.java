package me.ddggdd135.slimeae.core.commands;

import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.AEMenu;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.exceptions.NoEnoughMaterialsException;
import me.ddggdd135.slimeae.core.AutoCraftingSession;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CraftCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(
            @Nonnull CommandSender commandSender,
            @Nonnull Command command,
            @Nonnull String s,
            @Nonnull String[] strings) {
        if (commandSender instanceof Player player) {
            Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
            NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
            if (info == null) {
                player.sendMessage(CMIChatColor.translate("请站在对应网络方块上"));
                return false;
            }
            if (info.getCraftingSessions().size() >= 8) {
                player.sendMessage(CMIChatColor.translate("&c&l这个网络已经有8个合成任务了"));
                return false;
            }
            if (strings.length == 0) {
                player.sendMessage(CMIChatColor.translate("&c&l用法 /ae_craft <Amount>"));
            }
            try {
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack.getType().isAir()) {
                    player.sendMessage(CMIChatColor.translate("&c&l你是打算要合成空气吗?"));
                    return false;
                }
                CraftingRecipe recipe = info.getRecipeFor(itemStack);
                if (recipe == null) {
                    player.sendMessage(CMIChatColor.translate("&c&l没有找到样板"));
                    return false;
                }
                AutoCraftingSession session = new AutoCraftingSession(info, recipe, Integer.parseInt(strings[0]));
                session.refreshGUI(45, false);
                AEMenu menu = session.getMenu();
                int[] background = new int[] {45, 46, 48, 49, 50, 52, 53};
                int acceptSlot = 47;
                int cancelSlot = 51;
                for (int slot : background) {
                    menu.replaceExistingItem(slot, ChestMenuUtils.getBackground());
                    menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
                }
                menu.replaceExistingItem(acceptSlot, MenuItems.ACCEPT);
                menu.addMenuClickHandler(acceptSlot, (p, i, itemStack1, clickAction) -> {
                    player.sendMessage(CMIChatColor.translate("&a&l成功规划了合成任务"));
                    session.refreshGUI(54);
                    session.start();
                    return false;
                });
                menu.replaceExistingItem(cancelSlot, MenuItems.CANCEL);
                menu.addMenuClickHandler(cancelSlot, (p, i, itemStack1, clickAction) -> {
                    player.closeInventory();
                    return false;
                });
                menu.open(player);
            } catch (NumberFormatException e) {
                player.sendMessage(CMIChatColor.translate("&c&l用法 /ae_craft <Amount>"));
            } catch (NoEnoughMaterialsException | StackOverflowError e) {
                player.sendMessage(CMIChatColor.translate("&e&l没有足够的材料"));
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @Nonnull CommandSender commandSender,
            @Nonnull Command command,
            @Nonnull String s,
            @Nonnull String[] strings) {
        return new ArrayList<>();
    }
}

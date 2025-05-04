package me.ddggdd135.slimeae.core.commands.subcommands;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.SubCommand;
import me.ddggdd135.slimeae.api.items.MEStorageCellCache;
import me.ddggdd135.slimeae.core.slimefun.MEItemStorageCell;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CleardataCommand extends SubCommand {
    @Override
    @Nonnull
    public String getName() {
        return "cleardata";
    }

    @Override
    @Nonnull
    public String getDescription() {
        return "清空存储元件的数据";
    }

    @Override
    @Nonnull
    public String getUsage() {
        return "/ae cleardata <StorageCell_UUID>";
    }

    @Override
    public boolean onCommand(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!commandSender.hasPermission("slimeae.cleardata")) {
            commandSender.sendMessage(CMIChatColor.translate("&a你没有权限使用这个命令"));
            return false;
        }
        if (args.length == 1) {
            if (!(commandSender instanceof Player player)) {
                commandSender.sendMessage(CMIChatColor.translate("&e用法:" + getUsage()));
                return false;
            } else {
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack.getType().isAir()) {
                    commandSender.sendMessage(CMIChatColor.translate("&e你是要清除空气吗？"));
                    return false;
                }

                MEStorageCellCache data = MEItemStorageCell.getStorage(itemStack);
                if (data == null) {
                    commandSender.sendMessage(CMIChatColor.translate("&e你确定你拿着存储元件？"));
                    return false;
                }
                data.getStorageUnsafe().clear();
                MEItemStorageCell.updateLore(itemStack);

                SlimeAEPlugin.getStorageCellStorageDataController().delete(data.getStorageData());
                SlimeAEPlugin.getStorageCellFilterDataController().delete(data.getFilterData());
                commandSender.sendMessage(CMIChatColor.translate("&a成功清除了存储元件数据"));

                return false;
            }
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(args[1]);
        } catch (IllegalArgumentException e) {
            commandSender.sendMessage("UUID错误");
            return false;
        }
        MEStorageCellCache data = MEStorageCellCache.getMEStorageCellCache(uuid);
        if (data != null) data.getStorageUnsafe().clear();

        SlimeAEPlugin.getStorageCellStorageDataController().delete(uuid);
        SlimeAEPlugin.getStorageCellFilterDataController().delete(uuid);

        commandSender.sendMessage(CMIChatColor.translate("&a成功清除了存储元件数据"));
        return false;
    }

    @Override
    @Nullable public List<String> onTabComplete(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return List.of();
    }
}

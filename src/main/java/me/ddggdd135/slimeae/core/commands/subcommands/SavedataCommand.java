package me.ddggdd135.slimeae.core.commands.subcommands;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SavedataCommand extends SubCommand {
    @Override
    @Nonnull
    public String getName() {
        return "savedata";
    }

    @Override
    @Nonnull
    public String getDescription() {
        return "保存AE数据";
    }

    @Override
    @Nonnull
    public String getUsage() {
        return "/ae savedata";
    }

    @Override
    public boolean onCommand(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!commandSender.hasPermission("slimeae.savedata")) {
            commandSender.sendMessage(CMIChatColor.translate("&a你没有权限使用这个命令"));
            return false;
        }

        SlimeAEPlugin.getStorageCellDataController().saveAllAsync();
        commandSender.sendMessage(CMIChatColor.translate("&e成功保存ME存储元件数据"));

        return false;
    }

    @Override
    @Nullable public List<String> onTabComplete(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return List.of();
    }
}

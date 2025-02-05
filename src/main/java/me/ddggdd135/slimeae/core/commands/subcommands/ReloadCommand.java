package me.ddggdd135.slimeae.core.commands.subcommands;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {
    @Override
    @Nonnull
    public String getName() {
        return "reload";
    }

    @Override
    @Nonnull
    public String getDescription() {
        return "重新加载配置文件(有一些可能不支持)";
    }

    @Override
    @Nonnull
    public String getUsage() {
        return "/ae reload";
    }

    @Override
    public boolean onCommand(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!commandSender.hasPermission("slimeae.reload")) {
            commandSender.sendMessage(CMIChatColor.translate("&a你没有权限使用这个命令"));
            return false;
        }

        SlimeAEPlugin.getInstance().reloadConfig0();
        commandSender.sendMessage(CMIChatColor.translate("&e成功重新加载了配置文件"));

        return false;
    }

    @Override
    @Nullable public List<String> onTabComplete(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return List.of();
    }
}

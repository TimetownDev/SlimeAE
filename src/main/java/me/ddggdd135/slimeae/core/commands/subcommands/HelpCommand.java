package me.ddggdd135.slimeae.core.commands.subcommands;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.SubCommand;
import me.ddggdd135.slimeae.core.commands.SlimeAECommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HelpCommand extends SubCommand {
    @Override
    @Nonnull
    public String getName() {
        return "help";
    }

    @Override
    @Nonnull
    public String getDescription() {
        return "获取SlimeAE命令帮助";
    }

    @Override
    @Nonnull
    public String getUsage() {
        return "/ae help";
    }

    @Override
    public boolean onCommand(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (args.length == 1) {
            printHelpMessages(commandSender);
            return false;
        } else {
            SubCommand subCommand = SlimeAEPlugin.getSlimeAECommand().getSubCommand(args[1]);
            if (subCommand == null) {
                commandSender.sendMessage(CMIChatColor.translate("&a找不到命令" + args[0]));
                return false;
            }

            commandSender.sendMessage(CMIChatColor.translate("&e用法: " + subCommand.getUsage()));
        }
        return false;
    }

    @Override
    @Nullable public List<String> onTabComplete(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return SlimeAEPlugin.getSlimeAECommand().getSubCommands().stream()
                .map(SubCommand::getName)
                .toList();
    }

    public void printHelpMessages(@Nonnull CommandSender commandSender) {
        SlimeAECommand slimeAECommand = SlimeAEPlugin.getSlimeAECommand();
        for (SubCommand subCommand : slimeAECommand.getSubCommands()) {
            commandSender.sendMessage(
                    CMIChatColor.translate("&e" + subCommand.getName() + "   &7" + subCommand.getDescription()));
        }
    }
}

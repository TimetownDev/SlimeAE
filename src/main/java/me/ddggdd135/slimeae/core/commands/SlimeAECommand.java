package me.ddggdd135.slimeae.core.commands;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.api.SubCommand;
import me.ddggdd135.slimeae.core.commands.subcommands.HelpCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public class SlimeAECommand implements TabExecutor {
    private final Set<SubCommand> subCommands = new HashSet<>();

    @Nonnull
    public Set<SubCommand> getSubCommands() {
        return subCommands;
    }

    public void addSubCommand(@Nonnull SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    @Nullable public SubCommand getSubCommand(@Nonnull String subCommandName) {
        for (SubCommand subCommand : subCommands) {
            if (subCommand.getName().equals(subCommandName)) return subCommand;
        }

        return null;
    }

    @Override
    public boolean onCommand(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (args.length == 0) {
            HelpCommand helpCommand = (HelpCommand) getSubCommand("help");
            if (helpCommand != null) {
                helpCommand.printHelpMessages(commandSender);
            }

            return false;
        }

        SubCommand subCommand = getSubCommand(args[0]);
        if (subCommand == null) {
            commandSender.sendMessage(CMIChatColor.translate("&a找不到命令" + args[0]));
            return false;
        }

        return subCommand.onCommand(commandSender, command, s, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (args.length == 1) {
            return createReturnList(
                    subCommands.stream().map(SubCommand::getName).toList(), args[0]);
        }

        if (args.length >= 2) {
            SubCommand subCommand = getSubCommand(args[0]);
            if (subCommand != null) {
                return subCommand.onTabComplete(commandSender, command, s, args);
            }
        }

        return List.of();
    }

    @Nonnull
    private List<String> createReturnList(@Nonnull List<String> list, @Nonnull String string) {
        if (string.isEmpty()) {
            return list;
        }

        String input = string.toLowerCase(Locale.ROOT);
        List<String> returnList = new LinkedList<>();

        for (String item : list) {
            if (item.toLowerCase(Locale.ROOT).contains(input)) {
                returnList.add(item);
            } else if (item.equalsIgnoreCase(input)) {
                return Collections.emptyList();
            }
        }

        return returnList;
    }
}

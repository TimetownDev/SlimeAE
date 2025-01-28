package me.ddggdd135.slimeae.api;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public abstract class SubCommand implements TabExecutor {
    @Nonnull
    public abstract String getName();

    @Nonnull
    public abstract String getDescription();

    @Nonnull
    public abstract String getUsage();

    @Override
    public abstract boolean onCommand(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args);

    @Override
    @Nullable public abstract List<String> onTabComplete(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args);
}

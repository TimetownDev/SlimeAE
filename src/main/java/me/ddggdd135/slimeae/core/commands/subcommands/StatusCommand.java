package me.ddggdd135.slimeae.core.commands.subcommands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.SubCommand;
import me.ddggdd135.slimeae.api.database.v3.V3DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class StatusCommand extends SubCommand {
    @Override
    @Nonnull
    public String getName() {
        return "status";
    }

    @Override
    @Nonnull
    public String getDescription() {
        return "查看存储系统状态";
    }

    @Override
    @Nonnull
    public String getUsage() {
        return "/ae status";
    }

    @Override
    public boolean onCommand(
            @Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("slimeae.admin")) {
            sender.sendMessage(CMIChatColor.translate("&c你没有权限使用这个命令"));
            return false;
        }
        V3DatabaseManager mgr = SlimeAEPlugin.getV3DatabaseManager();
        sender.sendMessage(CMIChatColor.translate("&6========== SlimeAE 存储状态 =========="));
        sender.sendMessage(
                CMIChatColor.translate("&e后端: &f" + mgr.getStorageConfig().getBackend()));
        try (Connection conn = mgr.getConnectionManager().getReadConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ae_v3_cell_meta")) {
                    if (rs.next()) {
                        sender.sendMessage(CMIChatColor.translate("&e存储元件总数: &f" + rs.getInt(1)));
                    }
                }
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ae_v3_item_templates")) {
                    if (rs.next()) {
                        sender.sendMessage(CMIChatColor.translate("&e物品模板总数: &f" + rs.getInt(1)));
                    }
                }
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ae_v3_cell_items")) {
                    if (rs.next()) {
                        sender.sendMessage(CMIChatColor.translate("&e物品存储行数: &f" + rs.getInt(1)));
                    }
                }
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ae_v3_journal WHERE applied = 0")) {
                    if (rs.next()) {
                        sender.sendMessage(CMIChatColor.translate("&e待处理日志数: &f" + rs.getInt(1)));
                    }
                }
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ae_v3_journal_archive")) {
                    if (rs.next()) {
                        sender.sendMessage(CMIChatColor.translate("&e归档日志行数: &f" + rs.getInt(1)));
                    }
                }
                try (ResultSet rs = stmt.executeQuery(
                        "SELECT config_value FROM ae_v3_schema_info WHERE config_key = 'schema_version'")) {
                    if (rs.next()) {
                        sender.sendMessage(CMIChatColor.translate("&e数据库版本: &fv" + rs.getString("config_value")));
                    }
                }
            }
        } catch (Exception e) {
            sender.sendMessage(CMIChatColor.translate("&c获取状态失败: " + e.getMessage()));
        }
        sender.sendMessage(CMIChatColor.translate("&6======================================"));
        return true;
    }

    @Override
    @Nullable public List<String> onTabComplete(
            @Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return List.of();
    }
}

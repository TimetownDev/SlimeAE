package me.ddggdd135.slimeae.core.listeners;

import com.balugaq.jeg.api.objects.events.GuideEvents;
import com.balugaq.jeg.utils.ReflectionUtil;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JEGCompatibleListener implements Listener {
    public static final Map<UUID, GuideHistory> GUIDE_HISTORY = new ConcurrentHashMap<>();
    public static final Map<UUID, BiConsumer<GuideEvents.ItemButtonClickEvent, PlayerProfile>> PROFILE_CALLBACKS =
            new ConcurrentHashMap<>();

    public static void addCallback(
            @Nonnull UUID uuid, @Nonnull BiConsumer<GuideEvents.ItemButtonClickEvent, PlayerProfile> callback) {
        PROFILE_CALLBACKS.put(uuid, callback);
    }

    public static void removeCallback(@Nonnull UUID uuid) {
        PROFILE_CALLBACKS.remove(uuid);
    }

    @Nonnull
    public static PlayerProfile getPlayerProfile(OfflinePlayer player) {
        // Shouldn't be null;
        return PlayerProfile.find(player).orElseThrow(() -> new RuntimeException("PlayerProfile not found"));
    }

    public static void tagGuideOpen(Player player) {
        if (!PROFILE_CALLBACKS.containsKey(player.getUniqueId())) {
            return;
        }

        var profile = getPlayerProfile(player);
        saveOriginGuideHistory(profile);
        clearGuideHistory(profile);
    }

    @EventHandler
    public void onJEGItemClick(GuideEvents.ItemButtonClickEvent event) {
        var player = event.getPlayer();
        if (!PROFILE_CALLBACKS.containsKey(player.getUniqueId())) {
            return;
        }

        var profile = getPlayerProfile(player);
        rollbackGuideHistory(profile);
        PROFILE_CALLBACKS.get(player.getUniqueId()).accept(event, profile);
        PROFILE_CALLBACKS.remove(player.getUniqueId());
    }

    private static void saveOriginGuideHistory(PlayerProfile profile) {
        GuideHistory oldHistory = profile.getGuideHistory();
        GuideHistory newHistory = new GuideHistory(profile);
        ReflectionUtil.setValue(newHistory, "mainMenuPage", oldHistory.getMainMenuPage());
        ReflectionUtil.setValue(
                newHistory,
                "queue",
                ReflectionUtil.getValue(oldHistory, "queue", LinkedList.class).clone());
        GUIDE_HISTORY.put(profile.getUUID(), newHistory);
    }

    private void rollbackGuideHistory(PlayerProfile profile) {
        var originHistory = GUIDE_HISTORY.get(profile.getUUID());
        if (originHistory == null) {
            return;
        }

        ReflectionUtil.setValue(profile, "guideHistory", originHistory);
    }

    private static void clearGuideHistory(PlayerProfile profile) {
        ReflectionUtil.setValue(profile, "guideHistory", new GuideHistory(profile));
    }
}

package me.ddggdd135.slimeae.api.operations;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.api.abstracts.BusTickContext;
import me.ddggdd135.slimeae.api.interfaces.ISettingSlotHolder;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemInfo;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class ExportOperation {

    private ExportOperation() {}

    private static long pushToSlotsDirect(
            BlockMenu targetInv, ItemKey key, ItemStack template, long amount, int[] inputSlots) {
        int maxStack = template.getMaxStackSize();
        long remaining = amount;

        for (int slot : inputSlots) {
            if (remaining <= 0) break;
            ItemStack existing = targetInv.getItemInSlot(slot);
            if (existing == null || existing.getType().isAir()) {
                int toPlace = (int) Math.min(remaining, maxStack);
                targetInv.replaceExistingItem(slot, template.asQuantity(toPlace));
                remaining -= toPlace;
            } else if (key.equals(new ItemKey(existing))) {
                int spaceInSlot = maxStack - existing.getAmount();
                if (spaceInSlot > 0) {
                    int toAdd = (int) Math.min(remaining, spaceInSlot);
                    existing.setAmount(existing.getAmount() + toAdd);
                    remaining -= toAdd;
                }
            }
        }
        return remaining;
    }

    private static long pushToInventoryDirect(
            Inventory inventory, ItemKey key, ItemStack template, long amount, int[] inputSlots) {
        int maxStack = template.getMaxStackSize();
        long remaining = amount;

        for (int slot : inputSlots) {
            if (remaining <= 0) break;
            ItemStack existing = inventory.getItem(slot);
            if (existing == null || existing.getType().isAir()) {
                int toPlace = (int) Math.min(remaining, maxStack);
                inventory.setItem(slot, template.asQuantity(toPlace));
                remaining -= toPlace;
            } else if (key.equals(new ItemKey(existing))) {
                int spaceInSlot = maxStack - existing.getAmount();
                if (spaceInSlot > 0) {
                    int toAdd = (int) Math.min(remaining, spaceInSlot);
                    existing.setAmount(existing.getAmount() + toAdd);
                    remaining -= toAdd;
                }
            }
        }
        return remaining;
    }

    public static void executeToBlockMenu(
            @Nonnull BusTickContext context,
            @Nonnull Block busBlock,
            @Nonnull ISettingSlotHolder settingHolder,
            @Nonnull Block target) {
        if (!context.isValid()) return;
        BlockMenu targetInv = StorageCacheUtils.getMenu(target.getLocation());
        if (targetInv == null) return;

        IStorage networkStorage = context.getNetworkStorage();
        List<Pair<ItemKey, Integer>> settings = ensureSettingsCache(busBlock, settingHolder);
        if (settings == null) return;

        int tickMultiplier = context.getTickMultiplier();
        ItemHashMap<Long> storageSnapshot = networkStorage.getStorageUnsafe();

        int slotCount = settingHolder.getSettingSlots().length;
        int uniqueCount = 0;
        ItemKey[] uniqueKeys = new ItemKey[slotCount];
        long[] mergedAmounts = new long[slotCount];

        for (int i = 0; i < slotCount; i++) {
            Pair<ItemKey, Integer> setting = settings.get(i);
            if (setting == null) continue;
            ItemKey settingKey = setting.getFirstValue();
            ItemStack itemStack = settingKey.getItemStack();
            if (itemStack == null || itemStack.getType().isAir()) continue;

            int found = -1;
            for (int j = 0; j < uniqueCount; j++) {
                if (uniqueKeys[j].equals(settingKey)) {
                    found = j;
                    break;
                }
            }
            if (found >= 0) {
                mergedAmounts[found] += (long) setting.getSecondValue() * tickMultiplier;
            } else {
                uniqueKeys[uniqueCount] = settingKey;
                mergedAmounts[uniqueCount] = (long) setting.getSecondValue() * tickMultiplier;
                uniqueCount++;
            }
        }
        if (uniqueCount == 0) return;

        int reqCount = 0;
        int[] reqIndices = new int[uniqueCount];
        ItemRequest[] batchReqs = new ItemRequest[uniqueCount];
        int[][] slotArrays = new int[uniqueCount][];

        for (int i = 0; i < uniqueCount; i++) {
            Long available = storageSnapshot.getKey(uniqueKeys[i]);
            if (available == null || available <= 0) continue;
            int[] inputSlots;
            try {
                inputSlots = targetInv
                        .getPreset()
                        .getSlotsAccessedByItemTransport(
                                targetInv, ItemTransportFlow.INSERT, uniqueKeys[i].getItemStack());
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (inputSlots == null || inputSlots.length == 0) continue;

            long actualAmount = Math.min(mergedAmounts[i], available);
            if (actualAmount <= 0) continue;
            slotArrays[i] = inputSlots;
            reqIndices[reqCount] = i;
            batchReqs[reqCount] = new ItemRequest(uniqueKeys[i], actualAmount);
            reqCount++;
        }
        if (reqCount == 0) return;

        ItemRequest[] finalReqs =
                reqCount == batchReqs.length ? batchReqs : java.util.Arrays.copyOf(batchReqs, reqCount);
        ItemStorage taken = networkStorage.takeItem(finalReqs);
        ItemHashMap<Long> takenMap = taken.getStorageUnsafe();

        for (int r = 0; r < reqCount; r++) {
            int idx = reqIndices[r];
            Long takenAmount = takenMap.getKey(uniqueKeys[idx]);
            if (takenAmount == null || takenAmount <= 0) continue;
            long remainder = pushToSlotsDirect(
                    targetInv, uniqueKeys[idx], uniqueKeys[idx].getItemStack(), takenAmount, slotArrays[idx]);
            if (remainder > 0) {
                networkStorage.pushItem(new ItemInfo(uniqueKeys[idx], remainder));
            }
        }
    }

    public static void executeToVanillaContainer(
            @Nonnull BusTickContext context,
            @Nonnull Block busBlock,
            @Nonnull ISettingSlotHolder settingHolder,
            @Nonnull Block target) {
        if (!context.isValid()) return;
        if (!(PaperLib.getBlockState(target, false).getState() instanceof Container container)) return;

        IStorage networkStorage = context.getNetworkStorage();
        List<Pair<ItemKey, Integer>> settings = ensureSettingsCache(busBlock, settingHolder);
        if (settings == null) return;

        Inventory inventory = container.getInventory();
        int invSize = inventory.getSize();
        int[] inputSlots = new int[invSize];
        for (int s = 0; s < invSize; s++) inputSlots[s] = s;

        int tickMultiplier = context.getTickMultiplier();
        ItemHashMap<Long> storageSnapshot = networkStorage.getStorageUnsafe();

        int slotCount = settingHolder.getSettingSlots().length;
        int uniqueCount = 0;
        ItemKey[] uniqueKeys = new ItemKey[slotCount];
        long[] mergedAmounts = new long[slotCount];

        for (int i = 0; i < slotCount; i++) {
            Pair<ItemKey, Integer> setting = settings.get(i);
            if (setting == null) continue;
            ItemKey settingKey = setting.getFirstValue();
            ItemStack itemStack = settingKey.getItemStack();
            if (itemStack == null || itemStack.getType().isAir()) continue;

            int found = -1;
            for (int j = 0; j < uniqueCount; j++) {
                if (uniqueKeys[j].equals(settingKey)) {
                    found = j;
                    break;
                }
            }
            if (found >= 0) {
                mergedAmounts[found] += (long) setting.getSecondValue() * tickMultiplier;
            } else {
                uniqueKeys[uniqueCount] = settingKey;
                mergedAmounts[uniqueCount] = (long) setting.getSecondValue() * tickMultiplier;
                uniqueCount++;
            }
        }
        if (uniqueCount == 0) return;

        int reqCount = 0;
        int[] reqIndices = new int[uniqueCount];
        ItemRequest[] batchReqs = new ItemRequest[uniqueCount];

        for (int i = 0; i < uniqueCount; i++) {
            Long available = storageSnapshot.getKey(uniqueKeys[i]);
            if (available == null || available <= 0) continue;
            long actualAmount = Math.min(mergedAmounts[i], available);
            if (actualAmount <= 0) continue;
            reqIndices[reqCount] = i;
            batchReqs[reqCount] = new ItemRequest(uniqueKeys[i], actualAmount);
            reqCount++;
        }
        if (reqCount == 0) return;

        ItemRequest[] finalReqs =
                reqCount == batchReqs.length ? batchReqs : java.util.Arrays.copyOf(batchReqs, reqCount);
        ItemStorage taken = networkStorage.takeItem(finalReqs);
        ItemHashMap<Long> takenMap = taken.getStorageUnsafe();

        for (int r = 0; r < reqCount; r++) {
            int idx = reqIndices[r];
            Long takenAmount = takenMap.getKey(uniqueKeys[idx]);
            if (takenAmount == null || takenAmount <= 0) continue;
            long remainder = pushToInventoryDirect(
                    inventory, uniqueKeys[idx], uniqueKeys[idx].getItemStack(), takenAmount, inputSlots);
            if (remainder > 0) {
                networkStorage.pushItem(new ItemInfo(uniqueKeys[idx], remainder));
            }
        }
    }

    public static void executeSingleDirection(
            @Nonnull BusTickContext context,
            @Nonnull Block busBlock,
            @Nonnull ISettingSlotHolder settingHolder,
            boolean allowVanilla) {
        if (context.getDirection() == BlockFace.SELF) return;
        Block target = context.getBlock().getRelative(context.getDirection());

        if (allowVanilla) {
            BlockMenu targetMenu = StorageCacheUtils.getMenu(target.getLocation());
            if (targetMenu != null) {
                executeToBlockMenu(context, busBlock, settingHolder, target);
            } else {
                executeToVanillaContainer(context, busBlock, settingHolder, target);
            }
        } else {
            executeToBlockMenu(context, busBlock, settingHolder, target);
        }
    }

    public static void executeMultiDirection(
            @Nonnull BusTickContext context,
            @Nonnull Block busBlock,
            @Nonnull ISettingSlotHolder settingHolder,
            boolean allowVanilla) {
        if (context.getDirections() == null || context.getDirections().isEmpty()) return;
        for (BlockFace face : context.getDirections()) {
            if (face == BlockFace.SELF) continue;
            Block target = context.getBlock().getRelative(face);
            if (allowVanilla) {
                BlockMenu targetMenu = StorageCacheUtils.getMenu(target.getLocation());
                if (targetMenu != null) {
                    executeToBlockMenu(context, busBlock, settingHolder, target);
                } else {
                    executeToVanillaContainer(context, busBlock, settingHolder, target);
                }
            } else {
                executeToBlockMenu(context, busBlock, settingHolder, target);
            }
        }
    }

    public static void executeChained(
            @Nonnull BusTickContext context, @Nonnull Block busBlock, @Nonnull ISettingSlotHolder settingHolder) {
        if (context.getDirection() == BlockFace.SELF) return;
        if (!context.isValid()) return;

        List<Pair<ItemKey, Integer>> settings = ensureSettingsCache(busBlock, settingHolder);
        if (settings == null) return;

        IStorage networkStorage = context.getNetworkStorage();
        int tickMultiplier = context.getTickMultiplier();

        int slotCount = settingHolder.getSettingSlots().length;
        int uniqueCount = 0;
        ItemKey[] uniqueKeys = new ItemKey[slotCount];
        long[] baseAmounts = new long[slotCount];

        for (int i = 0; i < slotCount; i++) {
            Pair<ItemKey, Integer> setting = settings.get(i);
            if (setting == null) continue;
            ItemKey settingKey = setting.getFirstValue();
            ItemStack itemStack = settingKey.getItemStack();
            if (itemStack == null || itemStack.getType().isAir()) continue;

            int found = -1;
            for (int j = 0; j < uniqueCount; j++) {
                if (uniqueKeys[j].equals(settingKey)) {
                    found = j;
                    break;
                }
            }
            if (found >= 0) {
                baseAmounts[found] += (long) setting.getSecondValue() * tickMultiplier;
            } else {
                uniqueKeys[uniqueCount] = settingKey;
                baseAmounts[uniqueCount] = (long) setting.getSecondValue() * tickMultiplier;
                uniqueCount++;
            }
        }
        if (uniqueCount == 0) return;

        BlockFace dir = context.getDirection();
        int dx = dir.getModX();
        int dy = dir.getModY();
        int dz = dir.getModZ();
        Block origin = context.getBlock();
        World world = origin.getWorld();
        int bx = origin.getX() + dx;
        int by = origin.getY() + dy;
        int bz = origin.getZ() + dz;
        int chainDist = context.getChainDistance();

        int validCount = 0;
        BlockMenu[] validMenus = new BlockMenu[chainDist];
        int[][][] validSlotArrays = new int[chainDist][uniqueCount][];

        for (int i = 0; i < chainDist; i++) {
            Block target = world.getBlockAt(bx + dx * i, by + dy * i, bz + dz * i);
            BlockMenu targetInv = StorageCacheUtils.getMenu(target.getLocation());
            if (targetInv == null) continue;

            int[][] slotsForTarget = new int[uniqueCount][];
            boolean hasAnySlot = false;
            for (int j = 0; j < uniqueCount; j++) {
                try {
                    slotsForTarget[j] = targetInv
                            .getPreset()
                            .getSlotsAccessedByItemTransport(
                                    targetInv, ItemTransportFlow.INSERT, uniqueKeys[j].getItemStack());
                } catch (IllegalArgumentException e) {
                    slotsForTarget[j] = null;
                }
                if (slotsForTarget[j] != null && slotsForTarget[j].length > 0) hasAnySlot = true;
            }
            if (!hasAnySlot) continue;

            validMenus[validCount] = targetInv;
            validSlotArrays[validCount] = slotsForTarget;
            validCount++;
        }
        if (validCount == 0) return;

        ItemHashMap<Long> storageSnapshot = networkStorage.getStorageUnsafe();
        long[] totalDemand = new long[uniqueCount];
        for (int j = 0; j < uniqueCount; j++) {
            totalDemand[j] = baseAmounts[j] * validCount;
        }

        int reqCount = 0;
        int[] reqIndices = new int[uniqueCount];
        ItemRequest[] batchReqs = new ItemRequest[uniqueCount];

        for (int j = 0; j < uniqueCount; j++) {
            Long available = storageSnapshot.getKey(uniqueKeys[j]);
            if (available == null || available <= 0) continue;
            long actualAmount = Math.min(totalDemand[j], available);
            if (actualAmount <= 0) continue;
            reqIndices[reqCount] = j;
            batchReqs[reqCount] = new ItemRequest(uniqueKeys[j], actualAmount);
            reqCount++;
        }
        if (reqCount == 0) return;

        ItemRequest[] finalReqs =
                reqCount == batchReqs.length ? batchReqs : java.util.Arrays.copyOf(batchReqs, reqCount);
        ItemStorage taken = networkStorage.takeItem(finalReqs);
        ItemHashMap<Long> takenMap = taken.getStorageUnsafe();

        long[] remaining = new long[uniqueCount];
        for (int r = 0; r < reqCount; r++) {
            int idx = reqIndices[r];
            Long takenAmount = takenMap.getKey(uniqueKeys[idx]);
            if (takenAmount != null && takenAmount > 0) remaining[idx] = takenAmount;
        }

        for (int t = 0; t < validCount; t++) {
            BlockMenu targetInv = validMenus[t];
            for (int j = 0; j < uniqueCount; j++) {
                if (remaining[j] <= 0) continue;
                int[] inputSlots = validSlotArrays[t][j];
                if (inputSlots == null || inputSlots.length == 0) continue;
                long toPush = Math.min(remaining[j], baseAmounts[j]);
                long leftover =
                        pushToSlotsDirect(targetInv, uniqueKeys[j], uniqueKeys[j].getItemStack(), toPush, inputSlots);
                remaining[j] -= (toPush - leftover);
            }
        }

        for (int j = 0; j < uniqueCount; j++) {
            if (remaining[j] > 0) {
                networkStorage.pushItem(new ItemInfo(uniqueKeys[j], remaining[j]));
            }
        }
    }

    @Nullable private static List<Pair<ItemKey, Integer>> ensureSettingsCache(
            @Nonnull Block busBlock, @Nonnull ISettingSlotHolder settingHolder) {
        if (!ISettingSlotHolder.cache.containsKey(busBlock.getLocation())) {
            SlimefunBlockData data = StorageCacheUtils.getBlock(busBlock.getLocation());
            if (data == null) return null;
            ISettingSlotHolder.updateCache(busBlock, settingHolder, data);
        }
        return ISettingSlotHolder.getCache(busBlock.getLocation());
    }
}

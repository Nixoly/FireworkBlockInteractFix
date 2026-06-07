package dev.nixoly.fireworkblockinteractfix;

import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
final class RocketCharge {

    private final TaskScheduler scheduler;
    private final Map<UUID, Held> pendingSnapshots = new HashMap<>();

    void snapshot(Player player) {
        PlayerInventory inv = player.getInventory();
        pendingSnapshots.put(player.getUniqueId(), new Held(
                inv.getItemInMainHand().clone(),
                inv.getItemInOffHand().clone()));
    }

    void consumeNextTick(Player player) {
        UUID id = player.getUniqueId();
        scheduler.runTask(player, () -> {
            Held before = pendingSnapshots.remove(id);
            if (before == null || !player.isOnline() || player.getGameMode() == GameMode.CREATIVE) {
                return;
            }

            PlayerInventory inv = player.getInventory();
            if (before.serverAlreadyAteOne(inv)) {
                return;
            }
            takeOneRocket(inv);
        });
    }

    private static void takeOneRocket(PlayerInventory inv) {
        if (decrementIfRocket(inv.getItemInOffHand(), amount -> applyToOffHand(inv, amount))) {
            return;
        }
        if (decrementIfRocket(inv.getItemInMainHand(), amount -> applyToMainHand(inv, amount))) {
            return;
        }
        removeOneFromStorage(inv);
    }

    private static void removeOneFromStorage(PlayerInventory inv) {
        ItemStack[] contents = inv.getStorageContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack stack = contents[slot];
            if (stack == null || stack.getType() != Material.FIREWORK_ROCKET || stack.getAmount() <= 0) {
                continue;
            }
            int remaining = stack.getAmount() - 1;
            if (remaining <= 0) {
                inv.setItem(slot, null);
            } else {
                stack.setAmount(remaining);
                inv.setItem(slot, stack);
            }
            return;
        }
    }

    private static boolean decrementIfRocket(ItemStack stack, Consumer<ItemStack> apply) {
        if (stack.getType() != Material.FIREWORK_ROCKET || stack.getAmount() <= 0) {
            return false;
        }
        int remaining = stack.getAmount() - 1;
        if (remaining <= 0) {
            apply.accept(new ItemStack(Material.AIR));
        } else {
            stack.setAmount(remaining);
            apply.accept(stack);
        }
        return true;
    }

    private static void applyToMainHand(PlayerInventory inv, ItemStack updated) {
        inv.setItemInMainHand(updated);
    }

    private static void applyToOffHand(PlayerInventory inv, ItemStack updated) {
        inv.setItemInOffHand(updated);
    }

    private static final class Held {

        final ItemStack main;
        final ItemStack off;

        Held(ItemStack main, ItemStack off) {
            this.main = main;
            this.off = off;
        }

        boolean serverAlreadyAteOne(PlayerInventory now) {
            return lostOne(main, now.getItemInMainHand()) || lostOne(off, now.getItemInOffHand());
        }

        private static boolean lostOne(ItemStack before, ItemStack after) {
            if (before.getType() != Material.FIREWORK_ROCKET) {
                return false;
            }
            if (after.getType() != Material.FIREWORK_ROCKET) {
                return true;
            }
            return after.getAmount() < before.getAmount();
        }
    }
}

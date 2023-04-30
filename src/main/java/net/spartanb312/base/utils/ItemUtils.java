package net.spartanb312.base.utils;

import me.afterdarkness.moloch.utils.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.spartanb312.base.utils.math.Pair;

import java.util.ArrayList;
import java.util.List;

public class ItemUtils {
    public static Minecraft mc = Minecraft.getMinecraft();

    public static int getItemCount(Item item) {
        int count = mc.player.inventory.mainInventory.stream()
                .filter(itemStack -> itemStack.getItem() == item)
                .mapToInt(ItemStack::getCount).sum();
        if (mc.player.getHeldItemOffhand().getItem() == item) {
            count += mc.player.getHeldItemOffhand().getCount();
        }
        return count;
    }

    public static int findItemInHotBar(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static int findShulkerInHotBar() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Item.getItemFromBlock(Blocks.PURPLE_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.WHITE_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.ORANGE_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.MAGENTA_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.LIGHT_BLUE_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.YELLOW_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.LIME_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.PINK_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.GRAY_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.SILVER_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.CYAN_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.BLUE_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.BROWN_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.GREEN_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.RED_SHULKER_BOX)
                    || itemStack.getItem() == Item.getItemFromBlock(Blocks.BLACK_SHULKER_BOX)) {
                return i;
            }
        }
        return -1;
    }

    public static void switchToSlot(int slot, boolean silent) {
        if (slot == -1) return;

        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
        if (!silent) {
            mc.player.inventory.currentItem = slot;
            mc.playerController.updateController();
        }
    }

    public static void pickSwap(int slot) {
        if (slot == -1)
            mc.playerController.pickItem(slot);
    }

    public static int findBlockInHotBar(Block block) {
        return findItemInHotBar(Item.getItemFromBlock(block));
    }

    public static boolean isItemInHotbar(Item item) {
        boolean isItemPresent = false;
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == item) {
                isItemPresent = true;
            }
        }
        return isItemPresent;
    }

    public static int findWitherSkullInHotBar() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getDisplayName().equals("Wither Skeleton Skull")) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isWitherSkullInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getDisplayName().equals("Wither Skeleton Skull")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWitherSkullInInventory() {
        for (Slot slot : mc.player.inventoryContainer.inventorySlots) {
            if (slot.getStack().getDisplayName().equals("Wither Skeleton Skull"))
                return true;
        }
        return false;
    }

    public static int witherSkullSlotIDinInventory() {
        for (int i = 0; i < 45; i++) {
            if (i >= 5 && i <= 8) continue;
            if (mc.player.inventoryContainer.inventorySlots.get(i).getStack().getDisplayName().equals("Wither Skeleton Skull"))
                return i;
        }
        return 99999;
    }

    public static void swapWitherSkullFromInvToHotBar(int hotBarSlot) {
        int slotID = witherSkullSlotIDinInventory();

        if (slotID != 99999) {
            mc.playerController.windowClick(0, slotID, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, hotBarSlot + 36, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, slotID, 0, ClickType.PICKUP, mc.player);
            mc.playerController.updateController();
        }
    }

    public static int fastestMiningTool(Block toMineBlockMaterial) {
        float fastestSpeed = 1.0f;
        int theSlot = mc.player.inventory.currentItem;

        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.isEmpty || !(itemStack.getItem() instanceof ItemTool || itemStack.getItem() instanceof ItemSword || itemStack.getItem() instanceof ItemHoe || itemStack.getItem() instanceof ItemShears))
                continue;

            float mineSpeed = BlockUtil.blockBreakSpeed(toMineBlockMaterial.getDefaultState(), itemStack);

            if (mineSpeed > fastestSpeed) {
                fastestSpeed = mineSpeed;
                theSlot = i;
            }
        }

        return theSlot;
    }

    public static boolean isItemInInventory(Item item) {
        for (Slot slot : mc.player.inventoryContainer.inventorySlots) {
            if (slot.getStack().getItem() == item)
                return true;
        }
        return false;
    }

    public static int itemSlotIDinInventory(Item item) {
        for (int i = 0; i < 45; i++) {
            if (mc.player.inventoryContainer.inventorySlots.get(i).getStack().getItem() == item)
                return i;
        }
        return 99999;
    }

    public static void swapItemFromInvToHotBar(Item item, int hotBarSlot) {
        int slotID = itemSlotIDinInventory(item);

        if (slotID != 99999) {
            mc.playerController.windowClick(0, slotID, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, hotBarSlot + 36, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, slotID, 0, ClickType.PICKUP, mc.player);
            mc.playerController.updateController();
        }
    }

    public static int findItemInInv(Item item) {
        for (Pair<Integer, ItemStack> data : getInventoryAndHotbarSlots()) {
            if (data.b.getItem() == item) {
                return data.a;
            }
        }
        return -999;
    }

    public static List<Pair<Integer, ItemStack>> getInventoryAndHotbarSlots() {
        return getInventorySlots(9, 44);
    }

    private static List<Pair<Integer, ItemStack>> getInventorySlots(int current, int last) {
        List<Pair<Integer, ItemStack>> invSlots = new ArrayList<>();
        while (current <= last) {
            invSlots.add(new Pair<>(current, mc.player.inventoryContainer.getInventory().get(current)));
            current++;
        }
        return invSlots;
    }

    public static float getItemDMG(ItemStack itemStack) {
        return (itemStack.getMaxDamage() - itemStack.getItemDamage()) / (float)itemStack.getMaxDamage();
    }

    public static boolean isHelmet(Item item, boolean includeAir) {
        return (includeAir && item == Items.AIR) || item == Items.DIAMOND_HELMET || item == Items.IRON_HELMET || item == Items.GOLDEN_HELMET || item == Items.CHAINMAIL_HELMET || item == Items.LEATHER_HELMET;
    }

    public static boolean isChestplate(Item item, boolean includeAir) {
        return (includeAir && item == Items.AIR) || item == Items.DIAMOND_CHESTPLATE || item == Items.IRON_CHESTPLATE || item == Items.GOLDEN_CHESTPLATE || item == Items.CHAINMAIL_CHESTPLATE || item == Items.LEATHER_CHESTPLATE;
    }

    public static boolean isLeggings(Item item, boolean includeAir) {
        return (includeAir && item == Items.AIR) || item == Items.DIAMOND_LEGGINGS || item == Items.IRON_LEGGINGS || item == Items.GOLDEN_LEGGINGS || item == Items.CHAINMAIL_LEGGINGS || item == Items.LEATHER_LEGGINGS;
    }

    public static boolean isBoot(Item item, boolean includeAir) {
        return (includeAir && item == Items.AIR) || item == Items.DIAMOND_BOOTS || item == Items.IRON_BOOTS || item == Items.GOLDEN_BOOTS || item == Items.CHAINMAIL_BOOTS || item == Items.LEATHER_BOOTS;
    }

    public static ItemStack getItemFromSlot(int slot) {
        return mc.player.inventoryContainer.inventorySlots.get(slot).getStack();
    }

    public static boolean hasEmptySlots() {
        for (Pair<Integer, ItemStack> data : getInventoryAndHotbarSlots()) {
            if (data.b.isEmpty) return true;
        }
        return false;
    }

    public static int getEmptySlot() {
        for (int i = 0; i < 45; i++) {
            if (mc.player.inventoryContainer.inventorySlots.get(i).getStack().isEmpty)
                return i;
        }
        return -1;
    }
}

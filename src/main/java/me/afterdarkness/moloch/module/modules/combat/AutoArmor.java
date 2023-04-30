package me.afterdarkness.moloch.module.modules.combat;

import me.afterdarkness.moloch.client.ServerManager;
import me.afterdarkness.moloch.module.modules.client.FriendsEnemies;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.common.KeyBind;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.EntityUtil;
import net.spartanb312.base.utils.ItemUtils;
import net.spartanb312.base.utils.MathUtilFuckYou;
import net.spartanb312.base.utils.Timer;
import net.spartanb312.base.utils.math.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Parallel(runnable = true)
@ModuleInfo(name = "AutoArmor", category = Category.COMBAT, description = "Automatically equips armor")
public class AutoArmor extends Module {

    Setting<Integer> delay = setting("Delay", 50, 1, 500).des("Delay in milliseconds between attempts to move armor");
    Setting<Boolean> pauseWhileUsingItem = setting("OnUseItemPause", false).des("Doesn't attempt to equip armor while your hand is active");
    Setting<Boolean> pauseWhileMoving = setting("MovingPause", false).des("Doesn't attempt to equip armor if you're moving");
    Setting<Boolean> soft = setting("Soft", false).des("Only equips armor if that armor slot is empty");
    Setting<Boolean> equipInInv = setting("EquipInInventory", true).des("Attempts to equip armor while inventory is open");
    Setting<Boolean> elytraPriority = setting("ElytraPriority", false).des("Always prioritizes elytra over chestplate").whenFalse(soft);
    Setting<Boolean> elytraReplace = setting("ElytraReplace", true).des("Replaces elytra with another elytra when it isn't usable anymore");
    Setting<KeyBind> elytraSwapBind = setting("ElytraSwapBind", subscribeKey(new KeyBind(getAnnotation().keyCode(), this::swapElytra))).des("Keybind to swap elytra with").whenFalse(elytraPriority);
    Setting<Boolean> armorSave = setting("ArmorSave", true).des("Swaps out armor when it reaches a certain durability (if you have extra armor in inventory)");
    Setting<Integer> armorSaveThreshold = setting("ArmorSaveThreshold", 10, 1, 50).des("Durability percentage to swap out armor at").whenTrue(armorSave);
    Setting<Boolean> betterMend = setting("BetterMend", false).des("Takes off armor (while throwing xp) after they've reached a certain durability to mend other pieces of armor faster");
    Setting<Boolean> betterMendStrict = setting("BetterMendStrict", false).des("Only take off armor when standing still").whenTrue(betterMend);
    Setting<Float> minPlayerDistance = setting("MendMinPlayerDist", 8.0f, 0.0f, 20.0f).des("Minimum distance that a non friend player can be away from you before you can automatically take off armor to mend").whenTrue(betterMend);
    Setting<Integer> mendingThreshold = setting("MendingThreshold", 85, 1, 100).des("Durability percentage that your armor will be automatically removed when mending").whenTrue(betterMend);

    private boolean usingElytra, usingElytra1, switchingElytraFlag;
    private int armorIndex = 5;
    private final Timer armorTimer = new Timer();
    private final Timer mendTimer = new Timer();

    @Override
    public void onTick() {
        if ((mc.player.openContainer != mc.player.inventoryContainer && equipInInv.getValue())
                || (mc.currentScreen instanceof GuiContainer && !equipInInv.getValue())
                || (mc.currentScreen instanceof GuiInventory && !mc.player.inventory.itemStack.isEmpty)
                || (pauseWhileUsingItem.getValue() && mc.player.handActive)
                || (pauseWhileMoving.getValue() && !EntityUtil.isStill())
                || (!armorTimer.passed(delay.getValue()))) return;

        usingElytra = false;
        boolean preferElytra = (!soft.getValue() && elytraPriority.getValue())
                || (!elytraPriority.getValue() && usingElytra1 && ItemUtils.isItemInInventory(Items.ELYTRA))
                || (elytraReplace.getValue() && ItemUtils.getItemFromSlot(6).getItem() == Items.ELYTRA && !ItemElytra.isUsable(ItemUtils.getItemFromSlot(6)));
        usingElytra = preferElytra;
        if (soft.getValue() && ItemUtils.getItemFromSlot(6).getItem() == Items.ELYTRA) {
            usingElytra = true;
        }

        if (soft.getValue() && !ItemUtils.getItemFromSlot(armorIndex).isEmpty) {
            if (switchingElytraFlag && armorIndex == 6) {
                switchingElytraFlag = false;
            }
            else if (!(elytraReplace.getValue() && !ItemElytra.isUsable(ItemUtils.getItemFromSlot(6)) && armorIndex == 6)
                    && !(armorSave.getValue() && (ItemUtils.getItemDMG(ItemUtils.getItemFromSlot(armorIndex)) * 100.0f) <= armorSaveThreshold.getValue())
                    && !(betterMend.getValue() && (ItemUtils.getItemDMG(ItemUtils.getItemFromSlot(armorIndex)) * 100.0f) >= mendingThreshold.getValue() && canDoMendSwap())) {
                armorIndex++;

                if (armorIndex > 8) {
                    armorIndex = 5;
                }
                return;
            }
        }

        List<Pair<Integer, ItemStack>> listToSearch = new ArrayList<>();


        switch (armorIndex) {
            case 5: {
                listToSearch = ItemUtils.getInventoryAndHotbarSlots().stream().filter(data -> ItemUtils.isHelmet(data.b.getItem(), true)).collect(Collectors.toList());
                break;
            }

            case 6: {
                listToSearch = ItemUtils.getInventoryAndHotbarSlots().stream().filter(data -> ItemUtils.isChestplate(data.b.getItem(), true)).collect(Collectors.toList());
                break;
            }

            case 7: {
                listToSearch = ItemUtils.getInventoryAndHotbarSlots().stream().filter(data -> ItemUtils.isLeggings(data.b.getItem(), true)).collect(Collectors.toList());
                break;
            }

            case 8: {
                listToSearch = ItemUtils.getInventoryAndHotbarSlots().stream().filter(data -> ItemUtils.isBoot(data.b.getItem(), true)).collect(Collectors.toList());
                break;
            }
        }

        Integer slot;
        if (armorIndex == 6 && preferElytra) {
            slot = findBetterElytraSlot();
        }
        else {
            slot = findBetterArmorSlot(mc.player.inventoryContainer.inventorySlots.get(armorIndex).getStack(), listToSearch);
        }

        if (slot != null) {
            equipArmor(slot, armorIndex);
        }

        armorIndex++;

        if (armorIndex > 8) {
            armorIndex = 5;
        }
        armorTimer.reset();
    }

    @Override
    public void onPacketSend(PacketEvent.Send event) {
        if (betterMend.getValue() && event.getPacket() instanceof CPacketPlayerTryUseItem &&
                (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE || ServerManager.isServerSideHoldingMain(Items.EXPERIENCE_BOTTLE))) {
            mendTimer.reset();
        }
    }

    @Override
    public String getModuleInfo() {
        if (usingElytra) {
            return "Elytra";
        }
        else {
            return "Chestplate";
        }
    }

    private void equipArmor(int fromSlot, int toSlot) {
        if (betterMend.getValue() && (ItemUtils.getItemDMG(ItemUtils.getItemFromSlot(toSlot)) * 100.0f) >= mendingThreshold.getValue() && canDoMendSwap()) {
            mc.playerController.windowClick(0, toSlot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, fromSlot, 0, ClickType.PICKUP, mc.player);
        }
        else {
            if (ItemUtils.getItemFromSlot(fromSlot).getCount() > 1) {
                mc.playerController.windowClick(0, fromSlot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, toSlot, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, fromSlot, 0, ClickType.PICKUP, mc.player);
            }
            else {
                if (ItemUtils.getItemFromSlot(toSlot).isEmpty) {
                    mc.playerController.windowClick(0, fromSlot, 0, ClickType.QUICK_MOVE, mc.player);
                }
                else if (ItemUtils.hasEmptySlots()) {
                    mc.playerController.windowClick(0, toSlot, 0, ClickType.QUICK_MOVE, mc.player);
                    mc.playerController.windowClick(0, fromSlot, 0, ClickType.QUICK_MOVE, mc.player);
                }
                else {
                    mc.playerController.windowClick(0, fromSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, toSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, fromSlot, 0, ClickType.PICKUP, mc.player);
                }
            }
        }

        mc.playerController.updateController();
    }

    private Integer findBetterElytraSlot() {
        Integer preferredSlot = null;
        float highestWeight = getElytraWeight(ItemUtils.getItemFromSlot(6));
        for (Pair<Integer, ItemStack> data : ItemUtils.getInventoryAndHotbarSlots().stream().filter(data -> data.b.getItem() == Items.ELYTRA).collect(Collectors.toList())) {
            float weight = getElytraWeight(data.b);

            if (weight > highestWeight) {
                highestWeight = weight;
                preferredSlot = data.a;
            }
        }
        return preferredSlot;
    }

    private Integer findBetterArmorSlot(ItemStack stack, List<Pair<Integer, ItemStack>> listToSearch) {
        Integer preferredSlot = null;
        float highestWeight = getItemWeight(stack);
        for (Pair<Integer, ItemStack> data : listToSearch) {
            float weight = getItemWeight(data.b);

            if (weight > highestWeight) {
                highestWeight = weight;
                preferredSlot = data.a;
            }
        }
        return preferredSlot;
    }

    private float getItemWeight(ItemStack stack) {
        Item item = stack.getItem();

        if (!(item instanceof ItemArmor)) return 0.0f;
        else {
            ItemArmor armor = (ItemArmor) item;

            return armor.damageReduceAmount * getArmorProtectionWeight(stack) *
                    (armorSave.getValue() && (ItemUtils.getItemDMG(stack) * 100.0f) <= armorSaveThreshold.getValue() ? 0.1f : 1.0f) *
                    (betterMend.getValue() && ((ItemUtils.getItemDMG(stack) * 100.0f) >= mendingThreshold.getValue() || ItemUtils.itemSlotIDinInventory(item) < 5 || ItemUtils.itemSlotIDinInventory(item) > 8) && canDoMendSwap() ? -1.0f : 1.0f);
        }
    }

    //also from trollheck
    private float getArmorProtectionWeight(ItemStack stack) {
        int protLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack);
        float factor = protLevel;

        if (stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).armorType == EntityEquipmentSlot.LEGS) {
            factor = Math.max(EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, stack) * 2, protLevel);
        }

        return 1.0f + 0.04f * factor;
    }

    private float getElytraWeight(ItemStack stack) {
        if (stack.getItem() != Items.ELYTRA) return 0.0f;
        else return ((!ItemElytra.isUsable(stack) && elytraReplace.getValue()) ? 0.1f : 1.0f)
                * (1.0f + EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack) + (0.4f * EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack)));
    }

    private void swapElytra() {
        if (!ItemUtils.isItemInInventory(Items.ELYTRA)) {
            usingElytra1 = false;
            return;
        }
        usingElytra1 = ItemUtils.getItemFromSlot(6).getItem() != Items.ELYTRA;
        switchingElytraFlag = true;
    }

    private boolean canDoMendSwap() {
        EntityPlayer closestPlayer = null;
        HashMap<Entity, Pair<Boolean, Boolean>> map;
        synchronized (FriendsEnemies.INSTANCE.entityData) {
            map = new HashMap<>(FriendsEnemies.INSTANCE.entityData);
        }
        for (Map.Entry<Entity, Pair<Boolean, Boolean>> entry : map.entrySet()) {
            Entity entity = entry.getKey();
            if (entity instanceof EntityPlayer && entity != mc.renderViewEntity && !entry.getValue().a
                    && (closestPlayer == null || MathUtilFuckYou.getDistSq(mc.player.getPositionVector(), entity.getPositionVector()) < MathUtilFuckYou.getDistSq(mc.player.getPositionVector(), closestPlayer.getPositionVector()))) {
                closestPlayer = (EntityPlayer) entity;
            }
        }
        return (closestPlayer == null || !MathUtilFuckYou.isWithinRange(mc.player.getPositionVector(), closestPlayer.getPositionVector(), minPlayerDistance.getValue()))
                && !mendTimer.passed(150)
                && (!betterMendStrict.getValue() || EntityUtil.isStill());
    }
}

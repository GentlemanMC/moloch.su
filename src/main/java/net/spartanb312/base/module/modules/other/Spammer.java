package net.spartanb312.base.module.modules.other;

import me.afterdarkness.moloch.event.events.player.FinishEatingEvent;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.Vec3d;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.client.FriendManager;
import net.spartanb312.base.common.annotations.ModuleInfo;
import net.spartanb312.base.common.annotations.Parallel;
import net.spartanb312.base.core.concurrent.ConcurrentTaskManager;
import net.spartanb312.base.core.concurrent.repeat.RepeatUnit;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.setting.Setting;
import net.spartanb312.base.core.setting.settings.StringSetting;
import net.spartanb312.base.event.events.network.PacketEvent;
import net.spartanb312.base.module.Category;
import net.spartanb312.base.module.Module;
import net.spartanb312.base.utils.FileUtil;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.spartanb312.base.utils.MathUtilFuckYou;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.runRepeat;
import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.unregisterRepeatUnit;

@Parallel(runnable = true)
@ModuleInfo(name = "Spammer", category = Category.OTHER, description = "Automatically spam", hasCollector = true)
public class Spammer extends Module {

    Setting<Integer> delay = setting("DelayS", 10, 1, 100).des("Delay between spammer messages in seconds");
    Setting<Boolean> autoReadFile = setting("AutoReadFile", true).des("Automatically update spammer file in the background");
    Setting<Integer> readFileDelay = setting("ReadFileDelay", 1000, 100, 10000).des("Milliseconds between attempts to read spammer file").whenTrue(autoReadFile);
    Setting<Boolean> greenText = setting("GreenText", false).des("Puts '>' in front of message to make it green on some servers");
    Setting<Mode> mode = setting("Mode", Mode.File).des("Mode of spammer");
    Setting<String> fileInput = setting("FileInput", "", true, new ArrayList<>(), false).des("Manage spammer file");
    Setting<Boolean> targetFriends = setting("TargetFriends", false).des("Allow friends' names to be used in spammer messages").whenAtMode(mode, Mode.File);
    Setting<String> nameRegex = setting("NameRegex", "XXXX", false, null, false).des("String to replace with some random player's name in spammer").whenAtMode(mode, Mode.File);
    Setting<String> secondNameRegex = setting("SecondNameRegex", "SSSS", false, null, false).des("Second string to replace with some other random player's name in spammer").whenAtMode(mode, Mode.File);

    Setting<Boolean> plural = setting("Plural", true).des("Adds an 's' to the end of item/block names");
    Setting<Boolean> walking = setting("Walking", true).des("Send messages showing how far you walked");
    Setting<String> walkingPre = setting("WalkingPre", "I just walked", false, null, false).des("Stuff to add to the beginning of walking message").whenTrue(walking);
    Setting<String> walkingPost = setting("WalkingPost", "", false, null, false).des("Stuff to add to the end of walking message").whenTrue(walking);
    Setting<Boolean> eating = setting("Eating", true).des("Send messages showing how much you ate");
    Setting<String> eatingPre = setting("EatingPre", "I just ate", false, null, false).des("Stuff to add to the beginning of eating message").whenTrue(eating);
    Setting<String> eatingPost = setting("EatingPost", "", false, null, false).des("Stuff to add to the end of eating message").whenTrue(eating);
    Setting<Boolean> placing = setting("Placing", true).des("Send messages showing how many blocks you placed");
    Setting<String> placingPre = setting("PlacingPre", "I just placed", false, null, false).des("Stuff to add to the beginning of placing message").whenTrue(placing);
    Setting<String> placingPost = setting("PlacingPost", "", false, null, false).des("Stuff to add to the end of placing message").whenTrue(placing);
    Setting<Boolean> breaking = setting("Breaking", true).des("Send messages showing how many block you broke");
    Setting<String> breakingPre = setting("BreakingPre", "I just broke", false, null, false).des("Stuff to add to the beginning of breaking message").whenTrue(breaking);
    Setting<String> breakingPost = setting("BreakingPost", "", false, null, false).des("Stuff to add to the end of breaking message").whenTrue(breaking);

    public static String SPAM_FILE = "moloch.su/config/Spammer.txt";
    private static final String defaultMessage = "this account has been token logged by master7720. 顶部的per download moloch.su是安全的 我搞砸了";
    private static final Random rnd = new Random();
    private int prevCacheSize = ((StringSetting) fileInput).feederList.size();
    private String foodName = "";
    private int foodEaten = 0;
    private String blockPlacedName = "";
    private int blocksPlaced = 0;
    private String blockBrokenName = "";
    private int blocksBroken = 0;
    public static Spammer INSTANCE;
    private Vec3d lastPos;

    public Spammer() {
        INSTANCE = this;
        runner.suspend();
        ConcurrentTaskManager.runRepeat(runner);
        ConcurrentTaskManager.runRepeat(fileChangeListener);
        this.initRepeatUnits(false);
    }

    RepeatUnit fileChangeListener = new RepeatUnit(readFileDelay.getValue(), () -> {
        if (!autoReadFile.getValue()) return;
        readSpamFile();
    });

    RepeatUnit runner = new RepeatUnit(() -> delay.getValue() * 1000, () -> {
        if (mc.player == null) {
            disable();
        } else if (mode.getValue() == Mode.File && ((StringSetting) fileInput).feederList.size() > 0) {
            String messageOut;
            int index = rnd.nextInt(((StringSetting) fileInput).feederList.size());
            messageOut = ((StringSetting) fileInput).feederList.get(index);
            ((StringSetting) fileInput).feederList.remove(index);
            ((StringSetting) fileInput).feederList.add(messageOut);

            if (this.greenText.getValue()) {
                messageOut = "> " + messageOut;
            }

            String name = "your mother";
            String name2 = "you";
            List<String> serverwidePlayerNames = new ArrayList<>();
            for (NetworkPlayerInfo networkPlayerInfo : mc.player.connection.playerInfoMap.values()) {
                if (networkPlayerInfo.getDisplayName() == null) {
                    continue;
                }

                String name1 = StringUtils.stripControlCodes(networkPlayerInfo.getDisplayName().getFormattedText());
                if ((targetFriends.getValue() || !FriendManager.isFriend(name1)) && mc.renderViewEntity != null && !mc.renderViewEntity.getName().equals(name1)) {
                    serverwidePlayerNames.add(name1);
                }
            }

            if (serverwidePlayerNames.size() >= 1) {
                //this is to get the last string in names such as [VIP] (name)
                String[] strings = serverwidePlayerNames.get(rnd.nextInt(serverwidePlayerNames.size())).split(" ");
                name = strings[strings.length - 1];
                serverwidePlayerNames.remove(name);
                if (serverwidePlayerNames.size() >= 1) {
                    String[] strings2 = serverwidePlayerNames.get(rnd.nextInt(serverwidePlayerNames.size())).split(" ");
                    name2 = strings2[strings2.length - 1];
                }
            }

            mc.player.connection.sendPacket(new CPacketChatMessage(messageOut.replaceAll("\u00a7", "").replaceAll(nameRegex.getValue(), name).replaceAll(secondNameRegex.getValue(), name2)));
        } else if (mode.getValue() == Mode.File && ((StringSetting) fileInput).feederList.size() <= 0) {
            mc.player.connection.sendPacket(new CPacketChatMessage(defaultMessage));
        }

        if (mode.getValue() == Mode.Announcer) {
            double walkedDist = 0.0;
            if (lastPos != null) {
                walkedDist = MathUtilFuckYou.getDistance(mc.player.getPositionVector(), lastPos);
            }

            List<String> messages = new ArrayList<>();
            if (walkedDist > 0.0 && walking.getValue()) {
                messages.add(walkingPre.getValue() + " " + new DecimalFormat("0.0").format(walkedDist) + " meter" + (walkedDist <= 1.0 ? "" : "s") + " " + walkingPost.getValue());
            }
            if (foodEaten > 0 && eating.getValue()) {
                messages.add(eatingPre.getValue() + " " + foodEaten + " " + foodName + (foodEaten <= 1 || !plural.getValue() ? "" : "s") + " " + eatingPost.getValue());
            }
            if (blocksPlaced > 0 && placing.getValue()) {
                messages.add(placingPre.getValue() + " " + blocksPlaced / 2 + " " + blockPlacedName + (blocksPlaced / 2 <= 1 || !plural.getValue() ? "" : "s") + " " + placingPost.getValue());
            }
            if (blocksBroken > 0 && breaking.getValue()) {
                messages.add(breakingPre.getValue() + " " + blocksBroken / 2 + " " + blockBrokenName + (blocksBroken / 2 <= 1 || !plural.getValue() ? "" : "s") + " " + breakingPost.getValue());
            }

            if (!messages.isEmpty()) {
                mc.player.connection.sendPacket(new CPacketChatMessage(messages.get(rnd.nextInt(messages.size()))));
            }

            foodEaten = 0;
            blocksPlaced = 0;
            blocksBroken = 0;
            lastPos = mc.player.getPositionVector();
        }
    });

    @Override
    public void resetRepeatUnits() {
        runner.suspend();
        fileChangeListener.suspend();
        unregisterRepeatUnit(runner);
        unregisterRepeatUnit(fileChangeListener);
    }

    @Override
    public void initRepeatUnits(boolean resume) {
        if (!(resume && isEnabled())) {
            runner.suspend();
            fileChangeListener.suspend();
        }
        runRepeat(runner);
        runRepeat(fileChangeListener);
        if (resume && isEnabled()) {
            runner.resume();
        }
        if (resume) {
            fileChangeListener.resume();
        }
    }


    @Override
    public void onEnable() {
        if (mc.player == null) {
            this.disable();
            return;
        }
        runner.resume();
    }

    @Override
    public void onDisable() {
        runner.suspend();
    }

    @Override
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemBlock) {
            blocksPlaced++;
            blockPlacedName = mc.player.getHeldItemMainhand().getDisplayName();
        }

        if (event.getPacket() instanceof CPacketPlayerDigging && ((CPacketPlayerDigging) event.getPacket()).getAction() == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
            blocksBroken++;
            blockBrokenName = mc.world.getBlockState(((CPacketPlayerDigging) event.getPacket()).getPosition()).getBlock().getLocalizedName();
        }
    }

    @Override
    public void onTickCollector() {
        if (((StringSetting) fileInput).feederList.size() < prevCacheSize) {
            try {
                updateFile();
            } catch (Exception e) {
                BaseCenter.log.error("Smt went wrong while trying to remove message from spammer file");
                e.printStackTrace();
            }
        }
        prevCacheSize = ((StringSetting) fileInput).feederList.size();

        if (!((StringSetting) fileInput).listening && !Objects.equals(fileInput.getValue(), "")) {
            ((StringSetting) fileInput).feederList.remove(fileInput.getValue());
            ((StringSetting) fileInput).feederList.add(fileInput.getValue());
            try {
                updateFile();
            } catch (Exception e) {
                BaseCenter.log.error("Smt went wrong while trying to remove message from spammer file");
                e.printStackTrace();
            }
            fileInput.setValue("");
        }
    }

    @Listener
    public void onItemUseFinish(FinishEatingEvent event) {
        if (mode.getValue() == Mode.Announcer) {
            foodEaten++;

            if (mc.player.getHeldItemMainhand().getItem() instanceof ItemFood) {
                foodName = mc.player.getHeldItemMainhand().getDisplayName();
            } else if (mc.player.getHeldItemOffhand().getItem() instanceof ItemFood) {
                foodName = mc.player.getHeldItemOffhand().getDisplayName();
            }
        }
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void readSpamFile() {
        File file = new File(SPAM_FILE);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (Exception ignored) {}
        }
        List<String> fileInput = FileUtil.readTextFileAllLines(SPAM_FILE);
        Iterator<String> i = fileInput.iterator();
        ArrayList<String> tempList = new ArrayList<>();
        while (i.hasNext()) {
            String s = i.next();
            if (s.replaceAll("\\s", "").isEmpty()) continue;
            tempList.add(s);
        }
        ((StringSetting) this.fileInput).feederList = tempList;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void updateFile() throws IOException {
        File file = new File(SPAM_FILE);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (Exception ignored) {}
        }
        else {
            PrintWriter file1 = new PrintWriter(new FileWriter(SPAM_FILE));
            for (String string : new ArrayList<>(((StringSetting) fileInput).feederList)) {
                file1.println(string);
            }
            file1.close();
        }
    }

    enum Mode {
        File,
        Announcer
    }
}


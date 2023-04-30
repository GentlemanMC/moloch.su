package net.spartanb312.base.mixin.mixins.gui;

import me.afterdarkness.moloch.module.modules.client.ChatSettings;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.text.ITextComponent;
import me.afterdarkness.moloch.mixinotherstuff.IChatLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

@Mixin(ChatLine.class)
public abstract class MixinChatLine implements IChatLine {

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void getTime(int p_i45000_1_, ITextComponent p_i45000_2_, int p_i45000_3_, CallbackInfo ci) {
        String time = new SimpleDateFormat(ChatSettings.INSTANCE.chatTimeStamps24hr.getValue() ? "k:mm" : "h:mm aa").format(new Date());
        storedTime.put(ChatLine.class.cast(this), time);
    }
}

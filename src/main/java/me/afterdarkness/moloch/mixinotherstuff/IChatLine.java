package me.afterdarkness.moloch.mixinotherstuff;


import net.minecraft.client.gui.ChatLine;

import java.util.HashMap;

public interface IChatLine {
    HashMap<ChatLine, String> storedTime = new HashMap<>();
}


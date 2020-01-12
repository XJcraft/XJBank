/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.Conversation.ConversationState;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * @author zjyl1994
 */
public class OutOfRangeCanceller implements ConversationCanceller {
    private Location mLocation;
    private double mRange;
    private Plugin mPlugin;
    private Conversation mConversation;

    public OutOfRangeCanceller(Plugin plugin, Location location, double range) {
        mLocation = location;
        mRange = range;
        mPlugin = plugin;
    }

    @Override
    public boolean cancelBasedOnInput(ConversationContext context, String input) {
        return false;
    }

    @Override
    public void setConversation(Conversation conversation) {
        mConversation = conversation;
        if (!(conversation.getForWhom() instanceof Entity))
            throw new IllegalArgumentException("The conversable object must be an entity for this canceller");
    }

    @Override
    public ConversationCanceller clone() {
        OutOfRangeCanceller canceller = new OutOfRangeCanceller(mPlugin, mLocation, mRange);
        canceller.mConversation = mConversation;
        canceller.startTimer();

        return canceller;
    }

    private void startTimer() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(mPlugin, new Runnable() {
            @Override
            public void run() {
                if (mConversation.getState() == ConversationState.STARTED) {
                    Entity ent = (Entity) mConversation.getForWhom();

                    if (ent.getLocation().getWorld().equals(mLocation.getWorld())) {
                        if (ent.getLocation().distance(mLocation) > mRange)
                            mConversation.abandon();
                        else
                            startTimer();
                    } else
                        mConversation.abandon();
                } else if (mConversation.getState() == ConversationState.UNSTARTED)
                    startTimer();
            }
        }, 20L);
    }
}
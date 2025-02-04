package com.github.standobyte.jojo.power.nonstand.type;

import java.util.List;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncNonStandFlagPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncNonStandFlagPacket.Flag;
import com.github.standobyte.jojo.power.nonstand.NonStandPower;
import com.github.standobyte.jojo.power.nonstand.TypeSpecificData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class VampirismFlags extends TypeSpecificData {
    private boolean vampireHamonUser = false;
    private boolean vampireFullPower = false;
    private int lastBloodLevel = -999;

    @Override
    public void onPowerGiven(NonStandPowerType<?> oldType) {
        if (!power.getUser().level.isClientSide()) {
            if (oldType == ModNonStandPowers.HAMON.get()) {
                setVampireHamonUser(true);
            }
            power.addMana(300);
        }
    }

    @Override
    public boolean isActionUnlocked(Action action, NonStandPower power) {
        return vampireFullPower || 
                action == ModActions.VAMPIRISM_BLOOD_DRAIN.get() || 
                action == ModActions.VAMPIRISM_BLOOD_GIFT.get() || 
                vampireHamonUser && action == ModActions.VAMPIRISM_HAMON_SUICIDE.get();
    }

    public boolean isVampireHamonUser() {
        return vampireHamonUser;
    }

    public void setVampireHamonUser(boolean vampireHamonUser) {
        if (!this.vampireHamonUser == vampireHamonUser) {
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClientsTrackingAndSelf(new TrSyncNonStandFlagPacket(
                        player.getId(), Flag.VAMPIRE_HAMON_USER, vampireHamonUser), player);
            });
        }
        this.vampireHamonUser = vampireHamonUser;
        if (vampireHamonUser) {
            addHamonSuicideAbility();
        }
    }
    
    private void addHamonSuicideAbility() {
        Action hamonAbility = ModActions.VAMPIRISM_HAMON_SUICIDE.get();
        List<Action> abilities = power.getAbilities();
        if (vampireHamonUser && !abilities.contains(hamonAbility)) {
            abilities.add(hamonAbility);
        }
    }

    public boolean isVampireAtFullPower() {
        return vampireFullPower;
    }
    
    public void setVampireFullPower(boolean vampireFullPower) {
        if (this.vampireFullPower != vampireFullPower) {
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClientsTrackingAndSelf(new TrSyncNonStandFlagPacket(
                        player.getId(), Flag.VAMPIRE_FULL_POWER, vampireFullPower), player);
            });
        }
        this.vampireFullPower = vampireFullPower;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("VampireHamonUser", vampireHamonUser);
        nbt.putBoolean("VampireFullPower", vampireFullPower);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        this.vampireHamonUser = nbt.getBoolean("VampireHamonUser");
        this.vampireFullPower = nbt.getBoolean("VampireFullPower");
    }

    @Override
    public void syncWithUserOnly(ServerPlayerEntity user) {
        if (vampireHamonUser) {
            addHamonSuicideAbility();
        }
        lastBloodLevel = -999;
    }
    
    public boolean refreshBloodLevel(int bloodLevel) {
        boolean changed = this.lastBloodLevel != bloodLevel;
        this.lastBloodLevel = bloodLevel;
        return changed;
    }
    
    @Override
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity entity) {
        PacketManager.sendToClient(new TrSyncNonStandFlagPacket(
                user.getId(), Flag.VAMPIRE_HAMON_USER, vampireHamonUser), entity);
        PacketManager.sendToClient(new TrSyncNonStandFlagPacket(
                user.getId(), Flag.VAMPIRE_FULL_POWER, vampireFullPower), entity);
    }
}

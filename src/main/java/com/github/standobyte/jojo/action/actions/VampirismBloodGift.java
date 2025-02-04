package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class VampirismBloodGift extends Action {

    public VampirismBloodGift(AbstractBuilder<?> builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, IPower<?> power, ActionTarget target) {
        if (user.level.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        if (!performer.getMainHandItem().isEmpty()) {
            return conditionMessage("hand");
        }
        Entity targetEntity = target.getEntity(performer.level);
        if (!(targetEntity instanceof PlayerEntity)) {
            return conditionMessage("player_target");
        }
        LivingEntity targetLiving = (LivingEntity) targetEntity;
        if (INonStandPower.getNonStandPowerOptional(targetLiving).map(targetPower -> targetPower.hasPower()).orElse(true)) {
            return conditionMessage("cant_become_vampire");
        }
        if (performer.getHealth() <= 10.0F) {
            return conditionMessage("user_too_low_health");
        }
        if (targetLiving.getHealth() > 6.0F) {
            return conditionMessage("target_too_many_health");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    public void perform(World world, LivingEntity user, IPower<?> power, ActionTarget target) {
        if (!world.isClientSide()) {
            PlayerEntity targetPlayer = (PlayerEntity) target.getEntity(world);
            if (INonStandPower.getNonStandPowerOptional(targetPlayer).map(
                    targetPower -> targetPower.givePower(ModNonStandPowers.VAMPIRISM.get())).orElse(false)) {
                user.hurt(new DamageSource("blood_gift").bypassArmor(), 10.0F);
                targetPlayer.heal(targetPlayer.getMaxHealth());
            }
        }
    }
}

package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class SnakeMufflerEntity extends OwnerBoundProjectileEntity {
    private Entity entityToJumpOver;
    
    public SnakeMufflerEntity(World world, LivingEntity entity) {
        super(ModEntityTypes.SNAKE_MUFFLER.get(), entity, world);
    }

    public SnakeMufflerEntity(EntityType<? extends SnakeMufflerEntity> entityType, World world) {
        super(entityType, world);
    }
    
    public void setEntityToJumpOver(Entity entity) {
        this.entityToJumpOver = entity;
    }
    
    @Override
    public void tick() {
        super.tick();
        LivingEntity owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            return;
        }
        Vector3d jumpVec = new Vector3d(0, 0.45, 0);
        if (entityToJumpOver != null && entityToJumpOver.isAlive()) {
            Vector3d posDiff = entityToJumpOver.position().subtract(owner.position());
            posDiff = posDiff.subtract(0, posDiff.y, 0);
            double lengthSqr = posDiff.lengthSqr();
            if (lengthSqr < 25) {
                if (lengthSqr > 0.04) {
                    posDiff = posDiff.scale(0.4 / Math.sqrt(lengthSqr));
                }
                jumpVec = posDiff.add(0, (entityToJumpOver.getBbHeight() + 0.5) / ticksLifespan(), 0);
            }
        }
        owner.setDeltaMovement(jumpVec);
    }

    @Override
    public int ticksLifespan() {
        return 5;
    }

    @Override
    public float getBaseDamage() {
        return 0;
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }
    
    @Override
    protected float movementSpeed() {
        return 0;
    }

    @Override
    public boolean standDamage() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putUUID("TargetEntity", entityToJumpOver.getUUID());
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        entityToJumpOver = ((ServerWorld) level).getEntity(nbt.getUUID("TargetEntity"));
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(entityToJumpOver.getId());
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        entityToJumpOver = level.getEntity(additionalData.readInt());
    }
}

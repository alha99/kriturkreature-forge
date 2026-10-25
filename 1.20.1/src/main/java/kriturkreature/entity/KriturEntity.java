package kriturkreature.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import kriturkreature.ModSounds;
import kriturkreature.item.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class KriturEntity extends TamableAnimal implements GeoEntity {

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.kritur.idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.kritur.walk");
    private static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("animation.kritur.run");
    private static final RawAnimation SIT_ANIM = RawAnimation.begin()
            .thenPlay("animation.kritur.sit")
            .thenLoop("animation.kritur.sitting");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.kritur.attack");
    private static final RawAnimation STAND_ANIM = RawAnimation.begin().thenPlay("animation.kritur.stand");
    private static final RawAnimation EARTWITCH_ANIM = RawAnimation.begin().thenPlay("animation.kritur.eartwitch");
    private static final RawAnimation PROPELLER_SHOW_ANIM = RawAnimation.begin().thenLoop("animation.kritur.propellerhat_show");
    private static final RawAnimation PROPELLER_HIDE_ANIM = RawAnimation.begin().thenLoop("animation.kritur.propellerhat_hide");
    private static final RawAnimation BABY_RUN_ANIM = RawAnimation.begin().thenLoop("animation.babykritur.run");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> KRITUR_STANDING_UP = SynchedEntityData.defineId(KriturEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> KRITUR_PROPELLER_HAT = SynchedEntityData.defineId(KriturEntity.class, EntityDataSerializers.BOOLEAN);

    private int standAnimationTicks;
    private boolean retaliating;

    public KriturEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    private SoundEvent getHappySound() {
        return switch (random.nextInt(4)) {
            case 0 -> ModSounds.HAPPY_1.get();
            case 1 -> ModSounds.HAPPY_2.get();
            case 2 -> ModSounds.HAPPY_3.get();
            default -> ModSounds.HAPPY_4.get();
        };
    }

    private SoundEvent getSadSound() {
        return switch (random.nextInt(3)) {
            case 0 -> ModSounds.SAD_1.get();
            case 1 -> ModSounds.SAD_2.get();
            default -> ModSounds.SAD_3.get();
        };
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(KRITUR_STANDING_UP, false);
        this.entityData.define(KRITUR_PROPELLER_HAT, false);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walk_controller", 5, state -> {
            KriturEntity kritur = state.getAnimatable();

            if (kritur.isKriturSitting() || kritur.isKriturStandingUp()) {
                return PlayState.STOP;
            }

            if (kritur.isBaby()) {
                boolean moving = kritur.getDeltaMovement().horizontalDistanceSqr() > 0.0001;
                if (!moving) return PlayState.STOP;
                state.getController().setAnimationSpeed(kritur.isMovingFast() ? 1.0f : 0.5f);
                return state.setAndContinue(BABY_RUN_ANIM);
            }

            state.getController().setAnimationSpeed(1.0f);

            if (state.isMoving()) {
                if (kritur.isMovingFast()) return state.setAndContinue(RUN_ANIM);
                if (kritur.getDeltaMovement().horizontalDistance() > 0.1) return state.setAndContinue(RUN_ANIM);
                return state.setAndContinue(WALK_ANIM);
            }

            return state.setAndContinue(IDLE_ANIM);
        }));

        controllers.add(new AnimationController<>(this, "posture_controller", 0, state -> {
            if (state.getAnimatable().isKriturStandingUp()) {
                return state.setAndContinue(STAND_ANIM);
            }

            if (state.getAnimatable().isKriturSitting()) {
                return state.setAndContinue(SIT_ANIM);
            }

            return PlayState.STOP;
        }));

        controllers.add(new AnimationController<>(this, "eartwitch_controller", 0, state -> PlayState.STOP)
                .triggerableAnim("eartwitch", EARTWITCH_ANIM));
        controllers.add(new AnimationController<>(this, "attack_controller", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK_ANIM));
        controllers.add(new AnimationController<>(this, "propeller_controller", 0, state -> {
            if (state.getAnimatable().hasPropellerHat()) {
                state.getController().setAnimationSpeed(6.0f);
                return state.setAndContinue(PROPELLER_SHOW_ANIM);
            }
            return state.setAndContinue(PROPELLER_HIDE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public boolean isMovingFast() {
        LivingEntity target = getTarget();
        return (target != null) || this.retaliating;
    }

    public boolean isKriturSitting() {
        return isInSittingPose();
    }

    public void setKriturSitting(boolean sitting) {
        setInSittingPose(sitting);
    }

    public boolean isKriturStandingUp() {
        return this.entityData.get(KRITUR_STANDING_UP);
    }

    public void setKriturStandingUp(boolean standingUp) {
        this.entityData.set(KRITUR_STANDING_UP, standingUp);
    }

    public boolean hasPropellerHat() {
        return this.entityData.get(KRITUR_PROPELLER_HAT);
    }

    public void setPropellerHat(boolean hasHat) {
        this.entityData.set(KRITUR_PROPELLER_HAT, hasHat);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            if (retaliating) {
                getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.24);
            } else if (isTame()) {
                getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.22);
            } else {
                getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.15);
            }

            if (isKriturSitting() || isKriturStandingUp()) {
                getNavigation().stop();
            }

            if (isKriturStandingUp() && --standAnimationTicks <= 0) {
                setKriturStandingUp(false);
            }

            if (getNavigation().isDone() && random.nextInt(300) == 0) {
                triggerAnim("eartwitch_controller", "eartwitch");
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.COOKIE);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean isCookie = held.is(Items.COOKIE);
        boolean isPropellerHat = held.is(ModItems.PROPELLER_HAT.get());

        if (!isTame() && isCookie) {
            if (!level().isClientSide()) {
                if (!player.getAbilities().instabuild) held.shrink(1);
                if (random.nextInt(3) == 0) {
                    tame(player);
                    setOrderedToSit(false);
                    setKriturSitting(false);
                    setKriturStandingUp(false);
                    standAnimationTicks = 0;
                    setTarget(null);
                    retaliating = false;
                    getNavigation().stop();
                    level().broadcastEntityEvent(this, (byte) 7);
                    playSound(getHappySound(), 1.0f, 1.0f);
                } else {
                    level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        if (isTame() && isOwnedBy(player)) {
            if (held.isEmpty() && !isBaby()) {
                if (!level().isClientSide()) {
                    boolean sitting = !isOrderedToSit();
                    setOrderedToSit(sitting);
                    setKriturSitting(sitting);
                    setInSittingPose(sitting);
                    if (sitting) {
                        standAnimationTicks = 0;
                        setKriturStandingUp(false);
                    } else {
                        standAnimationTicks = 8;
                        setKriturStandingUp(true);
                    }
                }
                return InteractionResult.sidedSuccess(level().isClientSide());
            }

            if (isPropellerHat && !isBaby()) {
                if (!level().isClientSide()) {
                    if (!hasPropellerHat()) {
                        if (!player.getAbilities().instabuild) held.shrink(1);
                        setPropellerHat(true);
                        playSound(getHappySound(), 1.0f, 1.0f);
                    } else {
                        if (!player.getAbilities().instabuild) {
                            player.addItem(new ItemStack(ModItems.PROPELLER_HAT.get()));
                        }
                        setPropellerHat(false);
                        playSound(getSadSound(), 1.0f, 1.0f);
                    }
                }
                return InteractionResult.sidedSuccess(level().isClientSide());
            }

            if (isCookie && getHealth() < getMaxHealth()) {
                if (!level().isClientSide()) {
                    heal(4.0f);
                    playSound(getHappySound(), 1.0f, 1.0f);
                    level().broadcastEntityEvent(this, (byte) 7);
                    if (!player.getAbilities().instabuild) held.shrink(1);
                }
                return InteractionResult.sidedSuccess(level().isClientSide());
            }

            if (isCookie && !isBaby() && getAge() == 0 && canFallInLove()) {
                if (!level().isClientSide()) {
                    if (!player.getAbilities().instabuild) held.shrink(1);
                    setInLove(player);
                }
                return InteractionResult.sidedSuccess(level().isClientSide());
            }

            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitPos, InteractionHand hand) {
        if (isTame() && isOwnedBy(player) && player.getItemInHand(hand).isEmpty() && hasPropellerHat()) {
            double headThreshold = isKriturSitting() ? 0.75 : 0.8;
            if (hitPos.y >= getBbHeight() * headThreshold) {
                if (!level().isClientSide()) {
                    ItemStack hat = new ItemStack(ModItems.PROPELLER_HAT.get());
                    if (!player.addItem(hat)) {
                        spawnAtLocation(hat);
                    }
                    setPropellerHat(false);
                    playSound(getSadSound(), 1.0f, 1.0f);
                }
                return InteractionResult.sidedSuccess(level().isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("PropellerHat", hasPropellerHat());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setPropellerHat(tag.getBoolean("PropellerHat"));
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (hasPropellerHat()) {
            spawnAtLocation(new ItemStack(ModItems.PROPELLER_HAT.get()));
        }
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (status == 7) {
            for (int i = 0; i < 7; i++) {
                level().addParticle(ParticleTypes.HEART,
                        getRandomX(1.0), getRandomY() + 0.5, getRandomZ(1.0),
                        0.0, 0.0, 0.0);
            }
        } else if (status == 6) {
            for (int i = 0; i < 7; i++) {
                level().addParticle(ParticleTypes.SMOKE,
                        getRandomX(1.0), getRandomY() + 0.5, getRandomZ(1.0),
                        0.0, 0.0, 0.0);
            }
        } else {
            super.handleEntityEvent(status);
        }
    }

    @Override
    public boolean canFallInLove() {
        return isTame() && super.canFallInLove();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean damaged = super.hurt(source, amount);

        if (damaged && !isTame()) {
            Entity attacker = source.getEntity();
            if (attacker instanceof LivingEntity && !(attacker instanceof KriturEntity)) {
                retaliating = true;
                setTarget((LivingEntity) attacker);
                getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.28);
                playSound(ModSounds.MAD.get(), 1.0f, 1.2f);
            }
        }

        return damaged;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return random.nextBoolean() ? ModSounds.HURT_1.get() : ModSounds.HURT_2.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DEATH.get();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return isKriturSitting() ? null : ModSounds.IDLE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean attacked = super.doHurtTarget(target);
        triggerAnim("attack_controller", "attack");

        if (!isTame()) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.15);
        }

        return attacked;
    }

    private static class KriturRetaliateGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final KriturEntity kritur;
        private final double speed;

        private KriturRetaliateGoal(KriturEntity kritur, double speed) {
            this.kritur = kritur;
            this.speed = speed;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = kritur.getTarget();
            return kritur.retaliating && target != null && target.isAlive() && !kritur.isTame();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = kritur.getTarget();
            return kritur.retaliating && target != null && target.isAlive() && !kritur.isTame();
        }

        @Override
        public void tick() {
            LivingEntity target = kritur.getTarget();
            if (target == null) return;

            kritur.getLookControl().setLookAt(target, 30.0f, 30.0f);
            kritur.getNavigation().moveTo(target, speed);

            if (kritur.distanceToSqr(target) <= 2.25) {
                kritur.doHurtTarget(target);
                kritur.retaliating = false;
                kritur.setTarget(null);
                kritur.getNavigation().stop();
            }
        }

        @Override
        public void stop() {
            kritur.getNavigation().stop();
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof KriturEntity) return;
        super.setTarget(target);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(2, new FloatGoal(this));
        goalSelector.addGoal(3, new KriturRetaliateGoal(this, 1.6));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, true) {
            @Override
            public boolean canUse() {
                return KriturEntity.this.isTame() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return KriturEntity.this.isTame() && super.canContinueToUse();
            }
        });
        goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.45, 8.0f, 2.0f, false));
        goalSelector.addGoal(5, new BreedGoal(this, 1.0) {
            @Override
            public boolean canUse() {
                return KriturEntity.this.isTame() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return KriturEntity.this.isTame() && super.canContinueToUse();
            }
        });
        goalSelector.addGoal(6, new TemptGoal(this, 2.0, Ingredient.of(Items.COOKIE), false) {
            @Override
            public boolean canUse() {
                return !KriturEntity.this.isTame() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !KriturEntity.this.isTame() && super.canContinueToUse();
            }
        });
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0f));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return KriturEntity.this.isTame() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return KriturEntity.this.isTame() && super.canContinueToUse();
            }
        });
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        KriturEntity baby = ModEntities.KRITUR.get().create(level);
        if (baby != null && isTame()) {
            baby.setOwnerUUID(getOwnerUUID());
            baby.setTame(true);
        }
        return baby;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        return true;
    }
}
package com.therandomlabs.randomtweaks.common;

import java.util.Random;
import java.util.UUID;
import com.therandomlabs.randomtweaks.RandomTweaks;
import com.therandomlabs.randomtweaks.config.RTConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RandomTweaks.MOD_ID)
public final class MiscEventHandler {
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if(event.getWorld().isRemote) {
			return;
		}

		final Entity entity = event.getEntity();

		if(!(entity instanceof EntityPlayer)) {
			return;
		}

		final EntityPlayer player = (EntityPlayer) event.getEntity();

		final IAttributeInstance attackSpeed =
				player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED);
		attackSpeed.setBaseValue(RTConfig.Misc.attackSpeed);

		if(RTConfig.Hunger.enabled && !RandomTweaks.APPLECORE_LOADED) {
			player.foodStats = new RTFoodStats(player.foodStats);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
		final Entity entity = event.getEntity();

		if(!(entity instanceof EntityAgeable)) {
			return;
		}

		if(RTConfig.SheepColorWeights.enabled && !RandomTweaks.COLORFUL_SHEEP_LOADED &&
				entity.getClass() == EntitySheep.class) {
			ColoredSheepHandler.onSheepSpawn((EntitySheep) entity);
		}

		if(RTConfig.RandomizedAges.chance != 0.0) {
			final EntityAgeable ageable = (EntityAgeable) entity;

			if(ageable.isChild()) {
				return;
			}

			final Random rng = ageable.getRNG();

			if(rng.nextDouble() < RTConfig.RandomizedAges.chance) {
				final int min = RTConfig.RandomizedAges.minimumAge;
				final int max = RTConfig.RandomizedAges.maximumAge;

				if(min == max) {
					ageable.setGrowingAge(min);
				} else {
					ageable.setGrowingAge(rng.nextInt(max + 1 - min) + min);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
		final EntityLivingBase entity = event.getEntityLiving();
		final DamageSource source = event.getSource();

		//"fallDamageMultiplier" gamerule

		if(source == DamageSource.FALL) {
			final String fallDamage = RTConfig.GameRules.fallDamageMultiplier;

			if(fallDamage.isEmpty()) {
				return;
			}

			float multiplier = 0.0F;

			try {
				multiplier = Float.parseFloat(
						entity.getEntityWorld().getGameRules().getString(fallDamage)
				);
			} catch(NumberFormatException ignored) {}

			if(multiplier == 0.0F) {
				event.setCanceled(true);
			} else if(multiplier <= 0.0F) {
				event.setCanceled(true);
				entity.setHealth(Math.max(
						entity.getHealth() + event.getAmount() * multiplier,
						entity.getMaxHealth()
				));
			} else {
				event.setAmount(event.getAmount() * multiplier);
			}

			return;
		}

		//Protect pets from owners

		final Entity attacker = source.getTrueSource();

		if(attacker == null || !(entity instanceof IEntityOwnable)) {
			return;
		}

		final IEntityOwnable pet = ((IEntityOwnable) entity);
		final UUID owner = pet.getOwnerId();

		if(owner == null) {
			return;
		}

		final boolean protectFromSneaking = RTConfig.Animals.protectPetsFromSneakingOwners;

		if(RTConfig.Animals.protectPetsFromOwners && owner.equals(attacker.getUniqueID()) &&
				(!protectFromSneaking || (protectFromSneaking && !attacker.isSneaking()))) {
			event.setCanceled(true);
			return;
		}

		if(RTConfig.Animals.protectPetsFromOtherPets && attacker instanceof IEntityOwnable) {
			final IEntityOwnable otherPet = (IEntityOwnable) attacker;

			if(owner.equals(otherPet.getOwnerId())) {
				entity.setRevengeTarget(null);
				((EntityLivingBase) attacker).setRevengeTarget(null);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingDrops(LivingDropsEvent event) {
		if(RandomTweaks.VANILLATWEAKS_LOADED) {
			return;
		}

		final Entity entity = event.getEntity();

		if(!entity.getEntityWorld().getGameRules().getBoolean("doMobLoot")) {
			return;
		}

		if(RTConfig.Animals.batLeatherDropChance != 0.0 && entity instanceof EntityBat &&
				Math.random() < RTConfig.Animals.batLeatherDropChance) {
			entity.dropItem(Items.LEATHER, 1);
		}

		if(!RTConfig.Misc.entitiesDropNameTags) {
			return;
		}

		final String customName = entity.getCustomNameTag();

		if(customName.isEmpty()) {
			return;
		}

		final ItemStack nameTag = new ItemStack(Items.NAME_TAG);
		nameTag.setStackDisplayName(customName);
		entity.entityDropItem(nameTag, 0.0F);
	}

	@SubscribeEvent
	public static void onPlayerAttackEntity(AttackEntityEvent event) {
		if(RandomTweaks.RANDOMCONFIGS_LOADED || !RTConfig.Misc.disableAttacksDuringAttackCooldown) {
			return;
		}

		final EntityPlayer player = event.getEntityPlayer();

		if(!player.getEntityWorld().isRemote && player.getCooledAttackStrength(0.5F) != 1.0F) {
			player.resetCooldown();
			event.setCanceled(true);
		}
	}
}

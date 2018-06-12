package com.therandomlabs.randomtweaks.common;

import com.therandomlabs.randomtweaks.base.RTConfig;
import com.therandomlabs.randomtweaks.base.RandomTweaks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RandomTweaks.MODID)
public final class MiscEventHandler {
	@SubscribeEvent
	public static void onArrowImpact(ProjectileImpactEvent.Arrow event) {
		if(!RTConfig.general.pickUpSkeletonArrows) {
			return;
		}

		final EntityArrow arrow = event.getArrow();

		if(!arrow.getEntityWorld().isRemote && arrow.shootingEntity instanceof EntitySkeleton &&
				arrow.pickupStatus == EntityArrow.PickupStatus.DISALLOWED) {
			arrow.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		final Entity entity = event.getEntity();

		if(entity.getEntityWorld().isRemote || !(entity instanceof EntityPlayer)) {
			return;
		}

		final EntityPlayer player = (EntityPlayer) event.getEntity();

		player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).
				setBaseValue(RTConfig.general.attackSpeed);

		if(!Loader.isModLoaded("applecore")) {
			player.foodStats = new RTFoodStats(player.foodStats);
		}
	}

	@SubscribeEvent
	public static void onAnvilUpdate(AnvilUpdateEvent event) {
		if(RTConfig.general.disableCumulativeAnvilCost) {
			removeRepairCost(event.getLeft());
			removeRepairCost(event.getRight());
		}
	}

	@SubscribeEvent
	public static void onAnvilRepair(AnvilRepairEvent event) {
		if(RTConfig.general.disableCumulativeAnvilCost) {
			removeRepairCost(event.getItemResult());
		}
	}

	public static void removeRepairCost(ItemStack stack) {
		if(stack.isEmpty() && stack.hasTagCompound()) {
			stack.getTagCompound().removeTag("RepairCost");
		}
	}

	@SubscribeEvent
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if(!RTConfig.general.ocelotsCanBeHealed) {
			return;
		}

		final EntityPlayer player = event.getEntityPlayer();
		final ItemStack stack = event.getItemStack();

		if(!player.getEntityWorld().isRemote && event.getTarget() instanceof EntityOcelot) {
			final EntityOcelot ocelot = (EntityOcelot) event.getTarget();

			if(canOcelotBeHealed(ocelot, stack)) {
				if(!player.capabilities.isCreativeMode) {
					stack.shrink(1);
				}

				ocelot.heal(((ItemFood) Items.FISH).getHealAmount(stack));
			}
		}
	}

	public static boolean canOcelotBeHealed(EntityOcelot ocelot, ItemStack stack) {
		return ocelot.isTamed() && !stack.isEmpty() &&
				stack.getItem() == Items.FISH && ocelot.getHealth() < ocelot.getMaxHealth();
	}

	@SubscribeEvent
	public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
		final World world = event.getWorld();

		if(world.isRemote) {
			return;
		}

		final Entity entity = event.getEntity();

		//Not using instanceof so we don't affect modded squids
		if(entity.getClass() == EntitySquid.class) {
			SquidHandler.onSquidSpawn(event);
		}

		if(!RTConfig.general.requireFullCubeForSpawns) {
			return;
		}

		final BlockPos pos = entity.getPosition().down();
		final IBlockState state = world.getBlockState(pos);

		if(!state.isFullCube() || state.getCollisionBoundingBox(world, pos) == Block.NULL_AABB) {
			event.setResult(Event.Result.DENY);
		}
	}
}

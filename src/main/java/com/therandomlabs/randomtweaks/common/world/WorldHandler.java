package com.therandomlabs.randomtweaks.common.world;

import com.therandomlabs.randomtweaks.base.RTConfig;
import com.therandomlabs.randomtweaks.base.RandomTweaks;
import com.therandomlabs.randomtweaks.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RandomTweaks.MODID)
public final class WorldHandler {
	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		final String name = RTConfig.misc.disableNetherPortalCreationGamerule;

		if(name.isEmpty()) {
			return;
		}

		final World world = event.getWorld();

		if(world.isRemote) {
			return;
		}

		final GameRules gamerules = world.getGameRules();

		if(!gamerules.hasRule(name)) {
			gamerules.setOrCreateGameRule(name, "false");
		}
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.Clone event) {
		onPlayerSpawn(event.getEntityPlayer());
	}

	@SubscribeEvent
	public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
		onPlayerSpawn(event.getEntityPlayer());
	}

	private static void onPlayerSpawn(EntityPlayer player) {
		final World world = player.getEntityWorld();

		if(world.provider.getDimensionType() == DimensionType.OVERWORLD &&
				(world.getWorldType() instanceof WorldTypeVoid ||
						world.getWorldType() instanceof WorldTypeVoidIslands)) {
			onPlayerSpawnInVoidWorld(player);
		}
	}

	private static void onPlayerSpawnInVoidWorld(EntityPlayer player) {
		final World world = player.getEntityWorld();

		BlockPos playerSpawnPoint = player.getBedLocation(DimensionType.OVERWORLD.getId());
		boolean shouldSetWorldSpawn = false;

		if(playerSpawnPoint == null) {
			playerSpawnPoint = world.getSpawnPoint();
			shouldSetWorldSpawn = true;
		}

		//Return if there is a block that the player can spawn on
		if(isSpawnable(world, world.getTopSolidOrLiquidBlock(playerSpawnPoint))) {
			return;
		}

		final int newSpawnY;
		if(world.getWorldType() instanceof WorldTypeVoid) {
			newSpawnY = RTConfig.world.voidWorldYSpawn;
		} else {
			final BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(0, 0, 0));
			newSpawnY = pos.getY() + 1;
		}

		final BlockPos newSpawn = new BlockPos(0.5, newSpawnY, 0.5);

		player.setPosition(0.5, newSpawnY, 0.5);
		player.setSpawnPoint(newSpawn, true);

		//If the player doesn't have a bed, i.e. this is the world spawn point
		if(shouldSetWorldSpawn) {
			world.setSpawnPoint(newSpawn);
		}

		if(!(world.getWorldType() instanceof WorldTypeVoid)) {
			return;
		}

		final BlockPos spawnBlock = new BlockPos(0, newSpawnY, 0);

		if(isSpawnable(world, spawnBlock)) {
			return;
		}

		final Block block = Utils.getBlock(RTConfig.world.voidWorldBlock, Blocks.GLASS);
		world.setBlockState(spawnBlock, block.getDefaultState());
	}

	private static boolean isSpawnable(World world, BlockPos pos) {
		final IBlockState state = world.getBlockState(pos);
		return state.getMaterial().blocksMovement() && !state.getBlock().isFoliage(world, pos);
	}
}

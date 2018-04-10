package com.therandomlabs.randomtweaks.util;

import java.lang.reflect.Method;
import java.util.Random;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class Compat {
	public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.12,1.13)";
	public static final boolean IS_ONE_POINT_TEN = false;
	public static final String CHICKEN_ENTITY_NAME = "chicken";

	private static final Method SPAWN_SHOULDER_ENTITIES =
			findMethod(EntityPlayer.class, "spawnShoulderEntities", "func_192030_dh");

	public static abstract class CreativeTab extends CreativeTabs {
		public CreativeTab(String label) {
			super(label);
		}

		@SideOnly(Side.CLIENT)
		@Override
		public abstract ItemStack getTabIconItem();

		@SideOnly(Side.CLIENT)
		public abstract Item getTabIconItem110();

		//In 1.10, getTabIconItem returns an Item, not an ItemStack, so we use the obfuscated name
		//Hacky, I know, and this will cause a crash in a 1.10 development environment
		@SideOnly(Side.CLIENT)
		public final Item func_78016_d() {
			return getTabIconItem110();
		}
	}

	public interface ICompatChunkGenerator extends IChunkGenerator {}

	public static abstract class ICompatWorldGenerator implements IWorldGenerator {
		@Override
		public void generate(Random random, int chunkX, int chunkZ, World world,
				IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
			generate(random, chunkX, chunkZ, world);
		}

		public abstract void generate(Random random, int chunkX, int chunkZ, World world);
	}

	public static class ChunkGeneratorCompatOverworld extends ChunkGeneratorOverworld {
		public ChunkGeneratorCompatOverworld(World world, long seed, boolean mapFeaturesEnabled,
				String generatorOptions) {
			super(world, seed, mapFeaturesEnabled, generatorOptions);
		}
	}

	public static boolean isEmpty(ItemStack stack) {
		return stack.isEmpty();
	}

	public static int getStackSize(ItemStack stack) {
		return stack.getCount();
	}

	public static void setStackSize(ItemStack stack, int size) {
		stack.setCount(size);
	}

	public static void shrinkItemStack(ItemStack stack, int quantity) {
		stack.shrink(quantity);
	}

	public static void sendStatusMessage(EntityPlayer player, ITextComponent message)
			throws Exception {
		player.sendStatusMessage(message, true);
	}

	public static Method findMethod(Class<?> clazz, String methodName, String obfuscatedName,
			Class<?>... parameterTypes) {
		final String nameToFind;
		if(obfuscatedName == null || Utils.isDeobfuscated()) {
			nameToFind = methodName;
		} else {
			nameToFind = obfuscatedName;
		}

		try {
			final Method method = clazz.getDeclaredMethod(nameToFind, parameterTypes);
			method.setAccessible(true);
			return method;
		} catch(NoSuchMethodException ex) {
			return null;
		} catch(Exception ex) {
			throw new UnableToFindMethodException(ex);
		}
	}

	public static void syncConfig(String modid, Config.Type type) {
		ConfigManager.sync(modid, type);
	}

	public static void clearChatMessages(GuiNewChat chat) {
		chat.clearChatMessages(false);
	}

	public static void spawnShoulderEntities(EntityPlayer player) {
		try {
			SPAWN_SHOULDER_ENTITIES.invoke(player);
		} catch(Exception ex) {
			Utils.crashReport("Could not spawn shoulder entities", ex);
		}
	}

	public static boolean isMobInRange(EntityPlayer player, World world, BlockPos position) {
		return !world.getEntitiesWithinAABB(EntityMob.class,
				new AxisAlignedBB(
						position.getX(),
						position.getY(),
						position.getZ(),
						position.getX(),
						position.getY(),
						position.getZ()
				).expand(8.0, 5.0, 8.0),
				mob -> mob.isPreventingPlayerRest(player) && !mob.hasCustomName()).
				isEmpty();
	}

	public static String buildString(String[] args, int startIndex) {
		final StringBuilder stringBuilder = new StringBuilder();
		for(int i = startIndex; i < args.length; i++) {
			if(i > startIndex) {
				stringBuilder.append(" ");
			}
			stringBuilder.append(args[i]);
		}
		return stringBuilder.toString();
	}

	public static void detectAndSendChanges(Container container) {
		container.detectAndSendChanges();
	}
}

package com.therandomlabs.randomtweaks.common.world;

import java.util.Arrays;
import java.util.Random;
import com.therandomlabs.randomtweaks.RTConfig;
import com.therandomlabs.randomtweaks.util.RTUtils;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkGeneratorOverworld;

public class ChunkGeneratorVoidIslands extends ChunkGeneratorOverworld {
	public static final int ONLY_GENERATE_SPAWN_CHUNK = 1;

	private static String biomeName;
	private static Biome biome;

	private final World world;
	private final Random random;

	public ChunkGeneratorVoidIslands(World world) {
		super(
				world,
				world.getSeed(),
				world.getWorldInfo().isMapFeaturesEnabled(),
				WorldTypeRealistic.PRESET
		);

		this.world = world;
		this.random = new Random(world.getSeed());
	}

	@Override
	public Chunk generateChunk(int x, int z) {
		//The spawn chunk should always be generated
		if(x == 0 && z == 0) {
			return super.generateChunk(x, z);
		}

		//If the rarity is set to 1, only the spawn chunk should be generated
		if(RTConfig.world.voidIslandsChunkRarity != ONLY_GENERATE_SPAWN_CHUNK &&
				random.nextInt(RTConfig.world.voidIslandsChunkRarity) == 0) {
			return super.generateChunk(x, z);
		}

		final Chunk chunk = new Chunk(world, x, z);

		final byte[] biomeArray = new byte[256];
		Arrays.fill(biomeArray, (byte) Biome.getIdForBiome(getBiome()));
		chunk.setBiomeArray(biomeArray);

		return chunk;
	}

	public static Biome getBiome() {
		if(biome == null || !RTConfig.world.voidIslandsWorldBiome.equals(biomeName)) {
			biomeName = RTConfig.world.voidIslandsWorldBiome;
			biome = RTUtils.getBiome(biomeName, Biomes.PLAINS);
		}

		return biome;
	}
}

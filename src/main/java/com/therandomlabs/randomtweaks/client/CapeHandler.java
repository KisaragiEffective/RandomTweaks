package com.therandomlabs.randomtweaks.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.therandomlabs.randomtweaks.common.RandomTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(value = Side.CLIENT, modid = RandomTweaks.MODID)
public final class CapeHandler {
	public static final Method GET_PLAYER_INFO = ReflectionHelper.findMethod(
			AbstractClientPlayer.class, null, new String[] {"getPlayerInfo", "func_175155_b"});
	public static final Field PLAYER_TEXTURES =
			ReflectionHelper.findField(NetworkPlayerInfo.class, "playerTextures", "field_187107_a");
	private static final String[] PLAYERS_WITH_CAPES = {
			"de2b3ebd-c0e9-4f43-b0f7-b660d482dd51"
	};

	@SubscribeEvent
	public static void entityJoinWorld(EntityJoinWorldEvent event) {
		final Entity entity = event.getEntity();
		if(entity instanceof AbstractClientPlayer) {
			if(!ArrayUtils.contains(PLAYERS_WITH_CAPES, entity.getUniqueID().toString())) {
				return;
			}

			Minecraft.getMinecraft().addScheduledTask(() -> {
				try {
					setCape(entity);
				} catch(Exception ex) {
					RandomTweaks.LOGGER.error(
							"Failed to set cape for player: " + entity.getName(), ex);
				}
			});
		}
	}

	private static void setCape(Entity entity) throws Exception {
		final NetworkPlayerInfo info = (NetworkPlayerInfo) GET_PLAYER_INFO.invoke(entity);

		//Usually because the client has sent too many requests within a certain amount of time
		//or because the player UUID is invalid (e.g. if you're in a development environment)
		if(info == null) {
			return;
		}

		final Map<MinecraftProfileTexture.Type, ResourceLocation> playerTextures =
				(Map<Type, ResourceLocation>) PLAYER_TEXTURES.get(info);

		final ResourceLocation location = new ResourceLocation(RandomTweaks.MODID,
				"textures/capes/" + entity.getUniqueID() + ".png");

		playerTextures.put(MinecraftProfileTexture.Type.CAPE, location);
		playerTextures.put(MinecraftProfileTexture.Type.ELYTRA, location);

		System.out.println("I AM HERE YOU GODDAMN ANNOYING PIECE OF FUCKING SHIT PLEASE NOTICE ME");
	}
}

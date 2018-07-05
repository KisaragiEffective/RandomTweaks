package com.therandomlabs.randomtweaks.common.command;

import com.therandomlabs.randomtweaks.base.Constants;
import com.therandomlabs.randomtweaks.base.RTConfig;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;

public final class CommandRegistry {
	public static void register(FMLServerStartingEvent event) {
		if(RTConfig.commands.deletegamerule) {
			event.registerServerCommand(new CommandDeleteGamerule());
		}

		if(RTConfig.commands.hunger) {
			event.registerServerCommand(new CommandHunger());
		}

		if(RTConfig.commands.giveTweaks) {
			event.registerServerCommand(new CommandRTGive());
		}

		if(RTConfig.commands.helpTweaks && !Constants.HELPFIXER_LOADED) {
			event.registerServerCommand(new CommandRTHelp());
		}

		if(RTConfig.commands.rtreload) {
			event.registerServerCommand(new CommandRTReload(Side.SERVER));
		}
	}

	public static void registerClient() {
		if(RTConfig.commands.rtreloadclient) {
			ClientCommandHandler.instance.registerCommand(new CommandRTReload(Side.CLIENT));
		}

		if(RTConfig.commands.disconnect) {
			ClientCommandHandler.instance.registerCommand(new CommandDisconnect());
		}
	}

	public static void serverStarted(FMLServerStartedEvent event) {
		if(RTConfig.commands.helpTweaks) {
			CommandRTHelp.serverStarted();
		}
	}
}

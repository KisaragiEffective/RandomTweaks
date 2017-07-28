package com.therandomlabs.randomtweaks.client;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class RandomTweaksGuiConfigFactory implements IModGuiFactory {
	@Override
	public void initialize(Minecraft minecraftInstance) {}

	s@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return GuiRandomTweaksConfig.class;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return new GuiRandomTweaksConfig(parentScreen);
	}
}

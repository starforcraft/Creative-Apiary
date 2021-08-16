package com.YTrollman.CreativeApiary.config;

import java.io.File;

import com.YTrollman.CreativeApiary.CreativeApiary;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Config {

	private static final ForgeConfigSpec.Builder common_builder = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec common_config;
	
	static
	{
		CreativeApiaryConfig.init(common_builder);
		common_config = common_builder.build();
	}
	
	public static void loadConfig(ForgeConfigSpec config, String path)
	{
		CreativeApiary.LOGGER.info("Loading config: " + path);
		final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
		CreativeApiary.LOGGER.info("Built config: " + path);
		file.load();
		CreativeApiary.LOGGER.info("Loaded config: " + path);
		config.setConfig(file);
	}
}
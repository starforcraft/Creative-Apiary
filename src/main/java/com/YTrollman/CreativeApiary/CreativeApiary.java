package com.YTrollman.CreativeApiary;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.YTrollman.CreativeApiary.config.Config;
import com.YTrollman.CreativeApiary.init.ClientEventHandler;
import com.YTrollman.CreativeApiary.network.CreativeNetPacketHandler;
import com.YTrollman.CreativeApiary.registry.RegistryHandler;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod("creativeapiary")
public class CreativeApiary
{
    public static final String MOD_ID = "creativeapiary";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    
    public CreativeApiary() {	
        RegistryHandler.init();
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.client_config);
        
        Config.loadConfig(Config.client_config, FMLPaths.CONFIGDIR.get().resolve("creativeapiary-client.toml").toString());
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientEventHandler::clientStuff);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
    	CreativeNetPacketHandler.init();
    }

    private void doClientStuff(final FMLClientSetupEvent event) 
    {
    	
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) 
    {
    	
    }
}

package com.YTrollman.CreativeApiary.init;

import com.YTrollman.CreativeApiary.gui.UnvalidatedCreativeApiaryScreen;
import com.YTrollman.CreativeApiary.gui.ValidatedCreativeApiaryScreen;
import com.YTrollman.CreativeApiary.registry.ModContainers;

import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientEventHandler {

    public static void clientStuff() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientEventHandler::doClientStuff);
    }

    private static void doClientStuff(final FMLClientSetupEvent event) 
    {
        ScreenManager.register(ModContainers.VALIDATED_CREATIVE_APIARY_CONTAINER.get(), ValidatedCreativeApiaryScreen::new);
        ScreenManager.register(ModContainers.UNVALIDATED_CREATIVE_APIARY_CONTAINER.get(), UnvalidatedCreativeApiaryScreen::new);
    }
}
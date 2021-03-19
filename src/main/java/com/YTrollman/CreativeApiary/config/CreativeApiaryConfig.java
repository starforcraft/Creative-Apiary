package com.YTrollman.CreativeApiary.config;

import com.resourcefulbees.resourcefulbees.lib.ApiaryOutput;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;

public class CreativeApiaryConfig {

    public static EnumValue<ApiaryOutput> TCREATIVE_APIARY_OUTPUT;
    public static ForgeConfigSpec.IntValue TCREATIVE_APIARY_QUANTITY;

    public static void init(ForgeConfigSpec.Builder client) {

            client.comment("Creative Apiary Options");
            
            TCREATIVE_APIARY_OUTPUT = client
            		.comment("\nTier Creative Apiary Output")
                    .defineEnum("tierCreativeApiaryOutput", ApiaryOutput.BLOCK, ApiaryOutput.BLOCK, ApiaryOutput.BLOCK);
            TCREATIVE_APIARY_QUANTITY = client
            		.comment("\nTier Creative Apiary Quantity")
                    .defineInRange("tierCreativeApiaryQuantity", 10, 1, 2147483647);
    }
}

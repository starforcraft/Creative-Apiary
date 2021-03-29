package com.YTrollman.CreativeApiary.config;

import com.resourcefulbees.resourcefulbees.lib.ApiaryOutput;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;

public class CreativeApiaryConfig {

    public static EnumValue<ApiaryOutput> TCREATIVE_APIARY_OUTPUT;
    public static ForgeConfigSpec.IntValue TCREATIVE_APIARY_QUANTITY;
    public static ForgeConfigSpec.IntValue TCREATIVE_APIARY_MAX_BEES;
    public static ForgeConfigSpec.DoubleValue TCREATIVE_APIARY_SPEED;
    public static ForgeConfigSpec.DoubleValue CREATIVE_APIARY_BREEDER_SPEED;

    public static void init(ForgeConfigSpec.Builder client) {

            client.comment("Creative Apiary Options");
            
            TCREATIVE_APIARY_OUTPUT = client
            		.comment("\nTier Creative Apiary Output")
                    .defineEnum("tierCreativeApiaryOutput", ApiaryOutput.BLOCK, ApiaryOutput.BLOCK, ApiaryOutput.BLOCK);
            TCREATIVE_APIARY_QUANTITY = client
            		.comment("\nTier Creative Apiary Quantity \n How many Combs/Blocks per Bees?")
                    .defineInRange("tierCreativeApiaryQuantity", 100, 1, Integer.MAX_VALUE);
            TCREATIVE_APIARY_MAX_BEES = client
                    .comment("\nTier Creative Apiary Max Bees")
                    .defineInRange("tierCreativeApiaryMaxBees", 100, 1, Integer.MAX_VALUE);
            TCREATIVE_APIARY_SPEED = client
                    .comment("\nTier Creative Apiary Max Time in Hive (Value in %) \nFor example 0.7 = 70% reduction")
                    .defineInRange("tierCreativeApiaryMaxTimeInHive", 1D, 0D, 1D);
            CREATIVE_APIARY_BREEDER_SPEED = client
                    .comment("\nCreative Apiary Breeder Speed (Value in %) \nFor example 0.7 = 70% reduction")
                    .defineInRange("CreativeApiaryBreederSpeed", 1D, 0D, 1D);
    }
}

package com.YTrollman.CreativeApiary.config;

import com.resourcefulbees.resourcefulbees.lib.ApiaryOutput;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;

public class CreativeApiaryConfig {

    public static EnumValue<ApiaryOutput> TCREATIVE_APIARY_OUTPUT;
    public static ForgeConfigSpec.IntValue TCREATIVE_APIARY_OUTPUT_AMOUNT;
    public static ForgeConfigSpec.IntValue TCREATIVE_APIARY_QUANTITY;
    public static ForgeConfigSpec.IntValue TCREATIVE_APIARY_MAX_BEES;
    public static ForgeConfigSpec.IntValue TCREATIVE_APIARY_MAX_TIME_IN_HIVE;
    public static ForgeConfigSpec.IntValue TCREATIVE_APIARY_MIN_TIME_IN_HIVE;

    public static void init(ForgeConfigSpec.Builder client) {

            client.comment("Creative Apiary Options");
            
            TCREATIVE_APIARY_OUTPUT = client
            		.comment("\nTier Creative Apiary Output")
                    .defineEnum("tierCreativeApiaryOutput", ApiaryOutput.BLOCK, ApiaryOutput.BLOCK, ApiaryOutput.BLOCK);
            TCREATIVE_APIARY_OUTPUT_AMOUNT = client
                    .comment("\nTier Creative Apiary Output Amount")
                    .defineInRange("tierCreativeApiaryOutputAmount", 100, 1, Integer.MAX_VALUE);
            TCREATIVE_APIARY_QUANTITY = client
            		.comment("\nTier Creative Apiary Quantity")
                    .defineInRange("tierCreativeApiaryQuantity", 1, 1, Integer.MAX_VALUE);
            TCREATIVE_APIARY_MAX_BEES = client
                    .comment("\nTier Creative Apiary Max Bees")
                    .defineInRange("tierCreativeApiaryMaxBees", 100, 1, Integer.MAX_VALUE);
            TCREATIVE_APIARY_MAX_TIME_IN_HIVE = client
                    .comment("\nTier Creative Apiary Max Time in Hive (Value divides the default Resourceful Bees Apiary Value) \nDefault Tier 1-4 Apiary is 2400")
                    .defineInRange("tierCreativeApiaryMaxTimeInHive", 100, 1, Integer.MAX_VALUE);
            TCREATIVE_APIARY_MIN_TIME_IN_HIVE = client
                    .comment("\nTier Creative Apiary Min Time in Hive (Value divides the default Resourceful Bees Apiary Value) \nDefault Tier 1-4 Apiary is 600")
                    .defineInRange("tierCreativeApiaryMinTimeInHive", 100, 1, Integer.MAX_VALUE);
    }
}

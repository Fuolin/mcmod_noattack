package com.NoAttackMod;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    private static ModConfigSpec.BooleanValue disableAttacksConfig;
    private static ModConfigSpec.ConfigValue<List<? extends String>> whiteListConfig;
    private static ModConfigSpec.ConfigValue<List<? extends String>> blackListConfig;
    private static ModConfigSpec.BooleanValue WhiteOrBlack;

    public static void init(ModConfigSpec.Builder builder) {
        disableAttacksConfig = builder
                .comment("Whether to disable player attacks by default")
                .define("disableAttacks", true);

        // 在配置文件中定义黑白名单
        whiteListConfig = builder
                .comment("List of entity IDs that are allowed to be attacked (whitelist).",
                        "Use the format \"modid:entity_id\", e.g., \"minecraft:ender_dragon\".")
                .defineListAllowEmpty("whitelist", List.of(),() -> "modid:entity_id", obj -> obj instanceof String);

        blackListConfig = builder
                .comment("List of entity IDs that are NOT allowed to be attacked (blacklist).",
                        "Use the format \"modid:entity_id\", e.g., \"minecraft:ender_dragon\".")
                .defineListAllowEmpty("blacklist", List.of(),() -> "modid:entity_id", obj -> obj instanceof String);

        WhiteOrBlack = builder
                .comment("White List or Black List,true is white list")
                .define("whiteorblacks", true);
    }

    public static boolean isAttackDisabled() {
        return disableAttacksConfig.get();
    }

    public static void setDisableAttacksConfig(Boolean tf){
        disableAttacksConfig.set(tf);
        disableAttacksConfig.save();
    }

    public static List<? extends String> getWhiteList() {
        return whiteListConfig.get();
    }

    public static List<? extends String> getBlackList() {
        return blackListConfig.get();
    }

    public static Boolean getWhiteOrBlack() {
        return WhiteOrBlack.get();
    }
}
package com.NoAttackMod;

import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

@Mod(NoAttackMod.MODID)
public class NoAttackMod {
    public static final String MODID = "noattack";

    public NoAttackMod( ModContainer container) {
        // 构建配置规范
        ModConfigSpec.Builder configBuilder = new ModConfigSpec.Builder();
        Config.init(configBuilder);// 初始化配置定义
        ModConfigSpec serverSpec = configBuilder.build(); // 构建 Spec

        // 注册配置
        container.registerConfig(ModConfig.Type.SERVER, serverSpec,"noattack.toml");

        // 注册NeoForge事件监听器
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("noattack")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("on")
                        .executes(context -> {
                            Config.setDisableAttacksConfig(true);
                            context.getSource().sendSuccess(() ->
                                    Component.literal("§a[NoAttack] 已禁止所有玩家攻击"), false);
                            return 1;
                        }))
                .then(Commands.literal("off")
                        .executes(context -> {
                            Config.setDisableAttacksConfig(false);
                            context.getSource().sendSuccess(() ->
                                    Component.literal("§a[NoAttack] 已允许所有玩家攻击"), false);
                            return 1;
                        }))
                .executes(context -> {
                    String status = Config.isAttackDisabled() ? "§4禁止" : "§2允许";
                    context.getSource().sendSuccess(
                            () -> Component.literal("§6[NoAttack] 当前攻击状态: " + status),
                            false
                    );
                    return 1;
                }));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttack(AttackEntityEvent event) {
        if (!Config.isAttackDisabled()) {
            return;
        }

        //获取生物id
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getTarget().getType());

        //是否在黑/白名单
        if(isEntityWhiteOrBlacklisted(entityId)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKnockback(LivingKnockBackEvent event) {
        if (!Config.isAttackDisabled() || !(event.getEntity().getLastAttacker() instanceof Player)) {
            return;
        }

        //获取生物id
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());

        //是否在黑/白名单
        if(isEntityWhiteOrBlacklisted(entityId)) return;

        event.setCanceled(true);
    }

    public static boolean isEntityWhiteOrBlacklisted(ResourceLocation entityId) {
        if(Config.getWhiteOrBlack()) return Config.getWhiteList().contains(entityId.toString());
        else return !Config.getBlackList().contains(entityId.toString());
    }
}
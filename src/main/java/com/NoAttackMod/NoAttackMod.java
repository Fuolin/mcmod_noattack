package com.NoAttackMod;

import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

@Mod(NoAttackMod.MODID)
public class NoAttackMod {
    public static final String MODID = "noattack";
    private static ModConfigSpec.BooleanValue disableAttacksConfig;
    private static ModConfigSpec configSpec;
    private static boolean isAttackDisabled = true; // 默认值

    public NoAttackMod(IEventBus modEventBus, ModContainer container) {
        // 构建配置规范
        ModConfigSpec.Builder configBuilder = new ModConfigSpec.Builder();
        disableAttacksConfig = configBuilder
                .comment("是否默认禁用玩家攻击")
                .define("disableAttacks", true);
        configSpec = configBuilder.build();

        // 注册配置
        container.registerConfig(ModConfig.Type.SERVER, configSpec);

        // 注册配置加载事件监听器
        modEventBus.addListener(this::onConfigLoad);

        // 注册NeoForge事件监听器
        NeoForge.EVENT_BUS.register(this);
    }

    private void onConfigLoad(ModConfigEvent event) {
        // 当配置加载完成后更新值
        if (event.getConfig().getModId().equals(MODID)) {
            isAttackDisabled = disableAttacksConfig.get();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("noattack")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("on")
                        .executes(context -> {
                            isAttackDisabled = true;
                            context.getSource().sendSuccess(() ->
                                    Component.literal("§a[NoAttack] 已禁止所有玩家攻击"), false);
                            return 1;
                        }))
                .then(Commands.literal("off")
                        .executes(context -> {
                            isAttackDisabled = false;
                            context.getSource().sendSuccess(() ->
                                    Component.literal("§a[NoAttack] 已允许所有玩家攻击"), false);
                            return 1;
                        }))
                .executes(context -> {
                    String status = isAttackDisabled ? "§4禁止" : "§2允许";
                    context.getSource().sendSuccess(
                            () -> Component.literal("§6[NoAttack] 当前攻击状态: " + status),
                            false
                    );
                    return 1;
                }));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttack(AttackEntityEvent event) {
        if (!isAttackDisabled) {
            return;
        }
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKnockback(LivingKnockBackEvent event) {
        if (!isAttackDisabled || !(event.getEntity().getLastAttacker() instanceof Player)) {
            return;
        }
        event.setCanceled(true);
    }
}
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Mod(NoAttackMod.MODID)
public class NoAttackMod {
    public static final String MODID = "noattack";
    private static ModConfigSpec.BooleanValue disableAttacksConfig;
    private static Path configFilePath;
    private static final Logger LOGGER = LogManager.getLogger(MODID);

    public NoAttackMod(IEventBus modEventBus, ModContainer container) {
        // 构建配置规范
        ModConfigSpec.Builder configBuilder = new ModConfigSpec.Builder();
        disableAttacksConfig = configBuilder
                .comment("Whether to disable player attacks by default")
                .define("disableAttacks", true);
        ModConfigSpec configSpec = configBuilder.build();

        // 注册配置
        container.registerConfig(ModConfig.Type.SERVER, configSpec);

        // 注册配置加载事件监听器
        modEventBus.addListener(this::onConfigLoad);

        // 注册NeoForge事件监听器
        NeoForge.EVENT_BUS.register(this);
    }

    private void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER &&
                event.getConfig().getModId().equals(MODID)) {
            ModConfig serverConfig = event.getConfig();
            configFilePath = serverConfig.getFullPath();
        }
    }

    private boolean isAttackDisabled() {
        return disableAttacksConfig.get();
    }

    // 手动保存配置到文件
    private void saveConfigToFile(boolean value) {
        if (configFilePath == null) return;

        Properties props = new Properties();
        props.setProperty("disableAttacks", String.valueOf(value));

        try {
            Files.createDirectories(configFilePath.getParent());
            props.store(Files.newOutputStream(configFilePath), "NoAttackMod Configuration");
        } catch (IOException e) {
            LOGGER.error("无法保存配置文件", e);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("noattack")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("on")
                        .executes(context -> {
                            // 保存配置到文件
                            saveConfigToFile(true);
                            // 重新加载配置
                            disableAttacksConfig.set(true);
                            context.getSource().sendSuccess(() ->
                                    Component.literal("§a[NoAttack] 已禁止所有玩家攻击"), false);
                            return 1;
                        }))
                .then(Commands.literal("off")
                        .executes(context -> {
                            // 保存配置到文件
                            saveConfigToFile(false);
                            // 重新加载配置
                            disableAttacksConfig.set(false);
                            context.getSource().sendSuccess(() ->
                                    Component.literal("§a[NoAttack] 已允许所有玩家攻击"), false);
                            return 1;
                        }))
                .executes(context -> {
                    String status = isAttackDisabled() ? "§4禁止" : "§2允许";
                    context.getSource().sendSuccess(
                            () -> Component.literal("§6[NoAttack] 当前攻击状态: " + status),
                            false
                    );
                    return 1;
                }));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttack(AttackEntityEvent event) {
        if (!isAttackDisabled()) {
            return;
        }
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKnockback(LivingKnockBackEvent event) {
        if (!isAttackDisabled() || !(event.getEntity().getLastAttacker() instanceof Player)) {
            return;
        }
        event.setCanceled(true);
    }
}

package com.NoAttackMod;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

@Mod("noattack")
public class NoAttackMod {
    private static ModConfigSpec.BooleanValue disableAttacksConfig;
    private static ModConfigSpec configSpec;

    public NoAttackMod(ModContainer modContainer) {
        // 构建配置
        ModConfigSpec.Builder configBuilder = new ModConfigSpec.Builder();
        disableAttacksConfig = configBuilder
                .comment("Whether to disable player attacks")
                .define("disableAttacks", true);

        configSpec = configBuilder.build();

        // 注册配置
        modContainer.registerConfig(ModConfig.Type.SERVER, configSpec);

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPreDamage(LivingDamageEvent.Pre event) {
        if (!disableAttacksConfig.get()) return;

        DamageContainer container = event.getContainer();
        if (container.getSource().getEntity() instanceof Player) {
            // 将伤害设置为0来取消攻击
            container.setNewDamage(0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!disableAttacksConfig.get()) return;

        if (event.getSource().getEntity() instanceof Player) {
            // 取消受伤动画和声音
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onKnockback(LivingKnockBackEvent event) {
        if (!disableAttacksConfig.get()) return;

        if (event.getAttacker() instanceof Player) {
            // 取消击退效果
            event.setCanceled(true);
        }
    }
}
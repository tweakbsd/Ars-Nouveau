package com.hollingsworth.craftedmagic.spell.effect;

import com.hollingsworth.craftedmagic.ModConfig;
import com.hollingsworth.craftedmagic.api.spell.AbstractEffect;
import com.hollingsworth.craftedmagic.api.spell.AbstractAugment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayList;

public class EffectSlowfall extends AbstractEffect {
    public EffectSlowfall() {
        super(ModConfig.EffectSlowfallID, "Slowfall");
    }

    @Override
    public void onResolve(RayTraceResult rayTraceResult, World world, LivingEntity shooter, ArrayList<AbstractAugment> augments) {
        if(rayTraceResult instanceof EntityRayTraceResult && ((EntityRayTraceResult) rayTraceResult).getEntity() instanceof LivingEntity){
            applyPotion(((LivingEntity) ((EntityRayTraceResult) rayTraceResult).getEntity()), Effects.SLOW_FALLING, augments);
        }
    }

    @Override
    public int getManaCost() {
        return 10;
    }

    @Override
    public Tier getTier() {
        return Tier.TWO;
    }
}

package com.hollingsworth.arsnouveau.common.spell.effect;

import com.hollingsworth.arsnouveau.GlyphLib;
import com.hollingsworth.arsnouveau.api.spell.AbstractAugment;
import com.hollingsworth.arsnouveau.api.spell.AbstractEffect;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.common.entity.ModEntities;
import com.hollingsworth.arsnouveau.common.entity.SummonWolf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EffectSummonWolves extends AbstractEffect {

    public EffectSummonWolves() {
        super(GlyphLib.EffectSummonWolvesID, "Summon Wolves");
    }

    @Override
    public void onResolve(RayTraceResult rayTraceResult, World world, @Nullable LivingEntity shooter, List<AbstractAugment> augments, SpellContext spellContext) {
        super.onResolve(rayTraceResult, world, shooter, augments, spellContext);
        if(!canSummon(shooter))
            return;
        Vector3d hit = rayTraceResult.getHitVec();
        for(int i = 0; i < 2; i++){
            SummonWolf wolf = new SummonWolf(ModEntities.SUMMON_WOLF, world);
            wolf.ticksLeft = 400;
            wolf.setPosition(hit.getX(), hit.getY(), hit.getZ());
            wolf.setAttackTarget(shooter.getLastAttackedEntity());
            wolf.setAggroed(true);
            wolf.setTamed(true);
            wolf.setTamedBy((PlayerEntity) shooter);
            world.addEntity(wolf);
        }
    }

    @Override
    public int getManaCost() {
        return 0;
    }
}

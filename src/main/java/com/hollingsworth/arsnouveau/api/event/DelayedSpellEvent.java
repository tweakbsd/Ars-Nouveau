package com.hollingsworth.arsnouveau.api.event;

import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class DelayedSpellEvent implements ITimedEvent{
    private int duration;
    private final Spell spell;
    private final RayTraceResult result;
    private final World world;
    private final LivingEntity shooter;

    public DelayedSpellEvent(int delay, Spell recipe, RayTraceResult result, World world, LivingEntity shooter){
        this.duration = delay;
        this.spell = recipe;
        this.result = result;
        this.world = world;
        this.shooter = shooter;
    }

    @Override
    public void tick() {
        duration--;

        if(duration <= 0){
            resolveSpell();
        }
    }

    public void resolveSpell(){
        if(world == null)
            return;
        SpellContext context = new SpellContext(spell, shooter);
        SpellResolver.resolveEffects(world, shooter, result, spell, context);
    }

    @Override
    public boolean isExpired() {
        return duration <= 0 || world == null;
    }
}

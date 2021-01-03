package com.hollingsworth.arsnouveau.common.block.tile;

import com.hollingsworth.arsnouveau.api.spell.EntitySpellResolver;
import com.hollingsworth.arsnouveau.api.spell.IPickupResponder;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.util.BlockUtil;
import com.hollingsworth.arsnouveau.api.util.ManaUtil;
import com.hollingsworth.arsnouveau.common.block.RuneBlock;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import com.hollingsworth.arsnouveau.setup.BlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.UUID;

public class RuneTile extends AnimatedTile implements IPickupResponder {
    public Spell spell;
    public boolean isTemporary;
    public boolean isCharged;
    public int ticksUntilCharge;
    public UUID uuid;
    public RuneTile() {
        super(BlockRegistry.RUNE_TILE);
        isCharged = true;
        isTemporary = false;
        ticksUntilCharge = 0;
    }

    public void setSpell(Spell spell) {
        this.spell = spell;
    }

    public void castSpell(Entity entity){

        if(!this.isCharged || spell == null || !spell.isValid() || !(entity instanceof LivingEntity) || !(world instanceof ServerWorld) || !(spell.getRecipe().get(0) instanceof MethodTouch))
            return;
        try {

            PlayerEntity playerEntity = uuid != null ? world.getPlayerByUuid(uuid) : FakePlayerFactory.getMinecraft((ServerWorld) world);
            playerEntity = playerEntity == null ?  FakePlayerFactory.getMinecraft((ServerWorld) world) : playerEntity;
            EntitySpellResolver resolver = new EntitySpellResolver(spell, new SpellContext(spell, playerEntity).withCastingTile(this).withType(SpellContext.CasterType.RUNE));
            resolver.onCastOnEntity(ItemStack.EMPTY, playerEntity, (LivingEntity) entity, Hand.MAIN_HAND);
            if (this.isTemporary) {
                world.destroyBlock(pos, false);
                return;
            }
            this.isCharged = false;

            world.setBlockState(pos, world.getBlockState(pos).func_235896_a_(RuneBlock.POWERED));
            ticksUntilCharge = 20 * 2;
        }catch (Exception e){
            PortUtil.sendMessage(entity, new TranslationTextComponent("ars_nouveau.rune.error"));
            e.printStackTrace();
            world.destroyBlock(pos, false);
        }
    }

    public void setParsedSpell(Spell spell){
        if(spell.getRecipe().size() <= 1){
            this.spell = null;
            return;
        }
        spell.getRecipe().set(0, new MethodTouch());
        this.spell = spell;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        if(spell != null)
            tag.putString("spell", spell.serialize());
        tag.putBoolean("charged", isCharged);
        tag.putBoolean("temp", isTemporary);
        tag.putInt("cooldown", ticksUntilCharge);
        if(uuid != null)
            tag.putUniqueId("uuid", uuid);
        return super.write(tag);
    }

    @Override
    public void read( BlockState state, CompoundNBT tag) {
        this.spell = Spell.deserialize(tag.getString("spell"));
        this.isCharged = tag.getBoolean("charged");
        this.isTemporary = tag.getBoolean("temp");
        this.ticksUntilCharge = tag.getInt("cooldown");
        if(tag.contains("uuid"))
            this.uuid = tag.getUniqueId("uuid");
        super.read(state, tag);
    }

    @Override
    public void tick() {
        if(world == null)
            return;
        if(!world.isRemote) {
            if (ticksUntilCharge > 0) {
                ticksUntilCharge -= 1;
                return;
            }
        }
        if(this.isCharged)
            return;
        if(!world.isRemote && this.isTemporary){
            world.destroyBlock(this.pos, false);
        }
        if(!world.isRemote){
            BlockPos fromPos = ManaUtil.takeManaNearbyWithParticles(pos, world, 10, 100);
            if(fromPos != null) {
                this.isCharged = true;
                world.setBlockState(pos, world.getBlockState(pos).func_235896_a_(RuneBlock.POWERED));
            }else
                ticksUntilCharge = 20 * 3;
        }
    }

    @Override
    public ItemStack onPickup(ItemStack stack) {
        return BlockUtil.insertItemAdjacent(world, pos, stack);
    }
}
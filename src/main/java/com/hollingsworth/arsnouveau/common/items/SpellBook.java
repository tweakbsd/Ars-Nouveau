package com.hollingsworth.arsnouveau.common.items;

import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import com.hollingsworth.arsnouveau.api.client.IDisplayMana;
import com.hollingsworth.arsnouveau.api.item.IScribeable;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.api.spell.ISpellTier;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.util.MathUtil;
import com.hollingsworth.arsnouveau.api.util.SpellRecipeUtil;
import com.hollingsworth.arsnouveau.client.keybindings.ModKeyBindings;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.hollingsworth.arsnouveau.client.renderer.item.SpellBookRenderer;
import com.hollingsworth.arsnouveau.common.block.tile.IAnimationListener;
import com.hollingsworth.arsnouveau.common.block.tile.IntangibleAirTile;
import com.hollingsworth.arsnouveau.common.block.tile.PhantomBlockTile;
import com.hollingsworth.arsnouveau.common.block.tile.ScribesTile;
import com.hollingsworth.arsnouveau.common.capability.ManaCapability;
import com.hollingsworth.arsnouveau.common.network.Networking;
import com.hollingsworth.arsnouveau.common.network.PacketOpenGUI;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import com.hollingsworth.arsnouveau.setup.ItemsRegistry;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;

public class SpellBook extends Item implements ISpellTier, IScribeable, IDisplayMana, IAnimatable, IAnimationListener {

    public static final String BOOK_MODE_TAG = "mode";
    public static final String UNLOCKED_SPELLS = "spells";
    public static final String OPEN_TAG = "open";
    public static final String LAST_CAST = "last_cast";
    public static final int SEGMENTS = 10;
    public Tier tier;


    public SpellBook(Tier tier){
        super(new Item.Properties().maxStackSize(1).group(ArsNouveau.itemGroup).setISTER(() -> SpellBookRenderer::new));
        this.tier = tier;
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if(!stack.hasTag())
            stack.setTag(new CompoundNBT());

        if(isSelected)
            return;
        //if(entityIn instanceof PlayerEntity && ((PlayerEntity) entityIn).getHeldItemOffhand() == stack)
        if(worldIn.getGameTime() % 5 == 0 && stack.getTag().getBoolean(OPEN_TAG) && (entityIn instanceof PlayerEntity && ((PlayerEntity) entityIn).getHeldItemOffhand().getItem() != stack.getItem())){
            stack.getTag().putBoolean(OPEN_TAG, false);
            AnimationController controller = GeckoLibUtil.getControllerForStack(this.factory, stack, "openController");
            // If you don't do this, the popup animation will only play once because the animation will be cached.
            controller.markNeedsReload();
            //Set the animation to open the jackinthebox which will start playing music and eventually do the actual animation. Also sets it to not loop
            controller.setAnimation(new AnimationBuilder().addAnimation("close", false));


        }


        if(!worldIn.isRemote && worldIn.getGameTime() % 5 == 0 && !stack.hasTag()) {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt(SpellBook.BOOK_MODE_TAG, 0);
            StringBuilder starting_spells = new StringBuilder();

            if(stack.getItem() == ItemsRegistry.creativeSpellBook){
                ArsNouveauAPI.getInstance().getSpell_map().values().forEach(s -> starting_spells.append(",").append(s.getTag().trim()));
            }else{
                ArsNouveauAPI.getInstance().getDefaultStartingSpells().forEach(s-> starting_spells.append(",").append(s.getTag().trim()));
            }
            tag.putString(SpellBook.UNLOCKED_SPELLS, starting_spells.toString());
            stack.setTag(tag);
        }
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if(!stack.hasTag())
            return new ActionResult<>(ActionResultType.SUCCESS, stack);

        ManaCapability.getMana(playerIn).ifPresent(iMana -> {
            if(iMana.getBookTier() < this.tier.ordinal()){
                iMana.setBookTier(this.tier.ordinal());
            }
            if(iMana.getGlyphBonus() < SpellBook.getUnlockedSpells(stack.getTag()).size()){
                iMana.setGlyphBonus(SpellBook.getUnlockedSpells(stack.getTag()).size());
            }
        });

        RayTraceResult result = playerIn.pick(5, 0, false);
        if(result instanceof BlockRayTraceResult && worldIn.getTileEntity(((BlockRayTraceResult) result).getPos()) instanceof ScribesTile)
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        if(result instanceof BlockRayTraceResult && !playerIn.isSneaking()){


            if(worldIn.getTileEntity(((BlockRayTraceResult) result).getPos()) != null &&
                    !(worldIn.getTileEntity(((BlockRayTraceResult) result).getPos()) instanceof IntangibleAirTile
                    ||(worldIn.getTileEntity(((BlockRayTraceResult) result).getPos()) instanceof PhantomBlockTile))) {
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            }
        }


        if(worldIn.isRemote || !stack.hasTag()){
            //spawnParticles(playerIn.posX, playerIn.posY + 2, playerIn.posZ, worldIn);
            return new ActionResult<>(ActionResultType.CONSUME, stack);
        }

        if(!stack.getTag().getBoolean(OPEN_TAG)){
            stack.getTag().putBoolean(OPEN_TAG, true);
            stack.getTag().putLong(LAST_CAST, worldIn.getGameTime());
            AnimationController controller = GeckoLibUtil.getControllerForStack(this.factory, stack, "openController");
            // If you don't do this, the popup animation will only play once because the animation will be cached.
            controller.markNeedsReload();
            //Set the animation to open the jackinthebox which will start playing music and eventually do the actual animation. Also sets it to not loop
            controller.setAnimation(new AnimationBuilder().addAnimation("open", false).addAnimation("open_idle", true));
        }

//        CompoundNBT tag = stack.getTag();
//        tag.putLong(LAST_CAST, worldIn.getGameTime());
//        if(!tag.getBoolean(OPEN_TAG)){
//            for(int i = 0; i < playerIn.inventory.mainInventory.size(); ++i) {
//                if (!playerIn.inventory.mainInventory.get(i).isEmpty() && stack.getItem() == playerIn.inventory.getStackInSlot(i).getItem() && ItemStack.areItemStackTagsEqual(stack, playerIn.inventory.mainInventory.get(i))) {
//                    tag.putBoolean(OPEN_TAG, true);
//                    Networking.sendToNearby(worldIn, playerIn, new PacketAnimateBook(playerIn.getEntityId(), Animations.OPEN.ordinal(), i));
//
//                    break;
//                }
//            }
//        }

        // Crafting mode
        if(getMode(stack.getTag()) == 0 && playerIn instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) playerIn;
            Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()->player), new PacketOpenGUI(stack.getTag(), getTier().ordinal(), getUnlockedSpellString(player.getHeldItem(handIn).getTag())));
            return new ActionResult<>(ActionResultType.CONSUME, stack);
        }
        SpellResolver resolver = new SpellResolver(getCurrentRecipe(stack), new SpellContext(getCurrentRecipe(stack), playerIn)
                .withColors(SpellBook.getSpellColor(stack.getTag(), SpellBook.getMode(stack.getTag()))));
        EntityRayTraceResult entityRes = MathUtil.getLookedAtEntity(playerIn, 25);

        if(entityRes != null && entityRes.getEntity() instanceof LivingEntity){
            resolver.onCastOnEntity(stack, playerIn, (LivingEntity) entityRes.getEntity(), handIn);
            return new ActionResult<>(ActionResultType.CONSUME, stack);
        }

        if(result.getType() == RayTraceResult.Type.BLOCK){
            ItemUseContext context = new ItemUseContext(playerIn, handIn, (BlockRayTraceResult) result);
            resolver.onCastOnBlock(context);
            return new ActionResult<>(ActionResultType.CONSUME, stack);
        }

        resolver.onCast(stack,playerIn,worldIn);
        return new ActionResult<>(ActionResultType.CONSUME, stack);
    }


    @Override
    public boolean onScribe(World world, BlockPos pos, PlayerEntity player, Hand handIn, ItemStack stack) {
        if(!(player.getHeldItem(handIn).getItem() instanceof SpellBook))
            return false;

        List<AbstractSpellPart> spellParts = SpellBook.getUnlockedSpells(player.getHeldItem(handIn).getTag());
        int unlocked = 0;
        for(AbstractSpellPart spellPart : spellParts){
            if(SpellBook.unlockSpell(stack.getTag(), spellPart))
                unlocked++;
        }
        PortUtil.sendMessage(player, new StringTextComponent("Copied " + unlocked + " new spells to the book."));
        return true;
    }


    /**
     * How long it takes to use or consume an item
     */
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }


    /*
    Called on block use. TOUCH ONLY
     */
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        return ActionResultType.PASS;
    }

    public List<AbstractSpellPart> getCurrentRecipe(ItemStack stack){
        return SpellBook.getRecipeFromTag(stack.getTag(), getMode(stack.getTag()));
    }

    public static List<AbstractSpellPart> getRecipeFromTag(CompoundNBT tag, int r_slot){
        String recipeStr = getRecipeString(tag, r_slot);
        return SpellRecipeUtil.getSpellsFromTagString(recipeStr);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
        return true;
    }

    public static void setSpellName(CompoundNBT tag, String name, int slot){
        tag.putString(slot + "_name", name);
    }

    public static String getSpellName(CompoundNBT tag, int slot){
        if(slot == 0)
            return "Create Mode";
        return tag.getString( slot+ "_name");
    }

    public static void setSpellColor(CompoundNBT tag, ParticleColor.IntWrapper color, int slot){
        tag.putString(slot + "_color", color.serialize());
    }

    public static ParticleColor.IntWrapper getSpellColor(CompoundNBT tag, int slot){
        String key = slot+ "_color";
        if(!tag.contains(key))
            return new ParticleColor.IntWrapper(255, 25, 180);

        return ParticleColor.IntWrapper.deserialize(tag.getString(key));
    }

    public static String getSpellName(CompoundNBT tag){
        return getSpellName( tag, getMode(tag));
    }

    public static String getRecipeString(CompoundNBT tag, int spell_slot){
        return tag.getString(spell_slot + "recipe");
    }

    public static void setRecipe(CompoundNBT tag, String recipe, int spell_slot){
        tag.putString(spell_slot + "recipe", recipe);
    }

    public static int getMode(CompoundNBT tag){
        return tag.getInt(SpellBook.BOOK_MODE_TAG);
    }

    public static void setMode(CompoundNBT tag, int mode){
        tag.putInt(SpellBook.BOOK_MODE_TAG, mode);
    }

    public static List<AbstractSpellPart> getUnlockedSpells(CompoundNBT tag){
        return SpellRecipeUtil.getSpellsFromString(tag.getString(SpellBook.UNLOCKED_SPELLS));
    }

    public static String getUnlockedSpellString(CompoundNBT tag){
        return tag.getString(SpellBook.UNLOCKED_SPELLS);
    }

    public static boolean unlockSpell(CompoundNBT tag, AbstractSpellPart spellPart){
        if(containsSpell(tag, spellPart))
            return false;
        String newSpells = tag.getString(SpellBook.UNLOCKED_SPELLS) + "," + spellPart.getTag();
        tag.putString(SpellBook.UNLOCKED_SPELLS, newSpells);
        return true;
    }

    public static void unlockSpell(CompoundNBT tag, String spellTag){
        String newSpells = tag.getString(SpellBook.UNLOCKED_SPELLS) + "," + spellTag;
        tag.putString(SpellBook.UNLOCKED_SPELLS, newSpells);
    }

    public static boolean containsSpell(CompoundNBT tag, AbstractSpellPart spellPart){
        return SpellBook.getUnlockedSpells(tag).contains(spellPart);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<ITextComponent> tooltip, final ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if(stack != null && stack.hasTag()) {
            tooltip.add(new StringTextComponent(SpellBook.getSpellName(stack.getTag())));

            tooltip.add(new StringTextComponent("Press " + KeyBinding.getDisplayString(ModKeyBindings.OPEN_SPELL_SELECTION.getKeyBinding().getKeyDescription()).get().getString()+ " to quick select"));
            tooltip.add(new StringTextComponent("Press " + KeyBinding.getDisplayString(ModKeyBindings.OPEN_BOOK.getKeyBinding().getKeyDescription()).get().getString() + " to quick craft"));
        }
    }

    @Override
    public Tier getTier() {
        return this.tier;
    }
    public AnimationFactory factory = new AnimationFactory(this);



    @Override
    public void registerControllers(AnimationData data)
    {
        data.addAnimationController(new AnimationController<SpellBook>(this, "openController", 1, this::openPredicate));
        data.addAnimationController(new AnimationController<SpellBook>(this, "pageController", 1, this::pagePredicate));
    }

    private <P extends IAnimatable> PlayState pagePredicate(AnimationEvent<P> pAnimationEvent) {
        return PlayState.CONTINUE;
    }



    private <P extends IAnimatable> PlayState openPredicate(AnimationEvent<P> pAnimationEvent) {
      //  pAnimationEvent.getController().setAnimation(new AnimationBuilder().addAnimation("open", false).addAnimation("open_idle", true));
        return PlayState.CONTINUE; }


    public void playNextPage(ItemStack stack){
//        AnimationController controller = GeckoLibUtil.getControllerForStack(this.factory, stack, "openController");
//        // If you don't do this, the popup animation will only play once because the animation will be cached.
//        controller.markNeedsReload();
//        //Set the animation to open the jackinthebox which will start playing music and eventually do the actual animation. Also sets it to not loop
//        controller.setAnimation(new AnimationBuilder().addAnimation("page_turn_forward", false));

    }

    public void playBackPage(ItemStack stack){
        AnimationController controller = GeckoLibUtil.getControllerForStack(this.factory, stack, "pageController");
        // If you don't do this, the popup animation will only play once because the animation will be cached.
        controller.markNeedsReload();
        //Set the animation to open the jackinthebox which will start playing music and eventually do the actual animation. Also sets it to not loop
        controller.setAnimation(new AnimationBuilder().addAnimation("page_turn_forward", false));

    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public void startAnimation(int arg) {

    }



    public enum Animations{
        PAGE_FORWARD,
        PAGE_BACKWARD,
        OPEN,
        CLOSE
    }
}

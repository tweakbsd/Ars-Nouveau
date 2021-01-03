package com.hollingsworth.arsnouveau.api.spell;

import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import net.minecraft.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.hollingsworth.arsnouveau.api.util.SpellRecipeUtil.getEquippedAugments;

public class Spell {

    private List<AbstractSpellPart> recipe;
    private int cost;

    public Spell(@Nonnull List<AbstractSpellPart> recipe){
        this.setRecipe(recipe);
        this.cost = calculateRecipeCost();
    }

    public Spell(){
        this.setRecipe(new ArrayList<>());
        this.cost = 0;
    }

    public int getSpellSize(){
        return getRecipe().size();
    }

    private int calculateRecipeCost(){
        int cost = 0;
        for (int i = 0; i < getRecipe().size(); i++) {
            AbstractSpellPart spell = getRecipe().get(i);
            if (!(spell instanceof AbstractAugment)) {
                List<AbstractAugment> augments = getAugments(i, null);
                cost += spell.getAdjustedManaCost(augments);
            }
        }
        return cost;
    }

    public List<AbstractAugment> getAugments(int startPosition, @Nullable LivingEntity caster){
        ArrayList<AbstractAugment> augments = new ArrayList<>();
        for(int j = startPosition + 1; j < getRecipe().size(); j++){
            AbstractSpellPart next_spell = getRecipe().get(j);
            if(next_spell instanceof AbstractAugment){
                augments.add((AbstractAugment) next_spell);
            }else{
                break;
            }
        }
        // Add augment bonuses from equipment
        if(caster != null)
            augments.addAll(getEquippedAugments(caster));
        return augments;
    }

    public int getCost(){
        return Math.max(0, cost);
    }

    public void setCost(int cost){
        this.cost = cost;
    }

    public String serialize(){
        List<String> tags = new ArrayList<>();
        for(AbstractSpellPart slot : getRecipe()){
            tags.add(slot.tag);
        }
        return tags.toString();
    }

    public static Spell deserialize(String recipeStr){
        ArrayList<AbstractSpellPart> recipe = new ArrayList<>();
        if (recipeStr == null || recipeStr.isEmpty() || recipeStr.length() <= 3) // Account for empty strings and '[,]'
            return new Spell(recipe);
        String[] recipeList = recipeStr.substring(1, recipeStr.length() - 1).split(",");
        for(String id : recipeList){
            if (ArsNouveauAPI.getInstance().getSpell_map().containsKey(id.trim()))
                recipe.add(ArsNouveauAPI.getInstance().getSpell_map().get(id.trim()));
        }
        return new Spell(recipe);
    }

    public String getDisplayString(){
        StringBuilder str = new StringBuilder();

        for(int i = 0; i < getRecipe().size(); i++){
            AbstractSpellPart spellPart = getRecipe().get(i);
            int num = 1;
            for(int j = i + 1; j < getRecipe().size(); j++){
                if(spellPart.name.equals(getRecipe().get(j).name))
                    num++;
                else
                    break;
            }
            if(num > 1){
                str.append(spellPart.name).append(" x").append(num);
                i += num - 1;
            }else{
                str.append(spellPart.name);
            }
            if(i < getRecipe().size() - 1){
                str.append(" -> ");
            }
        }
        return str.toString();
    }

    public boolean isValid(){
        return this.getRecipe() != null && !this.getRecipe().isEmpty();
    }

    public List<AbstractSpellPart> getRecipe() {
        return recipe;
    }

    public void setRecipe(List<AbstractSpellPart> recipe) {
        this.recipe = recipe;
    }
}

package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import structures.basic.Unit;

//Maps Unit Names to List of Attributes that units can have

//Short Descriptions (in alphabetical order)
//attackTwice: unit can move+attack twice in one turn
//avatarDamageEffect: unit gains +2 attack if avatar is dealt damage
//drawCardOnSummon: both players draw a card when this unit is summonned
//drawCardOnDeath: owner draws a card on unit's death
//flying: can move anywhere on the board
//healAvatarOnSummon: increases avatar health by 3 (capped to maximum of 20)
//provoke: unit will provoke adjacent units 
//ranged: unit can attack anywhere on the board
//spellCastEffect: gains 1 health and mana if enemy casts 
//summonAnywhere: unit can be summoned anywhere

public class UnitAttributes {
	public final static Map<String, ArrayList<String>> map = Map.ofEntries(
			Map.entry( StaticConfFiles.u_silverguard_knight, new ArrayList<>(List.of("provoke","avatarDamageEffect"))),
			Map.entry( StaticConfFiles.u_azure_herald, new ArrayList<>(List.of("healAvatarOnSummon"))),
			Map.entry(StaticConfFiles.u_ironcliff_guardian, new ArrayList<>(List.of("summonAnywhere", "provoke"))),
			Map.entry(StaticConfFiles.u_azurite_lion, new ArrayList<>(List.of("attackTwice"))),
			Map.entry(StaticConfFiles.u_hailstone_golem, new ArrayList<>()),
			Map.entry(StaticConfFiles.u_fire_spitter, new ArrayList<>(List.of("ranged"))),
			Map.entry(StaticConfFiles.u_comodo_charger, new ArrayList<>()),
			Map.entry(StaticConfFiles.u_pureblade_enforcer, new ArrayList<>(List.of("spellCastEffect"))),
			Map.entry(StaticConfFiles.u_rock_pulveriser, new ArrayList<>(List.of("provoke"))),
			Map.entry(StaticConfFiles.u_bloodshard_golem, new ArrayList<>()),
			Map.entry(StaticConfFiles.u_blaze_hound, new ArrayList<>(List.of("drawCardOnSummon"))),
			Map.entry(StaticConfFiles.u_windshrike, new ArrayList<>(List.of("flying", "drawCardOnDeath"))),
			Map.entry(StaticConfFiles.u_pyromancer, new ArrayList<>(List.of("ranged"))),
			Map.entry(StaticConfFiles.u_hailstone_golemR, new ArrayList<>()),
			Map.entry(StaticConfFiles.u_serpenti, new ArrayList<>(List.of("attackTwice"))),
			Map.entry(StaticConfFiles.u_planar_scout, new ArrayList<>(List.of("summonAnywhere"))),
			Map.entry(StaticConfFiles.humanAvatar, new ArrayList<>()),
			Map.entry(StaticConfFiles.aiAvatar, new ArrayList<>())
			);
	
	
	public static ArrayList<String> getAbilities(String confFile){
		ArrayList<String> result = new ArrayList<>();
		
		try {
			result.addAll(map.get(confFile));
		} catch (NullPointerException e) {
			System.err.println("Abilities undefined for "+confFile);
		}
		
		return result;
	}
		
}

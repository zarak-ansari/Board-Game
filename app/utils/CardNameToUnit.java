package utils;

import java.util.Map;

public class CardNameToUnit {
	
	//Maps name of the card to unit's config file for summoning
	public final static Map<String, String> map = Map.ofEntries(
			Map.entry("Silverguard Knight", StaticConfFiles.u_silverguard_knight),
			Map.entry("Azure Herald", StaticConfFiles.u_azure_herald),
			Map.entry("Ironcliff Guardian", StaticConfFiles.u_ironcliff_guardian),
			Map.entry("Azurite Lion", StaticConfFiles.u_azurite_lion),
			Map.entry("Hailstone Golem", StaticConfFiles.u_hailstone_golem),
			Map.entry("Fire Spitter", StaticConfFiles.u_fire_spitter),
			Map.entry("Comodo Charger", StaticConfFiles.u_comodo_charger),
			Map.entry("Pureblade Enforcer", StaticConfFiles.u_pureblade_enforcer),
			Map.entry("Rock Pulveriser", StaticConfFiles.u_rock_pulveriser),
			Map.entry("Bloodshard Golem", StaticConfFiles.u_bloodshard_golem),
			Map.entry("Blaze Hound", StaticConfFiles.u_blaze_hound),
			Map.entry("WindShrike", StaticConfFiles.u_windshrike),
			Map.entry("Pyromancer", StaticConfFiles.u_pyromancer),
			Map.entry("Serpenti", StaticConfFiles.u_serpenti),
			Map.entry("Planar Scout", StaticConfFiles.u_planar_scout)
			);
}

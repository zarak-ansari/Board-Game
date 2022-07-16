package utils;

import java.util.Map;

public class CardNameToEffectAnimation {
	public final static Map<String, String> map = Map.ofEntries(
			Map.entry("Truestrike", StaticConfFiles.f1_inmolation),
			Map.entry("Sundrop Elixir", StaticConfFiles.f1_buff),
			Map.entry("Staff of Y'Kir'", StaticConfFiles.f1_buff),
			Map.entry("Entropic Decay", StaticConfFiles.f1_martyrdom)
			);

}

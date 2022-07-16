package structures.basic;

import java.util.ArrayList;

public class Deck {
	
	ArrayList<Card> cards;
	
	public Deck(Player p, ArrayList<Card> cards) {
		p.setDeck(this);
		this.cards = cards;
	}
	
	
	public Card drawCard() {
		if (cards.size() > 0) {
			Card card = cards.get(0);
			cards.remove(0);
			return card;
		}
		return null;
	}
	
	
	public int getDeckSize() {
		return cards.size();
	}
	
}

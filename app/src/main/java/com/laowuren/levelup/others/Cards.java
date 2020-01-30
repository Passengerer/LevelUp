package com.laowuren.levelup.others;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Cards {

	private List<Card> cards;
	
	public Cards() {
		cards = new ArrayList<Card>();
	}
	
	public Cards(Cards c1, Cards c2) {
		cards = new ArrayList<Card>();
		cards.addAll(c1.cards);
		cards.addAll(c1.cards.size(), c2.cards);
	}

	/**
	 * 洗牌
	 */
	public void shuffle() {
		Cards newCards = new Cards();
		Random ran = new Random();
		int index;
		int amount = cards.size();
		for (int i = 0; i < amount; ++i) {
			index = ran.nextInt(cards.size());
			newCards.cards.add(cards.get(index));
			cards.remove(index);
		}
		cards = newCards.cards;
	}
	
	public List<Card> getCards(){
		return cards;
	}
	
	public boolean add(Card card) {
		return cards.add(card);
	}
	
	public boolean remove(Card card) {
		return cards.remove(card);
	}
	
	public Card get(int index) {
		return cards.get(index);
	}

	public int size(){
		return cards.size();
	}
	
	public void printAll() {
		for (Card c : cards) {
			System.out.println(c);
		}
	}

}

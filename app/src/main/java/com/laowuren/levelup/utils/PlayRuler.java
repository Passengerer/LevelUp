package com.laowuren.levelup.utils;

import android.os.Build;

import com.laowuren.levelup.others.Card;
import com.laowuren.levelup.others.CodeComparator;
import com.laowuren.levelup.others.Rank;
import com.laowuren.levelup.others.Suit;

import java.util.ArrayList;

public class PlayRuler {
	
	private Card zhu;
	private CodeComparator com;
	
	public PlayRuler() {}
	
	public PlayRuler(Card zhu) {
		this.zhu = zhu;
	}

	public void setCodeComparator(CodeComparator com){
		this.com = com;
	}
	
	public void setZhu(Card card) {
		this.zhu = card;
	}
	
	public boolean checkSuit(ArrayList<Byte> cards) {
		if (Build.VERSION.SDK_INT >= 24)
			cards.sort(com);
		Suit zhuSuit = zhu.getSuit();
		Suit suit = CodeUtil.getCardFromCode(cards.get(0)).getSuit();
		Rank rank = CodeUtil.getCardFromCode(cards.get(0)).getRank();
		if (suit == null || rank == Rank.Deuce || rank == zhu.getRank()) {				
			if (zhuSuit == null) {		// 无小主
				ArrayList<Byte> zhuCards = CardsParser.getZhu(cards, zhu.getRank());
				if (cards.size() == zhuCards.size())
					return true;
				else return false;
			}else {						// 有小主
				ArrayList<Byte> zhuCards = CardsParser.getZhu(cards, zhu.getRank());
				ArrayList<Byte> xiaozhuCards = null;
				switch (zhu.getSuit()) {
				case Heart:
					xiaozhuCards = CardsParser.getHeart(cards, zhu.getRank());
					break;
				case Club:
					xiaozhuCards = CardsParser.getClub(cards, zhu.getRank());
					break;
				case Diamond:
					xiaozhuCards = CardsParser.getDiamond(cards, zhu.getRank());
					break;
				case Spade:
					xiaozhuCards = CardsParser.getSpade(cards, zhu.getRank());
					break;
				}
				if (zhuCards.size() + xiaozhuCards.size() == cards.size())
					return true;
				else return false;
			}
		}else {
			if (suit == Suit.Heart) {
				ArrayList<Byte> heartCards = CardsParser.getHeart(cards, zhu.getRank());
				if (cards.size() == heartCards.size())
					return true;
				else return false;
			}
			if (suit == Suit.Club) {
				ArrayList<Byte> clubCards = CardsParser.getClub(cards, zhu.getRank());
				if (cards.size() == clubCards.size())
					return true;
				else return false;
			}
			if (suit == Suit.Diamond) {
				ArrayList<Byte> diamondCards = CardsParser.getDiamond(cards, zhu.getRank());
				if (cards.size() == diamondCards.size())
					return true;
				else return false;
			}
			if (suit == Suit.Spade) {
				ArrayList<Byte> spadeCards = CardsParser.getSpade(cards, zhu.getRank());
				if (cards.size() == spadeCards.size())
					return true;
				else return false;
			}
			return false;
		}
	}

}

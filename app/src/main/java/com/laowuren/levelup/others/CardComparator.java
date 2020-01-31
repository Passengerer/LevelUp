package com.laowuren.levelup.others;

import java.util.Comparator;

public class CardComparator implements Comparator<Card> {
	
	private Suit zhu;
	private Rank rank;
	
	public void setZhu(Card zhuCard) {
		this.rank = zhuCard.getRank();
		this.zhu = zhuCard.getSuit();
	}

	@Override
	public int compare(Card c1, Card c2) {
		
		// 其中一个是Joker
		if (c1.getSuit() == null || c2.getSuit() == null)
			return c1.getRank().compareTo(c2.getRank());
		
		// 其中一个是当前等级
		if (c1.getRank() == rank || c2.getRank() == rank) {
			// 两个都是
			if (c1.getRank() == rank && c2.getRank() == rank)
				// 其中一个是正的
				if (c1.getSuit() == zhu || c2.getSuit() == zhu){
					// 两个都是
					if (c1.getSuit() == zhu && c2.getSuit() == zhu)
						return 0;
					else return c1.getSuit() == zhu ? 1 : -1;
				}// 两个都不是
				else return c1.getSuit().compareTo(c2.getSuit());
			else
				return c1.getRank() == rank ? 1 : -1;
		}
		
		// 其中一个是2
		if (c1.getRank() == Rank.Deuce || c2.getRank() == Rank.Deuce) {
			// 两个都是2
			if (c1.getRank() == Rank.Deuce && c2.getRank() == Rank.Deuce)
				// 其中一个2是正2
				if (c1.getSuit() == zhu || c2.getSuit() == zhu){
					// 两个2都是
					if (c1.getSuit() == zhu && c2.getSuit() == zhu)
						return 0;
					else return c1.getSuit() == zhu ? 1 : -1;
				}// 两个2都不是
				else return c1.getSuit().compareTo(c2.getSuit());
			else
				return c1.getRank().compareTo(c2.getRank());
		}

		if (c1.getSuit() != c2.getSuit())
			if (c1.getSuit() == zhu || c2.getSuit() == zhu)
				return c1.getSuit() == zhu ? 1 : -1;
			else return c1.getSuit().compareTo(c2.getSuit());
		else
			return c1.getRank().compareTo(c2.getRank());
		
	}

}

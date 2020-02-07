package com.laowuren.levelup.utils;

import com.laowuren.levelup.others.Card;
import com.laowuren.levelup.others.Rank;
import com.laowuren.levelup.others.Suit;

import java.util.ArrayList;


public class CardsParser {
	
	public static ArrayList<Byte> getDan(ArrayList<Byte> cards){
		ArrayList<Byte> dan = new ArrayList<>();
		for (byte b : cards) {
			if (dan.contains(b)) {
				dan.remove(b);
			}else {
				dan.add(b);
			}
		}
		return dan;
	}
	
	public static ArrayList<Byte> getDui(ArrayList<Byte> cards){
		ArrayList<Byte> dan = new ArrayList<>();
		ArrayList<Byte> dui = new ArrayList<>();
		for (byte b : cards) {
			if (dan.contains(b)) {
				dui.add(b);
			}else {
				dan.add(b);
			}
		}
		return dui;
	}
	
	public static ArrayList<Byte> getHeart(ArrayList<Byte> cards, Rank level) {
		ArrayList<Byte> heart = new ArrayList<>();
		Card card;
		Rank rank;
		for (byte b : cards) {
			card = CodeUtil.getCardFromCode(b);
			rank = card.getRank();
			if (card.getSuit() == Suit.Heart && rank != Rank.Deuce &&
					rank != Rank.Joker_black && rank != Rank.Joker_red && 
					rank != level) {
				heart.add(b);
			}
		}
		return heart;
	}
	
	public static ArrayList<Byte> getClub(ArrayList<Byte> cards, Rank level) {
		ArrayList<Byte> club = new ArrayList<>();
		Card card;
		Rank rank;
		for (byte b : cards) {
			card = CodeUtil.getCardFromCode(b);
			rank = card.getRank();
			if (card.getSuit() == Suit.Club && rank != Rank.Deuce &&
					rank != Rank.Joker_black && rank != Rank.Joker_red && 
					rank != level) {
				club.add(b);
			}
		}
		return club;
	}
	
	public static ArrayList<Byte> getDiamond(ArrayList<Byte> cards, Rank level) {
		ArrayList<Byte> diamond = new ArrayList<>();
		Card card;
		Rank rank;
		for (byte b : cards) {
			card = CodeUtil.getCardFromCode(b);
			rank = card.getRank();
			if (card.getSuit() == Suit.Diamond && rank != Rank.Deuce &&
					rank != Rank.Joker_black && rank != Rank.Joker_red && 
					rank != level) {
				diamond.add(b);
			}
		}
		return diamond;
	}
	
	public static ArrayList<Byte> getSpade(ArrayList<Byte> cards, Rank level) {
		ArrayList<Byte> spade = new ArrayList<>();
		Card card;
		Rank rank;
		for (byte b : cards) {
			card = CodeUtil.getCardFromCode(b);
			rank = card.getRank();
			if (card.getSuit() == Suit.Spade && rank != Rank.Deuce &&
					rank != Rank.Joker_black && rank != Rank.Joker_red && 
					rank != level) {
				spade.add(b);
			}
		}
		return spade;
	}
	
	public static ArrayList<Byte> getZhu(ArrayList<Byte> cards, Rank level) {
		ArrayList<Byte> zhu = new ArrayList<>();
		Card card;
		Rank rank;
		for (byte b : cards) {
			card = CodeUtil.getCardFromCode(b);
			rank = card.getRank();
			if (rank == Rank.Deuce || rank == Rank.Joker_black || 
					rank == Rank.Joker_red || rank == level) {
				zhu.add(b);
			}
		}
		return zhu;
	}

}

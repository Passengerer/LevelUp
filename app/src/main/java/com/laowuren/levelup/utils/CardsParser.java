package com.laowuren.levelup.utils;

import android.os.Build;
import android.util.Log;

import com.laowuren.levelup.others.Card;
import com.laowuren.levelup.others.Rank;
import com.laowuren.levelup.others.Suit;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class CardsParser {
	
	public static ArrayList<Byte> getDan(ArrayList<Byte> cards){
		ArrayList<Byte> dan = new ArrayList<>();
		ArrayList<Byte> dui = getDui(cards);
		dan.addAll(cards);
		dan.removeAll(dui);
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
	
	public static LinkedHashMap<Byte, Integer> getLiandui(ArrayList<Byte> cards, Card zhu){
		Log.d("CardsParser", "getLiandui");
		ArrayList<Byte> dui = getDui(cards);
		LinkedHashMap<Byte, Integer> ret = new LinkedHashMap<>();
		if (dui == null || dui.size() < 2) {
			return ret;
		}
		byte now;
		byte next;
		int length = 1;
		byte start = -1;
		for (int i = 0; i < dui.size() - 1; ++i) {
			now = dui.get(i);
			next = dui.get(i + 1);
			if (checkNext(now, next, zhu)) {
				++length;
				if (ret.containsKey(start)) {
					if (Build.VERSION.SDK_INT >= 24	)
						ret.replace(start, length);
				}else {
					start = now;
					ret.put(start, length);
				}
			}else {
				start = now;
				length = 1;
			}
		}
		Log.d("liandui", "" + ret.size());
		return ret;
	}
	/**
	 * code1应比code2大
	 * @param code1
	 * @param code2
	 * @param zhu
	 * @return 返回code2是否比code1小1级
	 */
	private static boolean checkNext(Byte code1, Byte code2, Card zhu) {
		Suit zhuSuit = zhu.getSuit();
		Rank zhuRank = zhu.getRank();
		if (CodeUtil.getCardFromCode(code1).getRank() == Rank.Joker_red) {
			// code1是大王，下一级是小王
			return CodeUtil.getCardFromCode(code2).getRank() == Rank.Joker_black;
		}else if (CodeUtil.getCardFromCode(code1).getRank() == Rank.Joker_black) {
			// code1是小王
			if (zhuSuit == null) {
				// 没有花色，是当前等级即可
				return CodeUtil.getCardFromCode(code2).getRank() == zhuRank;
			}else {
				// 有花色，下一级是正
				Card card2 = CodeUtil.getCardFromCode(code2);
				return card2.getSuit() == zhuSuit && card2.getRank() == zhuRank;
			}
		}else if (CodeUtil.getCardFromCode(code1).getRank() == zhuRank) {
			// code1是当前等级
			if (zhuSuit == null) {
				// 没有花色，是2即可
				return CodeUtil.getCardFromCode(code2).getRank() == Rank.Deuce;
			}else {
				Card card2 = CodeUtil.getCardFromCode(code2);
				// 有花色，如果code1是正，则下一级是副
				if (CodeUtil.getCardFromCode(code1).getSuit() == zhuSuit) {
					return card2.getSuit() != zhuSuit && card2.getRank() == zhuRank;
				}else {
					// code1是副，下一级是正2
					return card2.getSuit() == zhuSuit && card2.getRank() == Rank.Deuce;
				}
			}
		}else if (CodeUtil.getCardFromCode(code1).getRank() == Rank.Deuce) {
			// code1是2
			if (zhuSuit == null) {
				// 没有花色，没有下一级
				return false;
			}else {
				// 有花色，下一级是主花色的A(如果A不是当前等级)
				Card card2 = CodeUtil.getCardFromCode(code2);
				// 对于已排序的code1和code2，code1 > code2，故code2不可能是当前等级
				if (zhuRank != Rank.Ace) {
					// A不是当前等级
					return card2.getSuit() == zhuSuit && card2.getRank() == Rank.Ace;
				}else {
					// A是当前等级，下一级是K
					return card2.getSuit() == zhuSuit && card2.getRank() == Rank.King;
				}
			}
		}else {
			// code1不是王、2或当前等级，下一级与code1同花色，等级-1(如果-1后不是当前等级)
			Card card1 = CodeUtil.getCardFromCode(code1);
			Card card2 = CodeUtil.getCardFromCode(code2);
			Suit card1Suit = card1.getSuit();
			Rank card1Rank = card1.getRank();
			if (card2.getSuit() != card1Suit) {
				return false;
			}
			if (zhuRank.ordinal() != card1Rank.ordinal() - 1) {
				return card2.getRank().ordinal() == card1Rank.ordinal() - 1;
			}else {
				return card2.getRank().ordinal() == card1Rank.ordinal() - 2;
			}
		}
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

    public static ArrayList<Byte> getAllZhu(ArrayList<Byte> cards, Card zhu){
        ArrayList<Byte> ret = getZhu(cards, zhu.getRank());
        if (zhu.getSuit() != null){
            ArrayList<Byte> xiaoZhu = null;
            switch (zhu.getSuit()){
                case Heart:
                    xiaoZhu = getHeart(cards, zhu.getRank());
                    break;
                case Club:
                    xiaoZhu = getClub(cards, zhu.getRank());
                    break;
                case Diamond:
                    xiaoZhu = getDiamond(cards, zhu.getRank());
                    break;
                case Spade:
                    xiaoZhu = getSpade(cards, zhu.getRank());
                    break;
            }
            ret.addAll(xiaoZhu);
        }
        return ret;
    }

}

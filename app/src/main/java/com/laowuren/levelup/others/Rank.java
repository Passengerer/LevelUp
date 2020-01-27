package com.laowuren.levelup.others;

public enum Rank {

	Three,	// 0
	Four,
	Five,
	Six,
	Seven,
	Eight,
	Nine,
	Ten,
	Jack,
	Queen,
	King,
	Ace,
	Deuce,
	Joker_black,	// 13
	Joker_red;	// 14
	
	@Override
	public String toString(){
		switch(this) {
		
		case Three: return "3";
		case Four: return "4";
		case Five: return "5";
		case Six: return "6";
		case Seven: return "7";
		case Eight: return "8";
		case Nine: return "9";
		case Ten: return "10";
		case Jack: return "J";
		case Queen: return "Q";
		case King: return "K";
		case Ace: return "A";
		case Deuce: return "2";
		case Joker_black: return "Joker_black";
		case Joker_red: return "Joker_red";
		default: return null;
		
		}
	}
	
}

package com.laowuren.levelup.others;

public enum Suit {

	Heart,		// 红心
	Club,		// 梅花
	Diamond,	// 方块
    Spade;		// 黑桃
    
	@Override
	public String toString(){
		
		switch(this) {
		
		case Club: return "Club";
		case Diamond: return "Diamond";
		case Heart: return "Heart";
		case Spade: return "Spade";
		default: return "";
		
		}

	}
	
}

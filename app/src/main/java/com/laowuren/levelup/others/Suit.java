package com.laowuren.levelup.others;

public enum Suit {
	
	Diamond,	// 方块
	Club,		// 梅花
    Heart,		// 红心
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

package com.laowuren.levelup.others;

import java.io.Serializable;

public class MyMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public final static int TEXT = 1;
	public final static int CARD = 2;
	public final static int SCORE = 3;
	
	private int what;
	private String text;
	private Card card;
	private int score;
	
	public MyMessage(int what, String text, Card card, int score) {
		this.what = what;
		this.text = text;
		this.card = card;
		this.score = score;
	}

	public int getWhat() {
		return what;
	}

	public void setWhat(int what) {
		this.what = what;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

}

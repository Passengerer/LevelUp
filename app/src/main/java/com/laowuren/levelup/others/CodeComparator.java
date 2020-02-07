package com.laowuren.levelup.others;

import com.laowuren.levelup.utils.CodeUtil;

import java.util.Comparator;

public class CodeComparator implements Comparator<Byte> {
	
	private CardComparator com;
	
	public CodeComparator() {}
	
	public void setCardComparator(CardComparator com) {
		this.com = com;
	}

	@Override
	public int compare(Byte o1, Byte o2) {
		Card c1 = CodeUtil.getCardFromCode(o1);
		Card c2 = CodeUtil.getCardFromCode(o2);
		return com.compare(c2, c1);
	}

}

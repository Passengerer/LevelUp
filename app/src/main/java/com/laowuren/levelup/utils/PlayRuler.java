package com.laowuren.levelup.utils;

import android.os.Build;
import android.util.Log;

import com.laowuren.levelup.others.Card;
import com.laowuren.levelup.others.CodeComparator;
import com.laowuren.levelup.others.Rank;
import com.laowuren.levelup.others.Suit;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static com.laowuren.levelup.utils.CardsParser.getLiandui;

public class PlayRuler {
	
	public final static int DAN = 1;
	public final static int DUI = 2;
	public final static int LIANDUI = 3;
	public final static int SHUAI = 4;
	
	private Card zhu;
	
	private CodeComparator com;
	
	public PlayRuler() {}
	
	public PlayRuler(Card zhu) {
		this.zhu = zhu;
	}
	
	public void setCom(CodeComparator com) {
		this.com = com;
	}
	
	public void setZhu(Card card) {
		this.zhu = card;
	}
	
	/**
	 * 
	 * @param card
	 * @return 如果是主，返回null；不是则返回其花色
	 */
	public Suit getSuit(Card card) {
		Log.d("PlayRuler", "getSuit");
		if (card.getSuit() == null) {
            Log.d("getSuit", "null");
			return null;
		}
		if (card.getRank() == zhu.getRank() || card.getRank() == Rank.Deuce) {
            Log.d("getSuit", "null");
			return null;
		}
		if (zhu.getSuit() != null && card.getSuit() == zhu.getSuit()) {
            Log.d("getSuit", "null");
			return null;
		}else {
			Log.d("getSuit", card.getSuit().toString());
			return card.getSuit();
		}
	}

    public boolean checkRules(ArrayList<Byte> firstCards, ArrayList<Byte> handCards, ArrayList<Byte> playCards){
        if (firstCards.isEmpty()){
            Log.d("PlayRuler-checkRules", "firstCards empty");
            return true;
        }
        if (Build.VERSION.SDK_INT >= 24){
            handCards.sort(com);
            playCards.sort(com);
        }
        // 获取第一个出牌的花色、单、对、连对数量
        Suit firstSuit = getSuit(CodeUtil.getCardFromCode(firstCards.get(0)));
        ArrayList<Byte> firstDui = CardsParser.getDui(firstCards);
        LinkedHashMap<Byte, Integer> firstLiandui = getLiandui(firstCards, zhu);

        // 获取自己相应花色的牌
        ArrayList<Byte> suitCards = null;
        if (firstSuit == null) {
            Log.d("PlayRuler-checkRules", "first suit == zhu");
            suitCards = CardsParser.getAllZhu(handCards, zhu);
        }else {
            Log.d("PlayRuler-checkRules", "first suit != zhu");
            switch (firstSuit){
                case Heart: suitCards = CardsParser.getHeart(handCards, zhu.getRank());
                    break;
                case Club: suitCards = CardsParser.getClub(handCards, zhu.getRank());
                    break;
                case Diamond: suitCards = CardsParser.getDiamond(handCards, zhu.getRank());
                    break;
                case Spade: suitCards = CardsParser.getSpade(handCards, zhu.getRank());
                    break;
            }
        }
        // 如果打主牌
        if (firstSuit == null){
            // 如果数量不够，须打出所有主
            Log.d("PlayRuler-checkRules", "1");
            if (suitCards.size() <= firstCards.size()){
                Log.d("PlayRuler-checkRules", "2");
                ArrayList<Byte> playZhu = CardsParser.getAllZhu(playCards, zhu);
                if (playZhu.size() < suitCards.size()){
                    Log.d("PlayRuler-checkRules", "3");
                    return false;
                }else {
                    Log.d("PlayRuler-checkRules", "4");
                    return true;
                }
            }else{
                Log.d("PlayRuler-checkRules", "5");
                // 数量够
                // 只能打这一门花色
                if (getSuit(CodeUtil.getCardFromCode(playCards.get(0))) != null || !checkSuit(playCards)){
                    Log.d("PlayRuler-checkRules", "6");
                    return false;
                }
                // 第一个打了连对
                if (!firstLiandui.isEmpty()){
                    Log.d("PlayRuler-checkRules", "7");
                    // 获取第一个连对的第一张牌和长度
                    byte firstLianduiFirstCard = firstLiandui.entrySet().iterator().next().getKey();
                    int firstLianduiLength = firstLiandui.entrySet().iterator().next().getValue();
                    LinkedHashMap<Byte, Integer> selfLiandui = CardsParser.getLiandui(suitCards, zhu);
                    // 自己有连对
                    if (!selfLiandui.isEmpty()){
                        Log.d("PlayRuler-checkRules", "8");
                        boolean has = false;
                        for (int length : selfLiandui.values()){
                            if (length >= firstLianduiLength) {
                                has = true;
                                break;
                            }
                        }
                        LinkedHashMap<Byte, Integer> playLiandui = CardsParser.getLiandui(playCards, zhu);
                        // 有比第一个出的拖拉机长的连对，则playCards中也应有与第一个等长的拖拉机
                        if (has){
                            Log.d("PlayRuler-checkRules", "9");
                            boolean hasPlayed = false;
                            byte playLianduiFirstCard = -1;
                            for (byte b : playLiandui.keySet()){
                                if (playLiandui.get(b) >= firstLianduiLength) {
                                    playLianduiFirstCard = b;
                                    hasPlayed = true;
                                    break;
                                }
                            }
                            if (!hasPlayed){
                                Log.d("PlayRuler-checkRules", "10");
                                return false;
                            }else{
                                // 有而且出了，更新未对应的牌
                                Log.d("PlayRuler-checkRules", "11");
                                ArrayList<Byte> updateSuitCards = new ArrayList<>();
                                ArrayList<Byte> updateFirstCards = new ArrayList<>();
                                ArrayList<Byte> updatePlayCards = new ArrayList<>();
                                ArrayList<Byte> removeFirstCards = new ArrayList<>();
                                ArrayList<Byte> removePlayCards = new ArrayList<>();
                                updateSuitCards.addAll(suitCards);
                                updateFirstCards.addAll(firstCards);
                                updatePlayCards.addAll(playCards);
                                // 找到连对中第一张牌位置，从打出的牌中移除后面length * 2张牌
                                int removeIndex = playCards.indexOf(playLianduiFirstCard);
                                for (int i = 0; i < firstLianduiLength; ++i){
                                    removePlayCards.add(playCards.get(removeIndex + i * 2));
                                    removePlayCards.add(playCards.get(removeIndex + i * 2));
                                }
                                int removeFirstIndex = firstCards.indexOf(firstLianduiFirstCard);
                                for (int i = 0; i < firstLianduiLength; ++i){
                                    removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                    removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                }
                                removeCards(updateSuitCards, removePlayCards);
                                removeCards(updatePlayCards, removePlayCards);
                                removeCards(updateFirstCards, removeFirstCards);
                                return checkRules(updateFirstCards, updateSuitCards, updatePlayCards);
                            }
                        }else{
                            // 没有跟第一个出的一样长的连对，也应当出
                            if (playLiandui.isEmpty()){
                                Log.d("PlayRuler-checkRules", "12");
                                return false;
                            }
                            ArrayList<Byte> updateSuitCards = new ArrayList<>();
                            ArrayList<Byte> updateFirstCards = new ArrayList<>();
                            ArrayList<Byte> updatePlayCards = new ArrayList<>();
                            ArrayList<Byte> removeFirstCards = new ArrayList<>();
                            ArrayList<Byte> removePlayCards = new ArrayList<>();
                            updateSuitCards.addAll(suitCards);
                            updateFirstCards.addAll(firstCards);
                            updatePlayCards.addAll(playCards);
                            byte playLianduiFirstCard = playLiandui.entrySet().iterator().next().getKey();
                            int playLianduiLength = playLiandui.entrySet().iterator().next().getValue();
                            int removePlayIndex = playCards.indexOf(playLianduiFirstCard);
                            for (int i = 0; i < playLianduiLength; ++i){
                                removePlayCards.add(playCards.get(removePlayIndex + i * 2));
                                removePlayCards.add(playCards.get(removePlayIndex + i * 2));
                            }
                            int removeFirstIndex = firstCards.indexOf(firstLianduiFirstCard);
                            for (int i = 0; i < playLianduiLength; ++i){
                                removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                            }
                            removeCards(updateSuitCards, removePlayCards);
                            removeCards(updatePlayCards, removePlayCards);
                            removeCards(updateFirstCards, removeFirstCards);
                            return checkRules(updateFirstCards, updateSuitCards, updatePlayCards);
                        }
                    }else{
                        // 自己没有连对
                        Log.d("PlayRuler-checkRules", "12");
                        ArrayList<Byte> selfDui = CardsParser.getDui(suitCards);
                        // 自己有对
                        if (!selfDui.isEmpty()){
                            Log.d("PlayRuler-checkRules", "13");
                            ArrayList<Byte> playDui = CardsParser.getDui(playCards);
                            // 自己对子够，则出的对子数应等于第一家出的连对的对子数
                            if (selfDui.size() >= firstLianduiLength){
                                Log.d("PlayRuler-checkRules", "14");
                                if (playDui.size() < firstLianduiFirstCard){
                                    Log.d("PlayRuler-checkRules", "15");
                                    return false;
                                }else{
                                    // 出了足够对子数，移除已对应的牌
                                    Log.d("PlayRuler-checkRules", "16");
                                    ArrayList<Byte> updateSuitCards = new ArrayList<>();
                                    ArrayList<Byte> updateFirstCards = new ArrayList<>();
                                    ArrayList<Byte> updatePlayCards = new ArrayList<>();
                                    ArrayList<Byte> removeFirstCards = new ArrayList<>();
                                    ArrayList<Byte> removePlayCards = new ArrayList<>();
                                    updateSuitCards.addAll(suitCards);
                                    updateFirstCards.addAll(firstCards);
                                    updatePlayCards.addAll(playCards);
                                    for (int i = 0; i < firstLianduiLength; ++i){
                                        removePlayCards.add(playDui.get(i));
                                        removePlayCards.add(playDui.get(i));
                                    }
                                    int removeFirstIndex = firstCards.indexOf(firstLianduiFirstCard);
                                    for (int i = 0; i < firstLianduiLength; ++i){
                                        removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                        removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                    }
                                    removeCards(updateSuitCards, removePlayCards);
                                    removeCards(updatePlayCards, removePlayCards);
                                    removeCards(updateFirstCards, removeFirstCards);
                                    return checkRules(updateFirstCards, updateSuitCards, updatePlayCards);
                                }
                            }else{
                                // 自己对子不够，应出所有对子
                                Log.d("PlayRuler-checkRules", "17");
                                if (playDui.size() < selfDui.size()){
                                    Log.d("PlayRuler-checkRules", "18");
                                    return false;
                                }else{
                                    Log.d("PlayRuler-checkRules", "19");
                                    ArrayList<Byte> updateSuitCards = new ArrayList<>();
                                    ArrayList<Byte> updateFirstCards = new ArrayList<>();
                                    ArrayList<Byte> updatePlayCards = new ArrayList<>();
                                    ArrayList<Byte> removeFirstCards = new ArrayList<>();
                                    ArrayList<Byte> removePlayCards = new ArrayList<>();
                                    updateSuitCards.addAll(suitCards);
                                    updateFirstCards.addAll(firstCards);
                                    updatePlayCards.addAll(playCards);
                                    for (int i = 0; i < playDui.size(); ++i){
                                        removePlayCards.add(playDui.get(i));
                                        removePlayCards.add(playDui.get(i));
                                    }
                                    int removeFirstIndex = firstCards.indexOf(firstLianduiFirstCard);
                                    for (int i = 0; i < playDui.size(); ++i){
                                        removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                        removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                    }
                                    removeCards(updateSuitCards, removePlayCards);
                                    removeCards(updatePlayCards, removePlayCards);
                                    removeCards(updateFirstCards, removeFirstCards);
                                    return checkRules(updateFirstCards, updateSuitCards, updatePlayCards);
                                }
                            }
                        }else{
                            // 自己没有对
                            Log.d("PlayRuler-checkRules", "20");
                            ArrayList<Byte> updateSuitCards = new ArrayList<>();
                            ArrayList<Byte> updateFirstCards = new ArrayList<>();
                            ArrayList<Byte> updatePlayCards = new ArrayList<>();
                            ArrayList<Byte> removeFirstCards = new ArrayList<>();
                            ArrayList<Byte> removePlayCards = new ArrayList<>();
                            updateSuitCards.addAll(suitCards);
                            updateFirstCards.addAll(firstCards);
                            updatePlayCards.addAll(playCards);
                            for (int i = 0; i < firstLianduiLength; ++i){
                                removePlayCards.add(suitCards.get(i * 2));
                                removePlayCards.add(suitCards.get(i * 2 + 1));
                            }
                            // 剐最大牌，必须出
                            for (byte b : removePlayCards){
                                if (!playCards.contains(b)){
                                    if (checkYingZhu(CodeUtil.getCardFromCode(b))) {
                                        Log.d("PlayRuler-checkRules", "20-2");
                                        return false;
                                    }
                                }
                            }
                            int removeFirstIndex = firstCards.indexOf(firstLianduiFirstCard);
                            for (int i = 0; i < firstLianduiLength; ++i){
                                removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                            }
                            removeCards(updateSuitCards, removePlayCards);
                            removeCards(updatePlayCards, removePlayCards);
                            removeCards(updateFirstCards, removeFirstCards);
                            return checkRules(updateFirstCards, updateSuitCards, updatePlayCards);
                        }
                    }
                }
                // 第一个打了对子
                if (!firstDui.isEmpty()){
                    Log.d("PlayRuler-checkRules", "21");
                    ArrayList<Byte> selfDui = CardsParser.getDui(suitCards);
                    ArrayList<Byte> playDui = CardsParser.getDui(playCards);
                    // 手牌对子够，打的牌也应出足够对子
                    if (selfDui.size() >= firstDui.size()){
                        Log.d("PlayRuler-checkRules", "22");
                        if (playDui.size() < firstDui.size()){
                            Log.d("PlayRuler-checkRules", "23");
                            return false;
                        }else{
                            // 出了足够对子，剩下单牌随便出
                            Log.d("PlayRuler-checkRules", "24");
                            return true;
                        }
                    }else {
                        // 手牌对子不够，出所有对子
                        // 手牌有对子
                        Log.d("PlayRuler-checkRules", "25");
                        if (!selfDui.isEmpty()) {
                            if (playDui.size() < selfDui.size()) {
                                Log.d("PlayRuler-checkRules", "26");
                                return false;
                            } else {
                                Log.d("PlayRuler-checkRules", "27");
                                ArrayList<Byte> updateSuitCards = new ArrayList<>();
                                ArrayList<Byte> updateFirstCards = new ArrayList<>();
                                ArrayList<Byte> updatePlayCards = new ArrayList<>();
                                ArrayList<Byte> removeFirstCards = new ArrayList<>();
                                ArrayList<Byte> removePlayCards = new ArrayList<>();
                                updateSuitCards.addAll(suitCards);
                                updateFirstCards.addAll(firstCards);
                                updatePlayCards.addAll(playCards);
                                for (int i = 0; i < playDui.size(); ++i) {
                                    removePlayCards.add(playDui.get(i));
                                    removePlayCards.add(playDui.get(i));
                                }
                                for (int i = 0; i < playDui.size(); ++i) {
                                    removeFirstCards.add(firstDui.get(i));
                                    removeFirstCards.add(firstDui.get(i));
                                }
                                removeCards(updateSuitCards, removePlayCards);
                                removeCards(updatePlayCards, removePlayCards);
                                removeCards(updateFirstCards, removeFirstCards);
                                return checkRules(updateFirstCards, updateSuitCards, updatePlayCards);
                            }
                        } else {
                            // 手牌没有对子
                            Log.d("PlayRuler-checkRules", "27-2");
                            ArrayList<Byte> removePlayCards = new ArrayList<>();
                            for (int i = 0; i < firstDui.size(); ++i) {
                                removePlayCards.add(suitCards.get(i * 2));
                                removePlayCards.add(suitCards.get(i * 2 + 1));
                            }
                            // 剐最大牌，必须出
                            for (byte b : removePlayCards) {
                                if (!playCards.contains(b)) {
                                    Log.d("PlayRuler-checkRules", "27-3");
                                    if (checkYingZhu(CodeUtil.getCardFromCode(b))) {
                                        Log.d("PlayRuler-checkRules", "27-4");
                                        return false;
                                    }else{
                                        Log.d("PlayRuler-checkRules", "27-5");
                                        return true;
                                    }
                                }
                            }
                            Log.d("PlayRuler-checkRules", "27-6");
                            return true;
                        }
                    }
                }
                // 第一个打了单牌，随便出该门单牌即可
                Log.d("PlayRuler-checkRules", "28");
                return true;
            }
        }else {
            // 打副牌
            Log.d("PlayRuler-checkRules", "29");
            // 数量够
            if (suitCards.size() >= firstCards.size()) {
                // 只能打这一门花色
                if (getSuit(CodeUtil.getCardFromCode(playCards.get(0))) != firstSuit || !checkSuit(playCards)) {
                    Log.d("PlayRuler-checkRules", "30");
                    return false;
                }
                // 第一个打了连对
                if (!firstLiandui.isEmpty()) {
                    Log.d("PlayRuler-checkRules", "31");
                    // 获取第一个连对的第一张牌和长度
                    byte firstLianduiFirstCard = firstLiandui.entrySet().iterator().next().getKey();
                    int firstLianduiLength = firstLiandui.entrySet().iterator().next().getValue();
                    LinkedHashMap<Byte, Integer> selfLiandui = CardsParser.getLiandui(suitCards, zhu);
                    // 自己有连对
                    if (!selfLiandui.isEmpty()) {
                        Log.d("PlayRuler-checkRules", "32");
                        boolean has = false;
                        for (int length : selfLiandui.values()) {
                            if (length >= firstLianduiLength) {
                                has = true;
                                break;
                            }
                        }
                        LinkedHashMap<Byte, Integer> playLiandui = CardsParser.getLiandui(playCards, zhu);
                        // 有比第一个出的拖拉机长的连对，则playCards中也应有与第一个等长的拖拉机
                        if (has) {
                            Log.d("PlayRuler-checkRules", "33");
                            boolean hasPlayed = false;
                            byte playLianduiFirstCard = -1;
                            for (byte b : playLiandui.keySet()) {
                                if (playLiandui.get(b) >= firstLianduiLength) {
                                    playLianduiFirstCard = b;
                                    hasPlayed = true;
                                    break;
                                }
                            }
                            if (!hasPlayed) {
                                Log.d("PlayRuler-checkRules", "34");
                                return false;
                            } else {
                                // 有而且出了，更新未对应的牌
                                Log.d("PlayRuler-checkRules", "35");
                                ArrayList<Byte> updateSuitCards = new ArrayList<>();
                                ArrayList<Byte> updateFirstCards = new ArrayList<>();
                                ArrayList<Byte> updatePlayCards = new ArrayList<>();
                                ArrayList<Byte> removeFirstCards = new ArrayList<>();
                                ArrayList<Byte> removePlayCards = new ArrayList<>();
                                updateSuitCards.addAll(suitCards);
                                updateFirstCards.addAll(firstCards);
                                updatePlayCards.addAll(playCards);
                                // 找到连对中第一张牌位置，从打出的牌中移除后面length * 2张牌
                                int removeIndex = playCards.indexOf(playLianduiFirstCard);
                                for (int i = 0; i < firstLianduiLength; ++i) {
                                    removePlayCards.add(playCards.get(removeIndex + i * 2));
                                    removePlayCards.add(playCards.get(removeIndex + i * 2));
                                }
                                int removeFirstIndex = firstCards.indexOf(firstLianduiFirstCard);
                                for (int i = 0; i < firstLianduiLength; ++i) {
                                    removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                    removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                }
                                removeCards(updateSuitCards, removePlayCards);
                                removeCards(updatePlayCards, removePlayCards);
                                removeCards(updateFirstCards, removeFirstCards);
                                return checkRules(updateFirstCards, updateSuitCards, updatePlayCards);
                            }
                        } else {
                            // 没有跟第一个出的一样长的连对，也应当出
                            if (playLiandui.isEmpty()) {
                                Log.d("PlayRuler-checkRules", "36");
                                return false;
                            }
                            ArrayList<Byte> updateSuitCards = new ArrayList<>();
                            ArrayList<Byte> updateFirstCards = new ArrayList<>();
                            ArrayList<Byte> updatePlayCards = new ArrayList<>();
                            ArrayList<Byte> removeFirstCards = new ArrayList<>();
                            ArrayList<Byte> removePlayCards = new ArrayList<>();
                            updateSuitCards.addAll(suitCards);
                            updateFirstCards.addAll(firstCards);
                            updatePlayCards.addAll(playCards);
                            byte playLianduiFirstCard = playLiandui.entrySet().iterator().next().getKey();
                            int playLianduiLength = playLiandui.entrySet().iterator().next().getValue();
                            int removePlayIndex = playCards.indexOf(playLianduiFirstCard);
                            for (int i = 0; i < playLianduiLength; ++i) {
                                removePlayCards.add(playCards.get(removePlayIndex + i * 2));
                                removePlayCards.add(playCards.get(removePlayIndex + i * 2));
                            }
                            int removeFirstIndex = firstCards.indexOf(firstLianduiFirstCard);
                            for (int i = 0; i < playLianduiLength; ++i) {
                                removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                            }
                            removeCards(updateSuitCards, removePlayCards);
                            removeCards(updatePlayCards, removePlayCards);
                            removeCards(updateFirstCards, removeFirstCards);
                            return checkRules(updateFirstCards, updateSuitCards, updatePlayCards);
                        }
                    } else {
                        // 自己没有连对
                        Log.d("PlayRuler-checkRules", "37");
                        ArrayList<Byte> selfDui = CardsParser.getDui(suitCards);
                        // 自己有对
                        if (!selfDui.isEmpty()) {
                            Log.d("PlayRuler-checkRules", "38");
                            ArrayList<Byte> playDui = CardsParser.getDui(playCards);
                            // 自己对子够，则出的对子数应等于第一家出的连对的对子数
                            if (selfDui.size() >= firstLianduiLength) {
                                Log.d("PlayRuler-checkRules", "39");
                                if (playDui.size() < firstLianduiFirstCard) {
                                    Log.d("PlayRuler-checkRules", "40");
                                    return false;
                                } else {
                                    // 出了足够对子数，移除已对应的牌
                                    Log.d("PlayRuler-checkRules", "41");
                                    ArrayList<Byte> updateSuitCards = new ArrayList<>();
                                    ArrayList<Byte> updateFirstCards = new ArrayList<>();
                                    ArrayList<Byte> updatePlayCards = new ArrayList<>();
                                    ArrayList<Byte> removeFirstCards = new ArrayList<>();
                                    ArrayList<Byte> removePlayCards = new ArrayList<>();
                                    updateSuitCards.addAll(suitCards);
                                    updateFirstCards.addAll(firstCards);
                                    updatePlayCards.addAll(playCards);
                                    for (int i = 0; i < firstLianduiLength; ++i) {
                                        removePlayCards.add(playDui.get(i));
                                        removePlayCards.add(playDui.get(i));
                                    }
                                    int removeFirstIndex = firstCards.indexOf(firstLianduiFirstCard);
                                    for (int i = 0; i < firstLianduiLength; ++i) {
                                        removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                        removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                    }
                                    removeCards(updateSuitCards, removePlayCards);
                                    removeCards(updatePlayCards, removePlayCards);
                                    removeCards(updateFirstCards, removeFirstCards);
                                    return checkRules(updateFirstCards, updateSuitCards, updatePlayCards);
                                }
                            } else {
                                // 自己对子不够，应出所有对子
                                Log.d("PlayRuler-checkRules", "42");
                                if (playDui.size() < selfDui.size()) {
                                    Log.d("PlayRuler-checkRules", "43");
                                    return false;
                                } else {
                                    Log.d("PlayRuler-checkRules", "44");
                                    ArrayList<Byte> updateSuitCards = new ArrayList<>();
                                    ArrayList<Byte> updateFirstCards = new ArrayList<>();
                                    ArrayList<Byte> updatePlayCards = new ArrayList<>();
                                    ArrayList<Byte> removeFirstCards = new ArrayList<>();
                                    ArrayList<Byte> removePlayCards = new ArrayList<>();
                                    updateSuitCards.addAll(suitCards);
                                    updateFirstCards.addAll(firstCards);
                                    updatePlayCards.addAll(playCards);
                                    for (int i = 0; i < playDui.size(); ++i) {
                                        removePlayCards.add(playDui.get(i));
                                        removePlayCards.add(playDui.get(i));
                                    }
                                    int removeFirstIndex = firstCards.indexOf(firstLianduiFirstCard);
                                    for (int i = 0; i < playDui.size(); ++i) {
                                        removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                        removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                    }
                                    removeCards(updateSuitCards, removePlayCards);
                                    removeCards(updatePlayCards, removePlayCards);
                                    removeCards(updateFirstCards, removeFirstCards);
                                    return checkRules(updateFirstCards, updateSuitCards, updatePlayCards);
                                }
                            }
                        } else {
                            // 自己没有对, 随便出
                            Log.d("PlayRuler-checkRules", "45");
                            return true;
                        }
                    }
                }
                // 第一个打了对子
                if (!firstDui.isEmpty()) {
                    Log.d("PlayRuler-checkRules", "46");
                    ArrayList<Byte> selfDui = CardsParser.getDui(suitCards);
                    ArrayList<Byte> playDui = CardsParser.getDui(playCards);
                    // 手牌对子够，打的牌也应出足够对子
                    if (selfDui.size() >= firstDui.size()) {
                        Log.d("PlayRuler-checkRules", "47");
                        if (playDui.size() < firstDui.size()) {
                            Log.d("PlayRuler-checkRules", "48");
                            return false;
                        } else {
                            // 出了足够对子，剩下随便出，花色已限制
                            Log.d("PlayRuler-checkRules", "49");
                            return true;
                        }
                    } else {
                        // 手牌对子不够，出所有对子
                        Log.d("PlayRuler-checkRules", "50");
                        // 手牌有对子
                        if (!selfDui.isEmpty()) {
                            if (playDui.size() < selfDui.size()) {
                                Log.d("PlayRuler-checkRules", "51");
                                return false;
                            } else {
                                Log.d("PlayRuler-checkRules", "52");
                                ArrayList<Byte> updateSuitCards = new ArrayList<>();
                                ArrayList<Byte> updateFirstCards = new ArrayList<>();
                                ArrayList<Byte> updatePlayCards = new ArrayList<>();
                                ArrayList<Byte> removeFirstCards = new ArrayList<>();
                                ArrayList<Byte> removePlayCards = new ArrayList<>();
                                updateSuitCards.addAll(suitCards);
                                updateFirstCards.addAll(firstCards);
                                updatePlayCards.addAll(playCards);
                                for (int i = 0; i < playDui.size(); ++i) {
                                    removePlayCards.add(playDui.get(i));
                                    removePlayCards.add(playDui.get(i));
                                }
                                for (int i = 0; i < playDui.size(); ++i) {
                                    removeFirstCards.add(firstDui.get(i));
                                    removeFirstCards.add(firstDui.get(i));
                                }
                                removeCards(updateSuitCards, removePlayCards);
                                removeCards(updatePlayCards, removePlayCards);
                                removeCards(updateFirstCards, removeFirstCards);
                                return checkRules(updateFirstCards, updateSuitCards, updatePlayCards);
                            }
                        } else {
                            // 手牌没有对子，随便出
                            return true;
                        }
                    }
                }
                // 第一个打了单牌，随便出该门单牌即可
                Log.d("PlayRuler-checkRules", "53");
                return true;
            }else {
                // 打副牌，数量不够
                // 第一家只出了一张牌，随便出
                if (firstCards.size() == 1){
                    Log.d("PlayRuler-checkRules", "54");
                    return true;
                }
                // 第一家出了不止一张
                ArrayList<Byte> selfZhuCards = CardsParser.getAllZhu(handCards, zhu);
                // 不够但是有
                if (!suitCards.isEmpty()){
                    Log.d("PlayRuler-checkRules", "55");
                    ArrayList<Byte> playSuitCards = null;
                    switch (firstSuit){
                        case Heart:
                            playSuitCards = CardsParser.getHeart(playCards, zhu.getRank());
                            break;
                        case Club:
                            playSuitCards = CardsParser.getClub(playCards, zhu.getRank());
                            break;
                        case Diamond:
                            playSuitCards = CardsParser.getDiamond(playCards, zhu.getRank());
                            break;
                        case Spade:
                            playSuitCards = CardsParser.getSpade(playCards, zhu.getRank());
                            break;
                    }
                    if (playSuitCards.size() < suitCards.size()){
                        Log.d("PlayRuler-checkRules", "56");
                        return false;
                    }
                    // 有而且全出了
                    // 如果主够，剩下全出主
                    if (selfZhuCards.size() + suitCards.size() >= firstCards.size()){
                        Log.d("PlayRuler-checkRules", "57");
                        // 检查每张牌要么是所打花色，要么是主
                        for (byte b : playCards) {
                            Suit s = getSuit(CodeUtil.getCardFromCode(b));
                            if (s != firstSuit && s != null) {
                                Log.d("PlayRuler-checkRules", "58");
                                return false;
                            }
                        }

                        // 第一家带对子
                        if (!firstDui.isEmpty()) {
                            Log.d("PlayRuler-checkRules", "59");
                            ArrayList<Byte> selfSuitDui = CardsParser.getDui(suitCards);
                            // 对子出够了，剩下出主即可
                            if (selfSuitDui.size() >= firstDui.size()) {
                                Log.d("PlayRuler-checkRules", "60");
                                return true;
                            }else{
                                Log.d("PlayRuler-checkRules", "61");
                                // 对子没出够，主上有对应出对，没有则剐大牌
                                int lackDuiCount = firstDui.size() - selfSuitDui.size();
                                int maxDuiCount = (firstCards.size() - suitCards.size()) / 2;
                                int needDuiCount = lackDuiCount < maxDuiCount ? lackDuiCount : maxDuiCount;
                                ArrayList<Byte> selfZhuDui = CardsParser.getDui(selfZhuCards);
                                ArrayList<Byte> playDui = CardsParser.getDui(playCards);
                                // 主上对子够，出对子，最多剐一张
                                if (selfZhuDui.size() >= needDuiCount){
                                    Log.d("PlayRuler-checkRules", "62");
                                    if (playDui.size() < selfSuitDui.size() + needDuiCount){
                                        Log.d("PlayRuler-checkRules", "63");
                                        return false;
                                    }else{
                                        Log.d("PlayRuler-checkRules", "64");
                                        int lackCards = 0;
                                        if (needDuiCount < lackDuiCount && (firstCards.size() - suitCards.size()) % 2 == 1) {
                                            lackCards = 1;
                                        }
                                        int index = 0;
                                        if (lackCards != 0){
                                            if (playDui.contains(selfZhuCards.get(index))){
                                                ++index;
                                            }else{
                                                if (!playCards.contains(selfZhuCards.get(index))){
                                                    if (checkYingZhu(CodeUtil.getCardFromCode(selfZhuCards.get(index)))) {
                                                        return false;
                                                    }else{
                                                        return true;
                                                    }
                                                }
                                                return true;
                                            }
                                        }
                                    }
                                }else{
                                    Log.d("PlayRuler-checkRules", "65");
                                    // 主上对子不够，对子全部出，再剐大牌
                                    if (playDui.size() < selfSuitDui.size() + selfZhuDui.size()){
                                        Log.d("PlayRuler-checkRules", "66");
                                        return false;
                                    }
                                    // 出的所有对子算进去，计算差几对，计算差几张牌，取小
                                    lackDuiCount = firstDui.size() - playDui.size();
                                    int lackCardsCount = firstCards.size() - suitCards.size();
                                    int needGuaCount = (lackDuiCount * 2) < lackCardsCount ?
                                            (lackDuiCount * 2) : lackCardsCount;
                                    selfZhuCards.removeAll(selfZhuDui);
                                    for (int i = 0; i < needGuaCount; ++i){
                                        // 如果没有从大剐牌，则犯规
                                        if (!playCards.contains(selfZhuCards.get(i))){
                                            if (checkYingZhu(CodeUtil.getCardFromCode(selfZhuCards.get(i)))) {
                                                Log.d("PlayRuler-checkRules", "67");
                                                return false;
                                            }else{
                                                Log.d("PlayRuler-checkRules", "67-2");
                                                return true;
                                            }
                                        }
                                    }
                                    Log.d("PlayRuler-checkRules", "68");
                                    return true;
                                }
                            }
                        }
                        Log.d("PlayRuler-checkRules", "69");
                        // 第一家不带对子，是主即可
                        return true;
                    }else{
                        Log.d("PlayRuler-checkRules", "70");
                        // 主不够，主应全部打出
                        ArrayList<Byte> playZhuCards = CardsParser.getZhu(playCards, zhu.getRank());
                        if (playZhuCards.size() < selfZhuCards.size()){
                            Log.d("PlayRuler-checkRules", "71");
                            return false;
                        }
                        Log.d("PlayRuler-checkRules", "72");
                        return true;
                    }
                }
                // 自己一张都没有
                Log.d("PlayRuler-checkRules", "73");
                // 第一家出的对子数多于手中主数，主全部出
                ArrayList<Byte> playZhuCards = CardsParser.getAllZhu(playCards, zhu);
                if (selfZhuCards.size() <= firstDui.size() * 2){
                    Log.d("PlayRuler-checkRules", "74");
                    if (playZhuCards.size() < selfZhuCards.size()){
                        Log.d("PlayRuler-checkRules", "75");
                        return false;
                    }
                    Log.d("PlayRuler-checkRules", "75-2");
                    return true;
                }else {
                    // 手中主数多于第一家出的对子数，应出相应数量的主
                    if (playZhuCards.size() < firstDui.size() * 2) {
                        Log.d("PlayRuler-checkRules", "75-3");
                        return false;
                    }
                }
                // 第一家出了连对
                if (!firstLiandui.isEmpty()){
                    Log.d("PlayRuler-checkRules", "76");
                    // 获取第一个连对的第一张牌和长度
                    byte firstLianduiFirstCard = firstLiandui.entrySet().iterator().next().getKey();
                    int firstLianduiLength = firstLiandui.entrySet().iterator().next().getValue();
                    LinkedHashMap<Byte, Integer> selfZhuLiandui = CardsParser.getLiandui(selfZhuCards, zhu);
                    // 自己主上有连对
                    if (!selfZhuLiandui.isEmpty()){
                        Log.d("PlayRuler-checkRules", "8");
                        boolean has = false;
                        for (int length : selfZhuLiandui.values()){
                            if (length >= firstLianduiLength) {
                                has = true;
                                break;
                            }
                        }
                        LinkedHashMap<Byte, Integer> playZhuLiandui = CardsParser.getLiandui(playZhuCards, zhu);
                        // 有比第一个出的拖拉机长的连对，则playCards中也应有与第一个等长的拖拉机
                        if (has){
                            Log.d("PlayRuler-checkRules", "9");
                            boolean hasPlayed = false;
                            byte playLianduiFirstCard = -1;
                            for (byte b : playZhuLiandui.keySet()){
                                if (playZhuLiandui.get(b) >= firstLianduiLength) {
                                    playLianduiFirstCard = b;
                                    hasPlayed = true;
                                    break;
                                }
                            }
                            if (!hasPlayed){
                                Log.d("PlayRuler-checkRules", "10");
                                return false;
                            }else{
                                // 有而且出了，更新未对应的牌
                                Log.d("PlayRuler-checkRules", "11");
                                ArrayList<Byte> updateSelfZhuCards = new ArrayList<>();
                                ArrayList<Byte> updateFirstCards = new ArrayList<>();
                                ArrayList<Byte> updatePlayCards = new ArrayList<>();
                                ArrayList<Byte> removeFirstCards = new ArrayList<>();
                                ArrayList<Byte> removePlayCards = new ArrayList<>();
                                updateSelfZhuCards.addAll(selfZhuCards);
                                updateFirstCards.addAll(firstCards);
                                updatePlayCards.addAll(playCards);
                                // 找到连对中第一张牌位置，从打出的牌中移除后面length * 2张牌
                                int removeIndex = playCards.indexOf(playLianduiFirstCard);
                                for (int i = 0; i < firstLianduiLength; ++i){
                                    removePlayCards.add(playCards.get(removeIndex + i * 2));
                                    removePlayCards.add(playCards.get(removeIndex + i * 2));
                                }
                                int removeFirstIndex = firstCards.indexOf(firstLianduiFirstCard);
                                for (int i = 0; i < firstLianduiLength; ++i){
                                    removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                    removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                }
                                removeCards(updateSelfZhuCards, removePlayCards);
                                removeCards(updatePlayCards, removePlayCards);
                                removeCards(updateFirstCards, removeFirstCards);
                                return checkRules(updateFirstCards, updateSelfZhuCards, updatePlayCards);
                            }
                        }else{
                            // 没有跟第一个出的一样长的连对，也应当出
                            if (playZhuLiandui.isEmpty()){
                                Log.d("PlayRuler-checkRules", "12");
                                return false;
                            }
                            ArrayList<Byte> updateSelfZhuCards = new ArrayList<>();
                            ArrayList<Byte> updateFirstCards = new ArrayList<>();
                            ArrayList<Byte> updatePlayCards = new ArrayList<>();
                            ArrayList<Byte> removeFirstCards = new ArrayList<>();
                            ArrayList<Byte> removePlayCards = new ArrayList<>();
                            updateSelfZhuCards.addAll(selfZhuCards);
                            updateFirstCards.addAll(firstCards);
                            updatePlayCards.addAll(playCards);
                            byte playLianduiFirstCard = playZhuLiandui.entrySet().iterator().next().getKey();
                            int playLianduiLength = playZhuLiandui.entrySet().iterator().next().getValue();
                            int removePlayIndex = playCards.indexOf(playLianduiFirstCard);
                            for (int i = 0; i < playLianduiLength; ++i){
                                removePlayCards.add(playCards.get(removePlayIndex + i * 2));
                                removePlayCards.add(playCards.get(removePlayIndex + i * 2));
                            }
                            int removeFirstIndex = firstCards.indexOf(firstLianduiFirstCard);
                            for (int i = 0; i < playLianduiLength; ++i){
                                removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                            }
                            removeCards(updateSelfZhuCards, removePlayCards);
                            removeCards(updatePlayCards, removePlayCards);
                            removeCards(updateFirstCards, removeFirstCards);
                            return checkRules(updateFirstCards, updateSelfZhuCards, updatePlayCards);
                        }
                    }else{
                        // 自己没有连对
                        Log.d("PlayRuler-checkRules", "12");
                        ArrayList<Byte> selfZhuDui = CardsParser.getDui(selfZhuCards);
                        // 自己有对
                        if (!selfZhuDui.isEmpty()){
                            Log.d("PlayRuler-checkRules", "13");
                            ArrayList<Byte> playZhuDui = CardsParser.getDui(playZhuCards);
                            // 自己对子够，则出的对子数应等于第一家出的连对的对子数
                            if (selfZhuDui.size() >= firstLianduiLength){
                                Log.d("PlayRuler-checkRules", "14");
                                if (playZhuDui.size() < firstLianduiFirstCard){
                                    Log.d("PlayRuler-checkRules", "15");
                                    return false;
                                }else{
                                    // 出了足够对子数，移除已对应的牌
                                    Log.d("PlayRuler-checkRules", "16");
                                    ArrayList<Byte> updateSelfZhuCards = new ArrayList<>();
                                    ArrayList<Byte> updateFirstCards = new ArrayList<>();
                                    ArrayList<Byte> updatePlayCards = new ArrayList<>();
                                    ArrayList<Byte> removeFirstCards = new ArrayList<>();
                                    ArrayList<Byte> removePlayCards = new ArrayList<>();
                                    updateSelfZhuCards.addAll(selfZhuCards);
                                    updateFirstCards.addAll(firstCards);
                                    updatePlayCards.addAll(playCards);
                                    for (int i = 0; i < firstLianduiLength; ++i){
                                        removePlayCards.add(playZhuDui.get(i));
                                        removePlayCards.add(playZhuDui.get(i));
                                    }
                                    int removeFirstIndex = firstCards.indexOf(firstLianduiFirstCard);
                                    for (int i = 0; i < firstLianduiLength; ++i){
                                        removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                        removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                    }
                                    removeCards(updateSelfZhuCards, removePlayCards);
                                    removeCards(updatePlayCards, removePlayCards);
                                    removeCards(updateFirstCards, removeFirstCards);
                                    return checkRules(updateFirstCards, updateSelfZhuCards, updatePlayCards);
                                }
                            }else{
                                // 自己对子不够，应出所有对子
                                Log.d("PlayRuler-checkRules", "17");
                                if (playZhuDui.size() < selfZhuDui.size()){
                                    Log.d("PlayRuler-checkRules", "18");
                                    return false;
                                }else{
                                    Log.d("PlayRuler-checkRules", "19");
                                    ArrayList<Byte> updateSelfZhuCards = new ArrayList<>();
                                    ArrayList<Byte> updateFirstCards = new ArrayList<>();
                                    ArrayList<Byte> updatePlayCards = new ArrayList<>();
                                    ArrayList<Byte> removeFirstCards = new ArrayList<>();
                                    ArrayList<Byte> removePlayCards = new ArrayList<>();
                                    updateSelfZhuCards.addAll(selfZhuCards);
                                    updateFirstCards.addAll(firstCards);
                                    updatePlayCards.addAll(playCards);
                                    for (int i = 0; i < playZhuDui.size(); ++i){
                                        removePlayCards.add(playZhuDui.get(i));
                                        removePlayCards.add(playZhuDui.get(i));
                                    }
                                    int removeFirstIndex = firstCards.indexOf(firstLianduiFirstCard);
                                    for (int i = 0; i < playZhuDui.size(); ++i){
                                        removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                        removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                    }
                                    removeCards(updateSelfZhuCards, removePlayCards);
                                    removeCards(updatePlayCards, removePlayCards);
                                    removeCards(updateFirstCards, removeFirstCards);
                                    return checkRules(updateFirstCards, updateSelfZhuCards, updatePlayCards);
                                }
                            }
                        }else{
                            // 自己没有对
                            Log.d("PlayRuler-checkRules", "20");
                            ArrayList<Byte> updateSelfZhuCards = new ArrayList<>();
                            ArrayList<Byte> updateFirstCards = new ArrayList<>();
                            ArrayList<Byte> updatePlayCards = new ArrayList<>();
                            ArrayList<Byte> removeFirstCards = new ArrayList<>();
                            ArrayList<Byte> removePlayCards = new ArrayList<>();
                            updateSelfZhuCards.addAll(selfZhuCards);
                            updateFirstCards.addAll(firstCards);
                            updatePlayCards.addAll(playCards);
                            for (int i = 0; i < firstLianduiLength; ++i){
                                removePlayCards.add(selfZhuCards.get(i * 2));
                                removePlayCards.add(selfZhuCards.get(i * 2 + 1));
                            }
                            // 剐最大牌，必须出
                            for (byte b : removePlayCards){
                                if (!playCards.contains(b)){
                                    if (checkYingZhu(CodeUtil.getCardFromCode(b))) {
                                        Log.d("PlayRuler-checkRules", "20-2");
                                        return false;
                                    }
                                }
                            }
                            int removeFirstIndex = firstCards.indexOf(firstLianduiFirstCard);
                            for (int i = 0; i < firstLianduiLength; ++i){
                                removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                                removeFirstCards.add(firstCards.get(removeFirstIndex + i * 2));
                            }
                            removeCards(updateSelfZhuCards, removePlayCards);
                            removeCards(updatePlayCards, removePlayCards);
                            removeCards(updateFirstCards, removeFirstCards);
                            return checkRules(updateFirstCards, updateSelfZhuCards, updatePlayCards);
                        }
                    }
                }
                // 第一家出了对子
                if (!firstDui.isEmpty()){
                    Log.d("PlayRuler-checkRules", "77");
                    ArrayList<Byte> selfZhuDui = CardsParser.getDui(selfZhuCards);
                    ArrayList<Byte> playZhuDui = CardsParser.getDui(playZhuCards);
                    // 手牌对子够，打的牌也应出足够对子
                    if (selfZhuDui.size() >= firstDui.size()){
                        Log.d("PlayRuler-checkRules", "22");
                        if (playZhuDui.size() < firstDui.size()){
                            Log.d("PlayRuler-checkRules", "23");
                            return false;
                        }else{
                            // 出了足够对子，剩下单牌随便出
                            Log.d("PlayRuler-checkRules", "24");
                            return true;
                        }
                    }else {
                        // 手牌对子不够，出所有对子
                        // 手牌有对子
                        Log.d("PlayRuler-checkRules", "25");
                        if (!selfZhuDui.isEmpty()) {
                            if (playZhuDui.size() < selfZhuDui.size()) {
                                Log.d("PlayRuler-checkRules", "26");
                                return false;
                            } else {
                                Log.d("PlayRuler-checkRules", "27");
                                ArrayList<Byte> updateSelfZhuCards = new ArrayList<>();
                                ArrayList<Byte> updateFirstCards = new ArrayList<>();
                                ArrayList<Byte> updatePlayCards = new ArrayList<>();
                                ArrayList<Byte> removeFirstCards = new ArrayList<>();
                                ArrayList<Byte> removePlayCards = new ArrayList<>();
                                updateSelfZhuCards.addAll(selfZhuCards);
                                updateFirstCards.addAll(firstCards);
                                updatePlayCards.addAll(playCards);
                                for (int i = 0; i < playZhuDui.size(); ++i) {
                                    removePlayCards.add(playZhuDui.get(i));
                                    removePlayCards.add(playZhuDui.get(i));
                                }
                                for (int i = 0; i < playZhuDui.size(); ++i) {
                                    removeFirstCards.add(firstDui.get(i));
                                    removeFirstCards.add(firstDui.get(i));
                                }
                                removeCards(updateSelfZhuCards, removePlayCards);
                                removeCards(updatePlayCards, removePlayCards);
                                removeCards(updateFirstCards, removeFirstCards);
                                return checkRules(updateFirstCards, updateSelfZhuCards, updatePlayCards);
                            }
                        } else {
                            // 手牌没有对子
                            ArrayList<Byte> removePlayCards = new ArrayList<>();
                            for (int i = 0; i < firstDui.size(); ++i) {
                                removePlayCards.add(selfZhuCards.get(i * 2));
                                removePlayCards.add(selfZhuCards.get(i * 2 + 1));
                            }
                            // 剐最大牌，必须出，除非已剐到小主
                            for (byte b : removePlayCards) {
                                if (!playCards.contains(b)) {
                                    if (checkYingZhu(CodeUtil.getCardFromCode(b))) {
                                        return false;
                                    }else{
                                        return true;
                                    }
                                }
                            }
                            return true;
                        }
                    }
                }
                // 第一家出的单，随便出
                return true;
            }
        }
    }

    protected boolean checkYingZhu(Card card){
        Rank rank = card.getRank();
        if (rank == Rank.Joker_red || rank == Rank.Joker_black ||
                rank == Rank.Deuce || rank == zhu.getRank()){
            return true;
        }else{
            return false;
        }
    }
	
	public boolean checkSuit(ArrayList<Byte> cards) {
		Log.d("PlayRuler", "checkSuit");
		if (Build.VERSION.SDK_INT >= 24)
			cards.sort(com);
		Suit zhuSuit = zhu.getSuit();
		Suit suit = CodeUtil.getCardFromCode(cards.get(0)).getSuit();
		Rank rank = CodeUtil.getCardFromCode(cards.get(0)).getRank();
		if (suit == null || rank == Rank.Deuce || rank == zhu.getRank()) {				
			if (zhuSuit == null) {		// 无小主
				ArrayList<Byte> zhuCards = CardsParser.getZhu(cards, zhu.getRank());
				if (cards.size() == zhuCards.size()){
                Log.d("PlayRuler-checkSuit", "true");
                return true;
            }else {
                Log.d("PlayRuler-checkSuit", "false");
                return false;
            }
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
				if (zhuCards.size() + xiaozhuCards.size() == cards.size()){
                    Log.d("PlayRuler-checkSuit", "true");
                    return true;
                }else {
                    Log.d("PlayRuler-checkSuit", "false");
                    return false;
                }
			}
		}else {
			if (suit == Suit.Heart) {
				ArrayList<Byte> heartCards = CardsParser.getHeart(cards, zhu.getRank());
				if (cards.size() == heartCards.size()){
                    Log.d("PlayRuler-checkSuit", "true");
                    return true;
                }else {
                    Log.d("PlayRuler-checkSuit", "false");
                    return false;
                }
			}
			if (suit == Suit.Club) {
				ArrayList<Byte> clubCards = CardsParser.getClub(cards, zhu.getRank());
				if (cards.size() == clubCards.size()){
                    Log.d("PlayRuler-checkSuit", "true");
                    return true;
                }else {
                    Log.d("PlayRuler-checkSuit", "false");
                    return false;
                }
			}
			if (suit == Suit.Diamond) {
				ArrayList<Byte> diamondCards = CardsParser.getDiamond(cards, zhu.getRank());
				if (cards.size() == diamondCards.size()){
                    Log.d("PlayRuler-checkSuit", "true");
                    return true;
                }else {
                    Log.d("PlayRuler-checkSuit", "false");
                    return false;
                }
			}
			if (suit == Suit.Spade) {
				ArrayList<Byte> spadeCards = CardsParser.getSpade(cards, zhu.getRank());
				if (cards.size() == spadeCards.size()){
                    Log.d("PlayRuler-checkSuit", "true");
                    return true;
                }else {
                    Log.d("PlayRuler-checkSuit", "false");
                    return false;
                }
			}
            Log.d("PlayRuler-checkSuit", "invisible");
			return false;
		}
	}

	/*public ArrayList<Byte> checkShuai(ArrayList<Byte> cards, ArrayList<Byte> p1,
			ArrayList<Byte> p2, ArrayList<Byte> p3) {
		Log.d("PlayRuler", "checkShuai");
		boolean dan;
		boolean dui;
		boolean liandui;
		ArrayList<Byte> ret = new ArrayList<>();
		Suit cardsSuit = getSuit(CodeUtil.getCardFromCode(cards.get(0)));
		ArrayList<Byte> suitP1 = null;
		ArrayList<Byte> suitP2 = null;
		ArrayList<Byte> suitP3 = null;
		
		ArrayList<Byte>cardsDan = CardsParser.getDan(cards);
		ArrayList<Byte>cardsDui = CardsParser.getDui(cards);
		LinkedHashMap<Byte, Integer> cardsLiandui = getLiandui(cards, zhu);
		if (cardsSuit == null) {
			Log.d("checkShuai", "null");
			suitP1 = CardsParser.getZhu(p1, zhu.getRank());
			suitP2 = CardsParser.getZhu(p2, zhu.getRank());
			suitP3 = CardsParser.getZhu(p3, zhu.getRank());
			
			if (zhu.getSuit() != null) {
				switch (zhu.getSuit()) {
				case Heart:
					suitP1.addAll(CardsParser.getHeart(p1, zhu.getRank()));
					suitP2.addAll(CardsParser.getHeart(p2, zhu.getRank()));
					suitP3.addAll(CardsParser.getHeart(p3, zhu.getRank()));
					break;
				case Club:
					suitP1.addAll(CardsParser.getClub(p1, zhu.getRank()));
					suitP2.addAll(CardsParser.getClub(p2, zhu.getRank()));
					suitP3.addAll(CardsParser.getClub(p3, zhu.getRank()));
					break;
				case Diamond:
					suitP1.addAll(CardsParser.getDiamond(p1, zhu.getRank()));
					suitP2.addAll(CardsParser.getDiamond(p2, zhu.getRank()));
					suitP3.addAll(CardsParser.getDiamond(p3, zhu.getRank()));
					break;
				case Spade:
					suitP1.addAll(CardsParser.getSpade(p1, zhu.getRank()));
					suitP2.addAll(CardsParser.getSpade(p2, zhu.getRank()));
					suitP3.addAll(CardsParser.getSpade(p3, zhu.getRank()));
					break;
				}
			}
		}else if (cardsSuit == Suit.Heart) {
			Log.d("checkShuai", "heart");
			suitP1 = CardsParser.getHeart(p1, zhu.getRank());;
			suitP2 = CardsParser.getHeart(p2, zhu.getRank());;
			suitP3 = CardsParser.getHeart(p3, zhu.getRank());
		}else if (cardsSuit == Suit.Club) {
			Log.d("checkShuai", "club");
			suitP1 = CardsParser.getClub(p1, zhu.getRank());
			suitP2 = CardsParser.getClub(p2, zhu.getRank());
			suitP3 = CardsParser.getClub(p3, zhu.getRank());
		}else if (cardsSuit == Suit.Diamond) {
			Log.d("checkShuai", "diamond");
			suitP1 = CardsParser.getDiamond(p1, zhu.getRank());
			suitP2 = CardsParser.getDiamond(p2, zhu.getRank());
			suitP3 = CardsParser.getDiamond(p3, zhu.getRank());
		}else if (cardsSuit == Suit.Spade) {
			Log.d("checkShuai", "spade");
			suitP1 = CardsParser.getSpade(p1, zhu.getRank());
			suitP2 = CardsParser.getSpade(p2, zhu.getRank());
			suitP3 = CardsParser.getSpade(p3, zhu.getRank());
		}
		if (cardsLiandui.isEmpty()) {
			Log.d("liandui", "empty");
			liandui = true;
		}else {
			// 获取最后添加的即最小的连对
			byte min = -1;
			for (byte b : cardsLiandui.keySet()) {
				min = b;
			}
			
			if (!suitP1.isEmpty()) {
				Log.d("suitP1", "not empty");
				Log.d("suitP1", suitP1.toString());
				LinkedHashMap<Byte, Integer> p1Liandui = getLiandui(suitP1, zhu);
				if (!p1Liandui.isEmpty()) {
					Log.d("p1Liandui", "not empty");
					Iterator<Entry<Byte, Integer>> iterator = p1Liandui.entrySet().iterator();
					Entry<Byte, Integer> entry = null;
					while (iterator.hasNext()) {
						entry = iterator.next();
						// codeCom按从小到大排序，< 0表示前者大于后者
						if (com.compare(entry.getKey(), min) < 0 && 
								entry.getValue() >= cardsLiandui.get(min)) {

							Log.d(">= cardsLiandui", "can't shuaipai");
							ret.addAll(cards);
							int index = ret.indexOf(min);
							ArrayList<Byte> remove = new ArrayList<>();
							for (int i = 0; i < cardsLiandui.get(min); ++i) {
								remove.add(ret.get(index + i * 2));
								remove.add(ret.get(index + i * 2));
							}
							removeCards(ret, remove);
							Log.d("return", ret.toString());
							return ret;
						}
					}
				}
			}
			
			if (!suitP2.isEmpty()) {
				Log.d("suitP2", "not empty");
				Log.d("suitP2", suitP2.toString());
				LinkedHashMap<Byte, Integer> p2Liandui = getLiandui(suitP2, zhu);
				if (!p2Liandui.isEmpty()) {
					Log.d("p2Liandui", "not empty");
					Iterator<Entry<Byte, Integer>> iterator = p2Liandui.entrySet().iterator();
					Entry<Byte, Integer> entry = null;
					while (iterator.hasNext()) {
						entry = iterator.next();
						if (com.compare(entry.getKey(), min) < 0 && 
								entry.getValue() >= cardsLiandui.get(min)) {

							Log.d(">= cardsLiandui", "can't shuaipai");
							ret.addAll(cards);
							int index = ret.indexOf(min);
							ArrayList<Byte> remove = new ArrayList<>();
							for (int i = 0; i < cardsLiandui.get(min); ++i) {
								remove.add(ret.get(index + i * 2));
								remove.add(ret.get(index + i * 2));
							}
							removeCards(ret, remove);
							Log.d("return", ret.toString());
							return ret;
						}
					}
				}
			}
			
			if (!suitP3.isEmpty()) {
				Log.d("suitP3", "not empty");
				Log.d("suitP3", suitP3.toString());
				LinkedHashMap<Byte, Integer> p3Liandui = getLiandui(suitP3, zhu);
				if (!p3Liandui.isEmpty()) {
					Log.d("p3Liandui", "not empty");
					Iterator<Entry<Byte, Integer>> iterator = p3Liandui.entrySet().iterator();
					Entry<Byte, Integer> entry = null;
					while (iterator.hasNext()) {
						entry = iterator.next();
						if (com.compare(entry.getKey(), min) < 0 && 
								entry.getValue() >= cardsLiandui.get(min)) {

							Log.d(">= cardsLiandui", "can't shuaipai");
							ret.addAll(cards);
							int index = ret.indexOf(min);
							ArrayList<Byte> remove = new ArrayList<>();
							for (int i = 0; i < cardsLiandui.get(min); ++i) {
								remove.add(ret.get(index + i * 2));
								remove.add(ret.get(index + i * 2));
							}
							removeCards(ret, remove);
							Log.d("return", ret.toString());
							return ret;
						}
					}
				}
			}
			liandui = true;
		}
		if (cardsDui.isEmpty()) {
			Log.d("dui", "empty");
			dui = true;
		}else {
			byte min = cardsDui.get(cardsDui.size() - 1);
			Log.d("min", "" + min);
			
			if (!suitP1.isEmpty()) {
				Log.d("suitP1", "not empty");
				ArrayList<Byte> p1Dui = CardsParser.getDui(suitP1);
				if (!p1Dui.isEmpty()) {
					Log.d("p1dui", p1Dui.toString());
					if (com.compare(p1Dui.get(0), min) < 0) {
						Log.d(">= cardsDui", "can't shuaipai");
						ret.addAll(cards);
						ArrayList<Byte> remove = new ArrayList<>();
						remove.add(min);
						remove.add(min);
						removeCards(ret, remove);
						Log.d("return", ret.toString());
						return ret;
					}
				}
			}
			
			if (!suitP2.isEmpty()) {
				Log.d("suitP2", "not empty");
				ArrayList<Byte> p2Dui = CardsParser.getDui(suitP2);
				if (!p2Dui.isEmpty()) {
					Log.d("p2dui", p2Dui.toString());
					if (com.compare(p2Dui.get(0), min) < 0) {
						Log.d(">= cardsDui", "can't shuaipai");
						ret.addAll(cards);
						ArrayList<Byte> remove = new ArrayList<>();
						remove.add(min);
						remove.add(min);
						removeCards(ret, remove);
						Log.d("return", ret.toString());
						return ret;
					}
				}
			}
			
			if (!suitP3.isEmpty()) {
				Log.d("suitP3", "not empty");
				ArrayList<Byte> p3Dui = CardsParser.getDui(suitP3);
				if (!p3Dui.isEmpty()) {
					Log.d("p3dui", p3Dui.toString());
					if (com.compare(p3Dui.get(0), min) < 0) {
						Log.d(">= cardsDui", "can't shuaipai");
						ret.addAll(cards);
						ArrayList<Byte> remove = new ArrayList<>();
						remove.add(min);
						remove.add(min);
						removeCards(ret, remove);
						Log.d("return", ret.toString());
						return ret;
					}
				}
			}
			dui = true;
		}
		if (cardsDan.isEmpty()) {
			Log.d("dan", "empty");
			dan = true;
		}else {
			byte min = cardsDan.get(cardsDan.size() - 1);
			Log.d("min", "" + min);
			
			if (!suitP1.isEmpty()) {
				Log.d("suitP1", "not empty");
				// 有大于甩牌中最小牌即可
				if (com.compare(suitP1.get(0), min) < 0) {
					Log.d(">= cardsDui", "can't shuaipai");
					ret.addAll(cards);
					ret.remove(ret.size() - 1);
					return ret;
				}
			}
			
			if (!suitP2.isEmpty()) {
				Log.d("suitP2", "not empty");
				// 有大于甩牌中最小牌即可
				if (com.compare(suitP2.get(0), min) < 0) {
					Log.d(">= cardsDui", "can't shuaipai");
					ret.addAll(cards);
					ret.remove(ret.size() - 1);
					return ret;
				}
			}
			
			if (!suitP3.isEmpty()) {
				Log.d("suitP3", "not empty");
				// 有大于甩牌中最小牌即可
				if (com.compare(suitP3.get(0), min) < 0) {
					Log.d(">= cardsDui", "can't shuaipai");
					ret.addAll(cards);
					ret.remove(ret.size() - 1);
					return ret;
				}
			}
			dan = true;
		}
		return null;
	}
	*/
	protected void removeCards(ArrayList<Byte> handCards, ArrayList<Byte> playCards) {
		for (byte p : playCards) {
			for (int i = 0; i < handCards.size(); ++i) {
				if (handCards.get(i) == p) {
					handCards.remove(i);
					break;
				}
			}
		}
	}
	/*
	public int getType(ArrayList<Byte> cards) {
		// 只有1张牌
		if (cards.size() == 1) {
			return DAN;
		}
		if (cards.size() % 2 == 1) {
			return SHUAI;
		}
		ArrayList<Byte> dan = CardsParser.getDan(cards);
		if (dan != null && dan.size() > 1) {
			return SHUAI;
		}
		ArrayList<Byte> dui = CardsParser.getDui(cards);
		// 有对
		if (dui != null) {
			// 只有一对
			if (dui.size() == 1) {
				if (cards.size() == 2)
					return DUI;
				else return SHUAI;
			}else {
				// 不止一对
                if (Build.VERSION.SDK_INT >= 24)
				    dui.sort(com);
				LinkedHashMap<Byte, Integer> liandui = getLiandui(cards, zhu);
				if (liandui == null) {
					return SHUAI;
				}else {
					if (liandui.containsKey(dui.get(0)) && liandui.get(dui.get(0)) == dui.size()) {
						return LIANDUI;
					}else {
						return SHUAI;
					}
				}
			}
		}
		return -1;
	}
    */
}

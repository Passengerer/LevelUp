package com.laowuren.levelup.utils;

import com.laowuren.levelup.others.Card;
import com.laowuren.levelup.others.Rank;
import com.laowuren.levelup.others.Suit;

/**
 * Created by Administrator on 2020/1/28/028.
 */

public class CodeUtil {

    public final static byte CREATE = (byte)0xf0;   // -16
    public final static byte READY = (byte)0xf1;    // -15
    public final static byte FAILED1 = (byte)0xf2;	// -14 无空余房间
    public final static byte FAILED2 = (byte)0xf3;	// -13 房间人数已满
    public final static byte FAILED3 = (byte)0xf4;	// -12 错误请求
    public final static byte SUCCESS = (byte)0xf5;	// -11 请求成功
    public final static byte START = (byte)0xf6;	// 请求成功
    
    public final static byte ROOMID = (byte)0xe0;	// -32 房间id

    public static byte getHeader(byte b){
        return (byte)(b & 0xf0);
    }

    public static byte getTail(byte b){
        return (byte)(b & 0x0f);
    }

    public static byte getHigher(byte b){
        return (byte)((b & 0xf0) >> 4);
    }

    public static Card getCardFromCode(byte code){
        Card card;
        if (code == 0x4d)
            card = new Card(null, Rank.Joker_black);
        else if (code == 0x4e)
            card = new Card(null, Rank.Joker_red);
        else {
            Suit suit = null;
            Rank rank = null;
            switch(getHigher((code))){
                case 0:
                    suit = Suit.Heart;
                    break;
                case 1:
                    suit = Suit.Club;
                    break;
                case 2:
                    suit = Suit.Diamond;
                    break;
                case 3:
                    suit = Suit.Spade;
                    break;
            }
            switch(getTail((code))){
                case 0:
                    rank = Rank.Three;
                    break;
                case 1:
                    rank = Rank.Four;
                    break;
                case 2:
                    rank = Rank.Five;
                    break;
                case 3:
                    rank = Rank.Six;
                    break;
                case 4:
                    rank = Rank.Seven;
                    break;
                case 5:
                    rank = Rank.Eight;
                    break;
                case 6:
                    rank = Rank.Nine;
                    break;
                case 7:
                    rank = Rank.Ten;
                    break;
                case 8:
                    rank = Rank.Jack;
                    break;
                case 9:
                    rank = Rank.Queen;
                    break;
                case 10:
                    rank = Rank.King;
                    break;
                case 11:
                    rank = Rank.Ace;
                    break;
                case 12:
                    rank = Rank.Deuce;
                    break;
            }
            card = new Card(suit, rank);
        }
        return card;
    }

}

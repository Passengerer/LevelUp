package com.laowuren.levelup.others;

import java.util.Comparator;

/**
 * Created by Administrator on 2020/2/15/015.
 */

public class ImageViewsComparator implements Comparator<MyImageView> {

    private CodeComparator codeCom;

    public ImageViewsComparator(){}

    public void setCodeCom(CodeComparator codeCom){
        this.codeCom = codeCom;
    }

    @Override
    public int compare(MyImageView o1, MyImageView o2) {
        byte b1 = o1.code;
        byte b2 = o2.code;
        return codeCom.compare(b1, b2);
    }
}

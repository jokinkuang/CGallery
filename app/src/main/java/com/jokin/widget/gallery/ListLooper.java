package com.jokin.widget.gallery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jokin on 2018/8/9 10:05.
 */

public class ListLooper {

    public static final int StyleNormal = 0;
    public static final int StyleLoop = 1;

    private List mList = new ArrayList();
    private int mStyle = StyleNormal;

    public ListLooper(int style) {
        setStyle(style);
    }

    public void setStyle(int style) {
        mStyle = style;
    }

    public void setData(List list) {
        if (list == null) {
            mList = new ArrayList();
        } else {
            mList = list;
        }
    }

    /*
     * MAX_VALUE（2147483648） 够1w张照片循环了。
     * 中心作为0点：Integer.MAX_VALUE／2
     */
    public int getCount() {
        if (mStyle == StyleNormal) {
            return mList.size();
        }
        return Integer.MAX_VALUE;
    }

    public int getRealCount() {
        return mList.size();
    }


    public Object getItem(int position) {
        return mList.isEmpty() ? null : mList.get(position % mList.size());
    }

    public long getItemId(int position) {
        return mList.isEmpty() ? -1 : position % mList.size();
    }
}

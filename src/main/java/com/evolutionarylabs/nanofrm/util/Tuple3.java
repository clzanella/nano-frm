package com.evolutionarylabs.nanofrm.util;

/**
 * Created by cleberzanella on 03/04/17.
 */
public class Tuple3<T1, T2, T3> extends Tuple<T1, T2> {

    private final T3 item3;

    public Tuple3(T1 item1, T2 item2, T3 item3) {
        super(item1, item2);

        this.item3 = item3;
    }

    public T3 getItem3() {
        return item3;
    }
}
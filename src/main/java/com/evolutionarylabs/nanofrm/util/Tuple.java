package com.evolutionarylabs.nanofrm.util;

/**
 * Created by cleberzanella on 03/04/17.
 */
public class Tuple<T1, T2> {

    private final T1 item1;
    private final T2 item2;

    public Tuple(T1 item1, T2 item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    public T1 getItem1() {
        return item1;
    }

    public T2 getItem2() {
        return item2;
    }

    public static <T1, T2> Tuple<T1, T2> create(T1 item1, T2 item2){
        return new Tuple<T1,T2>(item1, item2);
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> create(T1 item1, T2 item2, T3 item3){
        return new Tuple3<T1,T2, T3>(item1, item2, item3);
    }

}

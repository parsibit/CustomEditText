package com.parsibit.nice_edit_text;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;


public class Typefaces {
    private static final Hashtable<String, Typeface> cache = new Hashtable<>();

    public static Typeface get(Context c, String name) {
        synchronized (cache) {
            if (!cache.containsKey(name)) {
                Typeface t = Typeface.createFromAsset(c.getAssets(), name);
                cache.put(name, t);
            }
            return cache.get(name);
        }
    }

}
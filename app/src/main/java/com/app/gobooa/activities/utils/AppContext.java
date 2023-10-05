package com.app.gobooa.activities.utils;

import android.app.Application;

import com.fxn.stash.Stash;
import com.mazenrashed.printooth.Printooth;

public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //Add this line in ApplicationContext.java
        Stash.init(this);
        Printooth.INSTANCE.init(this);

    }
}

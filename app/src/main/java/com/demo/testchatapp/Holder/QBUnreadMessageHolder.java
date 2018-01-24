package com.demo.testchatapp.Holder;

import android.os.Bundle;

/**
 * Created by User on 01/23/2018.
 */

public class QBUnreadMessageHolder {

    private static QBUnreadMessageHolder instance;
    private Bundle bundle;

    public static synchronized QBUnreadMessageHolder getInstance() {
        QBUnreadMessageHolder qbUnreadMessageHolder;
        synchronized (QBUnreadMessageHolder.class) {
            if (instance == null) {
                instance = new QBUnreadMessageHolder();
            }
        }
        qbUnreadMessageHolder = instance;
        return qbUnreadMessageHolder;
    }

    private QBUnreadMessageHolder() {
        bundle = new Bundle();
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public int getUnreadMessageDialogById(String id) {
        return this.bundle.getInt(id);
    }



    


}

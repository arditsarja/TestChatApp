package com.demo.testchatapp.Holder;

import com.quickblox.chat.model.QBChatDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by User on 01/23/2018.
 */

public class QBChatDialogHolder {
    private static QBChatDialogHolder instance;

    private HashMap<String, QBChatDialog> qbChatDialogs;

    public static synchronized QBChatDialogHolder getInstance() {
        QBChatDialogHolder qbChatDialogHolder;
        synchronized (QBChatDialogHolder.class) {
            if (instance == null) {
                instance = new QBChatDialogHolder();
            }
        }
        qbChatDialogHolder = instance;
        return qbChatDialogHolder;
    }

    private QBChatDialogHolder() {
        this.qbChatDialogs = new HashMap<>();
    }

    public void putChatDialogs(List<QBChatDialog> qbChatDialogs) {
        for (QBChatDialog qbChatDialog : qbChatDialogs)
            putChatDialog(qbChatDialog);

    }

    public void putChatDialog(QBChatDialog qbChatDialog) {
        this.qbChatDialogs.put(qbChatDialog.getDialogId(), qbChatDialog);
    }

    public QBChatDialog getChatDialogByID(String id) {
        return qbChatDialogs.get(id);
    }

    public List<QBChatDialog> getDialogByIds(List<String> dialogIDS) {
        List<QBChatDialog> qbChatDialogs = new ArrayList<>();
        for (String dialogID : dialogIDS)
            if (getChatDialogByID(dialogID) != null)
                qbChatDialogs.add(getChatDialogByID(dialogID));
        return qbChatDialogs;
    }
    public ArrayList<QBChatDialog> getAllDialogs(){
//        List<QBChatDialog> chatDialogs = new ArrayList<>();
//        for(String key : this.qbChatDialogs.keySet())
//            chatDialogs.add(this.qbChatDialogs.get(key));


        return new ArrayList<>(this.qbChatDialogs.values());
    }

}

package com.demo.testchatapp.Holder;

import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by User on 01/23/2018.
 */

public class QBChatMessageHolder {
    private static QBChatMessageHolder instance;
    private HashMap<String, ArrayList<QBChatMessage>> qbChatMessageArray;


    public static synchronized QBChatMessageHolder getInstance() {
        QBChatMessageHolder qbChatMessageHolder;
        synchronized (QBChatMessageHolder.class) {
            if (instance == null)
                instance = new QBChatMessageHolder();
            qbChatMessageHolder = instance;
        }
        return qbChatMessageHolder;
    }

    private QBChatMessageHolder() {
        this.qbChatMessageArray = new HashMap<>();
    }

    public void putMessages(String dialogId, ArrayList<QBChatMessage> qbChatMessages) {
        this.qbChatMessageArray.put(dialogId, qbChatMessages);
    }

    public void putMessage(String dialogId, QBChatMessage qbChatMessage) {
        List<QBChatMessage> lstResult = this.qbChatMessageArray.get(dialogId);
        if(lstResult==null)
            lstResult=new ArrayList<>();
        lstResult.add(qbChatMessage);
        ArrayList<QBChatMessage> lstAdded = new ArrayList<>(lstResult.size());
        lstAdded.addAll(lstResult);
        this.qbChatMessageArray.put(dialogId, lstAdded);
    }

    public ArrayList<QBChatMessage> getChatMessageByDialog(String dialogId){
        return this.qbChatMessageArray.get(dialogId);
    }
}

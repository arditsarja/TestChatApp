package com.demo.testchatapp.Holder;

import android.util.SparseArray;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 01/19/2018.
 */

public class QBUsersHolder {

    private static QBUsersHolder instance;
    private SparseArray<QBUser> qbUserSparseArray;

    public static synchronized QBUsersHolder getInstance() {
        if (instance == null)
            instance = new QBUsersHolder();
        return instance;
    }

    private QBUsersHolder() {
        qbUserSparseArray = new SparseArray<>();
    }


    public void putUsers(List<QBUser> users) {
        for (QBUser user : users)
            putUser(user);

    }

    public void putUser(QBUser user) {

        qbUserSparseArray.put(user.getId(), user);
    }

    public QBUser getUserbyId(int id) {
        return qbUserSparseArray.get(id);
    }


    public List<QBUser> getUsersByIDS(List<Integer> ids) {
        List<QBUser> listOfUsers = new ArrayList<>();
        for (int id : ids)
            if (getUserbyId(id) != null)
                listOfUsers.add(getUserbyId(id));

        return listOfUsers;
    }

    public ArrayList<QBUser> getAllUsers() {
        ArrayList<QBUser> users = new ArrayList<>();
        for (int i = 0; i < this.qbUserSparseArray.size(); i++) {
            users.add(this.qbUserSparseArray.valueAt(i));
        }
        return users;
    }
}

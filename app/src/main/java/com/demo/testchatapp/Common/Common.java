package com.demo.testchatapp.Common;

import com.demo.testchatapp.Holder.QBUsersHolder;
import com.quickblox.users.model.QBUser;

import java.util.List;

/**
 * Created by User on 01/19/2018.
 */

public class Common {
    public static final String DIALOG_EXTRA = "Dialogs";

    public static final String UPDATE_DIALOG_EXTRA = "ChatDialogs";
    public static final String UPDATE_MODE = "Mode";
    public static final String UPDATE_ADD_MODE = "add";
    public static final String UPDATE_REMOVE_MODE = "remove";

    public static boolean isNullOrEmptyString(String string) {
        return (string != null && !string.trim().isEmpty() ? false : true);
    }

    public static String createChateDialogName(List<Integer> qbUsers) {
        List<QBUser> qbUsers1 = QBUsersHolder.getInstance().getUsersByIDS(qbUsers);
        StringBuilder name = new StringBuilder();
        for (QBUser user : qbUsers1)
            name.append(user.getFullName()).append(" ");
        if (name.length() > 30)
            name = name.replace(30, name.length() - 1, "...");
        return name.toString();
    }
}

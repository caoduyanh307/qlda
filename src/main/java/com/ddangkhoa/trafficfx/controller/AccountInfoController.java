package com.ddangkhoa.trafficfx.controller;

import com.ddangkhoa.trafficfx.dao.AccountInfoDao;
import com.ddangkhoa.trafficfx.model.AccountInfo;
import com.ddangkhoa.trafficfx.model.AppUser;

public class AccountInfoController {
    private final AccountInfoDao dao = new AccountInfoDao();

    public AccountInfo loadAccountInfo(AppUser user) {
        return dao.findByUsername(user);
    }

    public void updateProfile(AccountInfo info, String fullName, String email, String phone, String address) {
        dao.updateProfile(info, fullName, email, phone, address);
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        if (!dao.isCurrentPasswordCorrect(username, oldPassword)) {
            return false;
        }
        dao.updatePassword(username, newPassword);
        return true;
    }
}

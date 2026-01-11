package com.easy.web.service.impl;

import com.easy.web.dao.IUserDao;
import com.easy.web.dao.impl.UserDaoImpl;
import com.easy.web.pojo.User;
import com.easy.web.service.IUserService;

public class UserServiceImpl implements IUserService {
    private IUserDao userDao = new UserDaoImpl();

    @Override
    public User login(String name, String password) {
        return userDao.login(name, password);
    }
}

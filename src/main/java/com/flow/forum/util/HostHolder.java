package com.flow.forum.util;

import com.flow.forum.entity.User;
import org.springframework.stereotype.Component;

//hold user info to replace session
@Component
public class HostHolder {
    private ThreadLocal<User> users= new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}

package com.flow.forum.dao;

import com.flow.forum.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User selectById(int id);
    User selectByName(String username);
    User selectByEmail(String email);

    int insertUser(User user);
    int updateStatus(int id, int status);
    int updateAvatar(int id, String avatarUrl);
    int updatePassword(int id, String password);

}

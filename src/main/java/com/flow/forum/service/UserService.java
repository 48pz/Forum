package com.flow.forum.service;

import com.flow.forum.dao.LoginTicketMapper;
import com.flow.forum.dao.UserMapper;
import com.flow.forum.entity.LoginTicket;
import com.flow.forum.entity.User;
import com.flow.forum.util.ForumConstant;
import com.flow.forum.util.ForumUtil;
import com.flow.forum.util.MailClient;
import com.flow.forum.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements ForumConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${forum.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }


//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user == null) {
            throw new IllegalArgumentException("Parameter cannot be null");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "Username cannot be empty");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "Password cannot be empty");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "Email cannot be empty");
            return map;
        }

        // Verify username
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "This username already exists");
            return map;
        }

        //verify password
        if (user.getPassword().length() < 8) {
            map.put("passwordMsg", "Password length must be greater than 8");
            return map;
        }


        // Verify email
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "This email already exists");
            return map;
        }

        // Register user
        user.setSalt(ForumUtil.generateUUID().substring(0, 5));
        user.setPassword(ForumUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(ForumUtil.generateUUID());
        user.setAvatarUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // Send activation email
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "Activation", content);

        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);

            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSec) {
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "Username cannot be empty");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "Password cannot be empty");
            return map;
        }

        //verify account
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "This account does not exist");
            return map;
        }

        if (user.getStatus() == 0) {
            map.put("usernameMsg", "This account has not been activated");
            return map;
        }

        password = ForumUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "Password is incorrect");
            return map;
        }

        //generate login ticket
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(ForumUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSec * 1000L));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }


    public LoginTicket getLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateAvatar(int userId, String avatarUrl) {
//        return userMapper.updateAvatar(userId, avatarUrl);

        int rows = userMapper.updateAvatar(userId, avatarUrl);
        clearCache(userId);
        return rows;
    }


    public Map<String, Object> updatePassword(int userId, String password, String newPassword) {
        Map<String, Object> map = new HashMap<>();
        User user = userMapper.selectById(userId);
        password = ForumUtil.md5(password + user.getSalt());

        if (!user.getPassword().equals(password)) {
            map.put("oldPasswordMsg", "Password is incorrect");
            return map;
        }

        if (newPassword.length() < 8) {
            map.put("newPasswordMsg", "Password length must be greater than 8");
            return map;
        }
        if (newPassword.equals(password)) {
            map.put("newPasswordMsg", "New password cannot be the same as the old password");
            return map;
        }

        try {
            int i = userMapper.updatePassword(userId, ForumUtil.md5(newPassword + user.getSalt()));
        } catch (Exception e) {
            map.put("newPasswordMsg", "Password update failed");
            return map;
        }
        return map;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    //Prioritize fetching values from cache
    private User getCache(int userId) {
        String redisKey = RedisUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    //If can not get values, initialize the cache data
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }


    //When the data changes, clear the cache data
    private void clearCache(int userId) {
        String redisKey = RedisUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

}

package com.flow.forum;

import com.flow.forum.dao.DiscussPostMapper;
import com.flow.forum.dao.LoginTicketMapper;
import com.flow.forum.dao.UserMapper;
import com.flow.forum.entity.DiscussPost;
import com.flow.forum.entity.LoginTicket;
import com.flow.forum.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class    MapperTests {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;



    @Test
    public void testSelectUser(){
        User user =userMapper.selectByEmail("nowcoder21@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("Bluey");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("bluey@outlook.com");
        user.setAvatarUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());
        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateStatus() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);
        rows = userMapper.updateAvatar(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);
        rows = userMapper.updatePassword(150, "999999");
        System.out.println(rows);
    }

    @Test
    public void testSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149 ,0 ,10);
        for(DiscussPost post: list){
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);

    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000*60*10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
        loginTicketMapper.updateStatus("abc", 1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

}

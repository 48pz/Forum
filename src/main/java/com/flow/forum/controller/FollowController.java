package com.flow.forum.controller;

import com.flow.forum.entity.Page;
import com.flow.forum.entity.User;
import com.flow.forum.service.FollowService;
import com.flow.forum.service.UserService;
import com.flow.forum.util.ForumConstant;
import com.flow.forum.util.ForumUtil;
import com.flow.forum.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@Controller
public class FollowController implements ForumConstant {


    private final UserService userService;

    public FollowController(FollowService followService, HostHolder hostHolder, UserService userService) {
        this.followService = followService;
        this.hostHolder = hostHolder;
        this.userService = userService;
    }

    private final FollowService followService;
    private final HostHolder hostHolder;


    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);
        return ForumUtil.getJSONString(0, "Followed");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return ForumUtil.getJSONString(0, "Unfollow");
    }


    @GetMapping(path = "/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("User cannot found");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ForumConstant.ENTITY_TYPE_USER));
        List<Map<String, Object>> userList = followService.findFollwees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {

                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);
        return "/site/followee";

    }


    @GetMapping(path = "/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("User cannot found");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));
        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {

                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);
        return "/site/follower";

    }

    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }
}

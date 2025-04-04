package com.flow.forum.controller;

import com.flow.forum.annotation.LoginRequired;
import com.flow.forum.entity.User;
import com.flow.forum.service.FollowService;
import com.flow.forum.service.LikeService;
import com.flow.forum.service.UserService;
import com.flow.forum.util.ForumConstant;
import com.flow.forum.util.ForumUtil;
import com.flow.forum.util.HostHolder;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.server.PathParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements ForumConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${forum.path.upload}")
    private String uploadPath;

    @Value("${forum.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile avatarImg, Model model) {
        if (avatarImg == null) {
            model.addAttribute("error", "Please select an image");
            return "/site/setting";
        }
        String filename = avatarImg.getOriginalFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(extension) || !(extension.equals(".jpg") || extension.equals(".png") || extension.equals(".jpeg"))) {
            model.addAttribute("error", "File format is not supported");
            return "/site/setting";
        }

        filename = ForumUtil.generateUUID() + extension;
        File dest = new File(uploadPath + "/" + filename);
        try {
            avatarImg.transferTo(dest);
        } catch (IOException e) {
            logger.error("Failed to upload file{}", e.getMessage());
            throw new RuntimeException("Failed to upload file, server error", e);
        }

        // Update current user's avatar
        User user = hostHolder.getUser();
        String avatarUrl = domain + contextPath + "/user/avatar/" + filename;
        userService.updateAvatar(user.getId(), avatarUrl);
        return "redirect:/index";
    }

    @RequestMapping(path = "avatar/{fileName}", method = RequestMethod.GET)
    public void getAvatar(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        fileName = uploadPath + "/" + fileName;
        String extension = fileName.substring((fileName.lastIndexOf(".")));
        response.setContentType("image/" + extension);
        try (OutputStream os = response.getOutputStream();
             FileInputStream fis = new FileInputStream(fileName)) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("Failed to get avatar{}", e.getMessage());
        }
    }

    @RequestMapping(path = "updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, String confirmPassword, Model model) {
        if (!confirmPassword.equals(newPassword)) {
            model.addAttribute("confirmPasswordMsg",
                    "Passwords do not match");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "Password updated successfully");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("User does not exist.");
        }
        model.addAttribute("user", user);

        int likeCount = likeService.queryUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //following
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        //follower
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        //has followed
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

}

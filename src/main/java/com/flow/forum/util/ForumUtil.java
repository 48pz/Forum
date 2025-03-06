package com.flow.forum.util;

import io.micrometer.common.util.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class ForumUtil {

    //generate random string
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    //MD5 encryption
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}

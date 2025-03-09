package com.flow.forum.util;

public interface ForumConstant {
    int ACTIVATION_SUCCESS = 0;
    int ACTIVATION_REPEAT = 1;
    int ACTIVATION_FAILURE = 2;

    //default expired time
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;
    //remember me expired time
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;
}

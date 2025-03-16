package com.flow.forum.dao;

import com.flow.forum.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    //query message list by id, return the newest 1 message
    List<Message> selectConversations(int userId, int offset, int limit);

    int selectConversationCount(int userId);

    List<Message> selectDMs(String conversationId, int offset, int limit);

    int selectDMCount(String conversationId);

    int selectUnreadCount(int userId, String conversationId);

    int insertMessage(Message message);

    int updateStatus(List<Integer> ids, int status);

}

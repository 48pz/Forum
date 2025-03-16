package com.flow.forum.service;

import com.flow.forum.dao.MessageMapper;
import com.flow.forum.entity.Message;
import com.flow.forum.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    public MessageService(MessageMapper messageMapper, SensitiveFilter sensitiveFilter) {
        this.messageMapper = messageMapper;
        this.sensitiveFilter = sensitiveFilter;
    }

    private final MessageMapper messageMapper;
    private final SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findDMs(String conversationId, int offset, int limit) {
        return messageMapper.selectDMs(conversationId, offset, limit);
    }

    public int findDMCount(String conversationId) {
        return messageMapper.selectDMCount(conversationId);
    }

    public int findUnreadCount(int userId, String conversationId) {
        return messageMapper.selectUnreadCount(userId, conversationId);
    }

    public int addMessage(Message message) {
        message.setContent((HtmlUtils.htmlEscape(message.getContent())));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }

}

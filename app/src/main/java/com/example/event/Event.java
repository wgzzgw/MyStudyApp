package com.example.event;

import cn.jpush.im.android.api.model.Conversation;

/**
 * Created by yy on 2018/5/25.
 */
/*
* 自定义事件
* */
public class Event {
    private EventType type;
    private Conversation conversation;
    public Event(EventType type, Conversation conv) {
        this.type = type;
        this.conversation = conv;
    }
    public EventType getType() {
        return type;
    }

    public Conversation getConversation() {
        return conversation;
    }
    public static class Builder {
        private EventType type;
        private Conversation conversation;

        public Builder setType(EventType type) {
            this.type = type;
            return this;
        }

        public Builder setConversation(Conversation conv) {
            this.conversation = conv;
            return this;
        }

        public Event build() {
            return new Event(type, conversation);
        }

    }

}

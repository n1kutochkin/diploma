package org.drinkless.tdlib.example;

import org.drinkless.tdlib.TdApi;

public class SwingChat {
    private TdApi.Chat chat;
    private String chatName;

    public SwingChat(TdApi.Chat chat) {
        this.chat = chat;
        this.chatName = chat.title;
    }

    public TdApi.Chat getChat() {
        return chat;
    }

    public String getChatName() {
        return chatName;
    }

    @Override
    public String toString() {
        return chatName;
    }
}

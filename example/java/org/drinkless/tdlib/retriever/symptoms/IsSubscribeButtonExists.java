package org.drinkless.tdlib.retriever.symptoms;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.retriever.Symptomatic;

public class IsSubscribeButtonExists implements Symptomatic {

    private TdApi.MessageText text;
    private TdApi.ReplyMarkupInlineKeyboard keyboard;

    @Override
    public Boolean isProvided(TdApi.Message message) {
        if (message.content instanceof TdApi.MessageText) {
            this.text = text;
        } else {
            return false;
        }

        if (message.replyMarkup instanceof TdApi.ReplyMarkupInlineKeyboard) {
            this.keyboard = (TdApi.ReplyMarkupInlineKeyboard) message.replyMarkup;
        }

        return false;
    }

    @Override
    public void findSymptom() {

    }
}

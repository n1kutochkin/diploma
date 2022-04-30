package org.drinkless.tdlib.retriever;

import org.drinkless.tdlib.TdApi;

public interface TgTextContentRetrievable {
    default TdApi.FormattedText retrieve(TdApi.Message message) {
        TdApi.MessageContent content = message.content;

        if (content instanceof TdApi.MessagePhoto) {
            return  ((TdApi.MessagePhoto) content).caption;
        } else if (content instanceof TdApi.MessageAnimation) {
            return  ((TdApi.MessageAnimation) content).caption;
        } else if (content instanceof TdApi.MessageText) {
            return ((TdApi.MessageText) content).text;
        } else if (content instanceof TdApi.MessageVideo) {
            return  ((TdApi.MessageVideo) content).caption;
        } else {
            throw new RuntimeException("Несовместимый тип сообщений");
        }
    }
}

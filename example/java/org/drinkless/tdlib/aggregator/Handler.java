package org.drinkless.tdlib.aggregator;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

public class Handler implements Client.ResultHandler {

    private TdApi.Messages messages = null;

    @Override
    public void onResult(TdApi.Object object) {
        if (object instanceof TdApi.Messages) {
            this.messages = (TdApi.Messages) object;
        }
    }

    public boolean isReceived() {
        return messages != null ? true : false;
    }

    public TdApi.Messages getMessages() {
        return messages;
    }
}

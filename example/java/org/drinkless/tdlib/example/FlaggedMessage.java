package org.drinkless.tdlib.example;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.retriever.Flag;

import java.util.EnumSet;

public class FlaggedMessage {

    public FlaggedMessage(EnumSet<Flag> flags, TdApi.Message message) {
        this.flags = flags;
        this.message = message;
    }

    private EnumSet<Flag> flags;
    private TdApi.Message message;


    public TdApi.Message getMessage() {
        return message;
    }

    public void setMessage(TdApi.Message message) {
        this.message = message;
    }

    public EnumSet<Flag> getFlags() {
        return flags;
    }

    public void setFlags(EnumSet<Flag> flags) {
        this.flags = flags;
    }

    @Override
    public String toString() {
        return "FlaggedMessage{" +
                "flags=" + flags +
                ", message=" + message +
                '}';
    }
}

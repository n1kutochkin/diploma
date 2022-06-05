package org.drinkless.tdlib.example;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.retriever.Flag;

import java.util.EnumSet;

public class FlaggedMessage {

    public FlaggedMessage(EnumSet<Flag> flags, String message) {
        this.flags = flags;
        this.message = message;
    }

    private EnumSet<Flag> flags;
    private String message;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
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

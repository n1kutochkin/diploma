package org.drinkless.tdlib.retriever;

import org.drinkless.tdlib.TdApi;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class Retriever {
    protected EnumSet<Flag> flags;

    public Retriever(EnumSet<Flag> flags) {
        this.flags = EnumSet.copyOf(flags);
    }

    public Retriever() {
        this.flags = EnumSet.noneOf(Flag.class);
    }

    //    private TdApi.Message message = null;
//    private List<Symptomatic> symptoms;

//    public void load(TdApi.Message message) {
//        this.message = message;
//    }
//
//    public void analyze() {
//        if (!Optional.ofNullable(message).isPresent()) {
//            throw new RuntimeException("No message provided");
//        } else {
//            symptoms.stream().map(s -> s.isProvided(message));
//        }
//    }
}

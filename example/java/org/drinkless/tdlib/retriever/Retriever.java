package org.drinkless.tdlib.retriever;

import org.drinkless.tdlib.TdApi;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Retriever implements Runnable, Applicable, TgTextContentRetrievable, Callable<EnumSet<Flag>> {
    protected EnumSet<Flag> flags;
    protected TdApi.FormattedText text;
    protected Logger logger;

    public Retriever(EnumSet<Flag> flags, TdApi.FormattedText text) {
        this.text = text;
        this.flags = EnumSet.copyOf(flags);
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    public Retriever(EnumSet<Flag> flags, TdApi.Message message) {
        this.text = retrieve(message);
        this.flags = flags;
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    public Retriever() {

    }

    public EnumSet<Flag> getFlags() {
        return flags;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, this.getClass().getName() + "is started");
        apply(text, flags);
    }

    @Override
    public EnumSet<Flag> call() throws Exception {
        return apply(text, flags);
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

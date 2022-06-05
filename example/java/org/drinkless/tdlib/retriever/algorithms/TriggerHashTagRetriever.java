package org.drinkless.tdlib.retriever.algorithms;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.retriever.*;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TriggerHashTagRetriever extends Retriever {

    public TriggerHashTagRetriever(EnumSet<Flag> flags, TdApi.FormattedText text) {
        super(flags, text);
    }

    public TriggerHashTagRetriever(EnumSet<Flag> flags, TdApi.Message message) {
        super(flags, message);
    }

    private boolean hashTagFilter(TdApi.TextEntity entity) {
        return entity.type instanceof TdApi.TextEntityTypeHashtag;
    }

    private void logInfo(String s) {
        logger.log(Level.INFO, s);
    }

    @Override
    public EnumSet<Flag> apply(TdApi.FormattedText text, EnumSet<Flag> existingFlags) {
        List<TdApi.TextEntity> hashTags = Arrays.stream(text.entities)
                .filter(this::hashTagFilter)
                .collect(Collectors.toList());
        hashTags.stream()
                .map(String::valueOf)
                .forEach(this::logInfo);
        return null;
    }

    @Override
    public EnumSet<Flag> apply(TdApi.Message message, EnumSet<Flag> flags) {
        return this.apply(message, flags);
    }
}

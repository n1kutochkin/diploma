package org.drinkless.tdlib.retriever.algorithms;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.retriever.Applicable;
import org.drinkless.tdlib.retriever.Flag;
import org.drinkless.tdlib.retriever.TextRetriever;
import org.drinkless.tdlib.retriever.TgTextContentRetrievable;

import java.util.EnumSet;

public class TriggerHashTagRetriever extends TextRetriever implements Applicable, TgTextContentRetrievable {

    @Override
    public EnumSet<Flag> apply(TdApi.FormattedText text, EnumSet<Flag> existingFlags) {
        return null;
    }

    @Override
    public EnumSet<Flag> apply(TdApi.Message message, EnumSet<Flag> flags) {
        return null;
    }
}

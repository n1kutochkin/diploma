package org.drinkless.tdlib.retriever.algorithms;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.retriever.*;

import java.util.EnumSet;

public class TriggerHashTagRetriever extends Retriever implements Applicable, TgTextContentRetrievable {

    @Override
    public EnumSet<Flag> apply(TdApi.FormattedText text, EnumSet<Flag> existingFlags) {
        return null;
    }

    @Override
    public EnumSet<Flag> apply(TdApi.Message message, EnumSet<Flag> flags) {
        return null;
    }
}

package org.drinkless.tdlib.retriever;

import org.drinkless.tdlib.TdApi;

import java.util.EnumSet;

public interface Applicable {
    EnumSet<Flag> apply(TdApi.FormattedText text, EnumSet<Flag> existingFlags);
    EnumSet<Flag> apply(TdApi.Message message, EnumSet<Flag> flags);
}

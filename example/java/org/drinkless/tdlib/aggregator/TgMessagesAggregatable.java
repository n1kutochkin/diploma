package org.drinkless.tdlib.aggregator;

import org.drinkless.tdlib.TdApi;

public interface TgMessagesAggregatable {
    TdApi.Messages aggregate(TdApi.Messages messages);
}

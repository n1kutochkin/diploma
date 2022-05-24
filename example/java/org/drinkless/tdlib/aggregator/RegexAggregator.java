package org.drinkless.tdlib.aggregator;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.retriever.TgTextContentRetrievable;

public class RegexAggregator extends Aggregator implements TgMessagesAggregatable, TgTextContentRetrievable {
    private String regex;

    public RegexAggregator(String regex) {
        super();
        this.regex = regex;
    }

    @Override
    public TdApi.Messages aggregate(TdApi.Messages messages) {
        return null;
    }
}

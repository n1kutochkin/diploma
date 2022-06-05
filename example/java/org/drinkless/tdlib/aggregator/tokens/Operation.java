package org.drinkless.tdlib.aggregator.tokens;

import org.drinkless.tdlib.TdApi;

import java.util.HashSet;
import java.util.Set;

public abstract class Operation extends Token {
    Set<TdApi.Message> resulted = null;

    public Set<TdApi.Message> getResulted() {
        return resulted;
    }

    public void setResulted(HashSet<TdApi.Message> resulted) {
        this.resulted = resulted;
    }
}

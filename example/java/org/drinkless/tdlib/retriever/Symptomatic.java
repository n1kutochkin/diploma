package org.drinkless.tdlib.retriever;

import org.drinkless.tdlib.TdApi;

public interface Symptomatic {
    public Boolean isProvided(TdApi.Message message);
    public void findSymptom();
}

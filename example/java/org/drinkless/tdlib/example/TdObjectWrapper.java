package org.drinkless.tdlib.example;

import org.drinkless.tdlib.TdApi;

public class TdObjectWrapper<T > {

    private boolean isChat = false;
    public T object;

    public TdObjectWrapper(T object) {
        if (object instanceof TdApi.Chat) {
            isChat = true;
        }
        this.object = object;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    @Override
    public String toString() {
        if (isChat) {
            return ((TdApi.Chat) object).title;
        }
        return object.toString();
    }
}

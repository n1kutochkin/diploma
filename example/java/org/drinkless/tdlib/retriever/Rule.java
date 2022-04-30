package org.drinkless.tdlib.retriever;

public abstract class Rule {
    boolean isProvided;

    abstract void apply();
}

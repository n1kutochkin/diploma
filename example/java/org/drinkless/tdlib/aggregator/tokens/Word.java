package org.drinkless.tdlib.aggregator.tokens;

public class Word extends Token {
    String result;

    public Word(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "Word{" +
                "result='" + result + '\'' +
                '}';
    }
}

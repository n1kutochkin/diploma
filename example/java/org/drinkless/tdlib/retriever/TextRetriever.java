package org.drinkless.tdlib.retriever;

import ru.textanalysis.tawt.graphematic.parser.text.GParserImpl;
import ru.textanalysis.tawt.graphematic.parser.text.GraphematicParser;

import java.util.EnumSet;

public class TextRetriever extends Retriever {

    protected static GraphematicParser parser;

    static {
        parser = new GParserImpl();
    }

    public TextRetriever(EnumSet<Flag> flags) {
        super(flags);
    }

    public TextRetriever() {
        super();
    }
}

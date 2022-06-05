package org.drinkless.tdlib.retriever;

import org.drinkless.tdlib.TdApi;
import ru.textanalysis.tawt.graphematic.parser.text.GParserImpl;
import ru.textanalysis.tawt.graphematic.parser.text.GraphematicParser;
import ru.textanalysis.tawt.jmorfsdk.JMorfSdk;
import ru.textanalysis.tawt.jmorfsdk.JMorfSdkFactory;

import java.util.EnumSet;

public class TextRetriever extends Retriever {

    protected volatile static ModifiedGraphematicParser parser;
    protected volatile static JMorfSdk jMorfSdk;

    static {
        parser = new GParserModImpl();
        jMorfSdk = JMorfSdkFactory.loadFullLibrary();
    }

    public TextRetriever(EnumSet<Flag> flags, TdApi.FormattedText text) {
        super(flags, text);
    }

    public TextRetriever(EnumSet<Flag> flags, TdApi.Message message) {
        super(flags, message);
    }

    public TextRetriever() {

    }

    @Override
    public EnumSet<Flag> apply(TdApi.FormattedText text, EnumSet<Flag> existingFlags) {
        return null;
    }

    @Override
    public EnumSet<Flag> apply(TdApi.Message message, EnumSet<Flag> flags) {
        return null;
    }
}

package org.drinkless.tdlib.aggregator;

import org.drinkless.tdlib.retriever.GParserModImpl;
import org.drinkless.tdlib.retriever.ModifiedGraphematicParser;
import ru.textanalysis.tawt.jmorfsdk.JMorfSdk;
import ru.textanalysis.tawt.jmorfsdk.JMorfSdkFactory;

public abstract class Aggregator {
    private ModifiedGraphematicParser parser;
    private JMorfSdk morphology;

    public Aggregator() {
        this.parser = new GParserModImpl();
        this.morphology = JMorfSdkFactory.loadFullLibrary();
    }
}

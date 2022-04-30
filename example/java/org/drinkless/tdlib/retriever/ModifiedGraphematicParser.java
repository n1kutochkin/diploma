package org.drinkless.tdlib.retriever;

import ru.textanalysis.tawt.graphematic.parser.text.GraphematicParser;

import java.util.List;

public interface ModifiedGraphematicParser extends GraphematicParser {
    List<String> parserTextToLowerCaseList(String text);
    List<String> parserTextToList(String text);
}

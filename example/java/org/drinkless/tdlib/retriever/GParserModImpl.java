package org.drinkless.tdlib.retriever;

import ru.textanalysis.tawt.graphematic.parser.text.GParserImpl;
import ru.textanalysis.tawt.graphematic.parser.text.GraphematicParser;

import java.util.List;
import java.util.stream.Collectors;

public class GParserModImpl extends GParserImpl implements ModifiedGraphematicParser {

    public List<String> parserTextToList(String text) {
        return parserText(text)
                .stream()
                .flatMap(List::stream)
                .flatMap(List::stream)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<String> parserTextToLowerCaseList(String text) {
        return parserTextToList(text).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

}

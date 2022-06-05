package org.drinkless.tdlib.aggregator;

import org.drinkless.tdlib.aggregator.tokens.*;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Lexer {
    String input;
    CharacterIterator iterator;

    Pattern pattern = Pattern.compile("[^|&()]+|[|&()]");

    Matcher matcher;

    public Lexer(String input) {
        this.input = input.replaceAll("[ \\r\\t\\n]+", "");
        this.matcher = pattern.matcher(this.input);
    }

    public static void main(String[] args) {
        var lexer = new Lexer("(Java|Kotlin)\n" +
                "   &(Kubernetes|OpenShift)\n" +
                "   &(Jenkins|TeamCity)\n" +
                "   &(Spring|Jakarta)");
        lexer.getAllTokens()
                .stream()
                .map(String::valueOf)
                .forEach(System.out::println);
    }

    public List<Token> getAllTokens() {
        return matcher.results()
                .map(x -> switch (x.group()) {
                    case "&" -> new And();
                    case "|" -> new Or();
                    case "(" -> new OpenBracket();
                    case ")" -> new CloseBracket();
                    case default -> new Word(x.group());
                })
                .collect(Collectors.toList());
    }




}

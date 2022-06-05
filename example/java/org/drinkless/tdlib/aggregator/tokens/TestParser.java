package org.drinkless.tdlib.aggregator.tokens;

import org.drinkless.tdlib.aggregator.Lexer;
import org.drinkless.tdlib.aggregator.Parser;

public class TestParser {
    public static void main(String[] args) {
        var input = "(Java|Kotlin)\n" +
                "   &(Kubernetes|OpenShift)\n" +
                "   &(Jenkins|TeamCity)\n" +
                "   &(Spring|Jakarta)";
//        var input = "A|(A&B)";
        var lexer = new Lexer(input);
        var allTokens = lexer.getAllTokens();
        var parser = new Parser(allTokens);
        var result = parser.parse();
    }
}

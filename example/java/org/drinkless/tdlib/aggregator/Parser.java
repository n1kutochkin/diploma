package org.drinkless.tdlib.aggregator;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.aggregator.tokens.*;


import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class Parser {

    Logger logger = Logger.getLogger(String.valueOf(Parser.class));
    List<Token> tokens;
    Iterator<Token> iterator;
    Token currentToken;

    private void next() {
        this.currentToken = iterator.next();
    }



    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        iterator = tokens.iterator();
        next();
    }

    public Node<Token> parse() {
        var res = S();
        return res;
    }

    private Node<Token> S() {
        var x = X();
        if (currentToken instanceof Or) {
            var buff = currentToken;
            if (iterator.hasNext()) {
                next();
            }
            var s = S();
            return new Node<>(buff, x, s); //TODO
        } else {
            return x;
        }
    }

    private Node<Token> X() {
        var v = V();

        if (currentToken instanceof And) {
            var buff = currentToken;
            if (iterator.hasNext()) {
                next();
            }
            var x = X();
            return new Node<>(buff, v, x); //TODO
        } else {
            return v;
        }
    }


    private Node<Token> V() {
        if (currentToken instanceof Word) {
            var buff = currentToken;
            if (iterator.hasNext()) {
                next();
            }
            return new Node<>(buff, null, null); // TODO: 05.06.2022
        } else if (currentToken instanceof OpenBracket) {
            if (iterator.hasNext()) {
                next();
            }
            var s = S();
            if (!(currentToken instanceof CloseBracket)) {
                throw new RuntimeException();
            }
            if (iterator.hasNext()) {
                next();
            }
            return s;
        } else {
            throw new RuntimeException();
        }
    }

//    private Node<Token> V(Token token) {
//        if (token instanceof Word) {
//            var operation = S(iterator.next());
//            if (operation instanceof Operation) {
//                var s1 = S(iterator.next());
//                if (s1 instanceof Word) {
//                    var messages = find(s1);
//                    var messages1 = find(token);
//                    var set = Set.of(messages1.messages);
//                    var set2 = Set.of(messages.messages);
//                    Set<TdApi.Message> result = new HashSet<>(set);
//                    if (operation instanceof Or) {
//                        result.addAll(set2);
//                    }
//                    if (operation instanceof And) {
//                        result.removeAll(set2);
//                    }
//                } else {
//
//                }
//            }
//            if (operation instanceof OpenBracket) {
//                var s = S(iterator.next());
//                if (s instanceof Word) {
//                    var s1 = S(iterator.next());
//                    if (s1 instanceof Operation) {
//                        var s2 = S(iterator.next());
//                        if (s2 instanceof Word) {
//
//                        }
//                    }
//                }
//            }
//        }
//    }


}

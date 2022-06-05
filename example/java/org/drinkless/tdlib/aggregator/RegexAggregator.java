package org.drinkless.tdlib.aggregator;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.aggregator.tokens.*;
import org.drinkless.tdlib.retriever.TgTextContentRetrievable;

import java.lang.reflect.Array;
import java.util.*;

public class RegexAggregator extends Aggregator implements TgMessagesAggregatable, TgTextContentRetrievable {
    private String expression;
    private Client client;
    private Client.ResultHandler handler;

    public RegexAggregator(String expression, Client.ResultHandler handler, Client client) {
        super();
        this.expression = expression;
        this.handler = handler;
        this.client = client;
    }

    @Override
    public void aggregate() {
        var lexer = new Lexer(expression);
        var parser = new Parser(lexer.getAllTokens());
        var result = parser.parse();
        var node = traversePostOrder(result);

        if (node.value instanceof Operation) {
            var resulted = ((Operation) node.value).getResulted();
            var objects = resulted.toArray();
            TdApi.Message[] array = new TdApi.Message[resulted.size()];

            for (int i = 0; i < resulted.size(); i++) {
                array[i] = (TdApi.Message) objects[i];
            }
            handler.onResult(new TdApi.Messages(resulted.size(), array));
        } else if (node.value instanceof Word) {
            handler.onResult(search(((Word) node.value).getResult()));
        } else {
            throw new RuntimeException();
        }
    }

    public Node<Token> traversePostOrder(Node<Token> node) {
        return Optional.ofNullable(node)
                .map(t -> {
                    var left = traversePostOrder(t.left);
                    var right = traversePostOrder(t.right);

                    HashSet<TdApi.Messages> resultSet;

                    if (left != null & right != null) {
                        if (left.value instanceof Word & right.value instanceof Word) {
                            //TOOD send messages and and find required set
                            var leftQueryResult = search(((Word) left.value).getResult()).messages;
                            var rightQueryResult = search(((Word) right.value).getResult()).messages;

                            var leftSet = Set.of(leftQueryResult);
                            var rightSet = Set.of(rightQueryResult);

                            makeOperation(t.value, leftSet, rightSet);
                        }
                        if (left.value instanceof Operation & right.value instanceof Word) {
                            var resulted = ((Operation) left.value).getResulted();
                            var rightQueryResult = search(((Word) right.value).getResult()).messages;
                            var rightSet = Set.of(rightQueryResult);

                            makeOperation(t.value, resulted, rightSet);
                        }
                        if (right.value instanceof Operation & left.value instanceof Word) {
                            var resulted = ((Operation) right.value).getResulted();
                            var leftQueryResult = search(((Word) left.value).getResult()).messages;
                            var leftSet = Set.of(leftQueryResult);

                            makeOperation(t.value, resulted, leftSet);
                        }
                        if (right.value instanceof Operation & left.value instanceof Operation) {
                            var resultedLeft = ((Operation) left.value).getResulted();
                            var resultedRight = ((Operation) right.value).getResulted();

                            makeOperation(t.value, resultedLeft, resultedRight);
                        }
                    }

                    System.out.print(" " + t.value);
                    return t;
                })
                .orElse(null);
    }


    private void makeOperation(Token token, Set<TdApi.Message> left, Set<TdApi.Message> right) {
        HashSet<TdApi.Message> resultSet = new HashSet<>(left);

        if (token instanceof Operation) {
            if (token instanceof Or) {
                resultSet.addAll(right);
                ((Or) token).setResulted(resultSet);
            }
            if (token instanceof And) {
                resultSet.retainAll(right);
                ((And) token).setResulted(resultSet);
            }
        }
    }

    private TdApi.Messages search(String query) {
        var handler = new Handler();

        client.send(
                new TdApi.SearchMessages(
                        null,
                        query,
                        0,
                        0,
                        0,
                        10,
                        null,
                        0,
                        0
                ),
                handler);

        while (!handler.isReceived()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return handler.getMessages();
    }
}

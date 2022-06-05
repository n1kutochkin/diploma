package org.drinkless.tdlib.aggregator;

import org.drinkless.tdlib.aggregator.tokens.Token;
import org.drinkless.tdlib.aggregator.tokens.Word;

import java.util.Optional;

public class TreeHelper {

    public Node<Token> traversePostOrder(Node<Token> node) {
        return Optional.ofNullable(node)
                .map(t -> {
                    var left = traversePostOrder(t.left);
                    var right = traversePostOrder(t.right);

                    if (left != null & right != null) {
                        //TOOD send messages and and find required set
                    }

                    System.out.print(" " + t.value);
                    return t;
                })
                .orElse(null);
    }

}

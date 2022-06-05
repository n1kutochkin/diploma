package org.drinkless.tdlib.retriever.algorithms;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.retriever.Applicable;
import org.drinkless.tdlib.retriever.Flag;
import org.drinkless.tdlib.retriever.TextRetriever;
import org.drinkless.tdlib.retriever.TgTextContentRetrievable;
import ru.textanalysis.tawt.ms.grammeme.MorfologyParameters;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TriggerWordsRetriever extends TextRetriever {

    private static Set triggerWords = Set.of(
            "промокод",
            "скидка",
            "код",
            "акция"
    );

    public TriggerWordsRetriever(EnumSet<Flag> flags, TdApi.FormattedText text) {
        super(flags, text);
    }

    public TriggerWordsRetriever(EnumSet<Flag> flags, TdApi.Message message) {
        super(flags, message);
    }

    @Override
    public EnumSet<Flag> apply(TdApi.FormattedText text, EnumSet<Flag> existingFlags) {
        var words = parser.parserTextToLowerCaseList(text.text);

        var nouns = words.stream()
                .map(w -> jMorfSdk.getOmoForms(w)
                        .stream()
                        .filter(f -> f.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.NOUN)
                        .findFirst())
                .filter(x -> x.isPresent())
                .map(n -> n.get().getInitialFormString())
                .collect(Collectors.toSet());

        var intersection = nouns.stream()
                .filter(triggerWords::contains)
                .collect(Collectors.toSet());

        if (!intersection.isEmpty()) {
            flags.add(Flag.TRIGGER_WORD);
        }

        return flags;
    }

    @Override
    public EnumSet<Flag> apply(TdApi.Message message, EnumSet<Flag> flags) {
        return null;
    }
}

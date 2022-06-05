package org.drinkless.tdlib.retriever.algorithms;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.retriever.*;
import ru.textanalysis.tawt.graphematic.parser.text.GParserImpl;
import ru.textanalysis.tawt.graphematic.parser.text.GraphematicParser;
import ru.textanalysis.tawt.jmorfsdk.JMorfSdk;
import ru.textanalysis.tawt.jmorfsdk.JMorfSdkFactory;
import ru.textanalysis.tawt.ms.grammeme.MorfologyParameters.*;
import ru.textanalysis.tawt.ms.model.jmorfsdk.Form;
import ru.textanalysis.tawt.ms.model.jmorfsdk.InitialForm;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ImperativeVerbsRetriever extends TextRetriever {

    public static final String PUNCT_REGEX = "[-?,!:—]";
    public static final String RUSSIAN_WORD = "([а-я]+)|([a-я]+-[а-я])";
    public static final String SIMPLE_RUSSIAN_WORD = "[а-я-]+";
    public static final String EMPTY_STRING = "";
    static JMorfSdk jMorfSdk;
    static GraphematicParser parser;

    private static final Set<String> TRIGGER_VERBS = Set.of(
            "подписываюсь",
            "присоединяюсь",
            "успею",
            "действую",
            "перехожу"
    );

    static {
        jMorfSdk = JMorfSdkFactory.loadFullLibrary();
        parser = new GParserImpl();
    }

    public ImperativeVerbsRetriever(EnumSet<Flag> flags, TdApi.FormattedText text) {
        super(flags, text);
    }

    public ImperativeVerbsRetriever(EnumSet<Flag> flags, TdApi.Message message) {
        super(flags, message);
    }

    public ImperativeVerbsRetriever() {
    }

    public static Set<String> normalizedString(String data) {
        Pattern pattern = Pattern.compile(SIMPLE_RUSSIAN_WORD);
        Matcher matcher = pattern.matcher(data.toLowerCase(Locale.ROOT));
        Set<String> dataSet = matcher.results().map(x -> x.group()).collect(Collectors.toSet());
        return dataSet;
    }

    public EnumSet<Flag> apply(String text, EnumSet<Flag> existingFlags) {
        EnumSet<Flag> newFlags = EnumSet.copyOf(existingFlags);

        ModifiedGraphematicParser parser = new GParserModImpl();

        List<String> normalizedWords = parser.parserTextToLowerCaseList(text);

        Set<String> imperativeVerbs = normalizedWords
                .stream()
                .map(word -> {
                    Optional<Form> first = jMorfSdk.getOmoForms(word)
                            .stream()
                            .filter(form -> form.getTypeOfSpeech() == TypeOfSpeech.VERB)
                            .findFirst();
                    return first;
                }) // омоформы только глаголов
                .filter(x -> x.isPresent())
                .filter(verb -> verb.get().isContainsMorphCharacteristic(Mood.class, Mood.IMPERATIVE))
                .map(verb -> verb.get().getInitialFormString())
                .collect(Collectors.toSet());

        if (!imperativeVerbs.isEmpty()) {
            newFlags.add(Flag.ACTION);
            var intersection = imperativeVerbs.stream()
                    .filter(TRIGGER_VERBS::contains)
                    .collect(Collectors.toSet());
            if (!intersection.isEmpty()) {
                newFlags.add(Flag.TRIGGER_ACTION);
            }
        }
        return newFlags;
    }

    @Override
    public EnumSet<Flag> apply(TdApi.FormattedText content, EnumSet<Flag> existingFlags) {
        return apply(content.text, existingFlags);
        //String data = content.text.toLowerCase(Locale.ROOT);
        // data.replaceAll(PUNCT_REGEX, EMPTY_STRING);

//        String buff = null;
//        normalizedString(content.text)
//                .stream()
//                .map(word -> {
//                    Optional<Form> first = jMorfSdk.getOmoForms(word)
//                            .stream()
//                            .filter(form -> form.getTypeOfSpeech() == TypeOfSpeech.VERB)
//                            .findFirst();
//                    return first;
//                }) // омоформы только глаголов
//                .filter(x -> x.isPresent())
//                .filter(verb -> verb.get().isContainsMorphCharacteristic(Mood.class, Mood.IMPERATIVE))
//                .map(verb -> verb.get().getInitialForm())
//                .collect(Collectors.toSet());
//
//        normalizedString(content.text)
//                .stream()
//                .map(word -> {
//                    jMorfSdk.getOmoForms(word)
//                            .stream()
//                            .filter(form -> form.getTypeOfSpeech() == TypeOfSpeech.VERB)
//                            .findFirst().
//                    return jMorfSdk.getOmoForms(word)
//                            .stream()
//                            .filter(form -> form.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.VERB);
//                });

//        normalizedString(content.text)
//                .stream()
//                .map(word -> {
////                    jMorfSdk.getO
////                    jMorfSdk.getAllCharacteristicsOfForm(word)
////                            .stream()
////                            .filter(form -> )
////                    jMorfSdk.getAllCharacteristicsOfForm(word)
////                            .forEach(form -> {
////                                if (form.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.VERB)
////                            });
//                });
//        return null;
    }

//    @Override
//    public EnumSet<Flag> apply(TdApi.FormattedText text, EnumSet<Flag> addedFlags) {
//        return apply(text.text, addedFlags);
//    }

    @Override
    public EnumSet<Flag> apply(TdApi.Message message, EnumSet<Flag> f) {
        return apply(retrieve(message), f);
    }


}

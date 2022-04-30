package org.drinkless.tdlib.retriever.algorithms;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.retriever.Applicable;
import org.drinkless.tdlib.retriever.Flag;
import org.drinkless.tdlib.retriever.Retriever;
import org.drinkless.tdlib.retriever.TgTextContentRetrievable;

import java.util.*;
import java.util.stream.Collectors;

public class WebsiteLinksRetriever extends Retriever implements Applicable, TgTextContentRetrievable {

    private static final String TELEGRAM_DOMAIN = "t.me";

    public WebsiteLinksRetriever(EnumSet<Flag> flags) {
        super(flags);
    }

    public WebsiteLinksRetriever() {
        super();
    }


    @Override
    public EnumSet<Flag> apply(TdApi.FormattedText content, EnumSet<Flag> existingFlags) {

        EnumSet<Flag> newFlags = EnumSet.copyOf(existingFlags);

        TdApi.TextEntity[] entities = content.entities;

        List<String> urls = Arrays.stream(entities)
                .filter(x -> x.type instanceof TdApi.TextEntityTypeTextUrl)
                .map(x -> ((TdApi.TextEntityTypeTextUrl) x.type).url)
                .collect(Collectors.toList());

        HashSet<String> uniqueUrls = new HashSet<>(urls);

        HashMap<String, Long> pairs = new HashMap<>();

        uniqueUrls.forEach(url ->
            pairs.put(
                    url, urls.stream().filter(s -> s.equals(url)).count()
            )
        );

        Set<String> tgUrls = uniqueUrls.stream().filter(url -> url.contains(TELEGRAM_DOMAIN)).collect(Collectors.toSet());

        tgUrls.forEach(url -> {
            if (pairs.get(url) > 1) {
                newFlags.add(Flag.MULTIPLE_CHANNEL_LINKS);
            } else {
                newFlags.add(Flag.SINGLE_CHANNEL_LINK);
            }
        });

        uniqueUrls.removeAll(tgUrls);

        uniqueUrls.forEach(url -> {
            if (pairs.get(url) > 1) {
                newFlags.add(Flag.MULTIPLE_WEBSITE_LINKS);
            } else {
                newFlags.add(Flag.SINGLE_WEBSITE_LINK);
            }
        });

        return newFlags;
    }

//    @Override
//    public EnumSet<Flag> apply(TdApi.FormattedText text, EnumSet<Flag> f) {
//        this.apply(text, f);
//        return null;
//    }

    @Override
    public EnumSet<Flag> apply(TdApi.Message message, EnumSet<Flag> f) {
        return this.apply(this.retrieve(message), f);
    }
}

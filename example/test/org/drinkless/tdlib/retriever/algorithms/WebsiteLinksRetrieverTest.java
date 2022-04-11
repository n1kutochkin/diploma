package org.drinkless.tdlib.retriever.algorithms;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.retriever.Flag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class WebsiteLinksRetrieverTest {

    WebsiteLinksRetriever retriever = new WebsiteLinksRetriever();
    private static final EnumSet<Flag> EMPTY_FLAGS = EnumSet.noneOf(Flag.class);

    @Test
    void apply() {

        TdApi.FormattedText data = new TdApi.FormattedText(
                "Empty text",
                new TdApi.TextEntity[]{
                        new TdApi.TextEntity(0, 0, new TdApi.TextEntityTypeTextUrl("vk.com"))
                }
        );

        EnumSet<Flag> result = retriever.apply(data, EnumSet.noneOf(Flag.class));

        assertEquals(EnumSet.of(Flag.SINGLE_WEBSITE_LINK), result);
    }

    @Test
    void telegramRetrieve() {
        TdApi.FormattedText data = new TdApi.FormattedText(
                "Empty text",
                new TdApi.TextEntity[]{
                        new TdApi.TextEntity(0, 0, new TdApi.TextEntityTypeTextUrl("t.me/n1kutochkin"))
                }
        );

        assertEquals(EnumSet.of(Flag.SINGLE_CHANNEL_LINK), retriever.apply(data, EnumSet.noneOf(Flag.class)));
    }

    @Test
    void multipleTgRetrieve() {
        TdApi.FormattedText data = new TdApi.FormattedText(
                "Empty text",
                new TdApi.TextEntity[]{
                        new TdApi.TextEntity(0, 0, new TdApi.TextEntityTypeTextUrl("vk.com")),
                        new TdApi.TextEntity(0, 0, new TdApi.TextEntityTypeTextUrl("vk.com"))
                }
        );

        assertEquals(EnumSet.of(Flag.MULTIPLE_WEBSITE_LINKS), retriever.apply(data, EMPTY_FLAGS));
    }

    @Test
    void multipleWebRetrieve() {
        TdApi.FormattedText data = new TdApi.FormattedText(
                "Empty text",
                new TdApi.TextEntity[]{
                        new TdApi.TextEntity(0, 0, new TdApi.TextEntityTypeTextUrl("t.me/n1kutochkin")),
                        new TdApi.TextEntity(0, 0, new TdApi.TextEntityTypeTextUrl("t.me/n1kutochkin"))
                }
        );

        assertEquals(EnumSet.of(Flag.MULTIPLE_CHANNEL_LINKS), retriever.apply(data, EMPTY_FLAGS));
    }

    @Test
    void TgWebRetrieve() {
        TdApi.FormattedText data = new TdApi.FormattedText(
                "Empty text",
                new TdApi.TextEntity[]{
                        new TdApi.TextEntity(0, 0, new TdApi.TextEntityTypeTextUrl("vk.com")),
                        new TdApi.TextEntity(0, 0, new TdApi.TextEntityTypeTextUrl("t.me/n1kutochkin"))
                }
        );

        assertEquals(EnumSet.of(Flag.SINGLE_WEBSITE_LINK, Flag.SINGLE_CHANNEL_LINK), retriever.apply(data, EMPTY_FLAGS));
    }
}
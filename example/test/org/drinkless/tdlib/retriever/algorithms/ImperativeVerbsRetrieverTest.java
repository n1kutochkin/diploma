package org.drinkless.tdlib.retriever.algorithms;

import org.drinkless.tdlib.retriever.Flag;
import org.junit.jupiter.api.Test;
//import ru.textanalysis.tawt.jmorfsdk.JMorfSdk;
//import ru.textanalysis.tawt.jmorfsdk.loader.JMorfSdkFactory;
//import ru.textanalysis.tawt.ms.storage.OmoFormList;

import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ImperativeVerbsRetrieverTest {

    @Test
    void gettingInitForm() {
        Logger logger = Logger.getLogger(String.valueOf(ImperativeVerbsRetrieverTest.class));
//        JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();
//        OmoFormList initialForm = jMorfSdk.getAllCharacteristicsOfForm("промоакции");
//        assertEquals(null, initialForm);
    }

    @Test
    void normalizing() {
        assertEquals(
                "приглашаем в монополию звука  главный" +
                        " музыкально-познавательный канал в telegram " +
                        "который ежедневно публикует подборки обзоры и новости" +
                        " из мира музыки",
                ImperativeVerbsRetriever.normalizedString(
                        "Приглашаем в Монополию звука — главный" +
                                " музыкально-познавательный канал в Telegram, " +
                                "который ежедневно публикует подборки, обзоры и новости" +
                                " из мира музыки!"
                )
        );
    }

    @Test
    void words() {
        assertEquals(
                Set.of(
                        "не",
                        "представляешь",
                        "свою",
                        "жизнь",
                        "без",
                        "музыки"
                ),
                ImperativeVerbsRetriever.normalizedString(
                        "Не представляешь свою жизнь без музыки?"
                )
        );
    }

    @Test
    void subscribeTest() {
        ImperativeVerbsRetriever retriever = new ImperativeVerbsRetriever();

        assertEquals(
                EnumSet.of(Flag.ACTION),
                retriever.apply("Не представляешь свою жизнь без музыки?\n" +
                        "\n" +
                        "Приглашаем в Монополию звука — главный музыкально-познавательный канал в Telegram, который ежедневно публикует подборки, обзоры и новости из мира музыки!\n" +
                        "\n" +
                        "От авторского взгляда нашего музыкального критика не устоит ни один меломан — подписывайся: @sound_monopoly", EnumSet.noneOf(Flag.class)
                )
        );
    }
}
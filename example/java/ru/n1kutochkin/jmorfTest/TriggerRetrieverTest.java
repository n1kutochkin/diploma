package ru.n1kutochkin.jmorfTest;

import org.drinkless.tdlib.retriever.Flag;
import org.drinkless.tdlib.retriever.algorithms.ImperativeVerbsRetriever;

import java.util.EnumSet;

public class TriggerRetrieverTest {

    public static void main(String[] args) {
        ImperativeVerbsRetriever retriever = new ImperativeVerbsRetriever();
        retriever.apply(
                "Не представляешь свою жизнь без музыки?\n" +
                "\n" +
                "Приглашаем в Монополию звука — главный музыкально-познавательный канал в Telegram, который ежедневно публикует подборки, обзоры и новости из мира музыки!\n" +
                "\n" +
                "От авторского взгляда нашего музыкального критика не устоит ни один меломан — подписывайся: @sound_monopoly",
                EnumSet.noneOf(Flag.class)
        ).stream().forEach(System.out::println);

        retriever.apply(
                "Подписчики Евгения Черных ждут и просят новых аналитических сводок\n" +
                        "\n" +
                        "Опытный инвестор с начала кризиса помогает своим читателям сбалансировать брокерские счета и вывести их в ➕\n" +
                        "\n" +
                        "Развернуть кризис в пользу и рост - его талант! \n" +
                        "\n" +
                        "Евгений уже смог сделать рывок в пандемию. Тогда его инвестиционный счет вырос с 50 до 120 млн₽т\n" +
                        "\n" +
                        "Читай и повторяй! Просто, как раз, два, три\n" +
                        "\n" +
                        "Подписывайся ⬇️\n" +
                        "\n" +
                        "https://t.me/трейдер черных \n" +
                        "\n" +
                        "И будь в курсе самых актуальных финансовых новостей!",
                EnumSet.noneOf(Flag.class)
        ).stream().forEach(System.out::println);
    }
}

//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2021
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
package org.drinkless.tdlib.example;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.aggregator.RegexAggregator;
import org.drinkless.tdlib.retriever.Flag;
import org.drinkless.tdlib.retriever.Retriever;
import org.drinkless.tdlib.retriever.TextRetriever;
import org.drinkless.tdlib.retriever.TgTextContentRetrievable;
import org.drinkless.tdlib.retriever.algorithms.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOError;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Example class for TDLib usage from Java.
 */
public final class Application {
    private static Client client = null;
    private static final Logger logger = Logger.getLogger(String.valueOf(Application.class));

    private static final TextRetriever tr = new TextRetriever();
    private static TdApi.AuthorizationState authorizationState = null;
    private static volatile boolean haveAuthorization = false;
    private static volatile boolean needQuit = false;
    private static volatile boolean canQuit = false;

    private static final Client.ResultHandler defaultHandler = new DefaultHandler();

    private static final Lock authorizationLock = new ReentrantLock();
    private static final Condition gotAuthorization = authorizationLock.newCondition();

    private static final ConcurrentMap<Long, TdApi.User> users = new ConcurrentHashMap<Long, TdApi.User>();
    private static final ConcurrentMap<Long, TdApi.BasicGroup> basicGroups = new ConcurrentHashMap<Long, TdApi.BasicGroup>();
    private static final ConcurrentMap<Long, TdApi.Supergroup> supergroups = new ConcurrentHashMap<Long, TdApi.Supergroup>();
    private static final ConcurrentMap<Integer, TdApi.SecretChat> secretChats = new ConcurrentHashMap<Integer, TdApi.SecretChat>();

    private static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();
    private static final NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();
    private static boolean haveFullMainChatList = false;

    private static final ConcurrentMap<Long, TdApi.UserFullInfo> usersFullInfo = new ConcurrentHashMap<Long, TdApi.UserFullInfo>();
    private static final ConcurrentMap<Long, TdApi.BasicGroupFullInfo> basicGroupsFullInfo = new ConcurrentHashMap<Long, TdApi.BasicGroupFullInfo>();
    private static final ConcurrentMap<Long, TdApi.SupergroupFullInfo> supergroupsFullInfo = new ConcurrentHashMap<Long, TdApi.SupergroupFullInfo>();

    private static final String newLine = System.getProperty("line.separator");
    private static final String commandsLine = "Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): ";
    private static final long FROM_LAST_MESSAGE = 0;
    private static final int OFFSET_FOR_LAST_MESSAGE = 0;
    private static volatile String currentPrompt = null;
    private static final String IN_ALL_CHATS = null;

    private static String query;
    private static DefaultListModel<SwingChat> model;
    private static Integer quantityOfChats = 10;
    private static volatile boolean adFilterIsOn = false;

    private JTextField filterTextBox;
    private JRadioButton radioButton;
    private JComboBox quantityOfMessagesComboBox;
    private JButton updateButton;
    private JList chatListPane;
    private JPanel panel;
    private JScrollPane chatsPane;
    private JScrollPane viewerPane;
    private JTextArea viewerArea;
    private JButton искатьButton;

    static {
        try {
            System.loadLibrary("tdjni");

        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    public Application() {
        model = new DefaultListModel<>();

        chatListPane = new JList();
        chatListPane.setModel(model);

        chatsPane.setViewportView(chatListPane);

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getSwingChatList(quantityOfChats);
            }
        });
        Integer quantity[] = {10, 20, 50};

        quantityOfMessagesComboBox = new JComboBox(quantity);
        quantityOfMessagesComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox source = (JComboBox)e.getSource();
                Integer selected = (Integer) source.getSelectedItem();
                quantityOfChats = selected.intValue();
            }
        });
        radioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adFilterIsOn = radioButton.isSelected();
            }
        });

        chatListPane.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    JList list = (JList) e.getSource();
                    long id = ((SwingChat) list.getSelectedValue()).getChat().id;
                    client.send(
                            new TdApi.GetChatHistory(
                                    id,
                                    FROM_LAST_MESSAGE,
                                    -1,
                                    100,
                                    false
                            ),
                            new SwingUIHandler(viewerArea)
                    );
                }
            }
        });
        искатьButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                var aggregator = new RegexAggregator(query, new SwingUIHandler(viewerArea), client);

                aggregator.aggregate();

                client.send(
                        new TdApi.SearchMessages(
                                null,
                                query,
                                0,
                                0,
                                0,
                                20,
                                null,
                                0,
                                0
                        ),
                        new SwingUIHandler(viewerArea)
                );
            }
        });
        filterTextBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                query = filterTextBox.getText();
            }
        });
    }

    private static void print(String str) {
        if (currentPrompt != null) {
            System.out.println("");
        }
        System.out.println(str);
        if (currentPrompt != null) {
            System.out.print(currentPrompt);
        }
    }

    private static void setChatPositions(TdApi.Chat chat, TdApi.ChatPosition[] positions) {
        synchronized (mainChatList) {
            synchronized (chat) {
                for (TdApi.ChatPosition position : chat.positions) {
                    if (position.list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
                        boolean isRemoved = mainChatList.remove(new OrderedChat(chat.id, position));
                        assert isRemoved;
                    }
                }

                chat.positions = positions;

                for (TdApi.ChatPosition position : chat.positions) {
                    if (position.list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
                        boolean isAdded = mainChatList.add(new OrderedChat(chat.id, position));
                        assert isAdded;
                    }
                }
            }
        }
    }

    private static void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        if (authorizationState != null) {
            Application.authorizationState = authorizationState;
        }
        switch (Application.authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                parameters.databaseDirectory = "tdlib";
                parameters.useMessageDatabase = true;
                parameters.useSecretChats = true;
                parameters.apiId = 94575;
                parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2";
                parameters.systemLanguageCode = "en";
                parameters.deviceModel = "Desktop";
                parameters.applicationVersion = "1.0";
                parameters.enableStorageOptimizer = true;

                client.send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                String phoneNumber = promptString("Please enter phone number: ");
                client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR: {
                String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) Application.authorizationState).link;
                System.out.println("Please confirm this login link on another device: " + link);
                break;
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                String code = promptString("Please enter authentication code: ");
                client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
                String firstName = promptString("Please enter your first name: ");
                String lastName = promptString("Please enter your last name: ");
                client.send(new TdApi.RegisterUser(firstName, lastName), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
                String password = promptString("Please enter password: ");
                client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                haveAuthorization = true;
                authorizationLock.lock();
                try {
                    gotAuthorization.signal();
                } finally {
                    authorizationLock.unlock();
                }
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                haveAuthorization = false;
                print("Logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                haveAuthorization = false;
                print("Closing");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                print("Closed");
                if (!needQuit) {
                    client = Client.create(new UpdateHandler(), null, null); // recreate client after previous has closed
                } else {
                    canQuit = true;
                }
                break;
            default:
                System.err.println("Unsupported authorization state:" + newLine + Application.authorizationState);
        }
    }

    private static int toInt(String arg) {
        int result = 0;
        try {
            result = Integer.parseInt(arg);
        } catch (NumberFormatException ignored) {
        }
        return result;
    }

    private static long getChatId(String arg) {
        long chatId = 0;
        try {
            chatId = Long.parseLong(arg);
        } catch (NumberFormatException ignored) {
        }
        return chatId;
    }

    private static String promptString(String prompt) {
        System.out.print(prompt);
        currentPrompt = prompt;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        try {
            str = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentPrompt = null;
        return str;
    }

    private static void getCommand() {
        String command = promptString(commandsLine);
        String[] commands = command.split(" ", 2);
        try {
            switch (commands[0]) {
                case "gcs": {
                    int limit = 20;
                    if (commands.length > 1) {
                        limit = toInt(commands[1]);
                    }
                    getMainChatList(limit);
                    break;
                }
                case "gc":
                    client.send(new TdApi.GetChat(getChatId(commands[1])), defaultHandler);
                    break;
                case "gch": {
                    int limit = 1;
                    boolean onlyLocal = false;
                    client.send(
                            new TdApi.GetChatHistory(
                                    getChatId(commands[1]),
                                    FROM_LAST_MESSAGE,
                                    OFFSET_FOR_LAST_MESSAGE,
                                    limit,
                                    onlyLocal
                            ),
                            defaultHandler
                    );
                    break;
                }
                case "me":
                    client.send(new TdApi.GetMe(), defaultHandler);
                    break;
                case "sm": {
                    String[] args = commands[1].split(" ", 2);
                    sendMessage(getChatId(args[0]), args[1]);
                    break;
                }
                case "lo":
                    haveAuthorization = false;
                    client.send(new TdApi.LogOut(), defaultHandler);
                    break;
                case "q":
                    needQuit = true;
                    haveAuthorization = false;
                    client.send(new TdApi.Close(), defaultHandler);
                    break;
                default:
                    System.err.println("Unsupported command: " + command);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            print("Not enough arguments");
        }
    }

    private static void getMainChatList(final int limit) {
        synchronized (mainChatList) {
            if (!haveFullMainChatList && limit > mainChatList.size()) {
                // send LoadChats request if there are some unknown chats and have not enough known chats
                client.send(new TdApi.LoadChats(new TdApi.ChatListMain(), limit - mainChatList.size()), new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        switch (object.getConstructor()) {
                            case TdApi.Error.CONSTRUCTOR:
                                if (((TdApi.Error) object).code == 404) {
                                    synchronized (mainChatList) {
                                        haveFullMainChatList = true;
                                    }
                                } else {
                                    System.err.println("Receive an error for LoadChats:" + newLine + object);
                                }
                                break;
                            case TdApi.Ok.CONSTRUCTOR:
                                // chats had already been received through updates, let's retry request
                                getMainChatList(limit);
                                break;
                            default:
                                System.err.println("Receive wrong response from TDLib:" + newLine + object);
                        }
                    }
                });
                return;
            }

            java.util.Iterator<OrderedChat> iter = mainChatList.iterator();
            System.out.println();
            System.out.println("First " + limit + " chat(s) out of " + mainChatList.size() + " known chat(s):");
            for (int i = 0; i < limit && i < mainChatList.size(); i++) {
                long chatId = iter.next().chatId;
                TdApi.Chat chat = chats.get(chatId);
                synchronized (chat) {
                    System.out.println(chatId + ": " + chat.title);
                }
            }
            print("");
        }
    }

    private static void sendMessage(long chatId, String message) {
        // initialize reply markup just for testing
        TdApi.InlineKeyboardButton[] row = {new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())};
        TdApi.ReplyMarkup replyMarkup = new TdApi.ReplyMarkupInlineKeyboard(new TdApi.InlineKeyboardButton[][] {row, row, row});

        TdApi.InputMessageContent content = new TdApi.InputMessageText(new TdApi.FormattedText(message, null), false, true);
        client.send(new TdApi.SendMessage(chatId, 0, 0, null, replyMarkup, content), defaultHandler);
    }

    public static void
    initUI() {

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JRadioButton adsDisableButton = new JRadioButton("Фильтрация рекламы");
        adsDisableButton.setBounds(499, 487, 168, 23);
        frame.getContentPane().add(adsDisableButton);

        JTextField keyWordsField = new JTextField();
        keyWordsField.setBounds(208, 486, 279, 26);
        frame.getContentPane().add(keyWordsField);
        keyWordsField.setColumns(10);

        JScrollPane chatsPane = new JScrollPane();
        chatsPane.setBounds(6, 6, 192, 504);
        frame.getContentPane().add(chatsPane);

        model = new DefaultListModel();

        JList chatList = new JList();
        chatList.setModel(model);

        chatsPane.setViewportView(chatList);

        JScrollPane viewerPane = new JScrollPane();
        viewerPane.setBounds(210, 6, 457, 469);
        frame.getContentPane().add(viewerPane);

        JTextArea viewer = new JTextArea();
        viewer.setEditable(false);
        viewerPane.setViewportView(viewer);

        chatList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    JList list = (JList) e.getSource();
                    long id = ((SwingChat) list.getSelectedValue()).getChat().id;
                    client.send(
                            new TdApi.GetChatHistory(
                                    id,
                                    FROM_LAST_MESSAGE,
                                    OFFSET_FOR_LAST_MESSAGE,
                                    10,
                                    false
                            ),
                            new SwingUIHandler(viewer)
                    );
                }
            }
        });

        JLabel lblNewLabel = new JLabel("Ключевые слова");
        lblNewLabel.setBounds(210, 474, 304, 16);
        frame.getContentPane().add(lblNewLabel);

        JButton refreshButton = new JButton("Обновить");
        refreshButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getSwingChatList(quantityOfChats);
            }
        });
        refreshButton.setBounds(600, 600, 117, 29);
        frame.getContentPane().add(refreshButton);

        Integer quantity[] = {10, 20, 50};

        JComboBox comboBox = new JComboBox(quantity);
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox source = (JComboBox)e.getSource();
                Integer selected = (Integer) source.getSelectedItem();
                quantityOfChats = selected.intValue();
            }
        });
        comboBox.setBounds(400, 424, 189, 27);
        frame.getContentPane().add(comboBox);

        frame.setVisible(true);
    }

    public static void getSwingChatList(int limit) {
        synchronized (mainChatList) {
            if (!haveFullMainChatList && limit > mainChatList.size()) {
                // send LoadChats request if there are some unknown chats and have not enough known chats
                client.send(new TdApi.LoadChats(new TdApi.ChatListMain(), limit - mainChatList.size()), new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        switch (object.getConstructor()) {
                            case TdApi.Error.CONSTRUCTOR:
                                if (((TdApi.Error) object).code == 404) {
                                    synchronized (mainChatList) {
                                        haveFullMainChatList = true;
                                    }
                                } else {
                                    System.err.println("Receive an error for LoadChats:" + newLine + object);
                                }
                                break;
                            case TdApi.Ok.CONSTRUCTOR:
                                // chats had already been received through updates, let's retry request
                                getSwingChatList(limit);
                                break;
                            default:
                                System.err.println("Receive wrong response from TDLib:" + newLine + object);
                        }
                    }
                });
                return;
            }
        }

        if (!model.isEmpty()) {
            model.clear();
        }

        model.addAll(
                mainChatList.stream()
                        .filter(c -> c.chatId < 0)
                        .map(chat -> {
                            TdApi.Chat received = chats.get(chat.chatId);
                            synchronized (received) {
                                return new SwingChat(received);
                            }
                        }).collect(Collectors.toList()));
    }

    public static void main(String[] args) throws InterruptedException {

        JFrame frame = new JFrame("Система агрегации и фильтрации сообщений Telegram");
        frame.setContentPane(new Application().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        // disable TDLib log
        Client.execute(new TdApi.SetLogVerbosityLevel(0));
        if (Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile("tdlib.log", 1 << 27, false))) instanceof TdApi.Error) {
            throw new IOError(new IOException("Write access to the current directory is required"));
        }

        // create client
        client = Client.create(new UpdateHandler(), null, null);

        // test Client.execute
        defaultHandler.onResult(Client.execute(new TdApi.GetTextEntities("@telegram /test_command https://telegram.org telegram.me @gif @test")));

//        initUI();

        // main loop
        while (!needQuit) {
            // await authorization
            authorizationLock.lock();
            try {
                while (!haveAuthorization) {
                    gotAuthorization.await();
                }
            } finally {
                authorizationLock.unlock();
            }

            while (haveAuthorization) {
                getCommand();
            }
        }
        while (!canQuit) {
            Thread.sleep(1);
        }
    }

    private static class OrderedChat implements Comparable<OrderedChat> {

        final long chatId;
        final TdApi.ChatPosition position;

        OrderedChat(long chatId, TdApi.ChatPosition position) {
            this.chatId = chatId;
            this.position = position;
        }

        @Override
        public int compareTo(OrderedChat o) {
            if (this.position.order != o.position.order) {
                return o.position.order < this.position.order ? -1 : 1;
            }
            if (this.chatId != o.chatId) {
                return o.chatId < this.chatId ? -1 : 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            OrderedChat o = (OrderedChat) obj;
            return this.chatId == o.chatId && this.position.order == o.position.order;
        }
    }

    private static class DefaultHandler implements Client.ResultHandler {

//        WebsiteLinksRetriever linksRetriever = new WebsiteLinksRetriever();
//        ImperativeVerbsRetriever verbsRetriever = new ImperativeVerbsRetriever();
//
//        AdvertisementObjectsRetriever advertisementObjectsRetriever = new AdvertisementObjectsRetriever();
//
//        TriggerWordsRetriever triggerWordsRetriever = new TriggerWordsRetriever();
//
//        TriggerHashTagRetriever triggerHashTagRetriever = new TriggerHashTagRetriever();

        @Override
        public void onResult(TdApi.Object object) {

//            if (object instanceof TdApi.Messages) {
//                Arrays.stream(((TdApi.Messages) object).messages)
//                        .map(message -> {
//                            EnumSet<Flag> emptyFlags = EnumSet.noneOf(Flag.class);
//                            var linkFlags = linksRetriever.apply(message, emptyFlags);
//                            var verbsFlags = verbsRetriever.apply(message, linkFlags);
//                            return new FlaggedMessage(verbsFlags, message);
//                        })
//                        .forEach(m -> print(m.toString()));
//            } else {
                print(object.toString());
//            }

//            try {
//                if (object instanceof TdApi.Messages) {
//                    TdApi.Message message = ((TdApi.Messages) object).messages[0];
//                    if (message instanceof TdApi.Message) {
//                        TdApi.MessageContent content = message.content;
//
//                        if (content instanceof TdApi.MessagePhoto) {
//                            TdApi.FormattedText caption = ((TdApi.MessagePhoto) content).caption;
//                            if (caption instanceof TdApi.FormattedText) {
//                                EnumSet<Flag> result = retriever.apply(caption, EnumSet.noneOf(Flag.class));
//                                print(object.toString() + "/n" + result.toString());
//                            }
//                        } else if (content instanceof TdApi.MessageAnimation) {
//                            TdApi.FormattedText caption = ((TdApi.MessageAnimation) content).caption;
//                            if (caption instanceof TdApi.FormattedText) {
//                                EnumSet<Flag> result = retriever.apply(caption, EnumSet.noneOf(Flag.class));
//                                print(object.toString() + "/n" + result.toString());
//                            }
//                        } else {
//                            print(object.toString());
//                        }
//                    } else {
//                        print(object.toString());
//                    }
//                }
//            } catch (RuntimeException e) {
//                print(e.getMessage());
//            }

        }
    }

//    private static class RetrieverHandler extends DefaultHandler implements Client.ResultHandler {
//
//        private Retriever retriever;
//
//        public RetrieverHandler(Retriever retriever) {
//            this.retriever = retriever;
//        }
//
//        @Override
//        public void onResult(TdApi.Object object) {
//            String result = "";
//
//            if (object instanceof TdApi.Message) {
//                retriever.load((TdApi.Message) object);
//            } else {
//                super.onResult(object);
//                return;
//            }
//
//            print(result);
//        }
//    }
    private static class SwingUIHandler implements Client.ResultHandler, TgTextContentRetrievable {

        private JTextArea viewer;

        private Set<EnumSet<Flag>> filters = Set.of(
                EnumSet.of(Flag.TRIGGER_ACTION, Flag.MULTIPLE_CHANNEL_LINKS),
                EnumSet.of(Flag.TRIGGER_ACTION, Flag.SINGLE_CHANNEL_LINK),
                EnumSet.of(Flag.ACTION, Flag.SINGLE_CHANNEL_LINK),
                EnumSet.of(Flag.ACTION, Flag.MULTIPLE_CHANNEL_LINKS),
                EnumSet.of(Flag.FREQUENTLY_ADVERTISED, Flag.TRIGGER_WORD),
                EnumSet.of(Flag.FREQUENTLY_ADVERTISED)
        );

        private ExecutorService threadPool = Executors.newFixedThreadPool(5);

//        private WebsiteLinksRetriever linksRetriever = new WebsiteLinksRetriever();
//        private ImperativeVerbsRetriever verbsRetriever = new ImperativeVerbsRetriever();
//
//        private AdvertisementObjectsRetriever advertisementObjectsRetriever = new AdvertisementObjectsRetriever();

//        private TriggerWordsRetriever triggerWordsRetriever = new TriggerWordsRetriever();

//        private TriggerHashTagRetriever triggerHashTagRetriever = new TriggerHashTagRetriever();

        public SwingUIHandler(JTextArea textArea) {
            this.viewer = textArea;
        }

        @Override
        public void onResult(TdApi.Object object) {
            try {
                if (object instanceof TdApi.Messages) {
                    StringBuilder builder = new StringBuilder();
                    Stream<FlaggedMessage> flaggedMessageStream;
                    if (adFilterIsOn) {
                        int length = ((TdApi.Messages) object).messages.length;
                        TdApi.Message[] receivedMessages = Arrays.copyOf(((TdApi.Messages) object).messages, length);

                        flaggedMessageStream = Arrays.stream(receivedMessages)
                                .map(message -> {

                                    EnumSet<Flag> emptyFlags = EnumSet.noneOf(Flag.class);

                                    List<Retriever> retrievers = List.of(
                                            new WebsiteLinksRetriever(emptyFlags, message),
                                            new ImperativeVerbsRetriever(emptyFlags, message),
                                            new AdvertisementObjectsRetriever(emptyFlags, message),
                                            new TriggerHashTagRetriever(emptyFlags, message),
                                            new TriggerWordsRetriever(emptyFlags, message)
                                    );

                                    List<Future<EnumSet<Flag>>> futures;

                                    try {
                                        futures = threadPool.invokeAll(retrievers);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }

                                    EnumSet<Flag> result = EnumSet.noneOf(Flag.class);

                                    futures.stream()
                                            .forEach(t -> {
                                                try {
                                                    result.addAll(t.get());
                                                } catch (InterruptedException e) {
                                                    logger.log(Level.SEVERE, e.getMessage());
                                                } catch (ExecutionException e) {
                                                    logger.log(Level.SEVERE, e.getMessage());
                                                } catch (Throwable e) {
                                                    logger.log(Level.SEVERE, e.getMessage());
                                                }
                                            });

//                                retrievers.stream()
//                                        .map(Retriever::getFlags)
//                                        .forEach(s -> result.addAll(s));

                                    return new FlaggedMessage(result, Optional.ofNullable(retrieve(message)).map(t -> t.text).orElse(""));

//                                var linkFlags = linksRetriever.apply(message, emptyFlags);
//                                var verbsFlags = verbsRetriever.apply(message, linkFlags);
//                                logger.log(Level.INFO, "закончил обработку");
//                                logger.log(Level.INFO, verbsFlags.toString());
//                                return new FlaggedMessage(verbsFlags, message);
                                });
//                            .filter(m -> !m.getFlags().containsAll(
//                                    EnumSet.of(Flag.MULTIPLE_CHANNEL_LINKS, Flag.TRIGGER_ACTION)
//                            ));
                    } else {
                        flaggedMessageStream = Arrays.stream(((TdApi.Messages) object).messages).
                                map(m -> new FlaggedMessage(EnumSet.noneOf(Flag.class), Optional.ofNullable(retrieve(m)).map(t -> t.text).orElse("")));
                    }

                    flaggedMessageStream.forEach(m -> {
//                    Date date = new Date((long) m.getMessage().date * 1000);

                        builder.append(">>>>>>>>>>>>>>>>>>>>>>>>>>");
                        builder.append("\n");

//                    switch (m.getFlags()) {
//                        case EnumSet e && e.containsAll(EnumSet.of(Flag.MULTIPLE_CHANNEL_LINKS, Flag.TRIGGER_ACTION)) -> builder.append("!!! РЕКЛАМА !!!");
//                        default -> logger.log(Level.INFO, "NOT AN AD");
//                    }

//                        if (m.getFlags().containsAll(EnumSet.of(Flag.MULTIPLE_CHANNEL_LINKS, Flag.TRIGGER_ACTION))) {
//                            builder.append("!!! РЕКЛАМА !!!");
//                        }
//
//                        if (m.getFlags().containsAll(EnumSet.of(Flag.TRIGGER_ACTION, Flag.SINGLE_CHANNEL_LINK))) {
//                            builder.append("!!! РЕКЛАМА !!!");
//                        }

                        for (EnumSet<Flag> f : filters) {
                            if (m.getFlags().containsAll(f)) {
                                builder.append("!!!!!!!!!!!! 🔥🔥🔥РЕКЛАМА🔥🔥🔥 !!!!!!!!!!!!");
                                break;
                            }
                        }

                        builder
                                .append("\n")
//                            .append(date)
                                .append("\n")
//                            .append(retrieve(m.getMessage()).text)
                                .append(m.getMessage())
                                .append("\n")
                                .append("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
                                .append("\n");
                    });

                    String s = builder.toString();

                    viewer.setText(builder.toString());
                }
            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static class UpdateHandler implements Client.ResultHandler {

        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                    onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                    break;

                case TdApi.UpdateUser.CONSTRUCTOR:
                    TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
                    users.put(updateUser.user.id, updateUser.user);
                    break;
                case TdApi.UpdateUserStatus.CONSTRUCTOR: {
                    TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
                    TdApi.User user = users.get(updateUserStatus.userId);
                    synchronized (user) {
                        user.status = updateUserStatus.status;
                    }
                    break;
                }
                case TdApi.UpdateBasicGroup.CONSTRUCTOR:
                    TdApi.UpdateBasicGroup updateBasicGroup = (TdApi.UpdateBasicGroup) object;
                    basicGroups.put(updateBasicGroup.basicGroup.id, updateBasicGroup.basicGroup);
                    break;
                case TdApi.UpdateSupergroup.CONSTRUCTOR:
                    TdApi.UpdateSupergroup updateSupergroup = (TdApi.UpdateSupergroup) object;
                    supergroups.put(updateSupergroup.supergroup.id, updateSupergroup.supergroup);
                    break;
                case TdApi.UpdateSecretChat.CONSTRUCTOR:
                    TdApi.UpdateSecretChat updateSecretChat = (TdApi.UpdateSecretChat) object;
                    secretChats.put(updateSecretChat.secretChat.id, updateSecretChat.secretChat);
                    break;

                case TdApi.UpdateNewChat.CONSTRUCTOR: {
                    TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
                    TdApi.Chat chat = updateNewChat.chat;
                    synchronized (chat) {
                        chats.put(chat.id, chat);

                        TdApi.ChatPosition[] positions = chat.positions;
                        chat.positions = new TdApi.ChatPosition[0];
                        setChatPositions(chat, positions);
                    }
                    break;
                }
                case TdApi.UpdateChatTitle.CONSTRUCTOR: {
                    TdApi.UpdateChatTitle updateChat = (TdApi.UpdateChatTitle) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.title = updateChat.title;
                    }
                    break;
                }
                case TdApi.UpdateChatPhoto.CONSTRUCTOR: {
                    TdApi.UpdateChatPhoto updateChat = (TdApi.UpdateChatPhoto) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.photo = updateChat.photo;
                    }
                    break;
                }
                case TdApi.UpdateChatLastMessage.CONSTRUCTOR: {
                    TdApi.UpdateChatLastMessage updateChat = (TdApi.UpdateChatLastMessage) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastMessage = updateChat.lastMessage;
                        setChatPositions(chat, updateChat.positions);
                    }
                    break;
                }
                case TdApi.UpdateChatPosition.CONSTRUCTOR: {
                    TdApi.UpdateChatPosition updateChat = (TdApi.UpdateChatPosition) object;
                    if (updateChat.position.list.getConstructor() != TdApi.ChatListMain.CONSTRUCTOR) {
                        break;
                    }

                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        int i;
                        for (i = 0; i < chat.positions.length; i++) {
                            if (chat.positions[i].list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
                                break;
                            }
                        }
                        TdApi.ChatPosition[] new_positions = new TdApi.ChatPosition[chat.positions.length + (updateChat.position.order == 0 ? 0 : 1) - (i < chat.positions.length ? 1 : 0)];
                        int pos = 0;
                        if (updateChat.position.order != 0) {
                            new_positions[pos++] = updateChat.position;
                        }
                        for (int j = 0; j < chat.positions.length; j++) {
                            if (j != i) {
                                new_positions[pos++] = chat.positions[j];
                            }
                        }
                        assert pos == new_positions.length;

                        setChatPositions(chat, new_positions);
                    }
                    break;
                }
                case TdApi.UpdateChatReadInbox.CONSTRUCTOR: {
                    TdApi.UpdateChatReadInbox updateChat = (TdApi.UpdateChatReadInbox) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastReadInboxMessageId = updateChat.lastReadInboxMessageId;
                        chat.unreadCount = updateChat.unreadCount;
                    }
                    break;
                }
                case TdApi.UpdateChatReadOutbox.CONSTRUCTOR: {
                    TdApi.UpdateChatReadOutbox updateChat = (TdApi.UpdateChatReadOutbox) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastReadOutboxMessageId = updateChat.lastReadOutboxMessageId;
                    }
                    break;
                }
                case TdApi.UpdateChatUnreadMentionCount.CONSTRUCTOR: {
                    TdApi.UpdateChatUnreadMentionCount updateChat = (TdApi.UpdateChatUnreadMentionCount) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
                    break;
                }
                case TdApi.UpdateMessageMentionRead.CONSTRUCTOR: {
                    TdApi.UpdateMessageMentionRead updateChat = (TdApi.UpdateMessageMentionRead) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
                    break;
                }
                case TdApi.UpdateChatReplyMarkup.CONSTRUCTOR: {
                    TdApi.UpdateChatReplyMarkup updateChat = (TdApi.UpdateChatReplyMarkup) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.replyMarkupMessageId = updateChat.replyMarkupMessageId;
                    }
                    break;
                }
                case TdApi.UpdateChatDraftMessage.CONSTRUCTOR: {
                    TdApi.UpdateChatDraftMessage updateChat = (TdApi.UpdateChatDraftMessage) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.draftMessage = updateChat.draftMessage;
                        setChatPositions(chat, updateChat.positions);
                    }
                    break;
                }
                case TdApi.UpdateChatPermissions.CONSTRUCTOR: {
                    TdApi.UpdateChatPermissions update = (TdApi.UpdateChatPermissions) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.permissions = update.permissions;
                    }
                    break;
                }
                case TdApi.UpdateChatNotificationSettings.CONSTRUCTOR: {
                    TdApi.UpdateChatNotificationSettings update = (TdApi.UpdateChatNotificationSettings) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.notificationSettings = update.notificationSettings;
                    }
                    break;
                }
                case TdApi.UpdateChatDefaultDisableNotification.CONSTRUCTOR: {
                    TdApi.UpdateChatDefaultDisableNotification update = (TdApi.UpdateChatDefaultDisableNotification) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.defaultDisableNotification = update.defaultDisableNotification;
                    }
                    break;
                }
                case TdApi.UpdateChatIsMarkedAsUnread.CONSTRUCTOR: {
                    TdApi.UpdateChatIsMarkedAsUnread update = (TdApi.UpdateChatIsMarkedAsUnread) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.isMarkedAsUnread = update.isMarkedAsUnread;
                    }
                    break;
                }
                case TdApi.UpdateChatIsBlocked.CONSTRUCTOR: {
                    TdApi.UpdateChatIsBlocked update = (TdApi.UpdateChatIsBlocked) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.isBlocked = update.isBlocked;
                    }
                    break;
                }
                case TdApi.UpdateChatHasScheduledMessages.CONSTRUCTOR: {
                    TdApi.UpdateChatHasScheduledMessages update = (TdApi.UpdateChatHasScheduledMessages) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.hasScheduledMessages = update.hasScheduledMessages;
                    }
                    break;
                }

                case TdApi.UpdateUserFullInfo.CONSTRUCTOR:
                    TdApi.UpdateUserFullInfo updateUserFullInfo = (TdApi.UpdateUserFullInfo) object;
                    usersFullInfo.put(updateUserFullInfo.userId, updateUserFullInfo.userFullInfo);
                    break;
                case TdApi.UpdateBasicGroupFullInfo.CONSTRUCTOR:
                    TdApi.UpdateBasicGroupFullInfo updateBasicGroupFullInfo = (TdApi.UpdateBasicGroupFullInfo) object;
                    basicGroupsFullInfo.put(updateBasicGroupFullInfo.basicGroupId, updateBasicGroupFullInfo.basicGroupFullInfo);
                    break;
                case TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR:
                    TdApi.UpdateSupergroupFullInfo updateSupergroupFullInfo = (TdApi.UpdateSupergroupFullInfo) object;
                    supergroupsFullInfo.put(updateSupergroupFullInfo.supergroupId, updateSupergroupFullInfo.supergroupFullInfo);
                    break;
                default:
                    // print("Unsupported update:" + newLine + object);
            }
        }
    }

    private static class AuthorizationRequestHandler implements Client.ResultHandler {

        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    System.err.println("Receive an error:" + newLine + object);
                    onAuthorizationStateUpdated(null); // repeat last action
                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    // result is already received through UpdateAuthorizationState, nothing to do
                    break;
                default:
                    System.err.println("Receive wrong response from TDLib:" + newLine + object);
            }
        }
    }
}

package io.github.memory_of_snow.ParallelConnectionSandBox.main;

import io.github.memory_of_snow.ParallelConnectionSandBox.clientendpoint.ClientEndPoint;
import io.github.memory_of_snow.ParallelConnectionSandBox.event.Event;
import io.github.memory_of_snow.ParallelConnectionSandBox.message.EventMessage;
import io.github.memory_of_snow.ParallelConnectionSandBox.message.Message;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.client.http.HttpClientConnectionFactory;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.ClientConnectionFactoryOverHTTP2;
import org.eclipse.jetty.io.ClientConnectionFactory;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.JettyUpgradeListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

@WebSocket
public class ParallelConnectionSandBox {

    private static final Logger log = Logger.getLogger(ParallelConnectionSandBox.class.getName());

    //購読IDは最大64文字
    private static final String SUBSCRIPTION_ID = "npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh";
    //private static final String SUBSCRIPTION_ID = "日本語のサブスクライバーID";


    //フィルターに”limit”：10を指定すると、対象のイベント最大10件+EOSE1件の合計11が送られてくるため合計は11
    //現在はEVENTメッセージのみを格納しているので10
    //private static final BlockingQueue<Message> queue = new ArrayBlockingQueue<>(10);


    public static void main(String[] args) throws Exception{

        Thread currentThread = Thread.currentThread();
        long threadId = currentThread.getId();
        log.log(Level.INFO,String.format("メインスレッドのIDは%d%n",threadId));

        ExecutorService executor = Executors.newFixedThreadPool(10);


        BlockingQueue<Event> wirednet = new ArrayBlockingQueue<>(5);
        executor.submit(() -> {
            try {
                connectAndGetPost("wss://relay-jp.nostr.wirednet.jp",wirednet);
            } catch (Exception e) {
                log.log(Level.WARNING,"タスクの処理中に例外発生。",e);
            }
        });

        BlockingQueue<Event> fediverse = new ArrayBlockingQueue<>(5);
        executor.submit(() -> {
            try {
                connectAndGetPost("wss://nostr.fediverse.jp",fediverse);
            } catch (Exception e) {
                log.log(Level.WARNING,"タスクの処理中に例外発生。",e);
            }
        });

        BlockingQueue<Event> h3z = new ArrayBlockingQueue<>(5);
        executor.submit(() -> {
            try {
                connectAndGetPost("wss://nostr.h3z.jp",h3z);
            } catch (Exception e) {
                log.log(Level.WARNING,"タスクの処理中に例外発生。",e);
            }
        });

        BlockingQueue<Event> holybea = new ArrayBlockingQueue<>(5);
        executor.submit(() -> {
            try {
                connectAndGetPost("wss://nostr.holybea.com",holybea);
            } catch (Exception e) {
                log.log(Level.WARNING,"タスクの処理中に例外発生。",e);
            }
        });

        BlockingQueue<Event> yabumi = new ArrayBlockingQueue<>(5);
        executor.submit(() -> {
            try {
                connectAndGetPost("wss://yabu.me",yabumi);
            } catch (Exception e) {
                log.log(Level.WARNING,"タスクの処理中に例外発生。",e);
            }
        });

        BlockingQueue<Event> nokotaro = new ArrayBlockingQueue<>(5);
        executor.submit(() -> {
            try {
                connectAndGetPost("wss://nostr-relay.nokotaro.com",nokotaro);
            } catch (Exception e) {
                log.log(Level.WARNING,"タスクの処理中に例外発生。",e);
            }
        });



        Thread.sleep(1500);

        executor.shutdown();

        Thread.sleep(1500);

        if (executor.isTerminated()) {
            log.log(Level.INFO, "エグゼキューターが終了しました。");
        }


        //次の長いコードと等価
        List<Event> posts = Stream.of(wirednet,fediverse,h3z,holybea,yabumi,nokotaro)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparingLong(Event::getCreated_at))
                .distinct()
                .toList();

        /*
        List<Event> posts = new ArrayList<>(wirednet);
        for (Event event : fediverse){
            if(!posts.contains(event)){
                posts.add(event);

            }
        }
        for (Event event : h3z){
            if(!posts.contains(event)){
                posts.add(event);

            }
        }
        for (Event event : holybea){
            if(!posts.contains(event)){
                posts.add(event);

            }
        }
        for (Event event : yabumi){
            if(!posts.contains(event)){
                posts.add(event);

            }
        }
        for (Event event : nokotaro){
            if(!posts.contains(event)){
                posts.add(event);

            }
        }
        Comparator<Event> comparator = new EventComparator();
        posts.sort(comparator);

         */
        log.log(Level.INFO,"\n\n\n統合されたリスト\n\n\n");
        int i = 1;
        for (Event event : posts){
            Instant instant = Instant.ofEpochSecond(event.getCreated_at());
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant,zoneId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = dateTime.format(formatter);

            //log.log(Level.INFO, String.format("%n投稿時間：%s%nコンテンツ：%s%n",formattedDateTime,event.getContent()));
            System.out.println();
            System.out.printf("%d件目%n",i);
            System.out.printf("投稿時間：%s%n",formattedDateTime);
            System.out.printf("内容：%s%n",event.getContent());
            System.out.println();
            i++;
        }

    }

    public static void connectAndGetPost(String relayURL,BlockingQueue<Event>resultStorage) throws Exception{

        Thread currentThread = Thread.currentThread();
        long threadId = currentThread.getId();
        log.log(Level.INFO,String.format("%s接続スレッドのIDは%d%n",relayURL,threadId));

        final BlockingQueue<Message> queue = new ArrayBlockingQueue<>(10);

        //SSLの場合の設定ここから
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setIncludeProtocols("TLSv1.3");
        ClientConnector clientConnector = new ClientConnector();
        clientConnector.setSslContextFactory(sslContextFactory);

        ClientConnectionFactory.Info h1 = HttpClientConnectionFactory.HTTP11;
        ClientConnectionFactory.Info h2 = new ClientConnectionFactoryOverHTTP2.HTTP2(new HTTP2Client(clientConnector));

        HttpClientTransport transport = new HttpClientTransportDynamic(clientConnector, h1, h2);
        HttpClient httpClient = new HttpClient(transport);
        //SSLの場合の設定ここまで

        WebSocketClient webSocketClient = new WebSocketClient(httpClient);

        webSocketClient.start();

        ClientEndPoint clientEndPoint = new ClientEndPoint(queue);

        URI serverURI = URI.create(relayURL);

        //カスタムリクエストの作成（何のため）？
        ClientUpgradeRequest customRequest = new ClientUpgradeRequest();
        customRequest.setHeader(HttpHeader.UPGRADE.asString(), "h2c");
        customRequest.setHeader(HttpHeader.CONNECTION.asString(), "Upgrade, HTTP2-Settings");

        JettyUpgradeListener listener = new JettyUpgradeListener() {};

        CompletableFuture<Session> clientSessionPromise = webSocketClient.connect(clientEndPoint, serverURI, customRequest, listener);

        //接続時間稼ぎ
        Thread.sleep(1500);

        try(Session session = clientSessionPromise.join()) {

            if (!session.isOpen()) {
                log.log(Level.WARNING,"接続できませんでした");
                System.exit(-1000);
            }

            //REQメッセージの作成、Kindsは1（通常テキスト投稿）のみ、Limit（取得件数）は10
            //["REQ","購読ID",{"kinds":1,"limit":5}]
            final String REQ = "[\"REQ\",\"" + SUBSCRIPTION_ID + "\",{\"kinds\":[1],\"limit\":5}]";

            log.log(Level.INFO,"REQ message = " + REQ);
            //System.out.println("REQ message = " + REQ);

            session.getRemote().sendString(REQ);

            Thread.sleep(500);
            //CLOSEメッセージの作成
            //["CLOSE","購読ID"]
            final String CLOSE = "[\"CLOSE\",\"" + SUBSCRIPTION_ID + "\"]";
            session.getRemote().sendString(CLOSE);

            //受信時間稼ぎ
            Thread.sleep(500);

            webSocketClient.stop();

            Set<Message> set = Set.copyOf(queue);

            System.out.println(relayURL + "の取得件数：" + set.size());

            int i = 1;
            for (Message body : set) {

                if("EVENT".equals(body.getCommand())){
                    EventMessage eventMessage = (EventMessage) body;
                    Event innerEvent = eventMessage.getInnerEvent();
                    log.log(Level.INFO,String.format("%sから取得した%d件目のEVENTのcontent = %s%n",relayURL,i,innerEvent.getContent()));

                    if(!resultStorage.offer(innerEvent)){
                        log.log(Level.WARNING,String.format("%sの方のキューへの格納に失敗しました。",relayURL));
                    }
                }else if("EOSE".equals(body.getCommand())){
                    log.log(Level.INFO,"EOSEを受信しました");
                }

                i++;
            }
        }
    }

}

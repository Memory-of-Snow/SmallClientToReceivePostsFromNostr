package io.github.memory_of_snow.SmallClientToReceivePostsFromNostr;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.client.http.HttpClientConnectionFactory;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.ClientConnectionFactoryOverHTTP2;
import org.eclipse.jetty.io.ClientConnectionFactory;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.JettyUpgradeListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

@WebSocket
public class SmallClientToReceivePostsFromNostr {

    //private static final String RELAY_URL = "wss://relay-jp.nostr.wirednet.jp";
    private static final String RELAY_URL = "wss://yabu.me";


    //購読IDは最大64文字
    private static final String SUBSCRIPTION_ID = "npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh";
    //private static final String SUBSCRIPTION_ID = "日本語のサブスクライバーID";


    //フィルターに”limit”：10を指定すると、対象のイベント最大10件+EOSE1件の合計11が送られてくるため11
    private static final BlockingQueue<String> queue = new ArrayBlockingQueue<>(11);

    @OnWebSocketConnect
    public void onConnect(Session session) {

        session.setMaxTextMessageSize(16 * 1024);
        System.out.printf("onConnect()---%sと接続しました%n",session.getRemoteAddress());

    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {

        System.out.printf("onClose()---%sを理由として、コード%dで接続が終了しました%n", reason,statusCode);

    }

    @OnWebSocketError
    public void onError(Throwable cause) {

        cause.printStackTrace();

    }

    @OnWebSocketMessage
    public void onTextMessage(Session session, String message)
    {
        if ("close".equalsIgnoreCase(message)) {
            session.close(StatusCode.NORMAL, "bye");
            return;
        }

        if(!queue.offer(message)){
            System.err.println("メッセージの受信に失敗しました。");
        }

        System.out.printf("onTextMessage()---%sからメッセージを受け取りました。内容:%s%n",session.getRemoteAddress(),message);

    }

    @OnWebSocketMessage
    public void onBinaryMessage(byte[] payload, int offset, int length) {

        //バイナリーメッセージのときにここを使う

    }

    public static void main(String[] args) throws Exception{

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


        SmallClientToReceivePostsFromNostr clientEndPoint = new SmallClientToReceivePostsFromNostr();

        URI serverURI = URI.create(RELAY_URL);

        //カスタムリクエストの作成（何のため）？
        ClientUpgradeRequest customRequest = new ClientUpgradeRequest();
        customRequest.setHeader(HttpHeader.UPGRADE.asString(), "h2c");
        customRequest.setHeader(HttpHeader.CONNECTION.asString(), "Upgrade, HTTP2-Settings");

        JettyUpgradeListener listener = new JettyUpgradeListener() {

            @Override
            public void onHandshakeRequest(HttpRequest request) {}

            @Override
            public void onHandshakeResponse(HttpRequest request, HttpResponse response) {}
        };

        CompletableFuture<Session> clientSessionPromise = webSocketClient.connect(clientEndPoint, serverURI, customRequest, listener);

        //接続時間稼ぎ
        Thread.sleep(1500);

        try(Session session = clientSessionPromise.join()) {

            if (!session.isOpen()) {
                System.err.println("接続できませんでした");
                System.exit(-1000);
            }

            //REQメッセージの作成、Kindsは1（通常テキスト投稿）のみ、Limit（取得件数）は10
            //["REQ","購読ID",{"kinds":1,"limit":10}]
            final String REQ = "[\"REQ\",\"" + SUBSCRIPTION_ID + "\",{\"kinds\":[1],\"limit\":10}]";
            session.getRemote().sendString(REQ);

            //CLOSEメッセージの作成
            //["CLOSE","購読ID"]
            final String CLOSE = "[\"CLOSE\",\"" + SUBSCRIPTION_ID + "\"]";
            session.getRemote().sendString(CLOSE);

            //受信時間稼ぎ
            Thread.sleep(1500);

            webSocketClient.stop();


            for (String str : queue) {

                System.out.println("メッセージを受信：" + str);

            }
        }

    }

}

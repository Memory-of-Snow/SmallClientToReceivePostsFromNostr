package io.github.memory_of_snow.ParallelConnectionSandBox.clientendpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.memory_of_snow.ParallelConnectionSandBox.event.Event;
import io.github.memory_of_snow.ParallelConnectionSandBox.message.EventMessage;
import io.github.memory_of_snow.ParallelConnectionSandBox.message.Message;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebSocket
public class ClientEndPoint {

    private static final Logger log = Logger.getLogger(ClientEndPoint.class.getName());


    private final BlockingQueue<Message> queue;

    public ClientEndPoint(BlockingQueue<Message> queue) {
        this.queue = queue;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {

        session.setMaxTextMessageSize(16 * 1024);
        Thread currentThread = Thread.currentThread();
        long threadId = currentThread.getId();


        log.log(Level.INFO,String.format("onConnect()---スレッドID：%dで、%sと接続しました%n",threadId,session.getRemoteAddress()));

    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {

        Thread currentThread = Thread.currentThread();
        long threadId = currentThread.getId();
        log.log(Level.INFO,String.format("onClose()---スレッドID：%dで、%sを理由として、コード%dで接続が終了しました%n",threadId, reason,statusCode));

    }

    @OnWebSocketError
    public void onError(Throwable cause) {

        log.log(Level.WARNING,"onError()---%sを理由として、エラーが発生しました。",cause);

    }

    @OnWebSocketMessage
    public void onTextMessage(Session session, String receivedString)
    {
        if ("close".equalsIgnoreCase(receivedString)) {
            session.close(StatusCode.NORMAL, "bye");
            return;
        }

        Thread currentThread = Thread.currentThread();
        long threadId = currentThread.getId();

        log.log(Level.INFO,String.format("onTextMessage()---スレッドID：%dで、%sからメッセージを受け取りました。%n内容:%s%n",threadId,session.getRemoteAddress(),receivedString));

        deserializeMessage(receivedString);
    }

    @OnWebSocketMessage
    public void onBinaryMessage(byte[] payload, int offset, int length) {

        //バイナリーメッセージのときにここを使う

    }

    //返り値をVoidにして、受信メッセージを直接キューに突っ込むように改変
    private void deserializeMessage(String inputString){
        ObjectMapper objectMapper = new ObjectMapper();


        try{
            JsonNode jsonNode = objectMapper.readTree(inputString);

            String command = jsonNode.get(0).asText();
            String subscriptionId = jsonNode.get(1).asText();

            if ("EVENT".equals(command)){

                JsonNode innerJSON = objectMapper.readTree(jsonNode.get(2).toString());

                String id = innerJSON.get("id").asText();
                int kind = innerJSON.get("kind").asInt();
                String pubkey = innerJSON.get("pubkey").asText();
                long created_at = innerJSON.get("created_at").asLong();
                String content = innerJSON.get("content").asText();
                //List<List<String>> tags;
                String sig = innerJSON.get("sig").asText();


                Event event = new Event.Builder().id(id).kind(kind).pubkey(pubkey)
                        .created_at(created_at).content(content).sig(sig).build();

                EventMessage eventMessage = new EventMessage();
                eventMessage.setSubscriptionId(subscriptionId);
                eventMessage.setInnerEvent(event);

                if(!queue.offer(eventMessage)){
                    log.log(Level.WARNING,"メッセージのキューへの格納に失敗しました。");
                }
            }

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }


}

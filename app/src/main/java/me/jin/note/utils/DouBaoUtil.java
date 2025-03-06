package me.jin.note.utils;

import android.os.Build;

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DouBaoUtil {

    private static final String API_KEY = "cc445550-d8a9-4916-b6aa-b4887ceda605";
    private static final String BASE_URL = "https://ark.cn-beijing.volces.com/api/v3";
    private static final ConnectionPool CONNECTION_POOL = new ConnectionPool(5, 1, TimeUnit.SECONDS);
    private static final Dispatcher DISPATCHER = new Dispatcher();
    private static final ArkService SERVICE = ArkService.builder()
            .dispatcher(DISPATCHER)
            .connectionPool(CONNECTION_POOL)
            .baseUrl(BASE_URL)
            .apiKey(API_KEY)
            .build();

    private DouBaoUtil() {
        // 私有构造函数，防止实例化
    }

    /**
     * 发送标准请求并获取响应
     *
     * @param systemMessageContent 系统消息内容
     * @param userMessageContent   用户消息内容
     * @return 响应内容
     */
    public static String sendStandardRequest(String systemMessageContent, String userMessageContent) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemMessageContent).build());
        messages.add(ChatMessage.builder().role(ChatMessageRole.USER).content(userMessageContent).build());

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("doubao-1-5-lite-32k-250115")
                .messages(messages)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return (String) SERVICE.createChatCompletion(request).getChoices().get(0).getMessage().getContent();
        }
        return null;
    }

    /**
     * 发送流式请求并处理响应
     *
     * @param systemMessageContent 系统消息内容
     * @param userMessageContent   用户消息内容
     */
    public static void sendStreamRequest(String systemMessageContent, String userMessageContent) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemMessageContent).build());
        messages.add(ChatMessage.builder().role(ChatMessageRole.USER).content(userMessageContent).build());

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("doubao-1-5-lite-32k-250115")
                .messages(messages)
                .build();

        SERVICE.streamChatCompletion(request)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(choice -> {
                    if (choice.getChoices().size() > 0) {
                        System.out.print(choice.getChoices().get(0).getMessage().getContent());
                    }
                });
    }

    /**
     * 关闭服务
     */
    public static void shutdown() {
        SERVICE.shutdownExecutor();
    }
}

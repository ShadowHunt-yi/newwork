package top.year21.computerstore.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.year21.computerstore.utils.WebSocket;

import java.io.IOException;

/**
 * @Author Mr.chen
 * @Description TODO
 * @Date 2023/6/6 21:44
 * @Version 1.0
 */
@RestController
@RequestMapping("/webSocket")
public class WebSocketController {
    @Autowired
    private WebSocket webSocket;

    // 指定ip发送
    @RequestMapping("/sentMessage")
    public void sentMessage(String userId, String message) {
        try {
            webSocket.sendMessageByUserId(userId, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 群发
    @RequestMapping("/sendInfo")
    public void sendInfo(String message) {
        try {
            webSocket.sendInfo(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
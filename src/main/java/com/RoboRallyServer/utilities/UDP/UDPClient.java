package com.RoboRallyServer.utilities.UDP;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

// UDP Sunucusu (Mesaj Alıcı)

@Component
public class UDPClient {
/*
    @Value("${udp.client.ip}")
    private String clientIP;

    @Value("${udp.client.port}")
    private int clientPort;

    public void sendMessage(String message) {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buffer = message.getBytes();
            InetAddress address = InetAddress.getByName(clientIP);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, clientPort);
            System.out.println("packet : " + packet);
            socket.send(packet);
            socket.close();
            System.out.println("mesaj gönderildi.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
 */
}


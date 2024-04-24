package com.RoboRallyServer.utilities.UDP;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.atomic.AtomicReference;

// UDP Sunucusu (Mesaj Alıcı)

@Component
public class UDPServer {

/*
    @Value("${udp.server.port}")
    private int serverPort;

    @PostConstruct
    public String startUDPServer() {

        try {
            DatagramSocket socket = new DatagramSocket(serverPort);
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Socket'ten bir mesaj al
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("received message from UDPServer: " + message);

            // Socket'i kapat
            socket.close();

            return message;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
 */

    }


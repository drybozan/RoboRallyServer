package com.RoboRallyServer.utilities.UDP;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

// UDP Client  (Mesaj Gönderici)

@Component
public class UDPClient {

   @Value("${udp.client.port}")
    private int clientPort;

    public void sendMessage(String message) {
       //String[] ips = {"10.200.4.101"};
        String[] ips = {"192.168.1.23","192.168.1.24","192.168.1.25","192.168.1.26"};

        try {
            for (String ip : ips) {
                DatagramSocket socket = new DatagramSocket();
                byte[] buffer = message.getBytes();
                InetAddress address = InetAddress.getByName(ip);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, clientPort);
                socket.send(packet);
                socket.close();
            }
            //System.out.println("Mesaj istasyonalara gönderildi.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


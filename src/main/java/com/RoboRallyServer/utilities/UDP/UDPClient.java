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

    private final int[] clientPorts = { 6000,6002}; // Önceden tanımlanmış port numaraları

    public void sendMessage(String message) {
        //String[] ips = {"192.168.1.29", "192.168.1.26","192.168.1.23"};
        //String[] ips = {"192.168.1.29", "192.168.1.26","192.168.1.27"};
        String[] ips = {"192.168.1.29", "192.168.1.26"};

        try {

            for (String ip : ips) {
                for (int port : clientPorts) {
                    DatagramSocket socket = new DatagramSocket();
                    byte[] buffer = message.getBytes();
                    InetAddress address = InetAddress.getByName(ip);
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                    socket.send(packet);
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ready i göonderirken port bilgisi ile birlikte gonder
    public void sendMessageWithPort(String message , Integer port) {
        // String[] ips = {"192.168.1.29", "192.168.1.26","192.168.1.27"};
        String[] ips = {"192.168.1.29", "192.168.1.26"};

        try {
            for (String ip : ips) {
                DatagramSocket socket = new DatagramSocket();
                byte[] buffer = message.getBytes();
                InetAddress address = InetAddress.getByName(ip);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


package com.RoboRallyServer.utilities.UDP;


import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;


// UDP Sunucusu (Mesaj Alıcı)
@Component
@Slf4j
public class UDPServer {

    public String startServer(int port) {

        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[1024];
            for(int i = 0 ; i < 2 ; i++){
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                log.info(port + " Received from port: " + received);

                if (received.contains("id")) {

                    return received;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String startServerForFinish(int port) {

        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[1024];

            while(true){
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                log.info(port + " Received from port: " + received);

                if (received.contains("id")) {
                    return received;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}







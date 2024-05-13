package com.RoboRallyServer.utilities.UDP;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;


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







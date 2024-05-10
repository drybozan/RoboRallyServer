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
import java.util.concurrent.atomic.AtomicReference;

// UDP Sunucusu (Mesaj Alıcı)
@Component
@Slf4j
public class UDPServer {


        public String startServer(int port) {
           //new Thread(() -> {
                try (DatagramSocket socket = new DatagramSocket(port)) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        String received = new String(packet.getData(), 0, packet.getLength());
                        log.info( port + " Received from port: " + received);
                        // Socket'i kapat
                       // socket.close();
                        return received;

                } catch (Exception e) {
                    e.printStackTrace();
                }
         //     }).start();
            return null;
        }


    }







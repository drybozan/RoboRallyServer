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
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicReference;

// UDP Sunucusu (Mesaj Alıcı)
@Component
@Slf4j
public class UDPServer {

/*        public String startServer(int port) {
           //new Thread(() -> {
                try (DatagramSocket socket = new DatagramSocket(port)) {
                    byte[] buffer = new byte[1024];
                    socket.setSoTimeout(30000); // 30 saniyelik zaman aşımı
                    while (true) {

                        try {
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            socket.receive(packet);
                            String received = new String(packet.getData(), 0, packet.getLength());
                            log.info(port + " Received from port: " + received);

                            if (received.contains("id")) {
                                return received;
                            }
                        }catch (SocketTimeoutException e){

                            // Zaman aşımı durumunda işlem
                            log.info("Socket timeout occurred. 30 saniye boyunca robot cevap vermedi dinleme bitti.");
                            break;
                        }
                        // Socket'i kapat
                        // socket.close();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
         //     }).start();
            return null;
        }*/


    public String startServer(int port) {
       // boolean receivedId = false;
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[1024];
            //socket.setSoTimeout(30000); // 30 saniyelik zaman aşımı
           // long startTime = System.currentTimeMillis();
           // while (true) {
                // Zaman aşımını kontrol et
           /*     if (System.currentTimeMillis() - startTime > 2000) {
                    log.info("30 saniye boyunca robot kendi id bilgisini göndermedi, dinleme bitti.");
                    break;
                }*/
            for(int i = 0 ; i < 10 ; i++){
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







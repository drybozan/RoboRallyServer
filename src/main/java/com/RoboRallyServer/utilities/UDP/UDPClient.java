package com.RoboRallyServer.utilities.UDP;


import com.RoboRallyServer.dataAccess.abstracts.DefPortsDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

// UDP Client  (Mesaj Gönderici)
@Component
@Slf4j

public class UDPClient {
    private final DefPortsDao defPortsDao;
    private List<Integer> clientPorts;

    public UDPClient(DefPortsDao defPortsDao) {
        this.defPortsDao = defPortsDao;
        this.clientPorts = defPortsDao.getAllStartPorts();
    }

   // private final int[] clientPorts = {6001,6002,6003,6004}; // Önceden tanımlanmış port numaraları


    // List<Integer> clientPorts = this.defPortsDao.getAllPorts();

    // ready ve finish icin mesaj atarken kullanılıyor
    public void sendMessage(String message) {
        //String[] ips = {"192.168.1.25", "192.168.1.23","192.168.1.24","192.168.1.26"};
       // String[] ips = {"192.168.1.26","192.168.1.25","192.168.1.23","192.168.1.24"};

        List<String> ips = this.defPortsDao.getAllIps();

       log.warn(" ***********MESAJ GİTTİ : " + message);


        try {

            for (String ip : ips) {
                for (int port : clientPorts) {
                    //System.out.println("port2: " + port);
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

    // ready den alınan port bilgisi db ye kaydediliyor, ardından start gonderirken mesaj ve port bilgisi ile burası kullanılıyor.
    public void sendMessageWithPort(String message , Integer port) {
       // String[] ips = {"192.168.1.25", "192.168.1.23","192.168.1.24","192.168.1.26"};
       // String[] ips = {"192.168.1.26","192.168.1.25","192.168.1.23","192.168.1.24"};
        List<String> ips = this.defPortsDao.getAllIps();

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


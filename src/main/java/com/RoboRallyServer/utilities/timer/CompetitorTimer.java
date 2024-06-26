package com.RoboRallyServer.utilities.timer;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
@Component
public class CompetitorTimer {

    private Instant startTime;
    public volatile boolean isRunning;

    public void startTimer() {
        this.startTime = Instant.now();
        this.isRunning = true;

        // Ayrı bir thread oluştur ve geçen süreyi ekrana yazdır
        Thread timerThread = new Thread(() -> {
            while (isRunning) {
                printElapsedTime();
           /*     try {
                    Thread.sleep(1000); // Her saniye
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
        });

        timerThread.start();
    }

    private Duration getElapsedTime() {
        if (startTime != null) {
            Instant now = Instant.now();
            return Duration.between(startTime, now);
        } else {
            // Başlatılmış bir süre yoksa sıfır süre döndür.
            return Duration.ZERO;
        }
    }

    public String stopTimer() {
        this.isRunning = false;
        // Stop zamanını burada alabilirsiniz
        Instant stopTime = Instant.now();
        Duration elapsedTime = Duration.between(startTime, stopTime);
        return getFormattedElapsedTime(elapsedTime);
    }

    public String printElapsedTime() {
        Duration elapsedTime = getElapsedTime();
        return getFormattedElapsedTime(elapsedTime);
    }


    private String getFormattedElapsedTime(Duration elapsedTime) {
        long minutes = elapsedTime.toMinutes();
        long seconds = elapsedTime.minusMinutes(minutes).getSeconds();
        long millis = elapsedTime.minusMinutes(minutes).minusSeconds(seconds).toMillis();

        return String.format("%02d:%02d:%03d", minutes, seconds, millis);
    }


}
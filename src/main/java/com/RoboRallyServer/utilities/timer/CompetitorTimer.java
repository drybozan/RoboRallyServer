package com.RoboRallyServer.utilities.timer;

import java.time.Duration;
import java.time.Instant;

public class CompetitorTimer {
   /* private Instant startTime;
    private Instant stopTime;

    public void startTimer() {
        this.startTime = Instant.now();
    }

    public void stopTimer() {
        this.stopTime = Instant.now();
    }

    public Duration getElapsedTime() {
        if (startTime != null && stopTime != null) {
            return Duration.between(startTime, stopTime);
        } else {
            // Başlatılmış ve durdurulmuş bir süre yoksa veya sadece başlatılmışsa sıfır süre döndür.
            return Duration.ZERO;
        }
    }

    public String getFormattedElapsedTime() {
        Duration elapsedTime = getElapsedTime();

        long minutes = elapsedTime.toMinutes();
        long seconds = elapsedTime.minusMinutes(minutes).getSeconds();
        long millis = elapsedTime.minusMinutes(minutes).minusSeconds(seconds).toMillis();

        return String.format("%d:%02d:%03d", minutes, seconds, millis);
    }*/

    private Instant startTime;
    private volatile boolean isRunning;

    public void startTimer() {
        this.startTime = Instant.now();
        this.isRunning = true;

        // Ayrı bir thread oluştur ve geçen süreyi ekrana yazdır
        Thread timerThread = new Thread(() -> {
            while (isRunning) {
                printElapsedTime();
                try {
                    Thread.sleep(1000); // Her saniye
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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

    private void printElapsedTime(Duration elapsedTime) {
        System.out.println("formattedElapsedTime: " + getFormattedElapsedTime(elapsedTime));
    }



    private String getFormattedElapsedTime(Duration elapsedTime) {
        long minutes = elapsedTime.toMinutes();
        long seconds = elapsedTime.minusMinutes(minutes).getSeconds();
        long millis = elapsedTime.minusMinutes(minutes).minusSeconds(seconds).toMillis();

        return String.format("%d:%02d:%03d", minutes, seconds, millis);
    }

}
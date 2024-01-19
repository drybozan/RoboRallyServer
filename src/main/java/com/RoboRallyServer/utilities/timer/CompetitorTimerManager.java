package com.RoboRallyServer.utilities.timer;


import com.RoboRallyServer.dataAccess.abstracts.DefCompetitorsDao;
import com.RoboRallyServer.entities.DefCompetitors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class CompetitorTimerManager {
    private final Map<Integer, CompetitorTimer> competitorTimers = new HashMap<>();
    private final DefCompetitorsDao defCompetitorsDao;
    private final Timer timer = new Timer();
    private TimerTask timerTask; // TimerTask'i bir kere oluştur


    @Autowired
    public CompetitorTimerManager(DefCompetitorsDao defCompetitorsDao) {
        this.defCompetitorsDao = defCompetitorsDao;
    }

    /*
  kullanıcıların ve zamanlayıcının aynı veri üzerinde işlem yapmasını engellemek için bir kopya almak olabilir.
  CompetitorTimerManager sınıfında, competitorTimers üzerinde işlem yapmak istediğinizde, o anki durumu bir kopyasını alabilir ve bu kopya üzerinde
  işlemleri gerçekleştirebilirsiniz. Bu şekilde, orijinal veri üzerinde değişiklik yapmazsınız ve ConcurrentModificationException hatası almazsınız.


    */

   public void startTimer(int competitorId) {
        CompetitorTimer competitorTimer = new CompetitorTimer();
        competitorTimer.startTimer();

        // Sistem tarihini al
        LocalDateTime now = LocalDateTime.now();
        updateStartTime(competitorId, now);

        synchronized (competitorTimers) {
            Map<Integer, CompetitorTimer> competitorTimersCopy = new HashMap<>(competitorTimers);
            competitorTimersCopy.put(competitorId, competitorTimer);
            competitorTimers.clear();
            competitorTimers.putAll(competitorTimersCopy);

            if (timerTask == null) {
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        Map<Integer, CompetitorTimer> competitorTimersCopy = new HashMap<>(competitorTimers);
                        competitorTimersCopy.forEach((id, timer) -> {
                            String formattedElapsedTime = timer.printElapsedTime();
                            //System.out.println("id : " + id + " formattedElapsedTime:" + formattedElapsedTime);
                            updateDurationById(id, formattedElapsedTime);
                        });
                    }
                };

                timer.scheduleAtFixedRate(timerTask, 0, 100);
            }
        }
    }


    public void stopTimer(int competitorId) {
        CompetitorTimer competitorTimer = competitorTimers.get(competitorId);

        if (competitorTimer != null) {

            String competitorDuration = competitorTimer.stopTimer();

            // Sistem tarihini al
            LocalDateTime now = LocalDateTime.now();
            updateStopTime(competitorId,now);

            // bu competitorId'ye ait zamanlayıcıyı kaldır
            competitorTimers.remove(competitorId);

            System.out.println("Stop competior id: " + competitorId + " competitorDuration :" + competitorDuration);

            //stop edildiğinde eldeki sayaç değerini kaydet
            updateDurationById(competitorId, competitorDuration);

        }
    }

    public void updateDurationById(int id, String duration) {
        System.out.println("updateDurationById:" + id + " duration:" + duration);
        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id)) {
            DefCompetitors competitor = this.defCompetitorsDao.findById(id);


            if (duration.equals("01:00:00") || duration.compareTo("01:00:00") > 0) {
                competitor.setEliminated(true); // eger süre 5dk ya esitse yarismaciyi ele
                competitor.setReady(false);
                competitor.setStart(false);
                // bu competitorId'ye ait zamanlayıcıyı kaldır
                competitorTimers.remove(id);
            }
            competitor.setDuration(duration);
            this.defCompetitorsDao.save(competitor);
            // System.out.println("Id bilgisine göre duration güncellendi..");
        } else {
            System.out.println("Id bilgisine göre yarışmacı bulunamadı.");
        }
    }

    public void updateStartTime(int id, LocalDateTime startTime) {

        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id)) {

            DefCompetitors competitor = this.defCompetitorsDao.findById(id);
            // Tarih formatını belirle
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss:SSS");

            // Tarih bilgisini belirli formatta ayarla
            String formattedDateTime = startTime.format(formatter);

            competitor.setStartTime(formattedDateTime);
            this.defCompetitorsDao.save(competitor);

        } else {
            System.out.println("Id bilgisine göre yarışmacı bulunamadı.");
        }
    }

    public void updateStopTime(int id, LocalDateTime stopTime) {

        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id)) {

            DefCompetitors competitor = this.defCompetitorsDao.findById(id);
            // Tarih formatını belirle
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss:SSS");

            // Tarih bilgisini belirli formatta ayarla
            String formattedDateTime = stopTime.format(formatter);

            competitor.setStopTime(formattedDateTime);
            this.defCompetitorsDao.save(competitor);

        } else {
            System.out.println("Id bilgisine göre yarışmacı bulunamadı.");
        }
    }

}
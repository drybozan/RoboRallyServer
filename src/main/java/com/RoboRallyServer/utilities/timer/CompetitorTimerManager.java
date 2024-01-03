package com.RoboRallyServer.utilities.timer;


import com.RoboRallyServer.dataAccess.abstracts.DefCompetitorsDao;
import com.RoboRallyServer.entities.DefCompetitors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
    public void startTimer(int competitorId) {
        CompetitorTimer competitorTimer = new CompetitorTimer();
        competitorTimer.startTimer();
        competitorTimers.put(competitorId, competitorTimer);
        // TimerTask'i sadece bir kere oluştur
        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    competitorTimers.forEach((id, timer) -> {
                            String formattedElapsedTime = timer.printElapsedTime();
                            System.out.println("id : "+ id +"formattedElapsedTime:" + formattedElapsedTime);
                            updateDurationById(id, formattedElapsedTime);

                    });
                }
            };

            // Belirli aralıklarla görevi çalıştır
            timer.scheduleAtFixedRate(timerTask, 0, 1000); // Her saniye
        }
    }

    public void stopTimer(int competitorId) {
        CompetitorTimer competitorTimer = competitorTimers.get(competitorId);

        if (competitorTimer != null) {
            String competitorDuration = competitorTimer.stopTimer();
            System.out.println("id: "+competitorId+"competitorDuration :" + competitorDuration);

            //stop edildiğinde eldeki sayaç değerini kaydet
            updateDurationById(competitorId, competitorDuration);

            // bu competitorId'ye ait zamanlayıcıyı kaldır
            competitorTimers.remove(competitorId);
        }
    }

    /*private void saveElapsedTime(int competitorId) {
        CompetitorTimer competitorTimer = competitorTimers.get(competitorId);

        if (competitorTimer != null) {
            String formattedElapsedTime = competitorTimer.getFormattedElapsedTime();
            System.out.println("formattedElapsedTime:" + formattedElapsedTime);
            updateDurationById(competitorId, formattedElapsedTime);
        }
    }*/

    public void updateDurationById(int id, String duration) {
        System.out.println("updateDurationById:"+id +" duration:" + duration);
        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id)) {
            DefCompetitors competitor = this.defCompetitorsDao.findById(id);
            if (duration.equals("05.00:00")) {
                competitor.setEliminated(true); // eger süre 5dk ya esitse yarismaciyi ele
            }
            competitor.setDuration(duration);
            this.defCompetitorsDao.save(competitor);
            System.out.println("Id bilgisine göre duration güncellendi..");
        } else {
            System.out.println("Id bilgisine göre yarışmacı bulunamadı.");
        }
    }

}
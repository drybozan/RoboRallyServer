package com.RoboRallyServer.business.concretes;

import com.RoboRallyServer.business.abstracts.DefCompetitorsService;
import com.RoboRallyServer.dataAccess.abstracts.DefCompetitorsDao;
import com.RoboRallyServer.entities.DefCompetitors;
import com.RoboRallyServer.utilities.results.*;
import com.RoboRallyServer.utilities.timer.CompetitorTimer;
import com.RoboRallyServer.utilities.timer.CompetitorTimerManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class DefCompetitorsManager implements DefCompetitorsService {

    private final DefCompetitorsDao defCompetitorsDao;

    private final CompetitorTimer competitorTimer;
    private final Timer timer = new Timer();
    private TimerTask timerTask; // TimerTask'i bir kere oluştur

    // Integer tipinde ID'leri tutan bir Map
    Map<Integer, String> idMap = new HashMap<>();


    @Override
    public Result add(DefCompetitors competitors) {

        if (competitors == null) {
            return new SuccessResult("Yarışmacı bilgileri null olamaz.");
        }
        this.defCompetitorsDao.save(competitors);
        return new SuccessResult("Yarışmacı başarıyla kaydedildi.");
    }

    @Override
    public DataResult<List<DefCompetitors>> getAllCompetitors() {

        try {

            List<DefCompetitors> competitors = this.defCompetitorsDao.findAll();

            return new SuccessDataResult<>(competitors, "Yarışmacılar listelendi.");

        } catch (Exception e) {
            return new ErrorDataResult<>("Yarışmacılar listelenirken bir hata oluştu.");
        }

    }

    @Override
    public DataResult<List<DefCompetitors>> getAllCompetitorsByDuration() {
        try {

            List<DefCompetitors> competitors = this.defCompetitorsDao.getAllCompetitorsByDuration();

            return new SuccessDataResult<>(competitors, "Yarışmacılar duration bilgisine göre listelendi.");

        } catch (Exception e) {
            return new ErrorDataResult<>("Yarışmacılar listelenirken bir hata oluştu.");
        }
    }

    @Override
    public Result delete(int id) {

        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id)) {
            this.defCompetitorsDao.deleteById(id);

          /*  // bu competitorId'ye ait zamanlayıcıyı kaldır
            CompetitorTimer competitorTimer = competitorTimers.get(id);

            if (competitorTimer != null) {

                // bu competitorId'ye ait zamanlayıcıyı kaldır
                competitorTimers.remove(id);
            }*/
            return new SuccessResult("Yarışmacı başarıyla silindi.");
        } else {
            return new ErrorResult("Yarışmacı bilgisi bulunamadı.");
        }

    }

    @Override
    public Result update(DefCompetitors newCompetitor) {

        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(newCompetitor.getId())) {
            DefCompetitors oldCompetitor = this.defCompetitorsDao.findById(newCompetitor.getId());
            oldCompetitor.setCity(newCompetitor.getCity());
            oldCompetitor.setName(newCompetitor.getName());
            oldCompetitor.setEliminated(newCompetitor.isEliminated());

            //eger kullancı elendiyse manuel olarak timer ı varsa sonlandır.
            if (newCompetitor.isEliminated()) {
                //stopTimer(newCompetitor.getId());
                oldCompetitor.setReady(false);
                oldCompetitor.setStart(false);
            }
            this.defCompetitorsDao.save(oldCompetitor);

            return new SuccessResult("Yarışmacı başarıyla güncellendi.");
        } else {
            return new ErrorResult("Yarışmacı bilgisi bulunamadı.");
        }

    }

    @Override
    public DataResult<DefCompetitors> getById(int id) {
        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id)) {
            DefCompetitors competitor = this.defCompetitorsDao.findById(id);

            return new SuccessDataResult<DefCompetitors>(competitor, "Id bilgisine göre data listelendi.");
        } else {
            return new ErrorDataResult<DefCompetitors>("Id bilgisine göre yarışmacı bulunamadı.");
        }
    }


    //gönderilen kod bilgisne göre kullanıcı varsa ve elenmediyse ready bitini true yapar
    @Override
    public Result updateReadyByCode(String code, boolean ready) {

        //System.out.println("Code :" + code + " ready :" + ready);

        DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(code);
        if (defCompetitor != null) {
            if (!defCompetitor.isEliminated()) {
                defCompetitor.setReady(ready);
                this.defCompetitorsDao.save(defCompetitor);
                return new SuccessResult(code + " koduna sahip yarışmacı için ready alanı güncellendi.");
            }
            return new SuccessResult(code + " koduna sahip yarışmacı için elenmiş durumda.");
        }
        return new ErrorResult(code + " koduna sahip yarışmacı bulunamadı.");
    }


    @Override
    public Result updateStartByCode(String[] codes) {
        // CountDownLatch oluşturuluyor, ve her bir kod için birer thread başlatılıyor.
        CountDownLatch latch = new CountDownLatch(codes.length);

        List<Thread> threads = new ArrayList<>();

        for (String code : codes) {
            Thread thread = new Thread(() -> {
                try {
                    // System.out.println("Code: " + code + "  start: " + start);

                    DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(code);

                    if (defCompetitor != null) {
                        if (!defCompetitor.isEliminated()) {
                            if (defCompetitor.isReady()) {


                              /*  LocalDateTime now = LocalDateTime.now();

                                updateStartTime(defCompetitor.getId(), now);*/

                                idMap.put(defCompetitor.getId(), defCompetitor.getName());

                                System.out.println("calisti 1 : "+ LocalDateTime.now());
                                System.out.println(code + " koduna sahip yarışmacı için isStart güncellendi ve sayaç başladı.");

                            } else {
                                System.out.println(code + " koduna sahip yarışmacı ready komutunu göndermedi.");
                            }
                        } else {
                            System.out.println(code + " koduna sahip yarışmacı elenmiş durumda, sayaç başlatılmadı");
                        }
                    } else {
                        System.out.println(code + " koduna sahip yarışmacı bulunamadı.");
                    }
                } finally {
                    // Thread tamamlandığında latch sayısını azalt
                    latch.countDown();
                }
            });

            threads.add(thread);
            thread.start();
        }

        try {
            // Tüm thread'lerin tamamlanmasını bekleyin
            latch.await();
            System.out.println("calisti 2: "+ LocalDateTime.now());
            // Yarışmacılar için timer'ı başlat
            startTimer(idMap);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ErrorResult("Thread çalışması kesildi");
        }

        return new SuccessResult("Sayaçlar başlatıldı.");
    }


    // gönderilen kod bilgisine göre eğer yarışmacı elenmemişse, hazır ve başlamışsa bunları false a çeker ve timerı durdurur
    @Override
    public Result updateReadyAndStartByCode(String[] codes) {

        // Kod sayısı kadar bir CountDownLatch oluşturun
        CountDownLatch latch = new CountDownLatch(codes.length);

        List<Thread> threads = new ArrayList<>();

        for (String code : codes) {
            Thread thread = new Thread(() -> {
                try {

                    DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(code);

                    if (defCompetitor != null) {

                        if (!defCompetitor.isEliminated()) { //eliminated false

                            if (defCompetitor.isStart()) { // start true

                                idMap.remove(defCompetitor.getId(), defCompetitor.getName());
                                System.out.println("calisti 4 : "+ LocalDateTime.now());
                                System.out.println(code + " koduna sahip yarışmacı için isStart güncellendi ve sayaç durduruldu.");
                            } else {
                                System.out.println(code + " koduna sahip yarışmacı  start komutunu göndermedi.");
                            }
                        } else {
                            System.out.println(code + " koduna sahip yarışmacı elenmiş durumda, sayacı yok.");
                        }
                    } else {
                        System.out.println(code + " koduna sahip yarışmacı bulunamadı.");
                    }
                } finally {
                    // Thread tamamlandığında latch sayısını azalt
                    latch.countDown();
                }
            });

            threads.add(thread);
            thread.start();
        }
        try {
            // Tüm thread'lerin tamamlanmasını bekleyin
            latch.await();
            LocalDateTime now = LocalDateTime.now();
            System.out.println("calisti 5 : "+ LocalDateTime.now());
            for (String code : codes) {
                int defCompetitorId = this.defCompetitorsDao.findByCode(code).getId();
                // yarışmacı için timer ı durdur.
                updateStopTime(defCompetitorId, now);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ErrorResult("Thread çalışması kesildi");
        }

        return new SuccessResult("Sayaçlar durduruldu.");


    }



    public void startTimer(Map<Integer, String> idMap) {

        System.out.println("calisti 3 : "+ LocalDateTime.now());

        competitorTimer.startTimer();

        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<Integer, String> entry : idMap.entrySet()) {
            Integer id = entry.getKey();
            updateStartTime(id, now);
        }

        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    // Güvenli bir kopya oluştur
                    Map<Integer, String> idMapCopy = new HashMap<>(idMap);

                    //System.out.println("idMap.isEmpty() : "+ idMapCopy.isEmpty());
                    if (idMapCopy.isEmpty()) {
                       // System.out.println("stop edildi *********************************");
                        competitorTimer.stopTimer();
                        timerTask.cancel();
                        timerTask = null;
                    } else {

                        System.out.println("Timer değeri : " + competitorTimer.printElapsedTime());
                        String duration = competitorTimer.printElapsedTime();
                        // Güvenli kopya üzerinde döngü yap
                        for (Map.Entry<Integer, String> entry : idMapCopy.entrySet()) {
                            Integer id = entry.getKey();
                            updateDurationById(id, duration);
                        }
                    }
                }
            };
            timer.scheduleAtFixedRate(timerTask, 0, 1);
        }
    }

    public  void updateDurationById(int id, String duration) {

       System.out.println("updateDurationById:" + id + " duration:" + duration);
        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id)) {
            DefCompetitors competitor = this.defCompetitorsDao.findById(id);


            if (duration.equals("02:00:00") || duration.compareTo("02:00:00") > 0) {
                competitor.setEliminated(true); // eger süre 5dk ya esitse yarismaciyi ele
                competitor.setReady(false);
                competitor.setStart(false);
                //map ten bu yarışmacıyı çıkar , artık timer değerini alamasın.
                idMap.remove(id);
            }
            competitor.setDuration(duration);
            this.defCompetitorsDao.save(competitor);
            // System.out.println("Id bilgisine göre duration güncellendi..");
        } else {
            System.out.println("Id bilgisine göre yarışmacı bulunamadı.");
        }

    }


    public void updateStartTime(int id, LocalDateTime startTime) {
        System.out.println("calisti 7: "+ LocalDateTime.now());
        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id)) {

            DefCompetitors competitor = this.defCompetitorsDao.findById(id);
            // Tarih formatını belirle
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss:SSS");

            // Tarih bilgisini belirli formatta ayarla
            String formattedDateTime = startTime.format(formatter);

            competitor.setReady(false);
            competitor.setStart(true);
            competitor.setStartTime(formattedDateTime);

            this.defCompetitorsDao.save(competitor);

        } else {
            System.out.println("Id bilgisine göre yarışmacı bulunamadı.");
        }
    }

    public void updateStopTime(int id, LocalDateTime stopTime) {
        System.out.println("calisti 6 : "+ LocalDateTime.now());

        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id)) {

            DefCompetitors competitor = this.defCompetitorsDao.findById(id);
            // Tarih formatını belirle
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss:SSS");

            // Tarih bilgisini belirli formatta ayarla
            String formattedDateTime = stopTime.format(formatter);

            competitor.setStopTime(formattedDateTime);
            competitor.setReady(false);
            competitor.setStart(false);
            this.defCompetitorsDao.save(competitor);

            //map ten bu yarışmacıyı çıkar , artık timer değerini alamasın.
            //idMap.remove(id);

            System.out.println("updateStopTime : " + competitor);

        } else {
            System.out.println("Id bilgisine göre yarışmacı bulunamadı.");
        }
    }
}
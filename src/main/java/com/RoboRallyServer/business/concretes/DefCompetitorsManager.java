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
    private final Map<Integer, CompetitorTimer> competitorTimers = new HashMap<>();
    private final Timer timer = new Timer();
    private TimerTask timerTask; // TimerTask'i bir kere oluştur
    private ExecutorService executorService = Executors.newFixedThreadPool(5); // Örnek olarak 5 thread


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

            // bu competitorId'ye ait zamanlayıcıyı kaldır
            CompetitorTimer competitorTimer = competitorTimers.get(id);

            if (competitorTimer != null) {

                // bu competitorId'ye ait zamanlayıcıyı kaldır
                competitorTimers.remove(id);
            }
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
                stopTimer(newCompetitor.getId());
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

                                CompetitorTimer competitorTimer = new CompetitorTimer();
                                LocalDateTime now = LocalDateTime.now();
                                updateStartTime(defCompetitor.getId(), now);
                                competitorTimers.put(defCompetitor.getId(), competitorTimer);

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
            // Yarışmacılar için timer'ı başlat
            startTimer();
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

                    //System.out.println("Code :" + code + " ready : " + ready + " start :" + start);
                    DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(code);

                    if (defCompetitor != null) {

                        if (!defCompetitor.isEliminated()) { //eliminated false

                            if (defCompetitor.isStart()) { // ready true,start true

                                // yarışmacı için timer ı durdur.
                                stopTimer(defCompetitor.getId());


                                System.out.println(code + " koduna sahip yarışmacı için isStart ve isReady güncellendi ve sayaç durduruldu.");
                            } else {
                                System.out.println(code + " koduna sahip yarışmacı ready ve start komutunu göndermedi.");
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

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ErrorResult("Thread çalışması kesildi");
        }

        return new SuccessResult("Sayaçlar durduruldu.");


    }


    public void startTimer() {
        System.out.println("basladi: " + LocalDateTime.now());

        synchronized (competitorTimers) {

            System.out.println("competitorTimers.size()" + competitorTimers.size());


            List<Thread> threads = new ArrayList<>();

            // Bütün CompetitorTimer nesnelerini aynı anda başlat
            competitorTimers.forEach((id, timer) -> {
                Thread startTimerThread = new Thread(() -> {
                    timer.startTimer();
                });
                threads.add(startTimerThread);
                startTimerThread.start();
            });

            // Tüm iş parçacıklarının tamamlanmasını bekleyin
            threads.forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            if (timerTask == null) {
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        synchronized (competitorTimers) {
                            Iterator<Map.Entry<Integer, CompetitorTimer>> iterator = competitorTimers.entrySet().iterator();
                            while (iterator.hasNext()) {
                                Map.Entry<Integer, CompetitorTimer> entry = iterator.next();
                                Integer id = entry.getKey();
                                CompetitorTimer timer = entry.getValue();
                                System.out.println("id : " + id + "timer : " + timer.printElapsedTime());

                                // Asenkron işlem başlat, ancak dönen değeri kullanma
                                updateDurationById(id, timer.printElapsedTime());


                            }
                        }
                    }
                };
                timer.scheduleAtFixedRate(timerTask, 0, 100);
            }
        }
    }

    @Async
    @Transactional
    public CompletableFuture<Void> updateDurationById(int id, String duration) {
        System.out.println("updateDurationById:" + id + " duration:" + duration);
        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id) && competitorTimers.get(id) != null) {
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
        return CompletableFuture.completedFuture(null);
    }



    public void stopTimer(int competitorId) {
        CompetitorTimer competitorTimer = competitorTimers.get(competitorId);

        if (competitorTimer != null) {

            // bu competitorId'ye ait zamanlayıcıyı kaldır
            competitorTimers.remove(competitorId);

            String competitorDuration = competitorTimer.stopTimer();

            // Sistem tarihini al
            LocalDateTime now = LocalDateTime.now();
            updateStopTime(competitorId, now, competitorDuration);

            System.out.println("Stop competior id: " + competitorId + " competitorDuration :" + competitorDuration);

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

            competitor.setReady(false);
            competitor.setStart(true);
            competitor.setStartTime(formattedDateTime);

            this.defCompetitorsDao.save(competitor);

        } else {
            System.out.println("Id bilgisine göre yarışmacı bulunamadı.");
        }
    }

    public void updateStopTime(int id, LocalDateTime stopTime,String competitorDuration) {

        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id)) {

            DefCompetitors competitor = this.defCompetitorsDao.findById(id);
            // Tarih formatını belirle
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss:SSS");

            // Tarih bilgisini belirli formatta ayarla
            String formattedDateTime = stopTime.format(formatter);

            competitor.setStopTime(formattedDateTime);
            competitor.setDuration(competitorDuration);
            competitor.setReady(false);
            competitor.setStart(false);
            this.defCompetitorsDao.save(competitor);

            System.out.println("updateStopTime : "+ competitor);

        } else {
            System.out.println("Id bilgisine göre yarışmacı bulunamadı.");
        }
    }
}
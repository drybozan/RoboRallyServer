package com.RoboRallyServer.business.concretes;

import com.RoboRallyServer.business.abstracts.DefCompetitorsService;
import com.RoboRallyServer.dataAccess.abstracts.DefCompetitorsDao;
import com.RoboRallyServer.entities.DefCompetitors;
import com.RoboRallyServer.utilities.log.LogEntity;
import com.RoboRallyServer.utilities.log.LogService;
import com.RoboRallyServer.utilities.results.*;
import com.RoboRallyServer.utilities.timer.CompetitorTimer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@RequiredArgsConstructor
public class DefCompetitorsManager implements DefCompetitorsService {

    private final DefCompetitorsDao defCompetitorsDao;
    private final Timer timer = new Timer();
    private TimerTask timerTask; // TimerTask'i bir kere oluştur

    // Integer tipinde ID'leri tutan bir Map
    //Map<Integer, String> idMap = new HashMap<>();
    static Map<Integer, CompetitorTimer> idMap = new HashMap<>();

    // Tarih formatını belirle
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss:SSS");

    @Autowired
    private final LogService logService;
    LogEntity logEntity = new LogEntity();


    @Override
    public Result add(DefCompetitors competitors) {

        if (competitors == null) {
            return new SuccessResult("Yarışmacı bilgileri null olamaz.");
        }

        // Yarışmacı bilgilerini veritabanına kaydet
        this.defCompetitorsDao.save(competitors);

        logEntity.setDate(LocalDateTime.now().format(formatter));
        logEntity.setMessage("Yarışmacı Kaydedildi. Yarışmacı bilgileri: " + competitors);
        logEntity.setSender(competitors.getName());
        logEntity.setMessageType("SUCCESS");

        logService.writeLog(logEntity);

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
            // sayacını kaldır
            idMap.remove(id);
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
                // sayacını kaldır
                idMap.remove(newCompetitor.getId());
                oldCompetitor.setReady(false);
                oldCompetitor.setStart(false);

                logEntity.setDate(LocalDateTime.now().format(formatter));
                logEntity.setMessage("Yarışmacı elendi . Sayacı durduruldu.");
                logEntity.setSender(newCompetitor.getName());
                logEntity.setMessageType("INFO");

                logService.writeLog(logEntity);
            }
            this.defCompetitorsDao.save(oldCompetitor);

            logEntity.setDate(LocalDateTime.now().format(formatter));
            logEntity.setMessage("Yarışmacı başarıyla güncellendi. Yarışmacı bilgileri: " + this.defCompetitorsDao.findById(newCompetitor.getId()));
            logEntity.setSender(newCompetitor.getName());
            logEntity.setMessageType("SUCCESS");

            logService.writeLog(logEntity);

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
    public Result updateReadyByCode(String code) {
        DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(code);

        if (defCompetitor != null) {
            if (!defCompetitor.isEliminated()) {
                defCompetitor.setReady(true);
                this.defCompetitorsDao.save(defCompetitor);

                logEntity.setDate(LocalDateTime.now().format(formatter));
                logEntity.setMessage(defCompetitor.getName() + " ready komutunu gönderdi. Kod :  " + code );
                logEntity.setSender(defCompetitor.getName());
                logEntity.setMessageType("INFO");

                logService.writeLog(logEntity);

                return new SuccessResult(code + " koduna sahip yarışmacı için ready alanı güncellendi.");
            }
            logEntity.setDate(LocalDateTime.now().format(formatter));
            logEntity.setMessage(defCompetitor.getName() + " ready komutu gönderdi ama elenmiş durumda. ready kodu : " + code);
            logEntity.setSender(defCompetitor.getName());
            logEntity.setMessageType("ERROR");

            logService.writeLog(logEntity);

            return new SuccessResult(code + " koduna sahip yarışmacı  elenmiş durumda.");

        }
        return new ErrorResult(code + " koduna sahip yarışmacı bulunamadı.");
    }


    @Override
    public Result updateStartByCode(String[] codes) {

        for (String code : codes) {

            DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(code);

            if (defCompetitor != null) {
                if (!defCompetitor.isEliminated()) {
                    if (defCompetitor.isReady()) {

                        // bu id ile bir timer oluştur.
                        idMap.put(defCompetitor.getId(), new CompetitorTimer());

                        for (Integer id : idMap.keySet().toArray(new Integer[0])) {

                            System.out.println("id :" + id);
                        }
                        logEntity.setDate(LocalDateTime.now().format(formatter));
                        logEntity.setMessage(defCompetitor.getName() + " start komutunu gönderdi. Kod :  " + code );
                        logEntity.setSender(defCompetitor.getName());
                        logEntity.setMessageType("INFO");

                        logService.writeLog(logEntity);

                        System.out.println(code + " koduna sahip yarışmacı için isStart güncellendi ve sayaç başladı.");

                    } else {
                        System.out.println(code + " koduna sahip yarışmacı ready komutunu göndermedi.");

                        logEntity.setDate(LocalDateTime.now().format(formatter));
                        logEntity.setMessage(defCompetitor.getName() + " start komutunu gönderdi ama daha önce ready komutunu göndermedi ! Kod :" + code);
                        logEntity.setSender(defCompetitor.getName());
                        logEntity.setMessageType("ERROR");

                        logService.writeLog(logEntity);
                    }
                } else {
                    System.out.println(code + " koduna sahip yarışmacı elenmiş durumda, sayaç başlatılmadı");

                    logEntity.setDate(LocalDateTime.now().format(formatter));
                    logEntity.setMessage(defCompetitor.getName() + " start komutu gönderdi ama elenmiş durumda, sayaç başlatılmadı");
                    logEntity.setSender(defCompetitor.getName());
                    logEntity.setMessageType("ERROR");
                    logService.writeLog(logEntity);
                }
            } else {
                System.out.println(code + " koduna sahip yarışmacı bulunamadı.");
            }

        }

        // idler toplandıktan sonra start çalışsın
        startTimer();

        // Map'ten sadece key'leri al
        Integer[] idArrayStart = idMap.keySet().toArray(new Integer[0]);

        //start tarihini ve start bitini güncelle ve timerları başlat
        updateStartTime(idArrayStart);

        return new SuccessResult("Sayaçlar başlatıldı.");
    }

    public void updateStartTime(Integer[] IdArray) {

        for (Integer id : IdArray) {
            // bu id ye ait kayıt var mı
            Optional<DefCompetitors> optionalCompetitor = this.defCompetitorsDao.findById(id);

            //bu id ye ait timer ı başlat
            CompetitorTimer timer = idMap.get(id);
            timer.startTimer();

            // başlangıç tarih bilgisini belirli formatta ayarla
            String formattedDateTime = LocalDateTime.now().format(formatter);


            if (optionalCompetitor.isPresent()) {
                DefCompetitors competitor = optionalCompetitor.get();
                competitor.setReady(false);
                competitor.setStart(true);
                competitor.setStartTime(formattedDateTime);

                this.defCompetitorsDao.save(competitor);

                logEntity.setDate(formattedDateTime);
                logEntity.setMessage(competitor.getName() + " için sayaç başladı.Yarışmacı bilgileri : " + competitor);
                logEntity.setSender(competitor.getName());
                logEntity.setMessageType("INFO");
                logService.writeLog(logEntity);


            } else {
                System.out.println("Id bilgisine göre yarışmacı bulunamadı.");
            }

        }

    }


    // gönderilen kod bilgisine göre eğer yarışmacı elenmemişse, hazır ve başlamışsa bunları false a çeker ve timerı durdurur
    @Override
    public Result updateReadyAndStartByCode(String[] codes) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss:SSS");


        for (String code : codes) {

            DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(code);

            if (defCompetitor != null) {

                if (!defCompetitor.isEliminated()) { //eliminated false

                    if (defCompetitor.isStart()) { // start true

                        System.out.println("********** stop id from map :" + defCompetitor.getId());

                        CompetitorTimer timer = idMap.get(defCompetitor.getId());


                        idMap.remove(defCompetitor.getId());



                        DefCompetitors competitor = this.defCompetitorsDao.findById(defCompetitor.getId());

                        // Tarih bilgisini belirli formatta ayarla
                        String formattedDateTime = LocalDateTime.now().format(formatter);

                        if (competitor != null) {

                            competitor.setStopTime(formattedDateTime);
                            competitor.setDuration(timer.printElapsedTime());
                            competitor.setReady(false);
                            competitor.setStart(false);
                            DefCompetitors stoppedCompetitor = this.defCompetitorsDao.save(competitor);
                            System.out.println("stoppedCompetitor : " + stoppedCompetitor);

                            logEntity.setDate(formattedDateTime);
                            logEntity.setMessage(stoppedCompetitor.getName() + " parkuru bitirdi.Sayaç durduruldu.Yarışmacı bilgileri : " + stoppedCompetitor);
                            logEntity.setSender(stoppedCompetitor.getName());
                            logEntity.setMessageType("INFO");
                            logService.writeLog(logEntity);

                        } else {
                            System.out.println("Id bilgisine göre yarışmacı bulunamadı.");
                        }


                        System.out.println(code + " koduna sahip yarışmacı için isStart güncellendi ve sayaç durduruldu.");
                    } else {
                        System.out.println(code + " koduna sahip yarışmacı  start komutunu göndermedi.");

                        logEntity.setDate(LocalDateTime.now().format(formatter));
                        logEntity.setMessage(defCompetitor.getName() + " daha önce start komutunu göndermedi.Yarışmacı bilgileri : " + defCompetitor);
                        logEntity.setSender(defCompetitor.getName());
                        logEntity.setMessageType("ERROR");
                        logService.writeLog(logEntity);
                    }
                } else {
                    System.out.println(code + " koduna sahip yarışmacı elenmiş durumda, sayacı yok.");

                    logEntity.setDate(LocalDateTime.now().format(formatter));
                    logEntity.setMessage(defCompetitor.getName() + " koduna sahip yarışmacı elenmiş durumda.Yarışmacı bilgileri : " + defCompetitor);
                    logEntity.setSender(defCompetitor.getName());
                    logEntity.setMessageType("ERROR");
                    logService.writeLog(logEntity);
                }
            } else {
                System.out.println(code + " koduna sahip yarışmacı bulunamadı.");
            }
        }


        return new SuccessResult("Sayaçlar durduruldu.");

    }


    public void startTimer() {
        //saniyede bir çalışan görev başlat
        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    Set<Integer> idSet = new HashSet<>(idMap.keySet()); // idMap'ten idSet oluştur

                    if (idSet.isEmpty()) {
                        timerTask.cancel();
                        timerTask = null;
                    } else {
                        updateDurationById(idSet); // idSet'i kullanarak updateDurationById metodunu çağır
                    }
                }
            };
            timer.scheduleAtFixedRate(timerTask, 0, 1);
        }
    }


    public void updateDurationById(Set<Integer> idSet) {
        for (Integer id : idSet) {
            //System.out.println("************* updateDurationById idSet.size() : " + idSet.size());
            // Bu id ye ait kayıt var mı
            Optional<DefCompetitors> optionalCompetitor = this.defCompetitorsDao.findById(id);

            if (optionalCompetitor.isPresent()) {
                DefCompetitors competitor = optionalCompetitor.get();

                if (competitor.isStart()) {
                    CompetitorTimer timer = idMap.get(id);

                    if (timer != null) {
                        String duration = timer.printElapsedTime();
                        System.out.println("update duration Id:" + id + " duration:" + duration);

                        if (duration.equals("02:00:00") || duration.compareTo("02:00:00") > 0) {
                            competitor.setEliminated(true);
                            competitor.setReady(false);
                            competitor.setStart(false);
                            competitor.setStopTime(LocalDateTime.now().format(formatter));

                            competitor.setDuration(duration);
                            DefCompetitors savedCompetitor = this.defCompetitorsDao.save(competitor);
                            System.out.println("updatedCompetitor:" + savedCompetitor);

                            logEntity.setDate(LocalDateTime.now().format(formatter));
                            logEntity.setMessage(savedCompetitor.getName() + " 5 dakika  boyunca parkuru tamamlayamadı.Yarışmacı elendi. Yarışmacı bilgileri :" + savedCompetitor);
                            logEntity.setSender(savedCompetitor.getName());
                            logEntity.setMessageType("INFO");
                            logService.writeLog(logEntity);

                            // idSet'ten de kaldır
                            idSet.remove(id);
                            // idMap'ten de kaldır
                            idMap.remove(id);
                        } else {
                            competitor.setDuration(duration);
                            DefCompetitors savedCompetitor = this.defCompetitorsDao.save(competitor);
                            System.out.println("updatedCompetitor:" + savedCompetitor);
                        }
                    }
                }
            } else {
                // idSet'ten de kaldır
                idSet.remove(id);
                System.out.println("updateDurationById Id bilgisine göre yarışmacı bulunamadı.");
            }
        }
    }






}
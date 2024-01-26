package com.RoboRallyServer.business.concretes;

import com.RoboRallyServer.business.abstracts.DefCompetitorsService;
import com.RoboRallyServer.dataAccess.abstracts.DefCompetitorsDao;
import com.RoboRallyServer.entities.DefCompetitors;
import com.RoboRallyServer.utilities.results.*;
import com.RoboRallyServer.utilities.timer.CompetitorTimer;
import lombok.RequiredArgsConstructor;
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


    @Override
    public Result add(DefCompetitors competitors) {

        if (competitors == null) {
            return new SuccessResult("Yarışmacı bilgileri null olamaz.");
        }

        // Yarışmacı bilgilerini veritabanına kaydet
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

        }

        // idler toplandıktan sonra start çalışsın
        startTimer(idMap);

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
                        } else {
                            System.out.println("Id bilgisine göre yarışmacı bulunamadı.");
                        }


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
        }
        // startTimer metodunu çağırıp elemanları güncelle
        startTimer(idMap);

        return new SuccessResult("Sayaçlar durduruldu.");

    }


    public void startTimer(Map<Integer, CompetitorTimer> idMap) {

        //saniyede bir çalışan görev başlat
        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {

                    if (idMap.isEmpty()) {
                        timerTask.cancel();
                        timerTask = null;
                    } else {

                        Integer[] idArray = idMap.keySet().toArray(new Integer[0]);

                        updateDurationById(idArray);

                    }
                }
            };
            timer.scheduleAtFixedRate(timerTask, 0, 1);
        }
    }


    public void updateDurationById(Integer[] idArray) {

        for (Integer id : idArray) {

            CompetitorTimer timer = idMap.get(id);

            if (timer != null) {
                String duration = timer.printElapsedTime();
                System.out.println("updateDurationById:" + id + " duration:" + duration);

                // bu id ye ait kayıt var mı
                Optional<DefCompetitors> optionalCompetitor = this.defCompetitorsDao.findById(id);

                if (optionalCompetitor.isPresent()) {
                    DefCompetitors competitor = optionalCompetitor.get();

                    if(competitor.isStart()) {

                        if (duration.equals("02:00:00") || duration.compareTo("02:00:00") > 0) {
                            competitor.setEliminated(true);
                            competitor.setReady(false);
                            competitor.setStart(false);
                            // map'ten bu yarışmacıyı çıkar, artık timer değerini alamasın.
                            idMap.remove(id);
                        }

                        competitor.setDuration(duration);
                        DefCompetitors savedCompetitor = this.defCompetitorsDao.save(competitor);
                        System.out.println("updatedCompetitor:" + savedCompetitor);
                    }
                }
            } else {
                System.out.println("updateDurationById Id bilgisine göre yarışmacı bulunamadı.");
            }
        }

    }





}
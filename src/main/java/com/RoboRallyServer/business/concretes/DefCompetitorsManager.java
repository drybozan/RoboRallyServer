package com.RoboRallyServer.business.concretes;

import com.RoboRallyServer.business.abstracts.DefCompetitorsService;
import com.RoboRallyServer.dataAccess.abstracts.DefCompetitorsDao;
import com.RoboRallyServer.entities.DefCompetitors;
import com.RoboRallyServer.utilities.UDP.UDPClient;
import com.RoboRallyServer.utilities.UDP.UDPServer;
import com.RoboRallyServer.utilities.log.LogEntity;
import com.RoboRallyServer.utilities.log.LogService;
import com.RoboRallyServer.utilities.results.*;
import com.RoboRallyServer.utilities.timer.CompetitorTimer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@RequiredArgsConstructor
public class DefCompetitorsManager implements DefCompetitorsService {

    private final DefCompetitorsDao defCompetitorsDao;
    private final Timer timer = new Timer();
    private TimerTask timerTask; // TimerTask'i bir kere oluştur

    static Map<Integer, CompetitorTimer> idMap = new HashMap<>();

    // Tarih formatını belirle
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss:SSS");

    @Autowired
    private final LogService logService;
    LogEntity logEntity = new LogEntity();
    @Autowired
    private UDPClient udpClient;
    @Autowired
    private UDPServer udpServer;

    int robotCount = 2;




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
            System.out.println("error: ... ");
            System.out.println(e);
            return new ErrorDataResult<>("Yarışmacılar listelenirken bir hata oluştu.");
        }
    }

    @Override
    public Result delete(int id) {

        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id)) {

           // logService.deleteLogFile(this.defCompetitorsDao.findById(id).getName() + ".json"); //log dosyasını sil

           // this.defCompetitorsDao.deleteById(id);
            DefCompetitors competitors = this.defCompetitorsDao.findById(id);
            competitors.setDelete(true);
            this.defCompetitorsDao.save(competitors);

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

            if (!oldCompetitor.getName().equals(newCompetitor.getName())) { // eger yarismaci ismi guncellendiyse eski log dosyasını sil

                logService.deleteLogFile(oldCompetitor.getName() + ".json");

            }

            oldCompetitor.setCity(newCompetitor.getCity());
            oldCompetitor.setName(newCompetitor.getName());
            oldCompetitor.setEliminated(newCompetitor.isEliminated());

            //eger kullancı elendiyse manuel olarak timer ı varsa sonlandır.
            if (newCompetitor.isEliminated()) {
                // sayacını kaldır
                idMap.remove(newCompetitor.getId());
                oldCompetitor.setReady(false);
                oldCompetitor.setStart(false);
                oldCompetitor.setFinish(false);


                logEntity.setDate(LocalDateTime.now().format(formatter));
                logEntity.setMessage("Yarışmacı elendi .");
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
    public Result ready(List<String>  codes) throws InterruptedException {

        //for (int i = 0 ; i < 10 ; i++) {
        codes = new ArrayList<>();

        while (true) {

            this.udpClient.sendMessage("id: 00  cmd: 11  stat: 00");

            String message = this.udpServer.startUDPServer();

            System.out.println("[Ready] ,STM den alınan mesaj : " + message);
            System.out.println(message);

            if (message != null && message.contains("id")) {
                // Mesajı ":" ile parçala ve boşlukları temizle
                String[] parts = message.split("\\s+");

                // id, cmd ve stat alanlarını ayır
                String idRobot = parts[0].split(":")[1].trim();
                String cmd = parts[1].split(":")[1].trim();
                String stat = parts[2].split(":")[1].trim();

                System.out.println("id: " + idRobot);
                System.out.println("cmd: " + cmd);
                System.out.println("stat: " + stat);

                // cmd--> 11 ready , stat 01 --> OK, id --00'dan farklı ve daha once codes listesinde yoksa ekle
                if (!idRobot.equals("00") && cmd.equals("11") && stat.contains("01") && !codes.contains(idRobot)) {

                    codes.add(idRobot);
                }

                //System.out.println("codes.size() : " + codes.size());

                if(codes.size() == robotCount)
                    break;
            }
            // sinyal gondermek ve almak icin 2 saniye bekle
            Thread.sleep(2000);

        }


        for (String code : codes) {
            System.out.println("code " + code);

            if (code != null) {

                DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(code);

                if (defCompetitor != null) {

                    System.out.println("ready kodu gönderenler :" + defCompetitor.getName());

                    if (!defCompetitor.isEliminated()) {
                        defCompetitor.setReady(true);
                        this.defCompetitorsDao.save(defCompetitor);

                        logEntity.setDate(LocalDateTime.now().format(formatter));
                        logEntity.setMessage(defCompetitor.getName() + " ready komutunu gönderdi.Kod :  " + code + " Yarışmacı Bilgileri : " + defCompetitor);
                        logEntity.setSender(defCompetitor.getName());
                        logEntity.setMessageType("INFO");

                        logService.writeLog(logEntity);


                    } else {
                        logEntity.setDate(LocalDateTime.now().format(formatter));
                        logEntity.setMessage(defCompetitor.getName() + " ready komutu gönderdi ama elenmiş durumda. Kod :  " + code + " Yarışmacı Bilgileri : " + defCompetitor);
                        logEntity.setSender(defCompetitor.getName());
                        logEntity.setMessageType("ERROR");

                        logService.writeLog(logEntity);

                    }

                }
            }
        }
        return new SuccessResult("Yarışmacılar hazır.");
    }


    @Override
    public Result start(List<String>  codes) throws InterruptedException {

        codes = new ArrayList<>();

       List<String> readyCodes = this.defCompetitorsDao.getReadyCompetiors();


       // for (int i = 0 ; i < 10 ; i++) {

        while(true){

            // start olan kodlara 12 sinyalini gonder gonder
            for (String code : readyCodes) {
                this.udpClient.sendMessage("id: " + code + "  cmd: 12  stat: 00");
            }


            String message = this.udpServer.startUDPServer();


            System.out.println("[Start], STM den alınan mesaj : " + message);

            if (message.contains("id")) {
                // Mesajı ":" ile parçala ve boşlukları temizle
                String[] parts = message.split("\\s+");

                // id, cmd ve stat alanlarını ayır
                String idRobot = parts[0].split(":")[1].trim();
                String cmd = parts[1].split(":")[1].trim();
                String stat = parts[2].split(":")[1].trim();

                System.out.println("start id: " + idRobot);
                System.out.println("cmd: " + cmd);
                System.out.println("stat: " + stat);

                // cmd--> 12 start , stat 01 --> OK, id --00'dan farklı ve daha once codes listesinde yoksa ekle
                if (!idRobot.equals("00") && cmd.equals("12") && stat.contains("01") && !codes.contains(idRobot)) {
                    codes.add(idRobot);
                }

                if(codes.size() == robotCount)
                    break;
            }
            // sinyal gondermek ve almak icin 2 saniye bekle
            Thread.sleep(2000);
        }

        for (String code : codes) {
            System.out.println("code " + code);

            if (code != null) {

                DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(code);

                if (defCompetitor != null) {
                    if (!defCompetitor.isEliminated()) {
                        if (defCompetitor.isReady()) {

                            // bu id ile bir timer oluştur.
                            idMap.put(defCompetitor.getId(), new CompetitorTimer());

                            logEntity.setDate(LocalDateTime.now().format(formatter));
                            logEntity.setMessage(defCompetitor.getName() + " start komutunu gönderdi. Kod :  " + code);
                            logEntity.setSender(defCompetitor.getName());
                            logEntity.setMessageType("INFO");

                            logService.writeLog(logEntity);

                            System.out.println(code + " koduna sahip yarışmacı için isStart güncellendi ve sayaç başladı.");

                        } else {
                            System.out.println(code + " koduna sahip yarışmacı ready komutunu göndermedi.");

                            logEntity.setDate(LocalDateTime.now().format(formatter));
                            logEntity.setMessage(defCompetitor.getName() + " start komutunu gönderdi ama daha önce ready komutunu göndermedi ! Kod :  " + code + "Yarışmacı Bilgileri : " + defCompetitor);
                            logEntity.setSender(defCompetitor.getName());
                            logEntity.setMessageType("ERROR");

                            logService.writeLog(logEntity);
                        }
                    } else {
                        System.out.println(code + " koduna sahip yarışmacı elenmiş durumda, sayaç başlatılmadı");

                        logEntity.setDate(LocalDateTime.now().format(formatter));
                        logEntity.setMessage(defCompetitor.getName() + " start komutu gönderdi ama elenmiş durumda, sayaç başlatılmadı.Kod :  " + code + "Yarışmacı Bilgileri : " + defCompetitor);
                        logEntity.setSender(defCompetitor.getName());
                        logEntity.setMessageType("ERROR");
                        logService.writeLog(logEntity);
                    }
                } else {
                    System.out.println("start için " + code + " koduna sahip yarışmacı bulunamadı.");
                }
            }

        }

        // idler toplandıktan sonra start çalışsın
        startTimer();

        // Map'ten sadece key'leri al
        Integer[] idArrayStart = idMap.keySet().toArray(new Integer[0]);

        //start tarihini ve start bitini güncelle ve timerları başlat
        updateStartTime(idArrayStart);

        listenForFinishSignal();

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

    public void listenForFinishSignal() {
        while (true) {

            String message = this.udpServer.startUDPServer();

            System.out.println("[Finish], STM den alınan mesaj :  " + message);

            if (message.contains("id")) {
                // Mesajı ":" ile parçala ve boşlukları temizle
                String[] parts = message.split("\\s+");

                // id, cmd ve stat alanlarını ayır
                String idRobot = parts[0].split(":")[1].trim();
                String cmd = parts[1].split(":")[1].trim();
                String stat = parts[2].split(":")[1].trim();

                System.out.println("finish id: " + idRobot);
                System.out.println("cmd: " + cmd);
                System.out.println("stat: " + stat);

                // cmd--> 13 ready , stat 01 --> OK, id --00'dan farklı ve daha once codes listesinde yoksa ekle
                if (!idRobot.equals("00") && cmd.equals("13") && stat.contains("00") ) {
                    finish(idRobot.lines().toList());
                    this.udpClient.sendMessage("id: " + idRobot + "  cmd: 13  stat: 01"); // finish olduğuna dair ack biti gonder
                }

            }

            if(idMap.isEmpty()){
                System.out.println("[Finish] yarısan robot kalmadı. Port dinleme bitti. ");
                break;
            }

        }
    }



    // gönderilen kod bilgisine göre eğer yarışmacı elenmemişse, hazır ve başlamışsa bunları false a çeker ve timerı durdurur
    @Override
    public Result finish(List<String>  codes) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss:SSS");


        for (String code : codes) {

            DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(code);

            if (defCompetitor != null) {

                if (!defCompetitor.isEliminated()) { //eliminated false

                    if (defCompetitor.isStart()) { // start true

                        System.out.println("********** stop id from map :" + defCompetitor.getId());

                        CompetitorTimer timer = idMap.get(defCompetitor.getId());


                        idMap.remove(defCompetitor.getId());

                        // Tarih bilgisini belirli formatta ayarla
                        String formattedDateTime = LocalDateTime.now().format(formatter);


                        defCompetitor.setStopTime(formattedDateTime);
                        defCompetitor.setDuration(timer.stopTimer());
                        defCompetitor.setReady(false);
                        defCompetitor.setStart(false);
                        defCompetitor.setFinish(true);
                        DefCompetitors stoppedCompetitor = this.defCompetitorsDao.save(defCompetitor);

                        System.out.println("stoppedCompetitor : " + stoppedCompetitor);

                        logEntity.setDate(formattedDateTime);
                        logEntity.setMessage(stoppedCompetitor.getName() + " parkuru bitirdi.Sayaç durduruldu.Yarışmacı bilgileri : " + stoppedCompetitor);
                        logEntity.setSender(stoppedCompetitor.getName());
                        logEntity.setMessageType("INFO");
                        logService.writeLog(logEntity);


                        System.out.println(code + " koduna sahip yarışmacı için isStart güncellendi ve sayaç durduruldu.");
                    } else {
                        System.out.println(code + " koduna sahip yarışmacı  start komutunu göndermedi.");

                        logEntity.setDate(LocalDateTime.now().format(formatter));
                        logEntity.setMessage(defCompetitor.getName() + " daha önce start komutunu göndermedi.Kod :  " + code + "Yarışmacı Bilgileri : " + defCompetitor);
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
            //timer.scheduleAtFixedRate(timerTask, 0, 1); // 1 ms
            timer.scheduleAtFixedRate(timerTask, 0, 10); //10 ms
        }
    }


    public void updateDurationById(Set<Integer> idSet) {
        Iterator<Integer> iterator = idSet.iterator();

        while (iterator.hasNext()) {

            Integer id = iterator.next();

            // Bu id ye ait kayıt var mı
            Optional<DefCompetitors> optionalCompetitor = this.defCompetitorsDao.findById(id);

            if (optionalCompetitor.isPresent()) {
                DefCompetitors competitor = optionalCompetitor.get();

                if (competitor.isStart()) {
                    CompetitorTimer timer = idMap.get(id);

                    if (timer != null) {
                        String duration = timer.printElapsedTime();
                        //System.out.println("update duration Id:" + id + " duration:" + duration);

                        if (duration.equals("05:00:00") || duration.compareTo("05:00:00") > 0) {
                            competitor.setEliminated(true);
                            competitor.setReady(false);
                            competitor.setStart(false);
                            competitor.setStopTime(LocalDateTime.now().format(formatter));

                            competitor.setDuration(duration);
                            DefCompetitors savedCompetitor = this.defCompetitorsDao.save(competitor);
                            //System.out.println("updatedCompetitor:" + savedCompetitor);

                            logEntity.setDate(LocalDateTime.now().format(formatter));
                            logEntity.setMessage(savedCompetitor.getName() + " 5 dakika  boyunca parkuru tamamlayamadı.Yarışmacı elendi. Yarışmacı bilgileri :" + savedCompetitor);
                            logEntity.setSender(savedCompetitor.getName());
                            logEntity.setMessageType("INFO");
                            logService.writeLog(logEntity);

                            // idSet'ten de kaldır
                            iterator.remove();
                            // idMap'ten de kaldır
                            idMap.remove(id);
                        } else {
                            competitor.setDuration(duration);
                            DefCompetitors savedCompetitor = this.defCompetitorsDao.save(competitor);
                            //System.out.println("updatedCompetitor:" + savedCompetitor);
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
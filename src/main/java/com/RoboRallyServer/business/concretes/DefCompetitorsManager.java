package com.RoboRallyServer.business.concretes;

import com.RoboRallyServer.business.abstracts.DefCompetitorsService;
import com.RoboRallyServer.dataAccess.abstracts.DefCompetitorsDao;
import com.RoboRallyServer.dataAccess.abstracts.DefPortsDao;
import com.RoboRallyServer.entities.DefCompetitors;
import com.RoboRallyServer.utilities.UDP.UDPClient;
import com.RoboRallyServer.utilities.UDP.UDPServer;
import com.RoboRallyServer.utilities.log.LogEntity;
import com.RoboRallyServer.utilities.log.LogService;
import com.RoboRallyServer.utilities.results.*;
import com.RoboRallyServer.utilities.timer.CompetitorTimer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class DefCompetitorsManager implements DefCompetitorsService {

    private final DefCompetitorsDao defCompetitorsDao;
    private final DefPortsDao defPortsDao;
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

    int robotCount = 0;
    List<String> robotCodes = new ArrayList<>();
    List<Integer> robotCodePort = new ArrayList<>();

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
            log.info("error: ... ");
            log.info("",e);
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
    public Result ready() throws InterruptedException {

        robotCount = this.defPortsDao.getRobotCount();

        robotCodes = new ArrayList<>(); // mevcut aldıgım robot kodları burada topla birden fazla aynı robot id yi almamak icin
        robotCodePort = new ArrayList<>(); // haberlesme yaptıgım portları burda tutuyorum, istediğim paketi aldıysam aynı porta tekrar data gondermemek icin
/*        List<Integer> port = new ArrayList<>(); // haberlesmem gereken portlar burda
        port.add(6001);
        port.add(6002);
        port.add(6003);
        port.add(6004);*/

        List<Integer> port = new ArrayList<>();
        port = this.defPortsDao.getAllStartPorts(); // haberlesmem gereken portlar burda


       while (true) {

            this.udpClient.sendMessage("id: 00  cmd: 11  stat: 00");


            port.parallelStream().forEach(s-> {
               if( robotCodePort.contains(s)) {
                  // log.info("[READY] port already returned data: " + s);
                    return;
                }


                String fromPort1 = udpServer.startServer(s);
                log.info("[Ready] ,STM den alınan mesaj port " + s + " " + fromPort1);

                if (fromPort1 != null && fromPort1.contains("id") ) {

                    // Mesajı ":" ile parçala ve boşlukları temizle
                    String[] parts = fromPort1.split("\\s+");

                    // id, cmd ve stat alanlarını ayır
                    String idRobot = parts[0].split(":")[1].trim();
                    String cmd = parts[1].split(":")[1].trim();
                    String stat = parts[2].split(":")[1].trim();

                    
                    
                    log.info("id: " + idRobot);
                    log.info("cmd: " + cmd);
                    log.info("stat: " + stat);

                    // cmd--> 11 ready , stat 01 --> OK, id --00'dan farklı ve daha once codes listesinde yoksa ekle
                    if ((!idRobot.equals("00") && cmd.equals("11") && stat.contains("01") && !robotCodes.contains(idRobot))) {

                        log.info("***** ROBOT READY : " + idRobot);
                        robotCodes.add(idRobot);
                        robotCodePort.add(s);

                        DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(idRobot);

                        if (defCompetitor != null  ) {

                            log.info("ready kodu gönderenler :" + defCompetitor.getName());

                            if (!defCompetitor.isEliminated()) {
                                defCompetitor.setReady(true);
                                defCompetitor.setSPort(s.toString());
                                this.defCompetitorsDao.save(defCompetitor);

                                logEntity.setDate(LocalDateTime.now().format(formatter));
                                logEntity.setMessage(defCompetitor.getName() + " ready komutunu gönderdi.Kod :  " + idRobot + " Yarışmacı Bilgileri : " + defCompetitor);
                                logEntity.setSender(defCompetitor.getName());
                                logEntity.setMessageType("INFO");

                                logService.writeLog(logEntity);


                            } else {
                                logEntity.setDate(LocalDateTime.now().format(formatter));
                                logEntity.setMessage(defCompetitor.getName() + " ready komutu gönderdi ama elenmiş durumda. Kod :  " + idRobot + " Yarışmacı Bilgileri : " + defCompetitor);
                                logEntity.setSender(defCompetitor.getName());
                                logEntity.setMessageType("ERROR");

                                logService.writeLog(logEntity);

                            }

                        }
                    }
                }

            });
            System.out.println("robotCodes.size() ==" + robotCodes.size() );
            if(robotCodes.size() == robotCount){
                break;
            }
            // sinyal gondermek ve almak icin 2 saniye bekle
           // Thread.sleep(2000);

        }
        return new SuccessResult("Yarışmacılar hazır.");
    }


    @Override
    public Result start() throws InterruptedException {

        robotCount = this.defPortsDao.getRobotCount();

        robotCodes = new ArrayList<>(); // mevcut aldıgım robot kodları burada topla birden fazla aynı robot id yi almamak icin
        robotCodePort = new ArrayList<>(); // haberlesme yaptıgım portları burda tutuyorum, istediğim paketi aldıysam aynı porta tekrar data gondermemek icin
        List<DefCompetitors> readyCodes = this.defCompetitorsDao.getReadyCompetiors();



        while (true) {

            for (DefCompetitors code : readyCodes) {
                log.info("start kod gonderecekler: " + code.getCode() + " port:" + code.getSPort());
                this.udpClient.sendMessageWithPort("id: " + code.getCode() + "  cmd: 12  stat: 00" , Integer.valueOf(code.getSPort()) );
            }

/*            List<Integer> port = new ArrayList<>();
            port.add(6001);
            port.add(6002);
            port.add(6003);
            port.add(6004);*/

            List<Integer> port = new ArrayList<>();
            port = this.defPortsDao.getAllStartPorts(); // haberlesmem gereken portlar burda


            port.parallelStream().forEach(s -> {

                if( robotCodePort.contains(s)) {
                   // log.info("port already returned data start: " + s);
                    return;
                }

                String message = this.udpServer.startServer(s);
                if (message != null && message.contains("id")) {
                    // Mesajı ":" ile parçala ve boşlukları temizle
                    String[] parts = message.split("\\s+");

                    // id, cmd ve stat alanlarını ayır
                    String idRobot = parts[0].split(":")[1].trim();
                    String cmd = parts[1].split(":")[1].trim();
                    String stat = parts[2].split(":")[1].trim();

                    log.info("start id: " + idRobot);
                    log.info("cmd: " + cmd);
                    log.info("stat: " + stat);

                    // cmd--> 12 start , stat 01 --> OK, id --00'dan farklı ve daha once codes listesinde yoksa ekle
                    if (!idRobot.equals("00") && cmd.equals("12") && stat.contains("01") && !robotCodes.contains(idRobot)) {
                        robotCodes.add(idRobot);
                        robotCodePort.add(s);
                        readyCodes.remove(idRobot); //start olduğu için artık start komutu göndermeye gerek yok.
                        log.info("***** ROBOT START : " + idRobot);
                    }
                }

            });

            if(robotCodes.size() == robotCount){
                break;
            }

        }

        for (String code : robotCodes) {
            //log.info("code " + code);

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

                            log.info(code + " koduna sahip yarışmacı için isStart güncellendi ve sayaç başladı.");

                        } else {
                            log.info(code + " koduna sahip yarışmacı ready komutunu göndermedi.");

                            logEntity.setDate(LocalDateTime.now().format(formatter));
                            logEntity.setMessage(defCompetitor.getName() + " start komutunu gönderdi ama daha önce ready komutunu göndermedi ! Kod :  " + code + "Yarışmacı Bilgileri : " + defCompetitor);
                            logEntity.setSender(defCompetitor.getName());
                            logEntity.setMessageType("ERROR");

                            logService.writeLog(logEntity);
                        }
                    } else {
                        log.info(code + " koduna sahip yarışmacı elenmiş durumda, sayaç başlatılmadı");

                        logEntity.setDate(LocalDateTime.now().format(formatter));
                        logEntity.setMessage(defCompetitor.getName() + " start komutu gönderdi ama elenmiş durumda, sayaç başlatılmadı.Kod :  " + code + "Yarışmacı Bilgileri : " + defCompetitor);
                        logEntity.setSender(defCompetitor.getName());
                        logEntity.setMessageType("ERROR");
                        logService.writeLog(logEntity);
                    }
                } else {
                    log.info("start için " + code + " koduna sahip yarışmacı bulunamadı.");
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
                log.info("Id bilgisine göre yarışmacı bulunamadı.");
            }

        }

    }

    public void listenForFinishSignal() {

        robotCodePort = new ArrayList<>(); // haberlesmesi biten portları buraya ata,tekrar veri almamak icin

       while (true) {

           if (idMap.isEmpty()) {
               log.info("[Finish] yarısan robot kalmadı. Port dinleme bitti. ");
               break;
           }

/*           List<Integer> port = new ArrayList<>();
           port.add(6001);
           port.add(6002);
           port.add(6003);
           port.add(6004);*/

           List<Integer> port = new ArrayList<>();
           port = this.defPortsDao.getAllFinishPorts(); // haberlesmem gereken portlar burda

           port.parallelStream().forEach(s -> {

               if (robotCodePort.contains(s)) {
                  // log.info("port already returned data: " + s);
                   return;
               }

               String message = this.udpServer.startServerForFinish(s);

               if (message != null && message.contains("id")) {
                   // Mesajı ":" ile parçala ve boşlukları temizle
                   String[] parts = message.split("\\s+");

                   // id, cmd ve stat alanlarını ayır
                   String idRobot = parts[0].split(":")[1].trim();
                   String cmd = parts[1].split(":")[1].trim();
                   String stat = parts[2].split(":")[1].trim();

                   log.info("finish id: " + idRobot);
                   log.info("cmd: " + cmd);
                   log.info("stat: " + stat);

                   // cmd--> 13 ready , stat 01 --> OK, id --00'dan farklı ve daha once codes listesinde yoksa ekle
                   if (!idRobot.equals("00") && cmd.equals("13") && stat.contains("00")) {
                       robotCodePort.add(s);
                       this.udpClient.sendMessage("id: " + idRobot + "  cmd: 13  stat: 01"); // finish olduğuna dair ack biti gonder
                       finish(idRobot);
                   }

               }
           });
       }
    }


    // gönderilen kod bilgisine göre eğer yarışmacı elenmemişse, hazır ve başlamışsa bunları false a çeker ve timerı durdurur
    @Override
    public Result finish(String code) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss:SSS");
        String formattedDateTime = LocalDateTime.now().format(formatter);

        DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(code);

            if (defCompetitor != null) {

                if (!defCompetitor.isEliminated()) { //eliminated false

                    if (defCompetitor.isStart()) { // start true

                        log.info("********** stop id from map :" + defCompetitor.getId());

                        CompetitorTimer timer = idMap.get(defCompetitor.getId());
                        idMap.remove(defCompetitor.getId());

                        // Tarih bilgisini belirli formatta ayarla


                        defCompetitor.setStopTime(formattedDateTime);
                        defCompetitor.setDuration(timer.stopTimer());
                        defCompetitor.setReady(false);
                        defCompetitor.setStart(false);
                        defCompetitor.setFinish(true);
                        DefCompetitors stoppedCompetitor = this.defCompetitorsDao.save(defCompetitor);

                        log.info("stoppedCompetitor : " + stoppedCompetitor);

                        logEntity.setDate(formattedDateTime);
                        logEntity.setMessage(stoppedCompetitor.getName() + " parkuru bitirdi.Sayaç durduruldu.Yarışmacı bilgileri : " + stoppedCompetitor);
                        logEntity.setSender(stoppedCompetitor.getName());
                        logEntity.setMessageType("INFO");
                        logService.writeLog(logEntity);


                        log.info(code + " koduna sahip yarışmacı için isStart güncellendi ve sayaç durduruldu.");
                    } else {
                        log.info(code + " koduna sahip yarışmacı  start komutunu göndermedi.");

                        logEntity.setDate(LocalDateTime.now().format(formatter));
                        logEntity.setMessage(defCompetitor.getName() + " daha önce start komutunu göndermedi.Kod :  " + code + "Yarışmacı Bilgileri : " + defCompetitor);
                        logEntity.setSender(defCompetitor.getName());
                        logEntity.setMessageType("ERROR");
                        logService.writeLog(logEntity);
                    }
                } else {
                    log.info(code + " koduna sahip yarışmacı elenmiş durumda, sayacı yok.");

                    logEntity.setDate(LocalDateTime.now().format(formatter));
                    logEntity.setMessage(defCompetitor.getName() + " koduna sahip yarışmacı elenmiş durumda.Yarışmacı bilgileri : " + defCompetitor);
                    logEntity.setSender(defCompetitor.getName());
                    logEntity.setMessageType("ERROR");
                    logService.writeLog(logEntity);
                }
            } else {
                log.info(code + " koduna sahip yarışmacı bulunamadı.");
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
                        //log.info("update duration Id:" + id + " duration:" + duration);

                        if (duration.equals("05:00:00") || duration.compareTo("05:00:00") > 0) {
                            competitor.setEliminated(true);
                            competitor.setReady(false);
                            competitor.setStart(false);
                            competitor.setStopTime(LocalDateTime.now().format(formatter));

                            competitor.setDuration(duration);
                            DefCompetitors savedCompetitor = this.defCompetitorsDao.save(competitor);
                            //log.info("updatedCompetitor:" + savedCompetitor);

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
                            //log.info("updatedCompetitor:" + savedCompetitor);
                        }
                    }
                }
            } else {
                // idSet'ten de kaldır
                idSet.remove(id);
                log.info("updateDurationById Id bilgisine göre yarışmacı bulunamadı.");
            }
        }
    }


}
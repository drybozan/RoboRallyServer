package com.RoboRallyServer.business.concretes;

import com.RoboRallyServer.business.abstracts.DefCompetitorsService;
import com.RoboRallyServer.dataAccess.abstracts.DefCompetitorsDao;
import com.RoboRallyServer.entities.DefCompetitors;
import com.RoboRallyServer.utilities.results.*;
import com.RoboRallyServer.utilities.timer.CompetitorTimer;
import com.RoboRallyServer.utilities.timer.CompetitorTimerManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefCompetitorsManager implements DefCompetitorsService {

    private final DefCompetitorsDao defCompetitorsDao;
    private final CompetitorTimerManager timerManager;
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
            if(newCompetitor.isEliminated()){
                timerManager.stopTimer(newCompetitor.getId());
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

    @Override
    public Result updateStartTimeById(int id, String starTime) {
        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id)) {
            DefCompetitors competitor = this.defCompetitorsDao.findById(id);
            competitor.setStartTime(starTime);
            this.defCompetitorsDao.save(competitor);

            // Yarışmacı  için timer'ı başlat
            timerManager.startTimer(id);
            return new SuccessResult("Id bilgisine göre startTime güncellendi..");
        } else {
            return new ErrorResult("Id bilgisine göre yarışmacı bulunamadı.");
        }
    }

    @Override
    public Result updateStopTimeById(int id, String stopTime) {
        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id)) {
            DefCompetitors competitor = this.defCompetitorsDao.findById(id);
            competitor.setStopTime(stopTime);
            this.defCompetitorsDao.save(competitor);
            timerManager.stopTimer(id);
            return new SuccessResult("Id bilgisine göre stopTime güncellendi..");
        } else {
            return new ErrorResult("Id bilgisine göre stopTime bulunamadı.");
        }
    }


//gönderilen kod bilgisne göre kullanıcı varsa ve elenmediyse ready bitini true yapar
    @Override
    public Result updateReadyByCode(String code, boolean ready) {

        System.out.println("Code :"+code + " ready :"+ready);

        DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(code);
        if (defCompetitor != null) {
            if(!defCompetitor.isEliminated()){
                defCompetitor.setReady(ready);
                this.defCompetitorsDao.save(defCompetitor);
                return new SuccessResult(code + " koduna sahip yarışmacı için ready alanı güncellendi.");
            }
            return new SuccessResult(code + " koduna sahip yarışmacı için elenmiş durumda.");
        }
        return new ErrorResult(code + " koduna sahip yarışmacı bulunamadı.");
    }

    //gönderilen kod bilgisine göre kullanıcı ready ise start bitini true yapar ve sayaç başlatır.
    @Override
    public Result updateStartByCode(String code, boolean start) {

        System.out.println("Code :"+code + " start :"+start);

        DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(code);
        if (defCompetitor != null) {
            if(!defCompetitor.isEliminated() ) {
                if( defCompetitor.isReady() ) {
                defCompetitor.setStart(start);
                //defCompetitor.setReady(false);


                this.defCompetitorsDao.save(defCompetitor);

                // Yarışmacı  için timer'ı başlat
                timerManager.startTimer(defCompetitor.getId());


                return new SuccessResult(code + " koduna sahip yarışmacı için isStart güncellendi ve sayaç başladı.");
                }
                return new ErrorResult(code + " koduna sahip yarışmacı ready komutunu göndermedi.");
            }
            return new SuccessResult(code + " koduna sahip yarışmacı elenmiş durumda,sayaç başlatılmadı");
        } else {
            return new ErrorResult(code + " koduna sahip yarışmacı bulunamadı.");
        }
    }

    // gönderilen kod bilgisine göre eğer yarışmacı elenmemişse, hazır ve başlamışsa bunları false a çeker ve timerı durdurur
    @Override
    public Result updateReadyAndStartByCode(String code, boolean ready,boolean start) {

        System.out.println("Code :"+code +" ready : "+ready+ " start :"+start );
        DefCompetitors defCompetitor = this.defCompetitorsDao.findByCode(code);

        if (defCompetitor != null) {

            if(!defCompetitor.isEliminated()) { //eliminated false

                if(defCompetitor.isReady() && defCompetitor.isStart()) { // ready true,start true

                    // yarışmacı için timer ı durdur.
                    timerManager.stopTimer(defCompetitor.getId());

                    System.out.println("before update : " + defCompetitor);

                    defCompetitor.setReady(ready);
                    defCompetitor.setStart(start);

                    System.out.println("for save: " + defCompetitor);
                    this.defCompetitorsDao.save(defCompetitor);


                    return new SuccessResult(code + " koduna sahip yarışmacı için isStart ve isReady güncellendi ve sayaç durduruldu.");
                }
                return new ErrorResult(code + " koduna sahip yarışmacı ready ve start komutunu göndermedi.");
            }
            return new SuccessResult(code + " koduna sahip yarışmacı elenmiş durumda, sayacı yok.");
        } else {
            return new ErrorResult(code + " koduna sahip yarışmacı bulunamadı.");
        }
    }


}

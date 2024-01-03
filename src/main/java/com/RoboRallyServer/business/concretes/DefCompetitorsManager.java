package com.RoboRallyServer.business.concretes;

import com.RoboRallyServer.business.abstracts.DefCompetitorsService;
import com.RoboRallyServer.dataAccess.abstracts.DefCompetitorsDao;
import com.RoboRallyServer.entities.DefCompetitors;
import com.RoboRallyServer.utilities.results.*;
import com.RoboRallyServer.utilities.timer.CompetitorTimer;
import com.RoboRallyServer.utilities.timer.CompetitorTimerManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
            oldCompetitor.setEliminated(newCompetitor.getEliminated());

            this.defCompetitorsDao.save(oldCompetitor);

            //eger kullancı elendiyse manuel olarak timer ı varsa sonlandır.
            if(newCompetitor.getEliminated()){
                timerManager.stopTimer(newCompetitor.getId());
            }

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
            return new SuccessResult("Id bilgisine göre starTime güncellendi..");
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

    @Override
    public Result updateDurationById(int id, String duration) {
        // bu id ye ait kayıt var mı
        if (this.defCompetitorsDao.existsById(id)) {
            DefCompetitors competitor = this.defCompetitorsDao.findById(id);
            if (duration.equals("05.00:00")) {
                competitor.setEliminated(true); // eger süre 5dk ya esitse yarismaciyi ele
            }
            competitor.setDuration(duration);
            this.defCompetitorsDao.save(competitor);

            return new SuccessResult("Id bilgisine göre duration güncellendi..");
        } else {
            return new ErrorResult("Id bilgisine göre yarışmacı bulunamadı.");
        }
    }



}

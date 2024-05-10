package com.RoboRallyServer.dataAccess.abstracts;

import com.RoboRallyServer.entities.DefCompetitors;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefCompetitorsDao extends JpaRepository<DefCompetitors, Integer> {

    //önce string uzunluğuna göre sıralama yapar, ardından aynı uzunluğa sahip olanları süre değerine göre sıralar.
   // @Query(value = "SELECT * FROM RoboRallyDB.DefCompetitors ORDER BY LENGTH(sDuration), sDuration ASC;", nativeQuery = true)
    /*
    * bu sorgu önce elimine edilmemiş kayıtları (bEliminated = false)
    * süre uzunluğuna göre küçükten büyüğe sıralar, ardından elimine edilmiş kayıtları (bEliminated = true) en altta tutar.
    */

    @Query(value = "SELECT * FROM RoboRallyDB.DefCompetitors \n" +
            "WHERE bIsdelete = 0 ORDER BY  \n" +
            "CASE  WHEN bEliminated = false \n" +
            "THEN 0  ELSE 1  END, \n" +
            "CASE   WHEN sDuration = '00:00:000' \n" +
            "THEN 1 ELSE 0  END,\n" +
            "LENGTH(sDuration),sDuration ASC;", nativeQuery = true)
    List<DefCompetitors> getAllCompetitorsByDuration();

    @Query(value = "SELECT * FROM RoboRallyDB.DefCompetitors ;", nativeQuery = true)
    List<DefCompetitors> findAll();


    @Transactional
    DefCompetitors findById(int id);

    DefCompetitors findByCode(String code);

    @Query(value = "SELECT sCode FROM RoboRallyDB.DefCompetitors where bIsReady = 1", nativeQuery = true)
    List<String> getReadyCompetiors();


}

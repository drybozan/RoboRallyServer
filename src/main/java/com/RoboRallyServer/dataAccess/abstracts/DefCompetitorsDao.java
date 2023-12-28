package com.RoboRallyServer.dataAccess.abstracts;

import com.RoboRallyServer.entities.DefCompetitors;
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

   /* @Query(value = "SELECT * FROM RoboRallyDB.DefCompetitors ORDER BY  CASE  WHEN bEliminated = false  THEN 0  ELSE 1    END,  LENGTH(sDuration),   sDuration ASC;", nativeQuery = true)
    List<DefCompetitors> getAllCompetitorsByDuration();*/

    @Query(value = "SELECT * FROM RoboRallyDB.DefCompetitors ;", nativeQuery = true)
    List<DefCompetitors> findAll();
    DefCompetitors findById(int id);


}

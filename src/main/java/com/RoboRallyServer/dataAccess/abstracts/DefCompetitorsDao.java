package com.RoboRallyServer.dataAccess.abstracts;

import com.RoboRallyServer.entities.DefCompetitors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefCompetitorsDao extends JpaRepository<DefCompetitors, Integer> {

    //önce string uzunluğuna göre sıralama yapar, ardından aynı uzunluğa sahip olanları süre değerine göre sıralar.
    @Query(value = "SELECT * FROM RoboRallyDB.DefCompetitors ORDER BY LENGTH(sDuration), sDuration ASC;", nativeQuery = true)
    List<DefCompetitors> getAllCompetitorsByDuration();

    DefCompetitors findById(int id);


}

package com.RoboRallyServer.dataAccess.abstracts;

import com.RoboRallyServer.entities.DefCompetitors;
import com.RoboRallyServer.entities.DefPorts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefPortsDao extends JpaRepository<DefPorts,Integer> {

    @Query(value = "SELECT startPort FROM RoboRallyDB.DefPorts where isDelete = 0 ;", nativeQuery = true)
    List<Integer> getAllStartPorts();

    @Query(value = "SELECT ip FROM RoboRallyDB.DefPorts where isDelete = 0 ;", nativeQuery = true)
    List<String> getAllIps();

    @Query(value = "SELECT count(*) FROM RoboRallyDB.DefPorts where isDelete = 0;", nativeQuery = true)
    int getRobotCount();

    @Query(value = "SELECT finishPort FROM RoboRallyDB.DefPorts where isDelete = 0 ;", nativeQuery = true)
    List<Integer> getAllFinishPorts();
}

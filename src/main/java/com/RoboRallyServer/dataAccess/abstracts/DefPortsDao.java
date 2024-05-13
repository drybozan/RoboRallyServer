package com.RoboRallyServer.dataAccess.abstracts;

import com.RoboRallyServer.entities.DefCompetitors;
import com.RoboRallyServer.entities.DefPorts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefPortsDao extends JpaRepository<DefPorts,Integer> {

    @Query(value = "SELECT port FROM RoboRallyDB.DefPorts ;", nativeQuery = true)
    List<Integer> getAllPorts();

    @Query(value = "SELECT ip FROM RoboRallyDB.DefPorts ;", nativeQuery = true)
    List<String> getAllIps();

    @Query(value = "SELECT count(*) FROM RoboRallyDB.DefPorts ;", nativeQuery = true)
    int getRobotCount();
}

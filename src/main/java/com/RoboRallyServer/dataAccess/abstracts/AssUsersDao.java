package com.RoboRallyServer.dataAccess.abstracts;

import com.RoboRallyServer.entities.AssUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssUsersDao extends JpaRepository<AssUsers,Integer> {

    AssUsers findByUsernameAndPassword(String username,String password);
}

package com.RoboRallyServer.business.abstracts;

import com.RoboRallyServer.entities.AssUsers;
import com.RoboRallyServer.utilities.results.DataResult;

public interface AssUsersService {

    DataResult<AssUsers> login(AssUsers assUsers);
}

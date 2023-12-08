package com.RoboRallyServer.controller.concretes;

import com.RoboRallyServer.business.abstracts.AssUsersService;
import com.RoboRallyServer.entities.AssUsers;
import com.RoboRallyServer.utilities.results.DataResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/AssUsersController")
@CrossOrigin
@RequiredArgsConstructor
public class AssUsersController {

    private final AssUsersService assUsersService;

    @PostMapping(value = "/login")
    public DataResult<AssUsers> login(@RequestBody AssUsers user) {

        return this.assUsersService.login(user);
    }
}

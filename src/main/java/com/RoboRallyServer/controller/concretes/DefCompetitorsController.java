package com.RoboRallyServer.controller.concretes;

import com.RoboRallyServer.business.abstracts.DefCompetitorsService;
import com.RoboRallyServer.entities.DefCompetitors;
import com.RoboRallyServer.utilities.results.DataResult;
import com.RoboRallyServer.utilities.results.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/DefCompetitorsController")
@CrossOrigin
@RequiredArgsConstructor
public class DefCompetitorsController {

    private final DefCompetitorsService defCompetitorsService;

    @PostMapping(value = "/add")
    public Result add(@RequestBody DefCompetitors competitors) {

        return this.defCompetitorsService.add(competitors);
    }

    @GetMapping(value = "/getAllByDuration")
    public DataResult<List<DefCompetitors>> getAllCompetitorsByDuration( ) {

        return this.defCompetitorsService.getAllCompetitorsByDuration();
    }
}

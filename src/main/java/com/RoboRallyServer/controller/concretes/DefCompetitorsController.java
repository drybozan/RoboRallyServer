package com.RoboRallyServer.controller.concretes;

import com.RoboRallyServer.business.abstracts.DefCompetitorsService;
import com.RoboRallyServer.entities.DefCompetitors;
import com.RoboRallyServer.utilities.results.DataResult;
import com.RoboRallyServer.utilities.results.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/getAllCompetitors")
    public DataResult<List<DefCompetitors>> getAllCompetitorsByDuration( ) {
        return this.defCompetitorsService.getAllCompetitors();
    }

    @DeleteMapping(value = "/deleteById")
    public Result deleteById( @RequestParam int id ) {

        return this.defCompetitorsService.delete(id);
    }

    @PostMapping(value = "/update")
    public Result update(@RequestBody DefCompetitors competitors) {

        return this.defCompetitorsService.update(competitors);
    }

    @GetMapping(value = "/getById")
    public DataResult<DefCompetitors> getById( @RequestParam int id ) {

        return this.defCompetitorsService.getById(id);
    }

    @PostMapping(value = "/updateStartTimeById")
    public Result updateStartTimeById( @RequestParam  int id, @RequestParam  String startTime) {

        return this.defCompetitorsService.updateStartTimeById(id,startTime);
    }

    @PostMapping(value = "/updateStopTimeById")
    public Result updateStopTimeById(@RequestParam int id, @RequestParam String stopTime) {

        return this.defCompetitorsService.updateStopTimeById(id,stopTime);
    }


}

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
    public DataResult<List<DefCompetitors>> getAllCompetitors( ) {
        return this.defCompetitorsService.getAllCompetitors();
    }

    @GetMapping(value = "/getAllCompetitorsByDuration")
    public DataResult<List<DefCompetitors>> getAllCompetitorsByDuration( ) {
        return this.defCompetitorsService.getAllCompetitorsByDuration();
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


    @PostMapping(value = "/updateReadyByCode")
    public Result updateReadyByCode( @RequestParam String code, @RequestParam  boolean ready) {

        return this.defCompetitorsService.updateReadyByCode(code,ready);
    }

    @PostMapping(value = "/updateStartByCode")
    public Result updateStartByCode(@RequestParam("codes") String[] codes) {

        return this.defCompetitorsService.updateStartByCode(codes);
    }

    @PostMapping(value = "/updateReadyAndStartByCode")
    public Result updateReadyAndStartByCode(@RequestParam("codes") String[] codes) {

        return this.defCompetitorsService.updateReadyAndStartByCode(codes);
    }



}

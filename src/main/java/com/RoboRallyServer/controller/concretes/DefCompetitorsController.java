package com.RoboRallyServer.controller.concretes;

import com.RoboRallyServer.business.abstracts.DefCompetitorsService;
import com.RoboRallyServer.entities.DefCompetitors;
import com.RoboRallyServer.utilities.UDP.UDPClient;
import com.RoboRallyServer.utilities.results.DataResult;
import com.RoboRallyServer.utilities.results.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/DefCompetitorsController")
@CrossOrigin
public class DefCompetitorsController {

    private  DefCompetitorsService defCompetitorsService;

    private UDPClient udpClient;

    public DefCompetitorsController(DefCompetitorsService defCompetitorsService, UDPClient udpClient) {
        this.udpClient = udpClient;
        this.defCompetitorsService = defCompetitorsService;
    }

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
    public Result updateReadyByCode() {

        return this.defCompetitorsService.updateReadyByCode();
    }

    @PostMapping(value = "/updateStartByCode")
    public Result updateStartByCode() {

        return this.defCompetitorsService.updateStartByCode();
    }

    @PostMapping(value = "/updateReadyAndStartByCode")
    public Result updateReadyAndStartByCode(@RequestParam("codes") String[] codes) {

        return this.defCompetitorsService.updateReadyAndStartByCode(codes);
    }

 /*   @PostMapping(value = "/udpTest")
    public void udpTest() {
        // UDP mesajı gönderme
        udpClient.sendMessage("selam canlarım !");
    }
*/


}

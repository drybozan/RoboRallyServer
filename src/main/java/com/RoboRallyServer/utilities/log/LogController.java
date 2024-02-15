package com.RoboRallyServer.utilities.log;

import com.RoboRallyServer.utilities.results.DataResult;
import com.RoboRallyServer.utilities.results.Result;
import org.springframework.core.io.Resource;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/LogController")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class LogController {

    private final LogService logService;

    @PostMapping("/writeLog")
    public Result createLogFileAndWrite(@RequestBody LogEntity logEntity){
        return logService.writeLog(logEntity);

    }

    @GetMapping(value = "/getLogFileNames")
    public DataResult<List<String>> getLogFileNames(){
        return logService.getLogFileNames();
    }


   @GetMapping(value = "/getLogFile", produces = MediaType.APPLICATION_JSON_VALUE)
    public   ResponseEntity<Resource> getLogFile( @RequestParam String logFileName) throws IOException {
        return logService.getLogFileByName( logFileName);
    }
}

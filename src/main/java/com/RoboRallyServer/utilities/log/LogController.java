package com.sunnyWalkmanServer.utilities.log;

import com.sunnyWalkmanServer.utilities.results.Result;
import org.springframework.core.io.Resource;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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

    @GetMapping(value = "/getLog", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public   ResponseEntity<Resource> getLogFile(@RequestParam String logFolderName, @RequestParam String logFileName) throws IOException {
        return logService.getLogFileByCategoryAndDate(logFolderName, logFileName);
    }
}

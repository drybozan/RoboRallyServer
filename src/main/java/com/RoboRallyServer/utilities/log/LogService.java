package com.RoboRallyServer.utilities.log;

import com.RoboRallyServer.utilities.results.DataResult;
import com.RoboRallyServer.utilities.results.Result;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface LogService {
    Result writeLog(LogEntity logEntity);
    Result createOrCheckFile(File file);
    Result createOrCheckDirectory(File directory);
     ResponseEntity<Resource> getLogFileByName(String logFileName) throws IOException;
     DataResult<List<String>> getLogFileNames();
    void deleteLogFile(String fileName);
}

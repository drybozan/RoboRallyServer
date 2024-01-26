package com.sunnyWalkmanServer.utilities.log;

import com.sunnyWalkmanServer.utilities.results.Result;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;

public interface LogService {
    Result writeLog(LogEntity logEntity);
    boolean isOldLogFile(File file);
    void cleanupOldLogs();
    Result createOrCheckFile(File file);
    Result createOrCheckDirectory(File directory);

     ResponseEntity<Resource> getLogFileByCategoryAndDate(String logFolderName,String logFileName) throws IOException;
}

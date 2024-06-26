package com.RoboRallyServer.utilities.log;

import com.RoboRallyServer.utilities.results.DataResult;
import com.RoboRallyServer.utilities.results.ErrorDataResult;
import com.RoboRallyServer.utilities.results.Result;

import com.RoboRallyServer.utilities.results.SuccessDataResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Service
public class LogManager implements LogService {
    // Kullanıcı ev dizininin yolunu al
    String userHome = System.getProperty("user.home");

    @Override
    public Result writeLog(LogEntity logEntity) {


        // oluşturacağın dizinin yolunu belirt
        Path directory = Paths.get(userHome, "RoboRallyLogs");

        // dizin oluştur veya varsa kontrol et
        Result directoryResult = createOrCheckDirectory(directory.toFile());

        if (!directoryResult.isSuccess()) {
            return directoryResult; // Dizin oluşturma veya kontrol etme başarısızsa hata mesajını döndür
        }

        // oluşturduğun dizinin altına log dosyasının yolunu belirt
        File logFilePath = new File(String.valueOf(directory), logEntity.getSender() + ".json");

        // log dosyasını oluştur veya varsa kontrol et
        Result fileResult = createOrCheckFile(logFilePath);

        if (!fileResult.isSuccess()) {
            return fileResult; // Dosya oluşturma veya kontrol etme başarısızsa hata mesajını döndür
        }

        // dosyanın içine json verisini yaz.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonLogEntity = objectMapper.writeValueAsString(logEntity);

            writer.write(jsonLogEntity);
            writer.newLine();  // Satır sonu karakteri ekle
            return new Result(true, "Log yazma işlemi başarılı.");
        } catch (IOException e) {
            System.out.println("Dosyaya veri yazarken bir hata meydana geldi: " + e);
            e.printStackTrace();
            return new Result(false, "Dosyaya veri yazarken bir hata meydana geldi: " + e.toString());
        }
    }

    // Dizini oluştur veya varsa kontrol et
    @Override
    public Result createOrCheckDirectory(File directory) {
        if (directory.exists()) {
           // System.out.println("Dizin mevcut.");
            return new Result(true, "Dizin mevcut.");
        } else {
            boolean isCreated = directory.mkdirs();
            if (isCreated) {
                //System.out.println("Dizin başarıyla oluşturuldu.");
                return new Result(true, "Dizin başarıyla oluşturuldu.");
            } else {
                System.out.println("Dizin oluşturulurken bir hata meydana geldi.");
                return new Result(false, "Dizin oluşturulurken bir hata meydana geldi.");
            }
        }
    }


    // Dosyayı oluştur veya varsa kontrol et
    @Override
    public Result createOrCheckFile(File file) {
        if (file.exists()) {
           // System.out.println("Dosya mevcut, sadece içine log yaz.");
            return new Result(true, "Dosya mevcut, sadece içine log yaz.");
        } else {
            try {
                boolean isCreated = file.createNewFile();
                if (isCreated) {
                   // System.out.println("Dosya başarıyla oluşturuldu.");
                    return new Result(true, "Dosya başarıyla oluşturuldu.");
                } else {
                    System.out.println("Dosya oluşturulurken bir hata meydana geldi.");
                    return new Result(false, "Dosya oluşturulurken bir hata meydana geldi.");
                }
            } catch (IOException e) {
                System.out.println("Dosya oluşturulurken bir hata meydana geldi: " + e);
                e.printStackTrace();
                return new Result(false, "Dosya oluşturulurken bir hata meydana geldi: " + e.toString());
            }
        }
    }


    @Override
    public ResponseEntity<Resource> getLogFileByName(String logFileName) throws IOException {

        // log un tutulduğu klasör yolu
        Path pathDirectory = Paths.get(userHome, "RoboRallyLogs");

        // log un tutulduğu klasör altındaki log dosyası
        Path pathFile = pathDirectory.resolve(logFileName);

        // Klasör ve dosyanın var olup olmadığını kontrol et
        if (!Files.exists(pathDirectory) || !Files.isRegularFile(pathFile)) {
            // 404 Bulunamadı yanıtını veya özel bir hata mesajını döndür
            String hataMesaji = "{'error': 'Dosya bulunamadı', 'file name': '" + logFileName + "'}";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ByteArrayResource(hataMesaji.getBytes()));
        }

        // Dosyayı ByteArrayResource olarak al
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(pathFile));

        // Set the content type and headers for the response
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + pathFile.getFileName().toString());

        // Return the file as a response entity
        return ResponseEntity.ok()
                // .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_JSON)
                .body(resource);

    }

    @Override
    public DataResult<List<String>> getLogFileNames() {

        List<String> logFileNames = new ArrayList<>();

        File logsDirectory = Paths.get(userHome, "RoboRallyLogs").toFile();
        //klasör mevcut mu kontrol et
        if (logsDirectory.exists() && logsDirectory.isDirectory()) {

            //klasördeki tüm dosyaları al file dizisine ata
            File[] files = logsDirectory.listFiles();

            //dizi boş değilse
            if (files != null) {
                //dizideki tüm dosyaları tek tek gez , istenen isimde dosya mevcut mu kontrol et
                for (File file : files) {
                    logFileNames.add(file.getName());
                }
            } else {
                return new ErrorDataResult<>("Log dosyası bulunamadı.");
            }
        } else {
            return new ErrorDataResult<>("RoboRallyLogs klasörü bulunamadı.");
        }


        return new SuccessDataResult<>(logFileNames, "Log dosyaları listelendi.");
    }

    @Override
    public void deleteLogFile(String fileName) {


        File logsDirectory = Paths.get(userHome, "RoboRallyLogs").toFile();
        //klasör mevcut mu kontrol et
        if (logsDirectory.exists() && logsDirectory.isDirectory()) {

            //klasördeki tüm dosyaları al file dizisine ata
            File[] files = logsDirectory.listFiles();

            //dizi boş değilse
            if (files != null) {
                //dizideki tüm dosyaları tek tek gez , istenen isimde dosya mevcut mu kontrol et
                for (File file : files) {
                    if (file.getName().equals(fileName)) {
                        file.delete(); //dosya eşleşiyorsa sil
                    }
                }
            }
        }

    }


}

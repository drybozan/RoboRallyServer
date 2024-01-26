package com.sunnyWalkmanServer.utilities.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunnyWalkmanServer.utilities.results.Result;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class LogManager implements LogService {

    static String username = System.getProperty("user.name");
    //WalkmanLog klasörünün içindeki klasör isimleri bunlar.Tabletten gelecek log mesajlarındaki "sender" alanındaki değerler ile aynı olmalıdır.
    private static final String[] logFolders = {"Camera", "Lidar", "VoiceAssistant", "Backend"};


    @Override
    public Result writeLog(LogEntity logEntity)  {

        // dosya ismi için tarih formatını al.
        String date = parseLogDate(logEntity.getDate());

        // oluşturacağın dizinin yolunu belirt
        File directoryPath = new File(File.separator +"home"+File.separator + username + File.separator+"WalkmanLogs" + File.separator + logEntity.getSender());

        // dizin oluştur veya varsa kontrol et
        Result directoryResult = createOrCheckDirectory(directoryPath);

        if (!directoryResult.isSuccess()) {
            return directoryResult; // Dizin oluşturma veya kontrol etme başarısızsa hata mesajını döndür
        }

        // oluşturduğun dizinin altına log dosyasının yolunu belirt
        File logFilePath = new File(directoryPath, date + ".json");

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

    // string gelen date değişkenini isimlendirme için tarih formatında düzenler
    private String parseLogDate(String logDate) {
        // Gelen tarih formatı
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime dateTime = LocalDateTime.parse(logDate, inputFormatter);

        // Yeni tarih formatı (sadece gün, ay ve yıl)
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = dateTime.format(outputFormatter);

        return formattedDate;
    }

    // Dizini oluştur veya varsa kontrol et
    @Override
    public Result createOrCheckDirectory(File directory) {
        if (directory.exists()) {
            System.out.println("Dizin mevcut.");
            return new Result(true, "Dizin mevcut.");
        } else {
            boolean isCreated = directory.mkdirs();
            if (isCreated) {
                System.out.println("Dizin başarıyla oluşturuldu.");
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
            System.out.println("Dosya mevcut, sadece içine log yaz.");
            return new Result(true, "Dosya mevcut, sadece içine log yaz.");
        } else {
            try {
                boolean isCreated = file.createNewFile();
                if (isCreated) {
                    System.out.println("Dosya başarıyla oluşturuldu.");
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


    // Haftanın her günü saat 00:00 da bu fonksiyon çalışacak. WalkmanLogs klasörü altındaki dosyaları tarayacak. & aydan eski dosya varsa silme işlemi yapacak
    // saniye(0-59),dakika(0-59),saat(0-23),gun(1-31),ay(1 - 12) (or JAN-DEC) ,haftanın hangi günleri (0-7 or MON-SUN)
    @Scheduled(cron = "0 43 9 * * MON-SUN")
    @Override
    public void cleanupOldLogs() {

        for (String logFolder : logFolders) {
            File logDirectory = new File(File.separator +"home"+File.separator + username + File.separator+"WalkmanLogs" + File.separator +  logFolder);

            if (logDirectory.exists() && logDirectory.isDirectory()) {
                File[] files = logDirectory.listFiles();

                if (files != null) {
                    for (File file : files) {
                        if (isOldLogFile(file)) {
                            if (file.delete()) {
                                System.out.println("Eski log dosyası silindi: " + file.getName());
                            } else {
                                System.out.println("Eski log dosyası silinemedi: " + file.getName());
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    // kayıtlı log dosyalarından 6 ayı geçen var mı ? varsa true döndürür yoksa false
    public boolean isOldLogFile(File file) {
        String fileName = file.getName().replace(".json", "");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        try {
            LocalDate logDate = LocalDate.parse(fileName, formatter);
            LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
            return logDate.isBefore(sixMonthsAgo);
        } catch (DateTimeParseException e) {
            System.out.println("Tarih formatı uyumsuz: " + fileName);
            e.printStackTrace();
            return false; // Hatalı bir tarih formatı ise, dosyayı silme
        }
    }

    @Override
    public ResponseEntity<Resource> getLogFileByCategoryAndDate(String logFolderName,String logFileName) throws IOException {

        // log un tutulduğu klasör yolu
        Path pathDirectory = Paths.get(File.separator +"home"+File.separator + username + File.separator+ "WalkmanLogs" + File.separator +  logFolderName);

        // log un tutulduğu klasör altındaki log dosyası
        Path pathFile = pathDirectory.resolve(logFileName);

        // Klasör ve dosyanın var olup olmadığını kontrol et
        if (!Files.exists(pathDirectory) || !Files.isRegularFile(pathFile)) {
            // 404 Bulunamadı yanıtını veya özel bir hata mesajını döndür
            String hataMesaji = "{'error': 'Dosya bulunamadı', 'folderName': '" + logFolderName + "', 'fileName': '" + logFileName + "'}";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ByteArrayResource(hataMesaji.getBytes()));
        }

        // Dosyayı ByteArrayResource olarak al
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(pathFile));

        // Set the content type and headers for the response
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + pathFile.getFileName().toString());

        // Return the file as a response entity
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);

    }


}

package com.sunnyWalkmanServer.utilities.log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "LogEntity" })
public class LogEntity {

    private String sender;
    private String messageType;
    private String message;
    private String date ;

}

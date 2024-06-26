package com.RoboRallyServer.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name="DefCompetitors")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "DefCompetitors" })
public class DefCompetitors {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Column(name = "id")
    private int id;

    @NotNull
    @Column(name = "sCity")
    private String city;

    @NotNull
    @Column(name = "sName")
    private String name;

    @Column(name = "sCode")
    private String code;

    @Column(name = "bIsReady")
    private boolean isReady;

    @Column(name = "bIsStart")
    private boolean isStart;

    @Column(name = "bIsFinish")
    private boolean isFinish;

    @Column(name = "sStartTime")
    private String startTime;

    @Column(name = "sStopTime")
    private String stopTime;

    @Column(name = "sDuration")
    private String duration;

    @Column(name = "bEliminated")
    private boolean eliminated;

    @Column(name = "bIsdelete")
    private boolean isDelete;

    @Column(name = "sPort")
    private String sPort;

}

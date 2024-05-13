package com.RoboRallyServer.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="DefPorts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "DefCompetitors" })
public class DefPorts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Column(name = "id")
    private int id;

    @Column(name = "startPort")
    private int startPort;

    @Column(name = "ip")
    private String ip;

    @Column(name = "finishPort")
    private int finishPort;

    @Column(name = "isDelete")
    private int isDelete;
}

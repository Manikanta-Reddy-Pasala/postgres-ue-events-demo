package com.example.ue_tracker.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "ue_events_history", indexes = {
    @Index(name = "idx_ue_history_imsi", columnList = "imsiOrSupi"),
    @Index(name = "idx_ue_history_updated", columnList = "updatedAt DESC")
})
@Getter
@Setter
public class UeEventHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imsiOrSupi;

    private String imei;
    private String msisdn;
    private String guti;
    private String tmsi;
    private Integer rssi;
    private String actionTaken;
    private Integer rejectCause;
    private String rat;
    private Integer frequencyBand;
    private Integer arfcn;
    private Integer trackingAreaCode;
    private String downlinkBandWidth;
    private Integer plmnMcc;
    private Integer plmnMnc;
    private String providerName;
    private String missionId;
    private String sensorId;
    private String subsystemId;
    private String trxCommandId;

    private Instant createdAt;
    private Instant updatedAt;

    private String countryIsoAlpha2;
    private String countryName;
    private Boolean target;
    private Integer captureCount;
    private Integer timingAdvance;
    private Integer distanceInMeters;
}

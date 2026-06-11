package com.example.ue_tracker.service;

import com.example.ue.proto.ActionTaken;
import com.example.ue.proto.RatType;
import com.example.ue.proto.UeEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class DataGeneratorService implements CommandLineRunner {

    private final UeEventService ueEventService;
    private final Random random = new Random();

    // Define arrays of possible values for generating realistic data
    private final String[] RAT_TYPES = {"RAT_2G", "RAT_3G", "RAT_4G_LTE", "RAT_5G"};
    private final String[] ACTIONS = {"REJECT", "ATTACH", "SILENT_CALL", "DETACH", "LOCATION_UPDATE", "PAGING"};
    private final String[] PROVIDERS = {"Etisalat", "Du", "Vodafone", "O2", "T-Mobile"};
    private final String[] COUNTRIES = {"ae", "us", "uk", "de", "fr"};
    private final String[] COUNTRY_NAMES = {"United Arab Emirates", "United States", "United Kingdom", "Germany", "France"};

    private final String[] BASE_IMSIS = {
            "424021478673415", "424021478673416", "424021478673417",
            "424021478673418", "424021478673419", "424021478673420",
            "424021478673421", "424021478673422", "424021478673423",
            "424021478673424"
    };

    @Override
    public void run(String... args) {
        log.info("Starting initial bulk data generation...");
        // Generate a decent amount of records to start (e.g., 1000 for quick startup, can be scaled up)
        for (int i = 0; i < 1000; i++) {
            generateAndSaveRandomEvent();
            if (i % 100 == 0) {
                log.info("Generated {} initial records", i);
            }
        }
        log.info("Initial bulk data generation complete.");
    }

    @Scheduled(fixedRate = 2000) // Generate a new record every 2 seconds
    public void generateContinuousData() {
        generateAndSaveRandomEvent();
    }

    private void generateAndSaveRandomEvent() {
        String imsi = BASE_IMSIS[random.nextInt(BASE_IMSIS.length)];

        // Randomly modify the IMSI slightly to simulate a massive amount of different UEs if needed,
        // but keeping it to a base set helps test history pagination for specific UEs.
        if (random.nextInt(10) > 7) {
            imsi = imsi.substring(0, 10) + String.format("%05d", random.nextInt(99999));
        }

        int countryIdx = random.nextInt(COUNTRIES.length);

        UeEvent event = UeEvent.newBuilder()
                .setImsiOrSupi(imsi)
                .setImei("35" + String.format("%013d", random.nextLong(10000000000000L)))
                .setMsisdn("+97150" + String.format("%07d", random.nextInt(10000000)))
                .setGuti("424023" + String.format("%014d", random.nextLong(100000000000000L)))
                .setTmsi(String.format("%010d", random.nextLong(10000000000L)))
                .setRssi(-50 - random.nextInt(50))
                .setActionTaken(ActionTaken.valueOf(ACTIONS[random.nextInt(ACTIONS.length)]))
                .setRejectCause(random.nextInt(20))
                .setRat(RatType.valueOf(RAT_TYPES[random.nextInt(RAT_TYPES.length)]))
                .setFrequencyBand(random.nextInt(40) + 1)
                .setArfcn(100 + random.nextInt(1000))
                .setTrackingAreaCode(40000 + random.nextInt(1000))
                .setDownlinkBandWidth(random.nextBoolean() ? "FIVE_MHZ" : "TEN_MHZ")
                .setPlmnMcc(424)
                .setPlmnMnc(2)
                .setProviderName(PROVIDERS[random.nextInt(PROVIDERS.length)])
                .setMissionId(UUID.randomUUID().toString())
                .setSensorId("sensor-" + random.nextInt(100))
                .setSubsystemId("sys-l" + random.nextInt(5) + "-" + random.nextInt(10))
                .setTrxCommandId("cmd_" + random.nextInt(1000))
                .setCreatedAt(Instant.now().toString())
                .setUpdatedAt(Instant.now().toString())
                .setCountryIsoAlpha2(COUNTRIES[countryIdx])
                .setCountryName(COUNTRY_NAMES[countryIdx])
                .setTarget(random.nextInt(100) > 95) // 5% chance to be a target
                .setCaptureCount(1 + random.nextInt(20))
                .setTimingAdvance(random.nextInt(100))
                .setDistanceInMeters(100 + random.nextInt(5000))
                .build();

        ueEventService.saveEvent(event);
    }
}

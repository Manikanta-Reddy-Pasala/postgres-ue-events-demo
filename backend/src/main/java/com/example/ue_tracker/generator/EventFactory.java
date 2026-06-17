package com.example.ue_tracker.generator;

import com.example.ue.proto.ActionTaken;
import com.example.ue.proto.RatType;
import com.example.ue.proto.UeEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class EventFactory {

    private final Random random = new Random();
    private static final String[] RAT_TYPES = {"RAT_2G", "RAT_3G", "RAT_4G_LTE", "RAT_5G"};
    private static final String[] ACTIONS = {"REJECT", "ATTACH", "SILENT_CALL", "DETACH", "LOCATION_UPDATE", "PAGING"};
    private static final String[] PROVIDERS = {"Etisalat", "Du", "Vodafone", "O2", "T-Mobile"};
    private static final String[] COUNTRIES = {"ae", "us", "uk", "de", "fr"};
    private static final String[] COUNTRY_NAMES = {"United Arab Emirates", "United States", "United Kingdom", "Germany", "France"};
    private static final String[] BASE_IMSIS = {
            "424021478673415", "424021478673416", "424021478673417", "424021478673418", "424021478673419",
            "424021478673420", "424021478673421", "424021478673422", "424021478673423", "424021478673424"
    };

    public List<UeEvent> randomBatch(int count) {
        List<UeEvent> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) out.add(randomEvent());
        return out;
    }

    public UeEvent randomEvent() {
        String imsi = BASE_IMSIS[random.nextInt(BASE_IMSIS.length)];
        if (random.nextInt(10) > 7) {
            imsi = imsi.substring(0, 10) + String.format("%05d", random.nextInt(99999));
        }
        int c = random.nextInt(COUNTRIES.length);
        String now = Instant.now().toString();
        return UeEvent.newBuilder()
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
                .setPlmnMcc(424).setPlmnMnc(2)
                .setProviderName(PROVIDERS[random.nextInt(PROVIDERS.length)])
                .setMissionId(UUID.randomUUID().toString())
                .setSensorId("sensor-" + random.nextInt(100))
                .setSubsystemId("sys-l" + random.nextInt(5) + "-" + random.nextInt(10))
                .setTrxCommandId("cmd_" + random.nextInt(1000))
                .setCreatedAt(now).setUpdatedAt(now)
                .setCountryIsoAlpha2(COUNTRIES[c]).setCountryName(COUNTRY_NAMES[c])
                .setTarget(random.nextInt(100) > 95)
                .setCaptureCount(1 + random.nextInt(20))
                .setTimingAdvance(random.nextInt(100))
                .setDistanceInMeters(100 + random.nextInt(5000))
                .build();
    }
}

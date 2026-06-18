package com.example.ue_tracker.generator;

import com.example.ue.proto.ActionTaken;
import com.example.ue.proto.RatType;
import com.example.ue.proto.UeEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Builds random UeEvents. Hot path avoids {@code UUID.randomUUID()} (SecureRandom) and
 * {@code String.format} (both dominate generation CPU) and uses {@link ThreadLocalRandom}
 * so generation parallelizes without lock contention.
 */
@Component
public class EventFactory {

    private static final ActionTaken[] ACTIONS = {
            ActionTaken.REJECT, ActionTaken.ATTACH, ActionTaken.SILENT_CALL,
            ActionTaken.DETACH, ActionTaken.LOCATION_UPDATE, ActionTaken.PAGING};
    private static final RatType[] RATS = {RatType.RAT_2G, RatType.RAT_3G, RatType.RAT_4G_LTE, RatType.RAT_5G};
    private static final String[] PROVIDERS = {"Etisalat", "Du", "Vodafone", "O2", "T-Mobile"};
    private static final String[] COUNTRIES = {"ae", "us", "uk", "de", "fr"};
    private static final String[] COUNTRY_NAMES = {"United Arab Emirates", "United States", "United Kingdom", "Germany", "France"};
    private static final String[] BASE_IMSIS = {
            "424021478673415", "424021478673416", "424021478673417", "424021478673418", "424021478673419",
            "424021478673420", "424021478673421", "424021478673422", "424021478673423", "424021478673424"
    };

    private static final long IMSI_BASE = 424021478600000L;

    /** Share of under-load writes that create brand-new UEs; the rest update existing ones. */
    private static final int NEW_IMSI_PCT = 10;

    /**
     * A batch of "live" events whose IMSIs are drawn mostly from {@code existingImsis} — so the
     * load <em>updates existing UEs</em>: each event upserts that UE's latest row in place (the
     * real churn on {@code ue_events}/{@code cqrs_read_latest}) and appends to history, turning the
     * previous latest into a history row. ~{@value #NEW_IMSI_PCT}% are brand-new UEs (inserts).
     * Falls back to random new UEs when no pool is given (empty DB). Events are stamped "now",
     * 1µs apart, so every event becomes the newest for its UE and rows stay distinct.
     */
    public List<UeEvent> batchForImsis(List<String> existingImsis, int count) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        boolean havePool = existingImsis != null && !existingImsis.isEmpty();
        Instant base = Instant.now();
        List<UeEvent> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String imsi = (havePool && r.nextInt(100) >= NEW_IMSI_PCT)
                    ? existingImsis.get(r.nextInt(existingImsis.size()))   // update an existing UE
                    : newImsi(r);                                          // occasional brand-new UE
            out.add(build(imsi, base.plusNanos((long) i * 1000L).toString(), r));
        }
        return out;
    }

    /** Batch of events for brand-new random UEs (no existing pool). */
    public List<UeEvent> randomBatch(int count) {
        return batchForImsis(null, count);
    }

    /** Brand-new UE id, parked well above the generated range so it never collides. */
    private static String newImsi(ThreadLocalRandom r) {
        return Long.toString(IMSI_BASE + 1_000_000L + r.nextLong(9_000_000L));
    }

    /** Deterministic distinct 15-digit IMSI for index. */
    public String imsiAt(long index) {
        return Long.toString(IMSI_BASE + index);
    }

    /** Event for a specific IMSI at a specific timestamp; all other fields random. */
    public UeEvent eventFor(String imsi, Instant ts) {
        return build(imsi, ts.toString(), ThreadLocalRandom.current());
    }

    public UeEvent randomEvent() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        String imsi = BASE_IMSIS[r.nextInt(BASE_IMSIS.length)];
        if (r.nextInt(10) > 7) imsi = imsi.substring(0, 10) + (10000 + r.nextInt(90000));
        return build(imsi, Instant.now().toString(), r);
    }

    private UeEvent build(String imsi, String ts, ThreadLocalRandom r) {
        int c = r.nextInt(COUNTRIES.length);
        return UeEvent.newBuilder()
                .setImsiOrSupi(imsi)
                .setImei("35" + (1000000000000L + r.nextLong(9000000000000L)))
                .setMsisdn("+97150" + (1000000 + r.nextInt(9000000)))
                .setGuti("424023" + (10000000000000L + r.nextLong(90000000000000L)))
                .setTmsi(Long.toString(1000000000L + r.nextLong(9000000000L)))
                .setRssi(-50 - r.nextInt(50))
                .setActionTaken(ACTIONS[r.nextInt(ACTIONS.length)])
                .setRejectCause(r.nextInt(20))
                .setRat(RATS[r.nextInt(RATS.length)])
                .setFrequencyBand(r.nextInt(40) + 1)
                .setArfcn(100 + r.nextInt(1000))
                .setTrackingAreaCode(40000 + r.nextInt(1000))
                .setDownlinkBandWidth(r.nextBoolean() ? "FIVE_MHZ" : "TEN_MHZ")
                .setPlmnMcc(424).setPlmnMnc(2)
                .setProviderName(PROVIDERS[r.nextInt(PROVIDERS.length)])
                .setMissionId("m" + r.nextLong(Long.MAX_VALUE))
                .setSensorId("sensor-" + r.nextInt(100))
                .setSubsystemId("sys-l" + r.nextInt(5) + "-" + r.nextInt(10))
                .setTrxCommandId("cmd_" + r.nextInt(1000))
                .setCreatedAt(ts).setUpdatedAt(ts)
                .setCountryIsoAlpha2(COUNTRIES[c]).setCountryName(COUNTRY_NAMES[c])
                .setTarget(r.nextInt(100) > 95)
                .setCaptureCount(1 + r.nextInt(20))
                .setTimingAdvance(r.nextInt(100))
                .setDistanceInMeters(100 + r.nextInt(5000))
                .build();
    }
}

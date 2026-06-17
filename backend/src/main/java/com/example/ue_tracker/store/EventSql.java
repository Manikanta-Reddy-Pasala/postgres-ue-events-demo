package com.example.ue_tracker.store;

/**
 * Single source of truth for the event-table SQL shared across stores and the projector.
 * Keeps the latest-upsert SET clause in one place instead of copy-pasted at every call site.
 */
public final class EventSql {

    private EventSql() {}

    /** Canonical event-column order shared by COPY, staging, and every INSERT...SELECT. */
    public static final String COLS =
        "imsi_or_supi,imei,msisdn,guti,tmsi,rssi,action_taken,reject_cause,rat," +
        "frequency_band,arfcn,tracking_area_code,downlink_band_width,plmn_mcc,plmn_mnc," +
        "provider_name,mission_id,sensor_id,subsystem_id,trx_command_id,created_at,updated_at," +
        "country_iso_alpha2,country_name,target,capture_count,timing_advance,distance_in_meters";

    /** Append latest-wins upsert onto an INSERT...SELECT whose target is {@code table}. */
    public static String onConflictUpdate(String table) {
        return "ON CONFLICT (imsi_or_supi) DO UPDATE SET "
                + "updated_at = EXCLUDED.updated_at, action_taken = EXCLUDED.action_taken, "
                + "rat = EXCLUDED.rat, rssi = EXCLUDED.rssi, provider_name = EXCLUDED.provider_name, "
                + "country_name = EXCLUDED.country_name, msisdn = EXCLUDED.msisdn, "
                + "distance_in_meters = EXCLUDED.distance_in_meters "
                + "WHERE EXCLUDED.updated_at >= " + table + ".updated_at";
    }

    /** Append all event columns from {@code staging_events} into {@code table}. */
    public static String insertHistoryFromStaging(String table) {
        return "INSERT INTO " + table + " (" + COLS + ") "
                + "SELECT " + COLS + " FROM staging_events";
    }

    /** Upsert the latest event per IMSI into {@code table} from {@code staging_events}. */
    public static String upsertLatestFromStaging(String table) {
        return "INSERT INTO " + table + " (" + COLS + ") "
                + "SELECT DISTINCT ON (imsi_or_supi) " + COLS + " FROM staging_events "
                + "ORDER BY imsi_or_supi, updated_at DESC "
                + onConflictUpdate(table);
    }

    /** Prefix every column in {@link #COLS} with {@code alias} (e.g. {@code "wh."}). */
    public static String prefixedCols(String alias) {
        StringBuilder sb = new StringBuilder();
        for (String c : COLS.split(",")) {
            if (sb.length() > 0) sb.append(',');
            sb.append(alias).append(c);
        }
        return sb.toString();
    }
}

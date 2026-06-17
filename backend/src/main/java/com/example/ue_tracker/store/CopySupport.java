package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * COPYs a chunk into a session-local TEMP table {@code staging_events} (created if absent,
 * truncated each call), then runs caller-supplied work in the same transaction so it sees
 * the staged rows. Returns DB-elapsed millis of the COPY phase only.
 */
@Component
public class CopySupport {

    private static final String CREATE_STAGING = """
        CREATE TEMP TABLE IF NOT EXISTS staging_events (
            imsi_or_supi text, imei text, msisdn text, guti text, tmsi text, rssi int,
            action_taken text, reject_cause int, rat text, frequency_band int, arfcn int,
            tracking_area_code int, downlink_band_width text, plmn_mcc int, plmn_mnc int,
            provider_name text, mission_id text, sensor_id text, subsystem_id text,
            trx_command_id text, created_at timestamptz, updated_at timestamptz,
            country_iso_alpha2 text, country_name text, target boolean, capture_count int,
            timing_advance int, distance_in_meters int
        )
        """;

    private final DataSource dataSource;

    public CopySupport(DataSource dataSource) { this.dataSource = dataSource; }

    public long withStaging(List<UeEvent> chunk, StagingWork work) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.execute(CREATE_STAGING);
                st.execute("TRUNCATE staging_events");
            }
            long start = System.nanoTime();
            CopyManager cm = conn.unwrap(PGConnection.class).getCopyAPI();
            cm.copyIn("COPY staging_events (" + EventSql.COLS + ") FROM STDIN WITH (FORMAT csv)",
                    new StringReader(toCsv(chunk)));
            long copyMs = (System.nanoTime() - start) / 1_000_000L;
            work.run(conn);
            conn.commit();
            return copyMs;
        } catch (SQLException | java.io.IOException e) {
            throw new RuntimeException("COPY staging failed", e);
        }
    }

    @FunctionalInterface
    public interface StagingWork { void run(Connection conn) throws SQLException; }

    private String toCsv(List<UeEvent> chunk) {
        StringBuilder sb = new StringBuilder(chunk.size() * 256);
        for (UeEvent e : chunk) {
            sb.append(q(e.getImsiOrSupi())).append(',').append(q(e.getImei())).append(',')
              .append(q(e.getMsisdn())).append(',').append(q(e.getGuti())).append(',')
              .append(q(e.getTmsi())).append(',').append(e.getRssi()).append(',')
              .append(q(e.getActionTaken().name())).append(',').append(e.getRejectCause()).append(',')
              .append(q(e.getRat().name())).append(',').append(e.getFrequencyBand()).append(',')
              .append(e.getArfcn()).append(',').append(e.getTrackingAreaCode()).append(',')
              .append(q(e.getDownlinkBandWidth())).append(',').append(e.getPlmnMcc()).append(',')
              .append(e.getPlmnMnc()).append(',').append(q(e.getProviderName())).append(',')
              .append(q(e.getMissionId())).append(',').append(q(e.getSensorId())).append(',')
              .append(q(e.getSubsystemId())).append(',').append(q(e.getTrxCommandId())).append(',')
              .append(q(e.getCreatedAt())).append(',').append(q(e.getUpdatedAt())).append(',')
              .append(q(e.getCountryIsoAlpha2())).append(',').append(q(e.getCountryName())).append(',')
              .append(e.getTarget()).append(',').append(e.getCaptureCount()).append(',')
              .append(e.getTimingAdvance()).append(',').append(e.getDistanceInMeters())
              .append('\n');
        }
        return sb.toString();
    }

    private String q(String s) {
        if (s == null) return "";
        return '"' + s.replace("\"", "\"\"") + '"';
    }
}

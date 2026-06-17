package com.example.ue_tracker.adapter;

import com.example.ue.proto.ActionTaken;
import com.example.ue.proto.RatType;
import com.example.ue.proto.UeEvent;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/** Maps a SQL row (snake_case event columns) to a {@link UeEvent} protobuf message. */
@Component
public class UeEventAdapter {

    public UeEvent fromRow(ResultSet rs) throws SQLException {
        UeEvent.Builder b = UeEvent.newBuilder()
                .setImsiOrSupi(nz(rs.getString("imsi_or_supi")))
                .setImei(nz(rs.getString("imei")))
                .setMsisdn(nz(rs.getString("msisdn")))
                .setGuti(nz(rs.getString("guti")))
                .setTmsi(nz(rs.getString("tmsi")))
                .setRssi(rs.getInt("rssi"))
                .setRejectCause(rs.getInt("reject_cause"))
                .setFrequencyBand(rs.getInt("frequency_band"))
                .setArfcn(rs.getInt("arfcn"))
                .setTrackingAreaCode(rs.getInt("tracking_area_code"))
                .setDownlinkBandWidth(nz(rs.getString("downlink_band_width")))
                .setPlmnMcc(rs.getInt("plmn_mcc"))
                .setPlmnMnc(rs.getInt("plmn_mnc"))
                .setProviderName(nz(rs.getString("provider_name")))
                .setMissionId(nz(rs.getString("mission_id")))
                .setSensorId(nz(rs.getString("sensor_id")))
                .setSubsystemId(nz(rs.getString("subsystem_id")))
                .setTrxCommandId(nz(rs.getString("trx_command_id")))
                .setCreatedAt(tsToString(rs.getTimestamp("created_at")))
                .setUpdatedAt(tsToString(rs.getTimestamp("updated_at")))
                .setCountryIsoAlpha2(nz(rs.getString("country_iso_alpha2")))
                .setCountryName(nz(rs.getString("country_name")))
                .setTarget(rs.getBoolean("target"))
                .setCaptureCount(rs.getInt("capture_count"))
                .setTimingAdvance(rs.getInt("timing_advance"))
                .setDistanceInMeters(rs.getInt("distance_in_meters"));
        try { b.setActionTaken(ActionTaken.valueOf(nz(rs.getString("action_taken")))); }
        catch (IllegalArgumentException ex) { b.setActionTaken(ActionTaken.UNKNOWN_ACTION); }
        try { b.setRat(RatType.valueOf(nz(rs.getString("rat")))); }
        catch (IllegalArgumentException ex) { b.setRat(RatType.UNKNOWN_RAT); }
        return b.build();
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static String tsToString(Timestamp t) { return t == null ? "" : t.toInstant().toString(); }
}

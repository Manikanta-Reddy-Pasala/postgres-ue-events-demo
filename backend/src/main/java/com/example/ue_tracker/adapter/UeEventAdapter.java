package com.example.ue_tracker.adapter;

import com.example.ue.proto.ActionTaken;
import com.example.ue.proto.RatType;
import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.model.UeEventEntity;
import com.example.ue_tracker.model.UeEventHistoryEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class UeEventAdapter {

    public UeEventEntity toEntity(UeEvent proto) {
        UeEventEntity entity = new UeEventEntity();
        entity.setImsiOrSupi(proto.getImsiOrSupi());
        entity.setImei(proto.getImei());
        entity.setMsisdn(proto.getMsisdn());
        entity.setGuti(proto.getGuti());
        entity.setTmsi(proto.getTmsi());
        entity.setRssi(proto.getRssi());
        entity.setActionTaken(proto.getActionTaken().name());
        entity.setRejectCause(proto.getRejectCause());
        entity.setRat(proto.getRat().name());
        entity.setFrequencyBand(proto.getFrequencyBand());
        entity.setArfcn(proto.getArfcn());
        entity.setTrackingAreaCode(proto.getTrackingAreaCode());
        entity.setDownlinkBandWidth(proto.getDownlinkBandWidth());
        entity.setPlmnMcc(proto.getPlmnMcc());
        entity.setPlmnMnc(proto.getPlmnMnc());
        entity.setProviderName(proto.getProviderName());
        entity.setMissionId(proto.getMissionId());
        entity.setSensorId(proto.getSensorId());
        entity.setSubsystemId(proto.getSubsystemId());
        entity.setTrxCommandId(proto.getTrxCommandId());
        entity.setCreatedAt(parseInstant(proto.getCreatedAt()));
        entity.setUpdatedAt(parseInstant(proto.getUpdatedAt()));
        entity.setCountryIsoAlpha2(proto.getCountryIsoAlpha2());
        entity.setCountryName(proto.getCountryName());
        entity.setTarget(proto.getTarget());
        entity.setCaptureCount(proto.getCaptureCount());
        entity.setTimingAdvance(proto.getTimingAdvance());
        entity.setDistanceInMeters(proto.getDistanceInMeters());
        return entity;
    }

    public UeEventHistoryEntity toHistoryEntity(UeEvent proto) {
        UeEventHistoryEntity entity = new UeEventHistoryEntity();
        entity.setImsiOrSupi(proto.getImsiOrSupi());
        entity.setImei(proto.getImei());
        entity.setMsisdn(proto.getMsisdn());
        entity.setGuti(proto.getGuti());
        entity.setTmsi(proto.getTmsi());
        entity.setRssi(proto.getRssi());
        entity.setActionTaken(proto.getActionTaken().name());
        entity.setRejectCause(proto.getRejectCause());
        entity.setRat(proto.getRat().name());
        entity.setFrequencyBand(proto.getFrequencyBand());
        entity.setArfcn(proto.getArfcn());
        entity.setTrackingAreaCode(proto.getTrackingAreaCode());
        entity.setDownlinkBandWidth(proto.getDownlinkBandWidth());
        entity.setPlmnMcc(proto.getPlmnMcc());
        entity.setPlmnMnc(proto.getPlmnMnc());
        entity.setProviderName(proto.getProviderName());
        entity.setMissionId(proto.getMissionId());
        entity.setSensorId(proto.getSensorId());
        entity.setSubsystemId(proto.getSubsystemId());
        entity.setTrxCommandId(proto.getTrxCommandId());
        entity.setCreatedAt(parseInstant(proto.getCreatedAt()));
        entity.setUpdatedAt(parseInstant(proto.getUpdatedAt()));
        entity.setCountryIsoAlpha2(proto.getCountryIsoAlpha2());
        entity.setCountryName(proto.getCountryName());
        entity.setTarget(proto.getTarget());
        entity.setCaptureCount(proto.getCaptureCount());
        entity.setTimingAdvance(proto.getTimingAdvance());
        entity.setDistanceInMeters(proto.getDistanceInMeters());
        return entity;
    }

    public UeEvent toProto(UeEventEntity entity) {
        UeEvent.Builder builder = UeEvent.newBuilder()
                .setImsiOrSupi(entity.getImsiOrSupi())
                .setImei(entity.getImei() != null ? entity.getImei() : "")
                .setMsisdn(entity.getMsisdn() != null ? entity.getMsisdn() : "")
                .setGuti(entity.getGuti() != null ? entity.getGuti() : "")
                .setTmsi(entity.getTmsi() != null ? entity.getTmsi() : "")
                .setRssi(entity.getRssi() != null ? entity.getRssi() : 0)
                .setRejectCause(entity.getRejectCause() != null ? entity.getRejectCause() : 0)
                .setFrequencyBand(entity.getFrequencyBand() != null ? entity.getFrequencyBand() : 0)
                .setArfcn(entity.getArfcn() != null ? entity.getArfcn() : 0)
                .setTrackingAreaCode(entity.getTrackingAreaCode() != null ? entity.getTrackingAreaCode() : 0)
                .setDownlinkBandWidth(entity.getDownlinkBandWidth() != null ? entity.getDownlinkBandWidth() : "")
                .setPlmnMcc(entity.getPlmnMcc() != null ? entity.getPlmnMcc() : 0)
                .setPlmnMnc(entity.getPlmnMnc() != null ? entity.getPlmnMnc() : 0)
                .setProviderName(entity.getProviderName() != null ? entity.getProviderName() : "")
                .setMissionId(entity.getMissionId() != null ? entity.getMissionId() : "")
                .setSensorId(entity.getSensorId() != null ? entity.getSensorId() : "")
                .setSubsystemId(entity.getSubsystemId() != null ? entity.getSubsystemId() : "")
                .setTrxCommandId(entity.getTrxCommandId() != null ? entity.getTrxCommandId() : "")
                .setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : "")
                .setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : "")
                .setCountryIsoAlpha2(entity.getCountryIsoAlpha2() != null ? entity.getCountryIsoAlpha2() : "")
                .setCountryName(entity.getCountryName() != null ? entity.getCountryName() : "")
                .setTarget(entity.getTarget() != null ? entity.getTarget() : false)
                .setCaptureCount(entity.getCaptureCount() != null ? entity.getCaptureCount() : 0)
                .setTimingAdvance(entity.getTimingAdvance() != null ? entity.getTimingAdvance() : 0)
                .setDistanceInMeters(entity.getDistanceInMeters() != null ? entity.getDistanceInMeters() : 0);

        try {
            if (entity.getActionTaken() != null) {
                builder.setActionTaken(ActionTaken.valueOf(entity.getActionTaken()));
            }
        } catch (IllegalArgumentException e) {
            builder.setActionTaken(ActionTaken.UNKNOWN_ACTION);
        }

        try {
            if (entity.getRat() != null) {
                builder.setRat(RatType.valueOf(entity.getRat()));
            }
        } catch (IllegalArgumentException e) {
            builder.setRat(RatType.UNKNOWN_RAT);
        }

        return builder.build();
    }

    public UeEvent toProto(UeEventHistoryEntity entity) {
        UeEvent.Builder builder = UeEvent.newBuilder()
                .setImsiOrSupi(entity.getImsiOrSupi())
                .setImei(entity.getImei() != null ? entity.getImei() : "")
                .setMsisdn(entity.getMsisdn() != null ? entity.getMsisdn() : "")
                .setGuti(entity.getGuti() != null ? entity.getGuti() : "")
                .setTmsi(entity.getTmsi() != null ? entity.getTmsi() : "")
                .setRssi(entity.getRssi() != null ? entity.getRssi() : 0)
                .setRejectCause(entity.getRejectCause() != null ? entity.getRejectCause() : 0)
                .setFrequencyBand(entity.getFrequencyBand() != null ? entity.getFrequencyBand() : 0)
                .setArfcn(entity.getArfcn() != null ? entity.getArfcn() : 0)
                .setTrackingAreaCode(entity.getTrackingAreaCode() != null ? entity.getTrackingAreaCode() : 0)
                .setDownlinkBandWidth(entity.getDownlinkBandWidth() != null ? entity.getDownlinkBandWidth() : "")
                .setPlmnMcc(entity.getPlmnMcc() != null ? entity.getPlmnMcc() : 0)
                .setPlmnMnc(entity.getPlmnMnc() != null ? entity.getPlmnMnc() : 0)
                .setProviderName(entity.getProviderName() != null ? entity.getProviderName() : "")
                .setMissionId(entity.getMissionId() != null ? entity.getMissionId() : "")
                .setSensorId(entity.getSensorId() != null ? entity.getSensorId() : "")
                .setSubsystemId(entity.getSubsystemId() != null ? entity.getSubsystemId() : "")
                .setTrxCommandId(entity.getTrxCommandId() != null ? entity.getTrxCommandId() : "")
                .setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : "")
                .setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : "")
                .setCountryIsoAlpha2(entity.getCountryIsoAlpha2() != null ? entity.getCountryIsoAlpha2() : "")
                .setCountryName(entity.getCountryName() != null ? entity.getCountryName() : "")
                .setTarget(entity.getTarget() != null ? entity.getTarget() : false)
                .setCaptureCount(entity.getCaptureCount() != null ? entity.getCaptureCount() : 0)
                .setTimingAdvance(entity.getTimingAdvance() != null ? entity.getTimingAdvance() : 0)
                .setDistanceInMeters(entity.getDistanceInMeters() != null ? entity.getDistanceInMeters() : 0);

        try {
            if (entity.getActionTaken() != null) {
                builder.setActionTaken(ActionTaken.valueOf(entity.getActionTaken()));
            }
        } catch (IllegalArgumentException e) {
            builder.setActionTaken(ActionTaken.UNKNOWN_ACTION);
        }

        try {
            if (entity.getRat() != null) {
                builder.setRat(RatType.valueOf(entity.getRat()));
            }
        } catch (IllegalArgumentException e) {
            builder.setRat(RatType.UNKNOWN_RAT);
        }

        return builder.build();
    }

    private Instant parseInstant(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return Instant.now();
        }
        try {
            return Instant.parse(dateStr);
        } catch (DateTimeParseException e) {
            return Instant.now();
        }
    }
}

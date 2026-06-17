-- ============ shared sequences ============
CREATE SEQUENCE ue_events_history_seq;
CREATE SEQUENCE cqrs_write_history_seq;
CREATE SEQUENCE cqrs_read_history_seq;
CREATE SEQUENCE cqrs_outbox_seq;

-- ============ NORMAL: latest ============
CREATE TABLE ue_events (
    imsi_or_supi VARCHAR(64) PRIMARY KEY,
    imei VARCHAR(32), msisdn VARCHAR(32), guti VARCHAR(64), tmsi VARCHAR(32),
    rssi INT, action_taken VARCHAR(32), reject_cause INT, rat VARCHAR(32),
    frequency_band INT, arfcn INT, tracking_area_code INT, downlink_band_width VARCHAR(32),
    plmn_mcc INT, plmn_mnc INT, provider_name VARCHAR(64), mission_id VARCHAR(64),
    sensor_id VARCHAR(64), subsystem_id VARCHAR(64), trx_command_id VARCHAR(64),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ,
    country_iso_alpha2 VARCHAR(4), country_name VARCHAR(64), target BOOLEAN,
    capture_count INT, timing_advance INT, distance_in_meters INT
);
CREATE INDEX idx_ue_events_seek ON ue_events (updated_at DESC, imsi_or_supi DESC);

-- ============ NORMAL: history (hash-partitioned by imsi) ============
CREATE TABLE ue_events_history (
    id BIGINT NOT NULL DEFAULT nextval('ue_events_history_seq'),
    imsi_or_supi VARCHAR(64) NOT NULL,
    imei VARCHAR(32), msisdn VARCHAR(32), guti VARCHAR(64), tmsi VARCHAR(32),
    rssi INT, action_taken VARCHAR(32), reject_cause INT, rat VARCHAR(32),
    frequency_band INT, arfcn INT, tracking_area_code INT, downlink_band_width VARCHAR(32),
    plmn_mcc INT, plmn_mnc INT, provider_name VARCHAR(64), mission_id VARCHAR(64),
    sensor_id VARCHAR(64), subsystem_id VARCHAR(64), trx_command_id VARCHAR(64),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ,
    country_iso_alpha2 VARCHAR(4), country_name VARCHAR(64), target BOOLEAN,
    capture_count INT, timing_advance INT, distance_in_meters INT,
    PRIMARY KEY (imsi_or_supi, id)
) PARTITION BY HASH (imsi_or_supi);
DO $$ BEGIN
  FOR i IN 0..7 LOOP
    EXECUTE format('CREATE TABLE ue_events_history_p%s PARTITION OF ue_events_history FOR VALUES WITH (MODULUS 8, REMAINDER %s)', i, i);
  END LOOP;
END $$;
CREATE INDEX idx_ue_history_seek ON ue_events_history (imsi_or_supi, updated_at DESC, id DESC);

-- ============ CQRS write: latest ============
CREATE TABLE cqrs_write_latest (LIKE ue_events INCLUDING ALL);

-- ============ CQRS write: history (partitioned) ============
CREATE TABLE cqrs_write_history (
    id BIGINT NOT NULL DEFAULT nextval('cqrs_write_history_seq'),
    imsi_or_supi VARCHAR(64) NOT NULL,
    imei VARCHAR(32), msisdn VARCHAR(32), guti VARCHAR(64), tmsi VARCHAR(32),
    rssi INT, action_taken VARCHAR(32), reject_cause INT, rat VARCHAR(32),
    frequency_band INT, arfcn INT, tracking_area_code INT, downlink_band_width VARCHAR(32),
    plmn_mcc INT, plmn_mnc INT, provider_name VARCHAR(64), mission_id VARCHAR(64),
    sensor_id VARCHAR(64), subsystem_id VARCHAR(64), trx_command_id VARCHAR(64),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ,
    country_iso_alpha2 VARCHAR(4), country_name VARCHAR(64), target BOOLEAN,
    capture_count INT, timing_advance INT, distance_in_meters INT,
    PRIMARY KEY (imsi_or_supi, id)
) PARTITION BY HASH (imsi_or_supi);
DO $$ BEGIN
  FOR i IN 0..7 LOOP
    EXECUTE format('CREATE TABLE cqrs_write_history_p%s PARTITION OF cqrs_write_history FOR VALUES WITH (MODULUS 8, REMAINDER %s)', i, i);
  END LOOP;
END $$;
CREATE INDEX idx_cqrs_write_history_seek ON cqrs_write_history (imsi_or_supi, updated_at DESC, id DESC);

-- ============ CQRS read: latest (denormalized, covering seek) ============
CREATE TABLE cqrs_read_latest (LIKE ue_events INCLUDING ALL);
CREATE INDEX idx_cqrs_read_latest_seek ON cqrs_read_latest (updated_at DESC, imsi_or_supi DESC);

-- ============ CQRS read: history (partitioned) ============
CREATE TABLE cqrs_read_history (
    id BIGINT NOT NULL DEFAULT nextval('cqrs_read_history_seq'),
    imsi_or_supi VARCHAR(64) NOT NULL,
    imei VARCHAR(32), msisdn VARCHAR(32), guti VARCHAR(64), tmsi VARCHAR(32),
    rssi INT, action_taken VARCHAR(32), reject_cause INT, rat VARCHAR(32),
    frequency_band INT, arfcn INT, tracking_area_code INT, downlink_band_width VARCHAR(32),
    plmn_mcc INT, plmn_mnc INT, provider_name VARCHAR(64), mission_id VARCHAR(64),
    sensor_id VARCHAR(64), subsystem_id VARCHAR(64), trx_command_id VARCHAR(64),
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ,
    country_iso_alpha2 VARCHAR(4), country_name VARCHAR(64), target BOOLEAN,
    capture_count INT, timing_advance INT, distance_in_meters INT,
    PRIMARY KEY (imsi_or_supi, id)
) PARTITION BY HASH (imsi_or_supi);
DO $$ BEGIN
  FOR i IN 0..7 LOOP
    EXECUTE format('CREATE TABLE cqrs_read_history_p%s PARTITION OF cqrs_read_history FOR VALUES WITH (MODULUS 8, REMAINDER %s)', i, i);
  END LOOP;
END $$;
CREATE INDEX idx_cqrs_read_history_seek ON cqrs_read_history (imsi_or_supi, updated_at DESC, id DESC);

-- ============ CQRS outbox ============
CREATE TABLE cqrs_outbox (
    seq BIGINT PRIMARY KEY DEFAULT nextval('cqrs_outbox_seq'),
    imsi_or_supi VARCHAR(64) NOT NULL,
    write_history_id BIGINT NOT NULL,
    created_at_ts TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_cqrs_outbox_seq ON cqrs_outbox (seq);

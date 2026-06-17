import axios from 'axios';
import { com } from './proto';

const API_BASE_URL = process.env.REACT_APP_API_BASE || 'http://localhost:8080/api';

export type Model = 'NORMAL' | 'CQRS';

export const generateData = async (count: number) => {
    const response = await axios.post(`${API_BASE_URL}/generate`, null, { params: { count } });
    return response.data as { uniqueImsis: number; totalEvents: number; normalMs: number; cqrsWriteMs: number };
};

export const clearData = async () => {
    const response = await axios.post(`${API_BASE_URL}/clear`);
    return response.data as { cleared: boolean };
};

export type LatencyStats = { avgMs: number; p50Ms: number; p95Ms: number; maxMs: number; samples: number };

export const runBenchmark = async (durationMs = 4000) => {
    const response = await axios.post(`${API_BASE_URL}/benchmark`, null, { params: { durationMs }, timeout: 120000 });
    return response.data as {
        durationMs: number; normal: LatencyStats; cqrs: LatencyStats;
        eventsWrittenPerModel: number; writeRatePerSec: number;
    };
};

export const fetchStats = async (model: Model) => {
    const response = await axios.get(`${API_BASE_URL}/stats`, { params: { model } });
    return response.data as { uniqueImsis: number; totalEvents: number };
};

export const fetchProjectionStatus = async () => {
    const response = await axios.get(`${API_BASE_URL}/projection/status`);
    return response.data as { outboxBacklog: number };
};

export const fetchLatest = async (
    model: Model, page: number, size = 50, filter?: string
) => {
    const response = await axios.get(`${API_BASE_URL}/events/latest`, {
        params: { model, page, size, q: filter && filter.trim() ? filter.trim() : undefined },
        responseType: 'arraybuffer',
    });
    return com.example.ue.UeEventPageResponse.decode(new Uint8Array(response.data));
};

export const fetchHistory = async (
    imsi: string, model: Model, page: number, size = 50
) => {
    const response = await axios.get(`${API_BASE_URL}/events/${imsi}/history`, {
        params: { model, page, size },
        responseType: 'arraybuffer',
    });
    return com.example.ue.UeEventPageResponse.decode(new Uint8Array(response.data));
};

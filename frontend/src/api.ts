import axios from 'axios';
import { com } from './proto';

const API_BASE_URL = 'http://localhost:8080/api';

export type Model = 'NORMAL' | 'CQRS';
export type Strategy = 'OFFSET' | 'KEYSET';

export const generateData = async (count: number) => {
    const response = await axios.post(`${API_BASE_URL}/generate`, null, { params: { count } });
    return response.data as { count: number; normalMs: number; cqrsWriteMs: number };
};

export const fetchProjectionStatus = async () => {
    const response = await axios.get(`${API_BASE_URL}/projection/status`);
    return response.data as { outboxBacklog: number };
};

export const fetchLatest = async (
    model: Model, strategy: Strategy, page: number, cursor: string | null, size = 50
) => {
    const response = await axios.get(`${API_BASE_URL}/events/latest`, {
        params: { model, strategy, page, cursor: cursor ?? undefined, size },
        responseType: 'arraybuffer',
    });
    return com.example.ue.UeEventPageResponse.decode(new Uint8Array(response.data));
};

export const fetchHistory = async (
    imsi: string, model: Model, strategy: Strategy, page: number, cursor: string | null, size = 50
) => {
    const response = await axios.get(`${API_BASE_URL}/events/${imsi}/history`, {
        params: { model, strategy, page, cursor: cursor ?? undefined, size },
        responseType: 'arraybuffer',
    });
    return com.example.ue.UeEventPageResponse.decode(new Uint8Array(response.data));
};

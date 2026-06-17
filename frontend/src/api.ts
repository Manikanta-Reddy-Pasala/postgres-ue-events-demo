import axios from 'axios';
import { com } from './proto';

const API_BASE_URL = process.env.REACT_APP_API_BASE || 'http://localhost:8080/api';

export type Model = 'NORMAL' | 'CQRS';

export const generateData = async (count: number) => {
    const response = await axios.post(`${API_BASE_URL}/generate`, null, { params: { count } });
    return response.data as { count: number; normalMs: number; cqrsWriteMs: number };
};

export const clearData = async () => {
    const response = await axios.post(`${API_BASE_URL}/clear`);
    return response.data as { cleared: boolean };
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

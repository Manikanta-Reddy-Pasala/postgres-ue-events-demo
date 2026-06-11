import axios from 'axios';
import { com } from './proto';

const API_BASE_URL = 'http://localhost:8080/api/events';

export const fetchLatestEvents = async (page = 0, size = 50) => {
    try {
        const response = await axios.get(`${API_BASE_URL}/latest`, {
            params: { page, size },
            responseType: 'arraybuffer',
        });
        const decoded = com.example.ue.UeEventPageResponse.decode(new Uint8Array(response.data));
        return decoded;
    } catch (error) {
        console.error("Error fetching latest events:", error);
        throw error;
    }
};

export const fetchEventHistory = async (imsi: string, page = 0, size = 50) => {
    try {
        const response = await axios.get(`${API_BASE_URL}/${imsi}/history`, {
            params: { page, size },
            responseType: 'arraybuffer',
        });
        const decoded = com.example.ue.UeEventPageResponse.decode(new Uint8Array(response.data));
        return decoded;
    } catch (error) {
        console.error("Error fetching event history:", error);
        throw error;
    }
};

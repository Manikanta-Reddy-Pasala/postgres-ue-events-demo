import React, { useEffect, useState } from 'react';
import {
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper,
    Typography, Button, Dialog, DialogTitle, DialogContent, DialogActions,
    Pagination, CircularProgress, Box
} from '@mui/material';
import { fetchLatestEvents, fetchEventHistory } from './api';
import { com } from './proto';

type UeEvent = com.example.ue.IUeEvent;

const Dashboard: React.FC = () => {
    const [latestEvents, setLatestEvents] = useState<UeEvent[]>([]);
    const [loading, setLoading] = useState<boolean>(true);

    const [currentPage, setCurrentPage] = useState<number>(1);
    const [totalPages, setTotalPages] = useState<number>(1);

    const [historyOpen, setHistoryOpen] = useState<boolean>(false);
    const [historyEvents, setHistoryEvents] = useState<UeEvent[]>([]);
    const [selectedImsi, setSelectedImsi] = useState<string | null>(null);
    const [historyLoading, setHistoryLoading] = useState<boolean>(false);
    const [historyPage, setHistoryPage] = useState<number>(1);
    const [historyTotalPages, setHistoryTotalPages] = useState<number>(1);

    const loadLatestEvents = async (page: number) => {
        setLoading(true);
        try {
            const response = await fetchLatestEvents(page - 1, 50);
            setLatestEvents(response.events || []);
            setTotalPages(response.totalPages || 1);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadLatestEvents(currentPage);

        // Auto-refresh every 5 seconds
        const intervalId = setInterval(() => {
            if (!historyOpen) {
                loadLatestEvents(currentPage);
            }
        }, 5000);

        return () => clearInterval(intervalId);
    }, [currentPage, historyOpen]);

    const handlePageChange = (event: React.ChangeEvent<unknown>, value: number) => {
        setCurrentPage(value);
    };

    const handleViewHistory = async (imsi: string | null | undefined) => {
        if (!imsi) return;
        setSelectedImsi(imsi);
        setHistoryOpen(true);
        setHistoryPage(1);
        await loadHistory(imsi, 1);
    };

    const loadHistory = async (imsi: string, page: number) => {
        setHistoryLoading(true);
        try {
            const response = await fetchEventHistory(imsi, page - 1, 50);
            setHistoryEvents(response.events || []);
            setHistoryTotalPages(response.totalPages || 1);
        } catch (error) {
            console.error(error);
        } finally {
            setHistoryLoading(false);
        }
    };

    const handleHistoryPageChange = (event: React.ChangeEvent<unknown>, value: number) => {
        setHistoryPage(value);
        if (selectedImsi) {
            loadHistory(selectedImsi, value);
        }
    };

    const closeHistory = () => {
        setHistoryOpen(false);
        setSelectedImsi(null);
        setHistoryEvents([]);
    };

    const formatEnum = (val: any, enumObj: any) => {
        if (val === undefined || val === null) return 'N/A';
        const key = Object.keys(enumObj).find(k => enumObj[k] === val);
        return key ? key.replace('RAT_', '') : val;
    };

    return (
        <Box sx={{ p: 4 }}>
            <Typography variant="h4" gutterBottom>
                UE Events Tracker (Latest)
            </Typography>

            {loading ? <CircularProgress /> : (
                <TableContainer component={Paper}>
                    <Table size="small">
                        <TableHead>
                            <TableRow>
                                <TableCell>IMSI/SUPI</TableCell>
                                <TableCell>MSISDN</TableCell>
                                <TableCell>Action</TableCell>
                                <TableCell>RAT</TableCell>
                                <TableCell>Provider</TableCell>
                                <TableCell>Country</TableCell>
                                <TableCell>RSSI</TableCell>
                                <TableCell>Updated At</TableCell>
                                <TableCell>Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {latestEvents.map((event) => (
                                <TableRow key={event.imsiOrSupi} hover>
                                    <TableCell>{event.imsiOrSupi}</TableCell>
                                    <TableCell>{event.msisdn}</TableCell>
                                    <TableCell>{formatEnum(event.actionTaken, com.example.ue.ActionTaken)}</TableCell>
                                    <TableCell>{formatEnum(event.rat, com.example.ue.RatType)}</TableCell>
                                    <TableCell>{event.providerName}</TableCell>
                                    <TableCell>{event.countryName}</TableCell>
                                    <TableCell>{event.rssi}</TableCell>
                                    <TableCell>{new Date(event.updatedAt || '').toLocaleString()}</TableCell>
                                    <TableCell>
                                        <Button
                                            variant="contained"
                                            size="small"
                                            onClick={() => handleViewHistory(event.imsiOrSupi)}
                                        >
                                            History
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            )}

            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                <Pagination count={totalPages} page={currentPage} onChange={handlePageChange} color="primary" />
            </Box>

            {/* History Dialog */}
            <Dialog open={historyOpen} onClose={closeHistory} maxWidth="lg" fullWidth>
                <DialogTitle>History for IMSI: {selectedImsi}</DialogTitle>
                <DialogContent>
                    {historyLoading ? <CircularProgress /> : (
                        <>
                            <TableContainer component={Paper}>
                                <Table size="small">
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Action</TableCell>
                                            <TableCell>RAT</TableCell>
                                            <TableCell>Provider</TableCell>
                                            <TableCell>RSSI</TableCell>
                                            <TableCell>Distance (m)</TableCell>
                                            <TableCell>Updated At</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {historyEvents.map((event, idx) => (
                                            <TableRow key={idx}>
                                                <TableCell>{formatEnum(event.actionTaken, com.example.ue.ActionTaken)}</TableCell>
                                                <TableCell>{formatEnum(event.rat, com.example.ue.RatType)}</TableCell>
                                                <TableCell>{event.providerName}</TableCell>
                                                <TableCell>{event.rssi}</TableCell>
                                                <TableCell>{event.distanceInMeters}</TableCell>
                                                <TableCell>{new Date(event.updatedAt || '').toLocaleString()}</TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                                <Pagination count={historyTotalPages} page={historyPage} onChange={handleHistoryPageChange} color="secondary" />
                            </Box>
                        </>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={closeHistory}>Close</Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default Dashboard;

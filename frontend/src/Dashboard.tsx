import React, { useCallback, useEffect, useState } from 'react';
import {
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper,
    Typography, Button, Dialog, DialogTitle, DialogContent, DialogActions,
    Pagination, CircularProgress, Box, ToggleButton, ToggleButtonGroup,
    TextField, Chip, Stack
} from '@mui/material';
import {
    fetchLatest, fetchHistory, generateData, fetchProjectionStatus, Model, Strategy
} from './api';
import { com } from './proto';

type UeEvent = com.example.ue.IUeEvent;

const Dashboard: React.FC = () => {
    const [model, setModel] = useState<Model>('NORMAL');
    const [strategy, setStrategy] = useState<Strategy>('OFFSET');

    const [events, setEvents] = useState<UeEvent[]>([]);
    const [loading, setLoading] = useState(false);
    const [queryMs, setQueryMs] = useState<number>(0);

    // offset paging
    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    // keyset paging
    const [cursorStack, setCursorStack] = useState<string[]>([]);
    const [nextCursor, setNextCursor] = useState<string>('');
    const [hasNext, setHasNext] = useState(false);

    const [count, setCount] = useState<number>(100000);
    const [genMsg, setGenMsg] = useState<string>('');
    const [backlog, setBacklog] = useState<number>(0);

    // history dialog
    const [historyOpen, setHistoryOpen] = useState(false);
    const [selectedImsi, setSelectedImsi] = useState<string | null>(null);
    const [historyEvents, setHistoryEvents] = useState<UeEvent[]>([]);
    const [historyMs, setHistoryMs] = useState<number>(0);
    const [historyLoading, setHistoryLoading] = useState(false);

    const loadLatest = useCallback(async (p: number, cursor: string | null) => {
        setLoading(true);
        try {
            const r = await fetchLatest(model, strategy, p - 1, cursor);
            setEvents(r.events || []);
            setQueryMs(Number(r.queryTimeMs || 0));
            setTotalPages(r.totalPages || 1);
            setNextCursor(r.nextCursor || '');
            setHasNext(!!r.hasNext);
        } catch (e) { console.error(e); } finally { setLoading(false); }
    }, [model, strategy]);

    // reset + reload whenever model/strategy changes
    useEffect(() => {
        setPage(1); setCursorStack([]); setNextCursor(''); setHasNext(false);
        loadLatest(1, null);
    }, [model, strategy, loadLatest]);

    // poll projection backlog when in CQRS mode
    useEffect(() => {
        if (model !== 'CQRS') { setBacklog(0); return; }
        const id = setInterval(async () => {
            try { setBacklog((await fetchProjectionStatus()).outboxBacklog); } catch { /* ignore */ }
        }, 1500);
        return () => clearInterval(id);
    }, [model]);

    const onGenerate = async () => {
        setGenMsg('Generating…');
        try {
            const r = await generateData(count);
            setGenMsg(`Generated ${r.count.toLocaleString()} — normal: ${r.normalMs} ms · cqrs-write: ${r.cqrsWriteMs} ms`);
            setPage(1); setCursorStack([]);
            await loadLatest(1, null);
        } catch (e) { setGenMsg('Generation failed (see console)'); console.error(e); }
    };

    const onOffsetPage = (_: unknown, value: number) => { setPage(value); loadLatest(value, null); };
    const onKeysetNext = () => {
        setCursorStack(s => [...s, nextCursor]);
        loadLatest(1, nextCursor);
    };
    const onKeysetPrev = () => {
        const s = [...cursorStack]; s.pop();
        const prev = s.length ? s[s.length - 1] : null;
        setCursorStack(s);
        loadLatest(1, prev);
    };

    const openHistory = async (imsi?: string | null) => {
        if (!imsi) return;
        setSelectedImsi(imsi); setHistoryOpen(true); setHistoryLoading(true);
        try {
            const r = await fetchHistory(imsi, model, strategy, 0, null);
            setHistoryEvents(r.events || []);
            setHistoryMs(Number(r.queryTimeMs || 0));
        } catch (e) { console.error(e); } finally { setHistoryLoading(false); }
    };

    const formatEnum = (val: any, enumObj: any) => {
        if (val === undefined || val === null) return 'N/A';
        const key = Object.keys(enumObj).find(k => enumObj[k] === val);
        return key ? key.replace('RAT_', '') : val;
    };

    return (
        <Box sx={{ p: 4 }}>
            <Typography variant="h4" gutterBottom>UE Events — Pagination Benchmark</Typography>

            <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2, flexWrap: 'wrap', rowGap: 1 }}>
                <ToggleButtonGroup size="small" exclusive value={model}
                    onChange={(_, v) => v && setModel(v)}>
                    <ToggleButton value="NORMAL">Normal (2 tables)</ToggleButton>
                    <ToggleButton value="CQRS">CQRS (Read-Write Sep)</ToggleButton>
                </ToggleButtonGroup>

                <ToggleButtonGroup size="small" exclusive value={strategy}
                    onChange={(_, v) => v && setStrategy(v)}>
                    <ToggleButton value="OFFSET">Offset</ToggleButton>
                    <ToggleButton value="KEYSET">Keyset</ToggleButton>
                </ToggleButtonGroup>

                <Chip color="info" label={`Latest query: ${queryMs} ms`} />
                {model === 'CQRS' && <Chip color="warning" label={`Projection backlog: ${backlog.toLocaleString()}`} />}
                <Button variant="outlined" size="small" onClick={() => loadLatest(page, null)}>Refresh</Button>
            </Stack>

            <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 3 }}>
                <TextField size="small" type="number" label="Record count" value={count}
                    onChange={e => setCount(parseInt(e.target.value || '0', 10))} sx={{ width: 180 }} />
                <Button variant="contained" onClick={onGenerate}>Generate Data</Button>
                <Typography variant="body2">{genMsg}</Typography>
            </Stack>

            {loading ? <CircularProgress /> : (
                <TableContainer component={Paper}>
                    <Table size="small">
                        <TableHead>
                            <TableRow>
                                <TableCell>IMSI/SUPI</TableCell><TableCell>MSISDN</TableCell>
                                <TableCell>Action</TableCell><TableCell>RAT</TableCell>
                                <TableCell>Provider</TableCell><TableCell>Country</TableCell>
                                <TableCell>RSSI</TableCell><TableCell>Updated At</TableCell>
                                <TableCell>Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {events.map((event, idx) => (
                                <TableRow key={`${event.imsiOrSupi}-${idx}`} hover>
                                    <TableCell>{event.imsiOrSupi}</TableCell>
                                    <TableCell>{event.msisdn}</TableCell>
                                    <TableCell>{formatEnum(event.actionTaken, com.example.ue.ActionTaken)}</TableCell>
                                    <TableCell>{formatEnum(event.rat, com.example.ue.RatType)}</TableCell>
                                    <TableCell>{event.providerName}</TableCell>
                                    <TableCell>{event.countryName}</TableCell>
                                    <TableCell>{event.rssi}</TableCell>
                                    <TableCell>{new Date(event.updatedAt || '').toLocaleString()}</TableCell>
                                    <TableCell>
                                        <Button variant="contained" size="small"
                                            onClick={() => openHistory(event.imsiOrSupi)}>History</Button>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            )}

            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                {strategy === 'OFFSET' ? (
                    <Pagination count={totalPages} page={page} onChange={onOffsetPage} color="primary"
                        siblingCount={2} />
                ) : (
                    <Stack direction="row" spacing={2} alignItems="center">
                        <Button variant="outlined" disabled={cursorStack.length === 0} onClick={onKeysetPrev}>Prev</Button>
                        <Button variant="outlined" disabled={!hasNext} onClick={onKeysetNext}>Next</Button>
                    </Stack>
                )}
            </Box>

            <Dialog open={historyOpen} onClose={() => setHistoryOpen(false)} maxWidth="lg" fullWidth>
                <DialogTitle>
                    History — IMSI {selectedImsi} <Chip size="small" color="info" label={`${historyMs} ms`} sx={{ ml: 2 }} />
                </DialogTitle>
                <DialogContent>
                    {historyLoading ? <CircularProgress /> : (
                        <TableContainer component={Paper}>
                            <Table size="small">
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Action</TableCell><TableCell>RAT</TableCell>
                                        <TableCell>Provider</TableCell><TableCell>RSSI</TableCell>
                                        <TableCell>Distance (m)</TableCell><TableCell>Updated At</TableCell>
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
                    )}
                </DialogContent>
                <DialogActions><Button onClick={() => setHistoryOpen(false)}>Close</Button></DialogActions>
            </Dialog>
        </Box>
    );
};

export default Dashboard;

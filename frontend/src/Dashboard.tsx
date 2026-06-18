import React, { useCallback, useEffect, useState } from 'react';
import {
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper,
    Typography, Button, Dialog, DialogTitle, DialogContent, DialogActions,
    Pagination, CircularProgress, Box, ToggleButton, ToggleButtonGroup,
    TextField, Chip, Stack
} from '@mui/material';
import {
    fetchLatest, fetchHistory, generateData, clearData, fetchProjectionStatus, fetchStats,
    runBenchmark, LatencyStats, WriteStats, Model
} from './api';
import { com } from './proto';

type UeEvent = com.example.ue.IUeEvent;

const PAGE_SIZE = 50;

const Dashboard: React.FC = () => {
    const [model, setModel] = useState<Model>('NORMAL');

    const [events, setEvents] = useState<UeEvent[]>([]);
    const [loading, setLoading] = useState(false);
    const [queryMs, setQueryMs] = useState<number>(0);

    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [totalElements, setTotalElements] = useState(0);

    // single search box: matches IMSI / MSISDN / RAT
    const [search, setSearch] = useState('');
    const [activeFilter, setActiveFilter] = useState('');

    const [count, setCount] = useState<number>(1000);
    const [genMsg, setGenMsg] = useState<string>('');
    const [backlog, setBacklog] = useState<number>(0);
    const [stats, setStats] = useState<{ uniqueImsis: number; totalEvents: number }>({ uniqueImsis: 0, totalEvents: 0 });
    const [bench, setBench] = useState<{
        durationMs: number;
        normalRead: LatencyStats; cqrsRead: LatencyStats;
        normalWrite: WriteStats; cqrsWrite: WriteStats;
    } | null>(null);
    const [benchRunning, setBenchRunning] = useState(false);

    // history dialog
    const [historyOpen, setHistoryOpen] = useState(false);
    const [selectedImsi, setSelectedImsi] = useState<string | null>(null);
    const [historyEvents, setHistoryEvents] = useState<UeEvent[]>([]);
    const [historyMs, setHistoryMs] = useState<number>(0);
    const [historyLoading, setHistoryLoading] = useState(false);

    const loadLatest = useCallback(async (p: number, filter: string) => {
        setLoading(true);
        try {
            const r = await fetchLatest(model, p - 1, PAGE_SIZE, filter);
            setEvents(r.events || []);
            setQueryMs(Number(r.queryTimeMs || 0));
            setTotalPages(r.totalPages || 1);
            setTotalElements(Number(r.totalElements || 0));
            try { setStats(await fetchStats(model)); } catch { /* ignore */ }
        } catch (e) { console.error(e); } finally { setLoading(false); }
    }, [model]);

    // reload when model changes (keep current filter)
    useEffect(() => {
        setPage(1);
        loadLatest(1, activeFilter);
    }, [model, activeFilter, loadLatest]);

    // poll projection backlog in CQRS mode
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
            setGenMsg(`Generated ${r.uniqueImsis.toLocaleString()} IMSIs · ${r.totalEvents.toLocaleString()} events — normal: ${r.normalMs} ms · cqrs-write: ${r.cqrsWriteMs} ms`);
            setPage(1);
            await loadLatest(1, activeFilter);
        } catch (e) { setGenMsg('Generation failed (see console)'); console.error(e); }
    };

    const onClear = async () => {
        if (!window.confirm('Clear ALL data in both models (Normal + CQRS)?')) return;
        setGenMsg('Clearing…');
        try {
            await clearData();
            setGenMsg('Cleared all data (both models)');
            setPage(1);
            await loadLatest(1, activeFilter);
        } catch (e) { setGenMsg('Clear failed (see console)'); console.error(e); }
    };

    const onBenchmark = async () => {
        setBenchRunning(true); setBench(null);
        try {
            setBench(await runBenchmark(4000));
        } catch (e) { console.error(e); } finally { setBenchRunning(false); }
    };

    const runSearch = () => { setPage(1); setActiveFilter(search); };
    const clearSearch = () => { setSearch(''); setPage(1); setActiveFilter(''); };

    const onPage = (_: unknown, value: number) => { setPage(value); loadLatest(value, activeFilter); };

    const openHistory = async (imsi?: string | null) => {
        if (!imsi) return;
        setSelectedImsi(imsi); setHistoryOpen(true); setHistoryLoading(true);
        try {
            const r = await fetchHistory(imsi, model, 0, PAGE_SIZE);
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
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                <b>Generate</b> creates that many <b>unique IMSIs</b>, each with 100–1000 history events. The table lists
                the <b>latest event per IMSI</b> (one row per UE); every event is kept in history — click <b>History</b>
                on a row to page through them.
            </Typography>

            <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2, flexWrap: 'wrap', rowGap: 1 }}>
                <ToggleButtonGroup size="small" exclusive value={model}
                    onChange={(_, v) => v && setModel(v)}>
                    <ToggleButton value="NORMAL">Normal (2 tables)</ToggleButton>
                    <ToggleButton value="CQRS">CQRS (Read-Write Sep)</ToggleButton>
                </ToggleButtonGroup>

                <Chip color="info" label={`Latest query: ${queryMs} ms`} />
                <Chip color="success" variant="outlined"
                    label={`${stats.totalEvents.toLocaleString()} events · ${stats.uniqueImsis.toLocaleString()} unique IMSIs`} />
                <Chip variant="outlined"
                    label={`${activeFilter ? 'matches' : 'list'}: ${totalElements.toLocaleString()} IMSIs · ${totalPages} pages`} />
                {model === 'CQRS' && <Chip color="warning" label={`Projection backlog: ${backlog.toLocaleString()}`} />}
                <Button variant="outlined" size="small" onClick={() => loadLatest(page, activeFilter)}>Refresh</Button>
            </Stack>

            <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2 }}>
                <TextField size="small" label="Search IMSI / MSISDN / RAT" value={search}
                    onChange={e => setSearch(e.target.value)}
                    onKeyDown={e => { if (e.key === 'Enter') runSearch(); }} sx={{ width: 320 }} />
                <Button variant="contained" onClick={runSearch}>Search</Button>
                <Button variant="text" onClick={clearSearch} disabled={!activeFilter}>Clear</Button>
                {activeFilter && <Chip color="secondary" label={`filter: "${activeFilter}"`} onDelete={clearSearch} />}
            </Stack>

            <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 3 }}>
                <TextField size="small" type="number" label="Unique IMSIs" value={count}
                    helperText="each gets 100–1000 events"
                    onChange={e => setCount(parseInt(e.target.value || '0', 10))} sx={{ width: 180 }} />
                <Button variant="contained" onClick={onGenerate}>Generate Data</Button>
                <Button variant="outlined" color="error" onClick={onClear}>Clear Data</Button>
                <Typography variant="body2">{genMsg}</Typography>
            </Stack>

            <Paper variant="outlined" sx={{ p: 2, mb: 3 }}>
                <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: bench ? 1.5 : 0 }}>
                    <Button variant="contained" color="secondary" onClick={onBenchmark} disabled={benchRunning}>
                        {benchRunning ? 'Running…' : 'Compare under load'}
                    </Button>
                    {benchRunning && <CircularProgress size={20} />}
                    <Typography variant="body2" color="text.secondary">
                        Hammers writes (3 threads/model) for ~4s while repeatedly reading the <b>first latest page</b>
                        on each model's read table — concurrent read under write, on the table the upsert-latest pattern
                        actually contends. NORMAL reads <code>ue_events</code>: every event UPSERTs a row in place, so
                        the read table churns (dead tuples, seek-index bloat, vacuum). CQRS reads <code>cqrs_read_latest</code>,
                        updated only by the deduped projector. Watch <b>p99</b> (tail) and <b>writes/s to read table</b>:
                        churn shows up in the tail, not the average. At small scale / in cache both can still look close —
                        the gap widens as data exceeds buffer cache and bloat accumulates.
                    </Typography>
                </Stack>
                {bench && (
                    <Table size="small" sx={{ maxWidth: 820 }}>
                        <TableHead>
                            <TableRow>
                                <TableCell>Model</TableCell>
                                <TableCell align="right">read avg</TableCell>
                                <TableCell align="right">p50</TableCell>
                                <TableCell align="right">p95</TableCell>
                                <TableCell align="right">p99</TableCell>
                                <TableCell align="right">max</TableCell>
                                <TableCell align="right">reads</TableCell>
                                <TableCell align="right">writes/s&nbsp;to&nbsp;read&nbsp;table</TableCell>
                                <TableCell align="right">writes</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell><b>NORMAL</b> — reads ue_events latest (load upserts here)</TableCell>
                                <TableCell align="right">{bench.normalRead.avgMs} ms</TableCell>
                                <TableCell align="right">{bench.normalRead.p50Ms} ms</TableCell>
                                <TableCell align="right">{bench.normalRead.p95Ms} ms</TableCell>
                                <TableCell align="right">{bench.normalRead.p99Ms} ms</TableCell>
                                <TableCell align="right">{bench.normalRead.maxMs} ms</TableCell>
                                <TableCell align="right">{bench.normalRead.samples}</TableCell>
                                <TableCell align="right">{bench.normalWrite.writesPerSec.toLocaleString()}</TableCell>
                                <TableCell align="right">{bench.normalWrite.writesToReadTable.toLocaleString()}</TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell><b>CQRS</b> — reads cqrs_read_latest (deduped projector writes here)</TableCell>
                                <TableCell align="right">{bench.cqrsRead.avgMs} ms</TableCell>
                                <TableCell align="right">{bench.cqrsRead.p50Ms} ms</TableCell>
                                <TableCell align="right">{bench.cqrsRead.p95Ms} ms</TableCell>
                                <TableCell align="right">{bench.cqrsRead.p99Ms} ms</TableCell>
                                <TableCell align="right">{bench.cqrsRead.maxMs} ms</TableCell>
                                <TableCell align="right">{bench.cqrsRead.samples}</TableCell>
                                <TableCell align="right">{bench.cqrsWrite.writesPerSec.toLocaleString()}</TableCell>
                                <TableCell align="right">{bench.cqrsWrite.writesToReadTable.toLocaleString()}</TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                )}
            </Paper>

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
                            {events.length === 0 && (
                                <TableRow><TableCell colSpan={9} align="center">No rows</TableCell></TableRow>
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
            )}

            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                <Pagination count={totalPages} page={page} onChange={onPage} color="primary" siblingCount={2} />
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

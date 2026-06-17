# Graph Report - postgres-ue-events-demo  (2026-06-17)

## Corpus Check
- 35 files · ~23,975 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 140 nodes · 173 edges · 12 communities detected
- Extraction: 78% EXTRACTED · 22% INFERRED · 0% AMBIGUOUS · INFERRED: 38 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]

## God Nodes (most connected - your core abstractions)
1. `CursorCodec` - 9 edges
2. `UeEventController` - 7 edges
3. `CqrsEventStore` - 7 edges
4. `NormalEventStore` - 7 edges
5. `SeekQuery` - 6 edges
6. `CqrsProjectorService` - 5 edges
7. `CopySupport` - 5 edges
8. `EventStore` - 5 edges
9. `CursorCodecTest` - 4 edges
10. `WebConfig` - 4 edges

## Surprising Connections (you probably didn't know these)
- `onGenerate()` --calls--> `generateData()`  [INFERRED]
  frontend/src/Dashboard.tsx → frontend/src/api.ts
- `openHistory()` --calls--> `fetchHistory()`  [INFERRED]
  frontend/src/Dashboard.tsx → frontend/src/api.ts

## Communities

### Community 0 - "Community 0"
Cohesion: 0.11
Nodes (5): CqrsEventStore, EventStore, NormalEventStore, PsSetter, SeekQuery

### Community 1 - "Community 1"
Cohesion: 0.14
Nodes (4): CqrsEventStoreTest, EventFactory, EventFactoryTest, GenerationService

### Community 2 - "Community 2"
Cohesion: 0.25
Nodes (2): CursorCodec, CursorCodecTest

### Community 3 - "Community 3"
Cohesion: 0.19
Nodes (3): CopySupport, StagingWork, UeTrackerApplication

### Community 4 - "Community 4"
Cohesion: 0.18
Nodes (5): fetchHistory(), fetchLatest(), generateData(), onGenerate(), openHistory()

### Community 5 - "Community 5"
Cohesion: 0.24
Nodes (2): EventStore, UeEventController

### Community 6 - "Community 6"
Cohesion: 0.27
Nodes (5): AbstractPostgresTest, CqrsProjectorServiceTest, GenerationServiceTest, NormalEventStoreTest, UeEventControllerTest

### Community 7 - "Community 7"
Cohesion: 0.33
Nodes (1): CqrsProjectorService

### Community 8 - "Community 8"
Cohesion: 0.5
Nodes (2): WebConfig, WebMvcConfigurer

### Community 9 - "Community 9"
Cohesion: 0.6
Nodes (1): UeEventAdapter

### Community 10 - "Community 10"
Cohesion: 0.5
Nodes (3): EmptyRequest, UeEvent, UeEventPageResponse

### Community 11 - "Community 11"
Cohesion: 0.5
Nodes (1): AbstractPostgresTest

## Knowledge Gaps
- **3 isolated node(s):** `UeEvent`, `UeEventPageResponse`, `EmptyRequest`
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 2`** (15 nodes): `CursorCodec.java`, `CursorCodecTest.java`, `CursorCodec`, `.b64()`, `.decode()`, `.decodeLatest()`, `.encode()`, `.encodeLatest()`, `.fromMicros()`, `.micros()`, `.split()`, `CursorCodecTest`, `.decodesNullOrBlankAsNull()`, `.roundTripsLatestStringTiebreaker()`, `.roundTripsUpdatedAtAndId()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 5`** (12 nodes): `EventStore.java`, `EventStore`, `.getHistory()`, `.getLatest()`, `.model()`, `.copyInLoadsHistoryAndLatestThenPaginatesBothWays()`, `UeEventController`, `.generate()`, `.history()`, `.latest()`, `.toProto()`, `.UeEventController()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 7`** (7 nodes): `CqrsProjectorService.java`, `CqrsProjectorService`, `.backlog()`, `.CqrsProjectorService()`, `.drainOnce()`, `.scheduledDrain()`, `.projectionStatus()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 8`** (5 nodes): `WebConfig.java`, `WebConfig`, `.addCorsMappings()`, `.protobufHttpMessageConverter()`, `WebMvcConfigurer`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 9`** (5 nodes): `UeEventAdapter.java`, `UeEventAdapter`, `.fromRow()`, `.nz()`, `.tsToString()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 11`** (4 nodes): `AbstractPostgresTest`, `.props()`, `.truncateAll()`, `AbstractPostgresTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CqrsEventStore` connect `Community 0` to `Community 3`?**
  _High betweenness centrality (0.182) - this node is a cross-community bridge._
- **Why does `NormalEventStore` connect `Community 0` to `Community 3`?**
  _High betweenness centrality (0.182) - this node is a cross-community bridge._
- **What connects `UeEvent`, `UeEventPageResponse`, `EmptyRequest` to the rest of the system?**
  _3 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.11 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.14 - nodes in this community are weakly interconnected._
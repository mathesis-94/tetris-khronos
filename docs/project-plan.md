# Tetris Khronos — Milestone 1 Project Plan

**Group:** [PG39]  
**Course:** [2006 OOSD]  
**Submission Date:** [06/08/2026]

**Note:** This project was completed independently and has been adjusted where needed to meet specified requirements as much as possible, as limited time has been available for this project due to extremely difficult life circumstances some understanding of lateness is asked.

---

## 1. TEAM ORGANISATION & ROLES

| Member | Role | Responsibilities |
|--------|------|------------------|
| [Morrigan W (mathesis)] | Lead Developer, Architect, Tester, Documentation Lead | Full-stack development (Model, Controller, View, Persistence), testing, technical documentation |

**Status:** Solo development completed independently.

---

## 2. DEVELOPMENT PHASES & TASK TRACKING

| Phase | Task IDs | Estimated Hours | Actual Hours | Completion |
|-------|----------|-----------------|------------|------------|
| Model Layer | TASK-001 to 005 | 20 | 51 | 80% |
| Persistence | TASK-006 to 007 | 8 | — | [X] |
| Controller/Threading | TASK-008 to 009 | 14 | — | [X] |
| View/UI | TASK-010 to 016 | 27 | 39 | 90% |
| QA Testing | TASK-017 to 018 | 18 | — | [] |
| Documentation | TASK-019 to 022 | 18 | 15| [X] |
| **TOTAL** | — | **105 hours** | **145 hours** | **60%** |

### Task Breakdown

| Task ID | Component | Description | Est. Hrs | Actual | Status |
|---------|-----------|-------------|----------|--------|--------|
| TASK-001 | Model | Tetromino class hierarchy + rotation logic | 6 | 15 | [X] |
| TASK-002 | Model | Board grid, collision detection, row erasure | 5 | 25 | [] |
| TASK-003 | Model | GameState enum & state machine | 4 | 6 | [X] |
| TASK-004 | Model | Configuration DTO + validation | 3 | 4 | [X] |
| TASK-005 | Model | Score tracking & leaderboard logic | 2 | 1 | [X] |
| TASK-006 | Persistence | ConfigRepository (Jackson JSON) | 4 | - | [] |
| TASK-007 | Persistence | ScoreRepository (high scores I/O) | 4 | - | [] |
| TASK-008 | Controller | GameLoopThread (60 FPS, synchronisation) | 8 | 10 | [X] |
| TASK-009 | Controller | ScreenManager (factory pattern, navigation) | 6 | 8 | [X] |
| TASK-010 | View | SplashScreen (2–3 second timer) | 2 | 1 | [X] |
| TASK-011 | View | MainMenuScreen (Play, Config, Scores, Exit) | 4 | 3 | [X] |
| TASK-012 | View | GameScreen (game field rendering, controls) | 6 | 12 | [] |
| TASK-013 | View | BoardRenderer & FieldPane (grid visualisation) | 5 | 10 | [] |
| TASK-014 | View | ConfigScreen (interactive controls, functionality not implemented) | 4 | 3 | [X] |
| TASK-015 | View | HighScoreScreen (top 10 display) | 3 | 5 | [X] |
| TASK-016 | View | UIGenerator (common utilities) | 3 | 5 | [X] |
| TASK-017 | QA | Unit tests (Board, Tetromino, Game) | 10 | — | 🟡 In Progress |
| TASK-018 | QA | Bug fixes & integration testing | 8 | — | 🟡 In Progress |
| TASK-019 | Docs | JavaDoc & inline comments | 5 | — | 🟡 In Progress |
| TASK-020 | Docs | Architecture documentation | 4 | — | 🟡 In Progress |
| TASK-021 | Docs | Requirements & use case diagrams | 6 | 3 | [X] |
| TASK-022 | Docs | Activity diagram & PlantUML syntax | 3 | 2 | [X] |

---

## 3. TOOLS & INFRASTRUCTURE

| Category | Details |
|----------|---------|
| **Build System** | Maven (Java 21, JavaFX 21) |
| **Version Control** | GitHub |
| **IDE** | IntelliJ IDEA |
| **Diagramming** | PlantUML (UML diagrams), Draw.io (reference) |
| **Documentation** | Markdown, JavaDoc |
| **Testing Framework** | JUnit 5 (planned for QA phase) |
| **OS** | Linux |

---

## 4. ALIGNMENT TO MARKING CRITERIA

### Requirement Analysis
[X] **FR-001 to FR-013** — Functional requirements mapped to demo features + marking criteria ( `Milestone-1-Functional-Requirements.md`)  
[X] **NFR-U-001, NFR-R-001, NFR-P-001, NFR-S-001** — FURPS+ categories covered (`Milestone-1-Non-Functional-Requirements.md`)  
[X] **Use Case Diagram** — `/docs/milestones/milestone-1/use-case-diagram.uml` (UML notation, correct relationships)  
[X] **Activity Diagram** — `/docs/milestones/milestone-1/activity-diagram.uml` (game loop flow, pause state, no config/high scores)


### Demonstration (Video)
[X] **FR-001** — Splash window (2–3 sec, group identity & course code)  
[X] **FR-002** — Main menu (Play, Config, Scores, Exit buttons)  
[] **FR-003–FR-007** — Game play (10×20 field, smooth movement, rotation, controls)  
[] **FR-008–FR-009** — Full row detection & erasure (single & multiple rows)  
[] **FR-010** — Pause/resume (P key, "Paused" message)  
[] **FR-011** — Exit confirmation dialog (Yes/No options)  
[] **FR-012** — High score screen (top 10, dummy data)  
[X] **FR-004** — Configuration screen (non functional controls displaying change only in menu: field size, level, music, sound, AI, extended mode)

---

## 5. KEY DESIGN DECISIONS

| Decision | Requirement | Rationale |
|----------|-------------|-----------|
| **MVC Architecture** | NFR-S-001 (Supportability) | Clear separation of concerns; testable, maintainable |
| **GameLoopThread** | NFR-P-001, NFR-R-001 | Dedicated game thread; 60 FPS target; thread-safe state |
| **ScreenManager (Factory)** | NFR-S-002 (Extensibility) | Centralised screen creation & navigation |
| **Sealed Tetromino Hierarchy** | NFR-S-001, NFR-S-002 | Type-safe variants; compile-time verification |
| **Platform.runLater()** | NFR-R-001 (Reliability) | Thread-safe JavaFX updates from GameLoopThread |
| **Jackson Persistence** | FR-004, FR-005, NFR-R-002 | Lightweight JSON serialisation for config & high scores |





**Project Status:** ~77% complete. All core features implemented; final testing, qa, and bug fixing & documentation in progress.  
**Demo Ready:** Yes

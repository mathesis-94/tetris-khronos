# Project Plan - Tetris Khronos

## Team & Roles
| Role | Name | Responsibilities |
|------|------|------------------|
| Lead Developer | Morrigan W (mathesis) | Model, Controller, View, FXML, Unit Tests, Documentation, GitHub, CI/CD |

**Note:** This project was completed independently and has been adjusted where needed to meet specified requirements as much as possible.

---

## Major Milestones

| Task | Owner | Est. Hours | Start | End | Status | Completion % |
|------|-------|-----------|-------|-----|--------|--------------|
| Model Layer (Game, Board, Tetromino, Configuration) | Lead Dev | 15 | Week 1 | Week 5 | Complete | 100% |
| Persistence (Jackson config/scores) | Lead Dev | 8 | Week 2 | Week 6 | Complete | 100% |
| Controller & Screen Navigation (ScreenManager, GameLoopThread) | Lead Dev | 10 | Week 3 | Week 5 | Complete | 100% |
| FXML UI & JavaFX Refactor (Screens, Layout Binding) | Lead Dev | 20 | Week 5 | Week 6 | In Progress | 85% |
| Bug Fixes (Threading, Layout, Pause Indicator) | Lead Dev | 8 | Week 6 | Week 6 | In Progress | 90% |
| Unit Tests (Board, Tetromino, Game logic) | Lead Dev | 12 | Week 3 | Week 6 | Not Started | 20% |
| Documentation (Requirement Analysis, JavaDoc) | Lead Dev | 8 | Week 4 | Week 7 | Not Started | 50% |
| Demo Video & Submission | Lead Dev | 6 | Week 6 | Week 7 | Not Started | 0% |

---

## Tools & Infrastructure
- **Version Control:** GitHub
- **Build System:** Maven (Java 21, JavaFX 21)
- **IDE/Editor:** Zeditor, IntelliJ IDEA, nano, zsh
- **OS:** Linux
- **Diagramming:** Lucidchart / Draw.io (for UML)


---

## Key Decisions & Design Patterns

| Decision | Rationale | Status |
|----------|-----------|--------|
| **Factory Pattern (ScreenManager)** | Centralized screen creation and navigation | ✅ Implemented |
| **Platform.runLater() for UI updates** | Avoid IllegalStateException in JavaFX threading | ✅ Implemented |
| **Property Binding for Layout** | Dynamic window resizing and responsive grid | ✅ Implemented |
| **Sealed Tetromino class hierarchy** | Type-safe tetromino definitions with rotation logic | ✅ Implemented |
| **Jackson persistence** | Config and high score serialization | ✅ Implemented |

---

## Risks & Mitigation

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Threading issues in game loop | Game crashes | Use Platform.runLater() for FX updates; test on multiple runs |
| Layout not filling window | Poor UX | Bind pref/min sizes; use VBox.setVgrow() with Priority.ALWAYS |
| Insufficient commit history | Mark deduction | Squash, rebase, or cherry-pick to create clean 20–30 commits |
| Demo video technical issues | Cannot demonstrate features | Record in advance; test all features; have backup video |

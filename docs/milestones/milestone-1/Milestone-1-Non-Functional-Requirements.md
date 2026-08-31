# Non-Functional Requirements

## Usability (U)
**NFR-U01:** Responsive UI
- **Category:** Usability  
- **Requirement:** Game controls must respond within 50ms of user input (keyboard/mouse).
- **Testable:** Use profiler to measure input-response latency.

**NFR-U02:** Intuitive Navigation
- **Category:** Usability
- **Requirement:** All screens must have clearly labeled buttons and consistent layout.
- **Testable:** Conduct user testing; >90% of testers should navigate without instruction.

## Reliability (R)
**NFR-R01:** Crash-Free Gameplay
- **Category:** Reliability
- **Requirement:** Application must not crash during normal gameplay for 30+ minutes.
- **Testable:** Run automated 30-minute game session; monitor logs for exceptions.

**NFR-R02:** Data Persistence
- **Category:** Reliability
- **Requirement:** Configuration and high scores must persist across app restarts.
- **Testable:** Save config, close app, restart, verify config restored.

## Performance (P)
**NFR-P01:** Render Frame Rate
- **Category:** Performance
- **Requirement:** Game must render at 60 FPS during gameplay.
- **Testable:** Monitor JavaFX render loop; measure frames per second.

**NFR-P02:** Memory Usage
- **Category:** Performance
- **Requirement:** Application must use <300 MB RAM during normal gameplay.
- **Testable:** Monitor JVM heap size during 10-minute game session.

## Supportability (S)
**NFR-S01:** Code Documentation
- **Category:** Supportability
- **Requirement:** All public classes and methods must have JavaDoc comments.
- **Testable:** Run JavaDoc generator; verify 100% documentation coverage.

**NFR-S02:** Error Logging
- **Category:** Supportability
- **Requirement:** Application must log all errors to console and file.
- **Testable:** Trigger errors; verify logs appear in console and log file.

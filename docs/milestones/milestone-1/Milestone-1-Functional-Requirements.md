# Functional Requirements Document
## Tetris Khronos – Milestone 1

**Project:** Tetris Game Implementation in Java with JavaFX following Maven structure for 2006ICT Object Oriented Software Development

**Team:** PG36 (formerly PG1)

**Course:** 2006ICT OOSD

**Milestone:** 1

**Intended Submission Date:** 21/08/2026

**Document Date:** 08/09/2026

---

## Overview

This document specifies the **functional requirements** for my submission for Milestone 1, Tetris Khronos. Each requirement follows the **Precondition-Event-Postcondition** pattern per course content and assignment requirements and is assigned a unique identifier (FR1–FR20) for traceability to design, implementation, and test artifacts. Requirements are modeled from those observable in the Milestone 1 demo video.

---

## Functional Requirements

| ID | Feature | Precondition | Event | Postcondition |
|----|---------|--------------|-------|---------------|
| **FR1** | Display splash window on application launch | Application process started; no window visible | Application initialises | Splash window displays centered on screen with group identity (PG36), course code (2006ICT), and project name (Tetris Khronos); window remains visible for 2–4 seconds then automatically transitions to main menu |
| **FR2** | Display main menu screen | Splash window duration expires | Transition triggered | Main menu appears with four buttons visible and functional: **Play**, **Configuration**, **High Scores**, and **Exit**; all buttons are clickable and responsive |
| **FR3** | Navigate to game play from main menu | User on main menu screen; no game in progress | User clicks **Play** button | Game screen initialises; 10×20 playfield loads; active tetromino appears at top-center; game loop begins execution |
| **FR4** | Initialise and display game field | Game screen requested to open | Screen loads and renders | Game board displays 10 columns × 20 rows with visible grid lines and borders; all cells initialised as empty; playfield occupies appropriate screen space and is fully responsive |
| **FR5** | Spawn initial tetromino at game start | Game board initialised; no active piece | Game loop first cycle executes | Random tetromino (I, O, T, S, Z, J, or L shape) appears at top-center of playfield (column 4–5 for 10-column grid); next tetromino preview generated and displayed; spawn event is logged |
| **FR6** | Move tetromino left | Tetromino active and not at left boundary; no collision with existing blocks | Player presses **LEFT arrow key** | Tetromino shifts exactly one column to the left; visual position updates immediately; no overlap occurs; movement is logged |
| **FR7** | Move tetromino right | Tetromino active and not at right boundary; no collision with existing blocks | Player presses **RIGHT arrow key** | Tetromino shifts exactly one column to the right; visual position updates immediately; no overlap occurs; movement is logged |
| **FR8** | Rotate tetromino clockwise | Tetromino active; rotation space available (no collision in rotated state) | Player presses **UP arrow key** | Tetromino rotates 90° clockwise around its center; if rotation would cause collision, rotation is cancelled and tetromino returns to pre-rotation state unchanged; rotation attempt is logged |
| **FR9** | Accelerate tetromino descent | Tetromino active; not at bottom boundary | Player presses **DOWN arrow key** | Tetromino immediately descends one row; normal descent rate resumes after key release; acceleration is logged |
| **FR10** | Continuous tetromino descent at fixed rate | Tetromino active; board not completely filled | Game tick timer triggers (configurable speed interval) | Tetromino automatically moves down one row; if bottom boundary or existing block collision occurs, descent halts and tetromino locks in place (transition to FR11 or FR12) |
| **FR11** | Lock tetromino when collision occurs | Tetromino active; collision detected with board bottom or existing blocks during descent | Collision occurs | Tetromino cells become permanent board cells; color and shape are preserved; tetromino is no longer controllable; locking event and collision details are logged; transition to FR12 (spawn next tetromino) |
| **FR12** | Detect full rows and erase | One or more rows completely filled with locked tetromino cells | Board evaluation cycle completes (after each lock) | System identifies all completely full rows; all blocks in identified rows are removed; rows above automatically fall downward to fill gaps; score is incremented for each row cleared; erasure event and row count are logged; if no rows are full, system proceeds directly to spawn next tetromino |
| **FR13** | Pause game with P key | Game in active play state (tetromino descending, input enabled) | Player presses **P key** | Game loop halts immediately; all movement and input processing stops; "PAUSED" message displays prominently on-screen in readable font; pause event is logged |
| **FR14** | Resume game from paused state | Game in paused state; "PAUSED" message visible on screen | Player presses **P key** again | Game loop resumes; "PAUSED" message disappears; tetromino descent continues from pre-pause position; input processing resumes; resume event is logged |
| **FR15** | Open configuration screen from main menu | User on main menu screen | User clicks **Configuration** button | Configuration screen appears displaying interactive controls: field size selector, level selector, toggles for **Music**, **Sound**, **AI Play**, and **Extended Mode**; all controls are functional; **Back** button is visible and accessible |
| **FR16** | Return to main menu from configuration | Configuration screen displayed with interactive controls | User clicks **Back** button | Configuration screen closes; main menu reappears with all four buttons (Play, Configuration, High Scores, Exit) visible and functional; user is returned to exact main menu state prior to navigation |
| **FR17** | Open high scores screen from main menu | User on main menu screen | User clicks **High Scores** button | High Scores screen appears displaying top 10 ranked scores in descending order; scores include placeholder/dummy data for Milestone 1; scores are ranked by value from highest to lowest; **Back** button is visible and accessible |
| **FR18** | Return to main menu from high scores | High Scores screen displayed with ranked scores | User clicks **Back** button | High Scores screen closes; main menu reappears with all four buttons (Play, Configuration, High Scores, Exit) visible and functional; user is returned to exact main menu state prior to navigation |
| **FR19** | Display exit confirmation dialog | User on main menu screen OR attempts to close application window | User clicks **Exit** button OR closes window via system close button | Confirmation dialog appears with clear message ("Are you sure you want to exit?") and two buttons: **Yes** and **No**; dialog is modal and blocks interaction with main menu until resolved |
| **FR20** | Exit application with confirmation | Exit confirmation dialog displayed | User clicks **Yes** button | Application terminates cleanly; all resources are released; Java Virtual Machine (JVM) closes completely; no background processes remain; exit event is logged |
| **FR21** | Cancel exit and return to menu | Exit confirmation dialog displayed | User clicks **No** button | Confirmation dialog closes; main menu reappears with all four buttons (Play, Configuration, High Scores, Exit) visible and functional; application remains running |

---

## Requirements Coverage Map

| Feature | FR Coverage | Description |
|---------|-------------|-------------|
| **Splash Window** | FR1 | Display on launch with group/course info for 2–4 seconds |
| **Main Menu Navigation** | FR2, FR3, FR15, FR16, FR18 | Central hub with four buttons (Play, Config, High Scores, Exit); navigate to all screens |
| **Game Field Display** | FR4 | 10×20 grid with visible borders and grid lines; responsive layout |
| **Tetromino Spawning** | FR5, FR11 | Initial spawn at top-center; subsequent spawns after lock |
| **Tetromino Movement – Left** | FR6 | LEFT arrow key moves piece one column left; collision detection |
| **Tetromino Movement – Right** | FR7 | RIGHT arrow key moves piece one column right; collision detection |
| **Tetromino Rotation** | FR8 | UP arrow key rotates 90° clockwise; cancel on collision |
| **Tetromino Descent – Accelerated** | FR9 | DOWN arrow key accelerates fall one row immediately |
| **Tetromino Descent – Continuous** | FR10 | Automatic descent at fixed configurable rate |
| **Tetromino Locking** | FR11 | Piece locks on collision with board or existing blocks |
| **Row Detection & Erasure** | FR12 | Full rows identified and removed; blocks above fall; score updated |
| **Pause Game** | FR13 | P key pauses; "PAUSED" message displays; input halts |
| **Resume Game** | FR14 | P key resumes from paused state; message disappears |
| **Configuration Screen** | FR15, FR16 | Access interactive controls; Back button returns to main menu |
| **High Scores Screen** | FR17, FR18 | Display top 10 ranked scores; Back button returns to main menu |
| **Exit Confirmation Dialog** | FR19, FR20, FR21 | Modal dialog with Yes/No options; Yes exits; No cancels |

---

## Design Notes

### Logging & Traceability

The following events should ideally be logged (with timestamp and detail level) for debugging and verification:

- Tetromino spawn (type, position, time)
- Tetromino movement (direction, collision detection result)
- Tetromino rotation (attempted, outcome)
- Tetromino lock (position, time)
- Row detection and erasure (row indices, count, time)
- Pause/Resume events (time, state)
- Navigation events (screen transitions, user actions)
- Application exit (clean or abnormal)

### Threading Model

- **Game Loop:** Executes on dedicated `GameLoopThread.java` instance
- **JavaFX Updates:** All visual updates dispatched via `Platform.runLater()` to ensure thread-safe GUI updates
- **Input Processing:** Keyboard input captured on JavaFX Application Thread and queued for game loop processing

### Collision Detection

- Collision detection is performed in FR6, FR7, FR8, FR10, and FR11
- Boundary collision (left, right, bottom) prevents invalid moves and locks pieces
- Block-to-block collision prevents overlap with existing settled pieces

### Testability & Demo Video Mapping

All 21 FRs are directly observable and testable in the Milestone 1 demo video:

- **Splash + Menu (FR1–FR2):** Shown at video start
- **Game Initialisation (FR3–FR5):** Shown when Play is selected
- **Movement & Controls (FR6–FR10):** Demonstrated with arrow key presses
- **Row Erasure (FR12):** Demonstrated by completing and clearing rows
- **Pause/Resume (FR13–FR14):** Demonstrated by pressing P key
- **Navigation (FR15–FR18):** All menu navigation sequences shown
- **Exit (FR19–FR21):** Exit button and confirmation dialog shown

---

## Deviations & Future Enhancements

- **Score Display:** Placeholder for Milestone 1; full scoring logic deferred to Milestone 2
- **Game Over Detection:** Not implemented in Milestone 1; full game over screen deferred
- **Extended Mode (Two-Player):** Configuration toggle included (FR15); full implementation deferred to Milestone 2
- **Difficulty Levels:** Configuration selector included (FR15); full difficulty scaling deferred to Milestone 2

---

## Approval

| Role | Name | Date |
|------|------|------|
| Lead Dev | [Morrigan W] | 08/09/2026 


---

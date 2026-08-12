# Functional Requirements Document
## Milestone 1 – Tetris-Khronos 

**Project:** Tetris Game Implementation in Java for 2006ICT Object Oriented Software Development

**Milestone:** 1 (21/08/2026)

**Date:** 02/08/2026  

**Team:** PG1

---

## Overview

This document specifies **15 functional requirements** for this implementation of Tetris, for 2006ICT OOSD Milestone 1 submission. Each requirement is intended to follow the **Precondition-Event-Postcondition** pattern, per the instructions outlined, and is mapped to features demonstrated in the Milestone 1 demo video.


---

## Functional Requirements

| ID | Description | Precondition | Event | Postcondition |
|----|-------------|--------------|-------|---------------|
| **FR1** | Display splash window on application launch | Application process started but no window visible | Application window opens | Splash window displays group identity, course code, and project name centered on screen for 2–4 seconds, then transitions to main screen |
| **FR2** | Display main menu screen after splash | Splash window displayed for required duration | Splash window duration expires | Main screen appears with Play, Configuration, High Scores, and Exit buttons visible and functional |
| **FR3** | Navigate to game play from main menu | User on main screen; game not yet started | User clicks Play button | Game screen initialises with 10×20 playfield, active tetromino appears at top, game loop begins |
| **FR4** | Initialise game board dimensions | Game play screen requested | Screen loads | Game board created with 10 columns and 20 rows; all cells initialised as empty |
| **FR5** | Spawn initial tetromino at game start | Game board initialised; no active tetromino | Game loop first cycle executes | Random tetromino (I, O, T, S, Z, J, or L) appears at top-center of playfield; next tetromino preview generated |
| **FR6** | Move tetromino left with arrow key | Tetromino active and not at left boundary; no collision detected | Player presses LEFT arrow key | Tetromino shifts one column left; no overlap with filled cells or boundaries |
| **FR7** | Move tetromino right with arrow key | Tetromino active and not at right boundary; no collision detected | Player presses RIGHT arrow key | Tetromino shifts one column right; no overlap with filled cells or boundaries |
| **FR8** | Rotate tetromino with UP arrow | Tetromino active; rotation space available | Player presses UP arrow key | Tetromino rotates 90° clockwise; if rotation invalid, tetromino returns to pre-rotation state (no collision) |
| **FR9** | Descend tetromino continuously at fixed rate | Tetromino active; board not full | Game tick timer triggers (configured game speed interval) | Tetromino moves down one row; if bottom boundary reached, tetromino locks in place |
| **FR10** | Lock tetromino when collision occurs | Tetromino active; collision detected with bottom or existing blocks | Collision occurs during descent or manual move | Tetromino cells become permanent board cells; color preserved; next tetromino spawned at top-center |
| **FR11** | Detect and erase full rows | One or more rows completely filled with cells | Board evaluation cycle completes | Full rows identified; all blocks in full rows removed; rows above fall down; score updated for each row cleared |
| **FR12** | Pause game with P key | Game in active play state | Player presses P key | Game loop halts; "PAUSED" message displays on-screen; input suspended until resume |
| **FR13** | Resume game from paused state | Game in paused state; "PAUSED" message visible | Player presses P key again | Game loop resumes; "PAUSED" message disappears; tetromino descent and input processing continue |
| **FR14** | Open configuration screen from main menu | User on main screen | User clicks Configuration button | Configuration screen appears with interactive controls for field size, level, music, sound, AI play, and extended mode; Back button present |
| **FR15** | Return to main menu from configuration | Configuration screen displayed | User clicks Back button | Configuration screen closes; main menu reappears with all buttons functional |

---


## Other Requirements Coverage

| Feature | FR Coverage |
|---|---|
| Splash window | FR1 |
| Main screen buttons | FR2, FR14 |
| Game play initialisation | FR3, FR4 |
| Tetromino spawning | FR5, FR10 |
| Arrow key controls | FR6, FR7, FR8 |
| Smooth descent | FR9 |
| Row erasure | FR11 |
| Pause/resume | FR12, FR13 |


## Notes

- All FRs should be testable and directly observable in the Milestone 1 demo video. (DOUBLE CHECK! (might have misremembered something?))
- Each FR should include a unique identifier (FR1–FR15) for traceability to design and test artifacts.
- Preconditions should establish the required system state; events should describe user actions or system triggers; postconditions should specify measurable outcomes.
- Configuration screen interactivity (FR14) will likely be expanded for milestone 2 to connect controls to game parameters via Observer pattern possibly.
- Row erasure detection (FR11) assumes basic collision detection is implemented in FR10.
- Double check everything!!!!!
- some details will definitely change/ deviate, map these and update this document
  
---
| Configuration screen | FR14, FR15 |

---

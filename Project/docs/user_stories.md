# User Stories - HiveNote

## Overview
This document contains user stories that describes the functionality of the **HiveNote**.
The app allows users to upload, view, delete, and join multiple DBs by conveniently using a Discord Bot.

---

## 1. User Management

### User Story 1.1 - Identification
**As a** user,
**I want** my uploaded notes to be associated with my userid
**so that** my contributions are valued by my peers.

**As a** user,
**I want** to associate with multiple DBs
**so that** I can share my notes among multiple groups.

**Acceptance Criteria:**
- Each note includes an 'userid' field.
- Each DB includes an array of 'userid's.

### User Story 1.2 - Admin Control
**As an** admin,
**I want** to oversee all notes in a DB
**So that** I can remove notes that are off-topic or move notes around to its proper categories.

**Acceptance Criteria:**
- User is an admin for the DB
- Notes are read, write, and edit only to the admin

---

## 2. Notes Management

### User Story 2.1 - Uploading Notes
**As a** user,
**I want** to upload notes to the DB
**so that** I can them store them for later use.

**Acceptance Criteria:**
- Discord Bot immediately reflects updates to the DB.

### User Story 2.2 - Deleting Notes
**As a** user,
**I want** to delete my notes
**so that** if there are errors in the notes I can remove them and reupload the note.

**Acceptance Criteria:**
- Discord Bot immediately reflects updates to the DB.

### User Story 2.3 - Retrieving Notes
**As a** user,
**I want** to retrieve notes based on tags
**so that** I can get relevant information that I need.

**Acceptance Criteria:**
- Submitted tags follows a RegEx to generalize multitude of different topics.

---

## 3. User Experience and AI Usage

### User Story 3.1 - Discord Bot Interaction
**As a** user,
**I want** to interact with a responsive Discord Bot
**so that** my notes get uploaded quickly and reliable.

**Acceptance Criteria:**
- Discord Bot works and is quickly responsive

### User Story 3.2 - AI Usage
**As a** user,
**I want** to use the AI
**so that** it can provide insight to my notes

**Acceptance Criteria:**
- LaTeX expressions form correctly.
- AI API is fast responsive.
- Output does not exceed Discord character limit.

---

## 4. Database Integration

### User Story 4.1 – Persist Data
**As a** user,
**I want** my notes to be saved in the DB,
**so that** they are not lost when I stop using the app.

**Acceptance Criteria:**
- Discord Bot connects to a MySQL DB.
- All operations are reflected in the database.

---

### User Story 4.2 – Data Consistency
**As a** user,
**I want** any note updates through the Discord Bot to immediately reflect in the database,
**so that** the displayed data is always current.

**Acceptance Criteria:**
- Discord Bot triggers accurate SQL operations.
- Notes reload after each operation to display the latest state.

---

## 5. Testing & Quality Assurance

### User Story 5.1 – Functional Tests
**As a** tester,
**I want** to verify that each feature works correctly
**so that** users have a bug-free experience.

**Acceptance Criteria:**
- Each function has a test case.
- Tests cover normal, edge, and failure conditions (e.g., empty input).

---

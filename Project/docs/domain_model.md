# HiveNote Database Schema

## Overview
This document defines the database schema for HiveNote, including entities, attributes, and relationships.  
The schema supports notes, courses, tags, attachments, and AI-generated outputs (summaries, insights, questions).

---

## Entities

### 1. ATTACHMENT
Stores uploaded files associated with notes.

| Attribute       | Type           | Description                                 |
|-----------------|----------------|---------------------------------------------|
| attachment_id   | NUMBER         | Unique identifier (auto-increment).         |
| note_id         | NUMBER         | Foreign key → `NOTE.NOTE_ID`.               |
| file_name       | VARCHAR2(255)  | Name of the uploaded file.                  |
| file_blob       | BLOB           | Binary file content.                        |

**Relationships:**
- Each `Attachment` belongs to **one Note**.
- One Note can have **many Attachments** (1-to-many).

---

### 2. COURSE
Stores course information.

| Attribute   | Type          | Description                              |
|-------------|---------------|------------------------------------------|
| course_id   | NUMBER        | Unique identifier (auto-increment).      |
| course_code | VARCHAR2(32)  | Short code for the course (e.g., CS101). |
| course_name | VARCHAR2(255) | Full name of the course.                 |

**Relationships:**
- One Course can have **many Notes**.
- One Course can have **many Tags**.

---

### 3. NOTE
Represents individual notes uploaded by users.

| Attribute    | Type           | Description                                |
|--------------|----------------|--------------------------------------------|
| note_id      | NUMBER         | Unique identifier (auto-increment).        |
| user_id      | VARCHAR2(255)  | ID of the user who uploaded the note.      |
| note_title   | VARCHAR2(200)  | Title of the note.                         |
| created_at   | TIMESTAMP(6)   | Timestamp when the note was created.       |
| updated_at   | TIMESTAMP(6)   | Timestamp when the note was last updated.  |
| note_summary | VARCHAR2(4000) | Optional AI-generated summary of the note. |
| course_id    | NUMBER         | Foreign key → `COURSE.COURSE_ID`.          |

**Relationships:**
- Each Note belongs to **one Course**.
- Each Note belongs to **one User**.
- Each Note can have **many Tags** through `NOTE_TAG_JUNCTION`.
- Each Note can have **many Attachments**.

---

### 4. TAG
Represents a label applied to notes for organization.

| Attribute | Type          | Description                         |
|-----------|---------------|-------------------------------------|
| tag_id    | NUMBER        | Unique identifier (auto-increment). |
| tag_name  | VARCHAR2(100) | Name of the tag.                    |
| course_id | NUMBER        | Foreign key → `COURSE.COURSE_ID`.   |

**Relationships:**
- One Tag belongs to **one Course**.
- One Tag can belong to **many Notes** via `NOTE_TAG_JUNCTION`.

---

### 5. NOTE_TAG_JUNCTION
Junction table representing many-to-many relationship between Notes and Tags.

| Attribute     | Type    | Description                     |
|---------------|---------|---------------------------------|
| note_id       | NUMBER  | Foreign key → `NOTE.NOTE_ID`.   |
| tag_id        | NUMBER  | Foreign key → `TAG.TAG_ID`.     |

**Relationships:**
- Connects Notes ↔ Tags for categorization.
- One Note can have multiple Tags, one Tag can belong to multiple Notes.

---

### Notes
- Primary keys are auto-increment numbers.
- Relationships are designed for querying notes by user, course, or tag.
- Schema is modular, allowing for easy expansion (e.g., AI outputs, additional metadata).  

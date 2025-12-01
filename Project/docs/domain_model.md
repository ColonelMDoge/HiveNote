# Domain Model - HiveNote

## Overview
This document defines the database schema, including entities, attributes, and relationships.  
The schema supports notes, tags, and optional AI-generated outputs (summaries, insights, questions).

---

## Entities

### 1. HiveNote_Note
Represents an individual note in the database.

| Attribute     | Type                  | Description                                         |
|---------------|-----------------------|-----------------------------------------------------|
| note_id       | String                | Unique identifier for the note.                     |
| userid        | String                | ID of the user who uploaded this note.              |
| title         | String                | Title of the note.                                  |
| upload_date   | DateTime              | Timestamp when the note was uploaded.               |
| document_file | Variable File         | Original file associated with the note.             |
| content       | Text                  | Plain-text or markdown version of the note content. |

**Relationships:**
- Each `Note` belongs to **one User** (many-to-one).
- Each `Note` can have **many Tags** (many-to-many via `NoteTags`).

---

### 2. HiveNote_Tag
Represents a label that can be applied to notes for categorization and retrieval.

| Attribute   | Type   | Description                                        |
|-------------|--------|----------------------------------------------------|
| tag_id      | String | Unique identifier for the tag.                     |
| name        | String | Name of the tag. Must be unique within a database. |

**Relationships:**
- One `Tag` can be associated with **many Notes** (many-to-many via `NoteTags`).

---

### 3. NoteTags (junction table)
Represents the many-to-many relationship between Notes and Tags.

| Attribute | Type    | Description                   |
|-----------|---------|-------------------------------|
| id        | Integer | Primary key (auto-increment). |
| note_id   | String  | Foreign key → `Note`.         |
| tag_id    | String  | Foreign key → `Tag`.          |

**Relationships:**
- Connects Notes ↔ Tags for categorization.

---

### 7. AI-Generated Outputs (Optional)
Stores summaries, insights, or questions generated from notes.

#### 7a. Summaries

| Attribute    | Type     | Description                               |
|--------------|----------|-------------------------------------------|
| summary_id   | String   | Unique identifier.                        |
| note_id      | String   | Foreign key → `Note`.                     |
| summary_text | Text     | Generated summary.                        |
| created_at   | DateTime | Timestamp when the summary was generated. |

#### 7b. Insights

| Attribute    | Type     | Description                          |
|--------------|----------|--------------------------------------|
| insight_id   | String   | Unique identifier.                   |
| note_id      | String   | Foreign key → `Note`.                |
| insight_text | Text     | AI-generated insights from the note. |
| created_at   | DateTime | Timestamp of generation.             |

#### 7c. Questions

| Attribute     | Type     | Description                  |
|---------------|----------|------------------------------|
| question_id   | String   | Unique identifier.           |
| note_id       | String   | Foreign key → `Note`.        |
| question_text | Text     | AI-generated study question. |
| question_type | String   | e.g., MCQ, short answer.     |
| created_at    | DateTime | Timestamp of generation.     |

---

### Notes
- Tags may not always be consistent; consider RegEx.  
- Relationships ensure easy querying: e.g., all notes for a user, notes with a certain tag, all users in a database.  
- The schema is modular, allowing for additional tables or AI outputs as the app evolves.

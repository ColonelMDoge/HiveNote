# Domain Model - HiveNote

## Overview
This document defines the database schema, including entities, attributes, and relationships.  
The schema supports multiple users, databases, notes, tags, and optional AI-generated outputs (summaries, insights, questions).

---

## Entities

### 1. User
Represents a person using HiveNote.

| Attribute | Type   | Description |
|-----------|--------|-------------|
| userid | String | Unique identifier for the user (Discord ID). |
| username | String | Optional: cached Discord username. |
| isAdmin | Boolean | Determines whether the user is an admin in their respective databases. |
| associated_dbs | Integer Array | Array of database IDs the user is associated with. |
| joined_at | DateTime | Timestamp when the user joined the system. |

**Relationships:**
- One `User` can have **many Notes** (one-to-many).  
- One `User` can be associated with **many Databases** (many-to-many via `DatabaseUsers`).

---

### 2. Note
Represents an individual note in the database.

| Attribute | Type | Description |
|-----------|------|-------------|
| note_id | String | Unique identifier for the note. |
| userid | String | ID of the user who uploaded this note. |
| database_id | String | ID of the database this note belongs to. |
| title | String | Title of the note. |
| upload_date | DateTime | Timestamp when the note was uploaded. |
| document_files | Variable File Array | Original files or attachments associated with the note. |
| content | Text | Plain-text or markdown version of the note content. |
| tags | Variable String Array | Array of tags associated with the note. |

**Relationships:**
- Each `Note` belongs to **one User** (many-to-one).  
- Each `Note` belongs to **one Database**.  
- Each `Note` can have **many Tags** (many-to-many via `NoteTags`).

---

### 3. Database
Represents a collection of notes that users can join.

| Attribute | Type | Description |
|-----------|------|-------------|
| database_id | String | Unique identifier for the database. |
| name | String | Optional human-readable name. |
| created_at | DateTime | Timestamp when the database was created. |
| owner_userid | String | User ID of the admin who created the database. |

**Relationships:**
- One `Database` can have **many Users** (many-to-many via `DatabaseUsers`).  
- One `Database` can contain **many Notes** (one-to-many).  
- One `Database` can have **many Tags** (one-to-many).

---

### 4. Tag
Represents a label that can be applied to notes for categorization and retrieval.

| Attribute | Type | Description |
|-----------|------|-------------|
| tag_id | String | Unique identifier for the tag. |
| database_id | String | ID of the database where the tag exists. |
| name | String | Name of the tag. Must be unique within a database. |

**Relationships:**
- One `Tag` can be associated with **many Notes** (many-to-many via `NoteTags`).  
- One `Tag` belongs to **one Database**.

---

### 5. DatabaseUsers (junction table)
Represents the many-to-many relationship between Users and Databases.

| Attribute | Type | Description |
|-----------|------|-------------|
| id | Integer | Primary key (auto-increment). |
| database_id | String | Foreign key → `Database`. |
| user_id | String | Foreign key → `User`. |
| is_admin | Boolean | Whether the user has admin privileges in this database. |
| joined_at | DateTime | Timestamp when the user joined the database. |

**Relationships:**
- Connects Users ↔ Databases with admin roles.

---

### 6. NoteTags (junction table)
Represents the many-to-many relationship between Notes and Tags.

| Attribute | Type | Description |
|-----------|------|-------------|
| id | Integer | Primary key (auto-increment). |
| note_id | String | Foreign key → `Note`. |
| tag_id | String | Foreign key → `Tag`. |

**Relationships:**
- Connects Notes ↔ Tags for categorization.

---

### 7. AI-Generated Outputs (Optional)
Stores summaries, insights, or questions generated from notes.

#### 7a. Summaries

| Attribute | Type | Description |
|-----------|------|-------------|
| summary_id | String | Unique identifier. |
| note_id | String | Foreign key → `Note`. |
| summary_text | Text | Generated summary. |
| created_at | DateTime | Timestamp when the summary was generated. |

#### 7b. Insights

| Attribute | Type | Description |
|-----------|------|-------------|
| insight_id | String | Unique identifier. |
| note_id | String | Foreign key → `Note`. |
| insight_text | Text | AI-generated insights from the note. |
| created_at | DateTime | Timestamp of generation. |

#### 7c. Questions

| Attribute | Type | Description |
|-----------|------|-------------|
| question_id | String | Unique identifier. |
| note_id | String | Foreign key → `Note`. |
| question_text | Text | AI-generated study question. |
| question_type | String | e.g., MCQ, short answer. |
| created_at | DateTime | Timestamp of generation. |

---

### Notes
- Tags may not always be consistent; consider RegEx.  
- Relationships ensure easy querying: e.g., all notes for a user, notes with a certain tag, all users in a database.  
- The schema is modular, allowing for additional tables or AI outputs as the app evolves.

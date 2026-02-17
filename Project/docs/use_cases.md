# Use Cases – HiveNote

## Overview
This document describes the main use cases for the HiveNote, including user actions, system behavior, and expected outcomes.

### Use Case 1 — Upload a Note

**Primary Actor:** User  
**Goal:** Upload a note or file to a database.

**Preconditions:**
- User must be a member of the database.

**Trigger:**  
`/upload_note <provided_course_code>`

**Main Success Scenario:**
1. User uploads a note or file.
2. The System stores the note and metadata.
3. System extracts text content.
4. System stores tags.
5. System confirms the upload.

**Postconditions:**
- Note is stored and linked to the user and database.

---

### Use Case 2 — Updating a Note

**Primary Actor:** User  
**Goal:** Updates a note.

**Preconditions:**
- Note exists in the database.

**Trigger:**  
`/modify_note <noteID>`

**Main Success Scenario:**
1. User requests note modification
2. System searches within the database.
3. The System returns matching note to modify.
4. User modifies note and submits.
5. Database reflects changes.
6. Bot confirms the change.

**Postconditions:**
- Discord bot sends a message.

---

### Use Case 3 — Retrieve Notes by Course Code or Tag

**Primary Actor:** User  
**Goal:** Search for notes using tags.

**Preconditions:**
- Tagged notes exist in the database.

**Trigger:**  
`/retrieve_ids_by_filter <course_code> <tag>`

**Main Success Scenario:**
1. User requests notes by course code or tag.
2. System searches within the database.
3. The System returns matching notes.

**Postconditions:**
- User receives a list of tagged notes.

---

### Use Case 4 — Generate AI Summary for a Note

**Primary Actor:** User  
**Goal:** Produce a short AI-generated summary.

**Preconditions:**
- Note must exist.

**Trigger:**  
`/generate_summary_by_id <noteID>`

**Main Success Scenario:**
1. User requests a summary.
2. System retrieves note content.
3. The System sends content to the AI.
4. AI generates a summary.
5. System stores summary.
6. The System sends summary to the user.

**Postconditions:**
- Summary saved in a database for future retrieval.

---

### Use Case 5 — Generate Study Questions

**Primary Actor:** User  
**Goal:** Receive AI-generated study questions.

**Trigger:**  
`/generate_questions_by_id <noteID>`

**Main Success Scenario:**
1. User requests question generation.
2. System retrieves note content.
3. AI produces questions.
4. System stores generated questions.
5. The System sends questions to the user.

---

### Use Case 6 — Add or Remove Tags

**Primary Actor:** User  
**Goal:** Add or remove tags from a note.

**Triggers:**  
- `/create_tag <course_code> <tag>`  
- `/delete_tag <course_code> <tag>`

**Main Success Scenario:**
1. User requests tag modification.
2. System checks note access/ownership.
3. System updates tag associations.
4. The System confirms the change.

**Postconditions:**
- Note tags updated successfully.

---
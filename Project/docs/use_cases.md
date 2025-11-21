# Use Cases – HiveNote

## Overview
This document describes the main use cases for the HiveNote, including user actions, system behavior, and expected outcomes.

---

### Use Case 1 — Create a New Note Database

**Primary Actor:** User  
**Goal:** Create a unique shared database for storing notes.

**Preconditions:**
- User is authenticated via Discord.

**Trigger:**  
`/create_db`

**Main Success Scenario:**
1. User requests to create a new database.
2. System generates a unique database ID.
3. System stores database metadata.
4. System assigns the requesting user as admin.
5. System returns the database ID to the user.

**Postconditions:**
- A new database exists.
- The user is the admin of that database.

---

### Use Case 2 — Join an Existing Database

**Primary Actor:** User  
**Goal:** Join a database created by another user.

**Preconditions:**
- Database ID must exist.

**Trigger:**  
`/join_db <databaseID>`

**Main Success Scenario:**
1. User submits the database ID.
2. System verifies the database exists.
3. System adds the user to the database membership list.
4. System confirms the user has joined.

**Postconditions:**
- User gains access to the database’s notes.

---

### Use Case 3 — Upload a Note

**Primary Actor:** User  
**Goal:** Upload a note or file to a database.

**Preconditions:**
- User must be a member of the database.

**Trigger:**  
`/upload_note <databaseID> <file/title>`

**Main Success Scenario:**
1. User selects a database.
2. User uploads a note or file.
3. System stores the note and metadata.
4. System extracts text content.
5. System stores tags (manual or auto).
6. System confirms the upload.

**Postconditions:**
- Note is stored and linked to the user and database.

---

### Use Case 4 — Retrieve Notes by Tag

**Primary Actor:** User  
**Goal:** Search for notes using tags.

**Preconditions:**
- Tagged notes exist in the database.

**Trigger:**  
`/search_tag <tag>`

**Main Success Scenario:**
1. User requests notes by tag.
2. System searches within the user’s database.
3. System returns matching notes.

**Postconditions:**
- User receives a list of tagged notes.

---

### Use Case 5 — Generate AI Summary for a Note

**Primary Actor:** User  
**Goal:** Produce a short AI-generated summary.

**Preconditions:**
- Note must exist.

**Trigger:**  
`/summarize <noteID>`

**Main Success Scenario:**
1. User requests a summary.
2. System retrieves note content.
3. System sends content to the AI.
4. AI generates a summary.
5. System stores summary.
6. System sends summary to the user.

**Postconditions:**
- Summary saved in database for future retrieval.

---

### Use Case 6 — Generate Study Questions

**Primary Actor:** User  
**Goal:** Receive AI-generated study questions.

**Trigger:**  
`/questions <noteID>`

**Main Success Scenario:**
1. User requests question generation.
2. System retrieves note content.
3. AI produces questions.
4. System stores generated questions.
5. System sends questions to the user.

**Postconditions:**
- Questions saved in database.

---

### Use Case 7 — Add or Modify Tags

**Primary Actor:** User  
**Goal:** Add or remove tags from a note.

**Triggers:**  
- `/add_tag <noteID> <tag>`  
- `/remove_tag <noteID> <tag>`

**Main Success Scenario:**
1. User requests tag modification.
2. System checks note access/ownership.
3. System updates tag associations.
4. System confirms the change.

**Postconditions:**
- Note tags updated successfully.

---

### Use Case 8 — Admin Removes a User

**Primary Actor:** Database Admin  
**Goal:** Remove a user from the database.

**Trigger:**  
`/remove_user <databaseID> <userid>`

**Main Success Scenario:**
1. Admin requests user removal.
2. System verifies admin permissions.
3. System removes the user from the database.
4. System confirms the removal.

**Postconditions:**
- User no longer has database access.


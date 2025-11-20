# Requirements – HiveMind Discord Bot

## 1. Overview
The HiveMind Discord Bot allows users to upload their notes to a centralized database that automatically organizes them using tags.  
Users can then retrieve notes based on tags or filters, and use AI to generate summaries, study questions, and insights.

---

## 2. Functional Requirements

### 2.1 createDB()
**Description:**  
Creates a unique database instance for users to join.

**Inputs:**  
- N/A

**Process:**  
- Program generates a unique database ID not associated with any existing databases.

**Outputs:**  
- `databaseID`  
- Confirmation message

---

### 2.2 addUserToDB()
**Description:**  
Adds a new user to the specified database.

**Inputs:**  
- `userid` (string)  
- `databaseID` (string)  
- `isAdmin` (boolean)

**Process:**  
- Verify the database exists  
- Check if the user is already a member  
- Add the user with correct role (admin or non-admin)

**Outputs:**  
- Confirmation message  
- Updated user list

---

### 2.3 uploadNotes()
**Description:**  
Allows a user to upload notes and tag them for organization and retrieval.

**Inputs:**  
- `userid` (string)  
- `databaseID` (string)  
- `noteContent` (string or file)  
- `tags` (string list)

**Process:**  
- Validate user membership  
- Store note content in database  
- Associate tags with note  
- Timestamp the entry
- Add author's userid

**Outputs:**  
- `noteID`  
- Confirmation message

---

### 2.4 retrieveNotes()
**Description:**  
Fetches notes that match user-defined filters such as tags or keywords.

**Inputs:**  
- `databaseID` (string)  
- `filters` (tags, date, keywords, userID, etc.)

**Process:**  
- Validate access  
- Query notes table based on filters  
- Return matching notes in sorted order (relevance or time)

**Outputs:**  
- List of matching notes with metadata

---

### 2.5 generateSummary()
**Description:**  
Uses AI to generate a concise summary based on selected notes.

**Inputs:**  
- `noteID` or list of note IDs  
- `summaryLength` (optional)

**Process:**  
- Retrieve note content  
- Send to AI summarization module  
- Optionally store generated summary

**Outputs:**  
- Summary text

---

### 2.6 generateQuestions()
**Description:**  
Creates study questions from provided notes using an AI model.

**Inputs:**  
- `noteID` or list of note IDs  
- `questionType` (optional)

**Process:**  
- Retrieve content  
- Run question-generation function  
- Format for Discord output using LaTeX

**Outputs:**  
- List of questions

---

### 2.7 generateInsights()
**Description:**  
Produces conceptual insights or themes extracted from notes.

**Inputs:**  
- `noteID` or list of note IDs

**Process:**  
- Retrieve notes  
- Analyze with AI for themes or patterns  
- Compile insights

**Outputs:**  
- Insight report

---

### 2.8 createTag()
**Description:**  
Allows admins or users to add new tags to the database.

**Inputs:**  
- `databaseID` (string)  
- `tagName` (string)

**Process:**  
- Validate database  
- Check for duplicates  
- Insert new tag

**Outputs:**  
- Confirmation message  
- Updated tag list

---

### 2.9 removeUserFromDB()
**Description:**  
Removes a user from a database.

**Inputs:**  
- `userid`  
- `databaseID`  
- `deleteNotes` (boolean)

**Process:**  
- Verify admin privileges  
- Remove user  
- If requested, delete or orphan their notes

**Outputs:**  
- Confirmation message

---

### 2.10 deleteMyNotes()
**Description:**
Deletes notes that are owned by the calling user

**Inputs:**
- `userID`
- list<string> `noteIDs`
- `databaseID`

**Process:**
- Verify that respective notes are owned by the user
- Verify that user is in the DB
- Delete the provided notes

**Outputs:**
- Confirmation message

---

### 2.11 deleteDB()
**Description:**  
Deletes an entire database instance.

**Inputs:**  
- `databaseID`  
- `adminID`

**Process:**  
- Verify admin permissions  
- Delete database, users, notes, and tags  
- Remove database from registry

**Outputs:**  
- Confirmation message

---

## 3. Non-Functional Requirements

### 3.1 Performance
- Bot must respond to user commands within **2 seconds** for standard operations.  
- AI processing (summaries, insights) should return results within **10 seconds**.

### 3.2 Reliability
- Data must be saved with 99% reliability; no unintentional data loss.  
- Database IDs must be globally unique.

### 3.3 Scalability
- System should support **thousands** of notes and **hundreds** of users per database.

### 3.4 Security
- Only verified users may access a database.  
- Only admins may delete databases or users.

### 3.5 Maintainability
- Code must follow modular structure: DB operations, AI operations, and Discord command handlers.

---

## 4. System Requirements

### 4.1 Software
- Java 17+
- Maven for dependency management.
- MySQL.
- JaCoco for logging.
- OCI API for DB connection.
- Java Discord API.
- OpenAI or related GPT.
- LaTeX renderer.

---

## 5. Testing Requirements

### 5.1 Test Cases
- Unit tests for the required functions.
- Documented in test_plan.md.
- Test database isolation per user.
- Test edge cases for non-existent users.

---

# HiveMind Discord Bot Requirements

---

## 1. Overview
The **HiveMind Discord Bot** allows users to upload their notes to a centralized database that automatically organizes them using tags. Users can retrieve notes based on tags or filters, and use AI to generate summaries, study questions, and insights.

---

## 2. Commands and Functional Requirements

### **/ask**
**Description:** Prompt the bot with a question.  
**Options:**
- `asked_prompt` (STRING, required): The prompt you want to ask.  
  **Outputs:**
- AI-generated response based on the prompt.

---

### **/help**
**Description:** Get a list of all available commands.  
**Options:** None  
**Outputs:**
- List of commands and their brief descriptions.

---

### **/retrieve_course_codes**
**Description:** Retrieve a list of all course codes in the database.  
**Options:** None  
**Outputs:**
- List of course codes.

---

### **/retrieve_tags_by_course**
**Description:** Retrieve a list of all tags associated with a specific course.  
**Options:**
- `provided_course` (STRING, required): The course code to query tags for.  
  **Outputs:**
- List of tags related to the provided course.

---

### **/create_tag**
**Description:** Create a new tag for a course.  
**Options:**
- `provided_course` (STRING, required): Course code the tag belongs to.
- `created_tag` (STRING, required): Name of the tag to create.  
  **Outputs:**
- Confirmation message.
- Updated tag list.

---

### **/delete_tag**
**Description:** Delete a tag from a course.  
**Options:**
- `provided_course` (STRING, required): Course code the tag belongs to.
- `deleted_tag` (STRING, required): Name of the tag to delete.  
  **Outputs:**
- Confirmation message.
- Updated tag list.

---

### **/create_course**
**Description:** Create a new course code in the database.  
**Options:**
- `created_course` (STRING, required): Course code to create.
- `provided_name` (STRING, required): Name of the course.  
  **Outputs:**
- Confirmation message.
- Updated course list.

---

### **/delete_course**
**Description:** Delete a course code from the database.  
**Options:**
- `deleted_course` (STRING, required): Course code to delete.  
  **Outputs:**
- Confirmation message.
- Updated course list.

---

### **/upload_note**
**Description:** Upload a note to the database.  
**Options:**
- `asked_course` (STRING, required): Course code associated with the note.  
  **Outputs:**
- `noteID` of the uploaded note.
- Confirmation message.

---

### **/retrieve_note_by_id**
**Description:** Retrieve a note based on its database ID.  
**Options:**
- `provided_id` (INTEGER, required): The ID of the note to retrieve.  
  **Outputs:**
- Note content and metadata.

---

### **/retrieve_ids_by_filter**
**Description:** Retrieve a list of note IDs based on course and optional tag filters.  
**Options:**
- `provided_course` (STRING, required): Course code to filter notes.
- `provided_tag` (STRING, optional): Tag to filter notes.  
  **Outputs:**
- List of note IDs matching the filter.

---

### **/modify_note**
**Description:** Modify a note based on its database ID.  
**Options:**
- `provided_id` (INTEGER, required): ID of the note to modify.  
  **Outputs:**
- Confirmation message.

---

### **/generate_summary_by_id**
**Description:** Generate an AI summary of a note based on its database ID.  
**Options:**
- `provided_id` (INTEGER, required): ID of the note to summarize.
- `provided_prompt` (STRING, optional): Optional custom prompt (default is standard summary).  
  **Outputs:**
- Generated summary text.

---

## 3. Non-Functional Requirements

### **3.1 Performance**
- Bot must respond to user commands within **5 seconds** for standard operations.
- AI processing (summaries, insights) should return results within **10 seconds**.

### **3.2 Reliability**
- Data must be saved with **99% reliability**; no unintentional data loss.
- Database IDs must be **globally unique**.

### **3.3 Scalability**
- The system should support **hundreds of notes** and **tens of users** per database.

### **3.4 Security**
- Only verified users may access a database.
- Only admins may delete databases through Oracle Cloud.

### **3.5 Maintainability**
- Code must follow a modular structure: **DB operations**, **AI operations**, and **Discord command handlers**.

---

## 4. System Requirements

### **4.1 Software**
- Java 17+
- Maven for dependency management
- SQL database
- JaCoCo for code coverage
- Ojdbc for OCI DB connection
- Java Discord API (JDA)
- Google Gemini API
- jLaTeXMath for LaTeX rendering

---

## 5. Testing Requirements

### **5.1 Test Cases**
- Unit tests for all commands and functional requirements.
- Documented in `test_plan.md`.
- Test note modifications.
- Test edge cases for large files or character limits.
- Test connectivity and error handling.  

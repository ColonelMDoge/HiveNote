# HiveNote Project Charter

### 1. Project Title
**HiveNote Discord Bot**

---

### 2. Project Purpose
The purpose of this project is to store both analog and digital notes, organizing them based on categories and tags.
Multiple people will be able to upload and retrieve their notes to and from a single database.
Google Gemini will be used to help summarize, provide insight, and generate practice problems from the provided notes.
All outputs of the AI will be formatted into LaTeX when applicable and sent through the Discord Bot.

---

### 3. Project Objectives
- Implement **upload**, **delete**, and **Google Gemini** functionality.
- Implement the core features: `uploadNotes()`, `deleteNotes()`, `retrieveNotes()`, `generateSummary()`, etc. 
- Integrate **logging** with **code coverage** to ensure thorough testing.

---

### 4. Project Scope

#### In Scope
- SQL database integration.
- Java Discord Application integration.
- Gemini API integration.
- User authentication system.
- LaTeX rendering.
- Rigorous testing and logging.

#### Out of Scope
- Mobile access.
- Web application development.

---

### 5. Project Deliverables
- `charter.md` - Project Charter
- Functional requirements and documentation
- Deployment of SQL table
- AI integration 

---

### 6. Stakeholders

| Stakeholder              | Role / Interest                    | Responsibilities / Expectations          |
|--------------------------|------------------------------------|------------------------------------------|
| **Development Team**     | Implementation of core             | Develop, test, document, and deployment. |
| **Testers / QA Members** | Ensure quality and coverage of app | Create reports of various tests.         |
| **End Users**            | App users                          | Expect secure and reliable app.          |

---

### 7. Roles and Responsibilities

| Role                 | Description                                | Assigned To  |
|----------------------|--------------------------------------------|--------------|
| **Product Owner**    | Oversees and defines the vision of project | ColonelMDoge |
| **Development Team** | Implements the features                    | ColonelMDoge |
| **Testers**          | Tests the app for any issues               | QA Team      |

---

### 8. Tools, Technologies, and Dependencies
- **Language:** Java
- **Database:** SQL on Oracle Cloud Infrastructure DB 
- **Version Control:** GitHub
- **Testing:** `java.util.Logging`
- **Deployment:** Oracle Cloud Infrastructure Instance
- **Documentation:** Markdown (`/docs` folder)
- **Coverage Reporting:** HTML coverage reports using JaCoCo

---

### 9. Constraints & Assumptions
- Data must be stored securely.
- Assume one or more admins to manage either redundant or troll uploads.
- Code coverage target of **>=80%** for core functions.
- System where users are associated with individual tables to represent a class.
- Assumes text output, if exceeding the character limit, will be sent in blocks.
- All private keys and environment files will be excluded from Git repository.

---

### 10. Success Criteria
- Discord application is live and functional.
- Authentication works.
- Desired code coverage is achieved.
- Documentation is complete.
- Database and respective core functions work.

---

### 11. Budget
- There are currently no expected costs for this project.
- As userbase scales, funding will be required to support database storage and Gemini API requests.

---

### 12. Approvals
- Project Charter Approval Date: 11/19/2025
- Project Manager Acknowledgement: ColonelMDoge

---

### 13. Revision History

| Change Made By | Date Change Made | Details of Change                                     | Approved By   | Approval Date |
|----------------|------------------|-------------------------------------------------------|---------------|---------------|
| ColonelMDoge   | 11/20/2025       | Renamed functions and formatted strings correctly     | PNC Logistics | 11/20/2025    |
| ColonelMDoge   | 11/27/2025       | Updated potential budget changes and SQL language use | PNC Logistics | 11/27/2025    |
| ColonelMDoge   | 11/30/2025       | Removed some core functions and edited clarity        | PNC Logistics | 11/30/2025    |
| ColonelMDoge   | 02/13/2025       | Full core implementation of application               | PNC Logistics | 02/13/2026    |
---

**File:** `Project/docs/charter.md`
**Version:** 1.0
**Date:** 2025/11/19
**Team:** PNC Logistics

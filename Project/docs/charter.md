# HiveNote Project Charter

### 1. Project Title
**HiveNote Discord Bot**

---

### 2. Project Purpose
The purpose of this project is to store both analog and digital notes, organizing them based on categories and tags.
Multiple people will be able to upload and retrieve their notes to and from a single database.
A HiveMind AI tool will be used to help summarize, provide insight, and generate practice problems from the provided notes plus internet searches.
All outputs of the AI will be formatted into LaTeX and sent through the Discord Bot.

---

### 3. Project Objectives
- Implement **user registration**, **upload**, **delete**, and **HiveMind AI** functionality.
- Implement the core features: 'attachToDB()', 'upload()', 'delete()', 'retrieveBasedOnTags()', "summarize()' 
- Integrate **logging** with **code coverage** to ensure thorough testing.

---

### 4. Project Scope

#### In Scope
- MySQL database integration.
- Java Discord Application integration.
- OpenAI or related GPT API integration.
- User authentication system.
- LaTeX rendering.
- Rigorous testing and logging.

#### Out of Scope
- Mobile access.
- Web application development.

---

### 5. Project Deliverables
- 'charter.md' - Project Charter
- Functional requirements and documentation
- Deployment of SQL table
- AI integration 

---

### 6. Stakeholders

| Stakeholder | Role / Interest | Responsibilities / Expectations |
|-------------|-----------------|---------------------------------|
| **Development Team** | Implementation of core | Develop, test, document, and deployment.|
| **Testers / QA Members** | Ensure quality and coverage of app | Create reports of various tests.|
| **End Users** | App users | Expect secure and reliable app.|

---

### 7. Roles and Responsibilities

| Role | Description | Assigned To |
| **Product Owner** | Oversees and defines the vision of project | ColonelMDoge|
| **Development Team** | Implements the features | ColonelMDoge |
| **Testers** | Tests the app for any issues | QA Team |

---

### 8. Tools and Technologies
- **Language:** Java
- **Database:** MySQL on 
- **Version Control:** GitHub
- **Testing:** 'java.util.Logging'
- **Deployment:** Oracle Cloud Infrastructure Instance
- **Documentation:** Markdown ('/docs' folder)
- **Coverage Reporting:** HTML coverage reports using JaCoCo

---

### 9. Constraints & Assumptions
- Data must be stored securely.
- Assumes one or more admins to manage either redundant or troll uploads.
- Code coverage target of **>=80%** for core functions.
- System where users are associated to individuale tables to represent a class.
- Assumes text output will not exceed Discord maximum message length
- All private keys and environment files will be excluded from Git repository.

---

### 10. Success Criteraira
- Discord application is live and functional.
- Authentication works.
- Desired code coverage is achieved.
- Documentation is complete.
- Database and respective core functions work.

---

**File:** 'Project/docs/charter.md'
**Version:** 1.0
**Date:** 2025/11/19
**Team:** PNC Logistics

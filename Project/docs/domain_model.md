# Domain Model

## **Entities**

### 1. User
Represents a person using the HiveNote

| Attribute | Type   | Description |
|-----------|--------|-------------|
| userid | String | Identifier for the user. |
| isAdmin | Boolean | Determines whether the user is an admin of respective DBs. |
| associated_dbs | Integer Array | Array of note DBs the user is associated to. |

**Relationships:**
- One 'User' can have **many notes** (one-to-many relationship)
- One 'User' can have **many associated_dbs** (one-to-many relationship)

---

### 2. Note
Represents an individual note in the database

| Attribute | Type | Description |
|-----------|------|-------------|
| userid | String | ID of the user who uploaded this note. |
| title | String | Title of the note. |
| upload_date | DateTime | Timestamp when the note was uploaded. |
| document_files | Variable File Array | The original files of the notes. |
| tags | Variable String Array | Array of related tags to the note. |

**Relationships:**
- Each 'Note' belongs to **one User**.
- Each 'Note' could belong to **many dbs**.

---

### **Notes**
- Assumes tags are not lexically consistent. May require RegEx or AI insight. 
- Additional tables could be added as app is in development.

# EXTORT WITH ME

Template repository for java-explore-with-me project.

## Entity Relationship

```mermaid
erDiagram
    USERS ||--o{ EVENTS: initiates
    USERS ||--o{ REQUESTS: creates
    EVENTS ||--o{ REQUESTS: includes
    CATEGORIES ||--o{ EVENTS: includes
    USERS {
        bigint id PK
        string username
        string email
    }
    CATEGORIES {
        bigint id PK
        string name
    }
    EVENTS {
        bigint id PK
        bigint category_id FK
        bigint user_id FK
    }
    REQUESTS {
        bigint id PK
        string status
        bigint requester FK
    }
```
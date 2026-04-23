# DocuSpace — AI-powered Document Workspace SaaS

DocuSpace is a Notion-like document workspace platform designed with a distributed, async-first architecture.  
It supports document management, versioning, search, and AI-powered workflows.

---

## 🚀 Architecture Overview
Client (React) -> API Service (Spring Boot - ECS) -> Amazon SQS (Queue) -> Worker Service (Spring Boot - ECS) -> PostgreSQL Database

---

## 🧠 Key Concepts

- **Asynchronous Processing** — API delegates heavy tasks to background workers
- **Producer-Consumer Architecture** — API produces jobs, workers consume via SQS
- **Fault Tolerance** — Retry handling and Dead Letter Queue (DLQ)
- **Idempotent Processing** — Ensures safe retries without duplicate execution
- **Multi-tenant Design** — Data isolation per workspace

---

## 🛠️ Tech Stack

- **Backend:** Spring Boot, Java  
- **Database:** PostgreSQL  
- **Cloud:** AWS ECS (Fargate), SQS, ALB, IAM  
- **Other:** Docker, REST APIs  

---

## ⚙️ Core Features

- Document CRUD with versioning  
- Asynchronous AI job processing  
- Global search API  
- Background worker processing using SQS  
- Retry handling with DLQ support  

---

## 🔄 Job Processing Flow
Client → API → SQS → Worker → DB
↓
Retry
↓
DLQ


---

## 🤖 AI Capabilities (In Progress)

- Document chunking  
- Embeddings generation  
- Semantic search  
- Retrieval-Augmented Generation (RAG)  
- Agentic AI workflows (tool-based execution)

---

## 🌐 Frontend Repository

Frontend code is available here:  
👉 https://github.com/nikhilpeshwe-rgb/docuspace-frontend

---

## 🚀 Running Locally

### Backend

```bash
docker build -t docuspace-backend .
docker run -p 8080:8080 docuspace-backend
```

📌 Future Enhancements
Semantic search (embeddings)
Agent-based workflows
S3 document storage
Observability dashboards
RBAC and audit logs


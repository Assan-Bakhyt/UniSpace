UniSpace — Research-Oriented University Information System
Project Overview

UniSpace is a console-based information system that models the core processes of a modern research university. The project is built using strong Object-Oriented Programming (OOP) principles, layered architecture, and design patterns to simulate real-world academic and research workflows.

The system integrates user management, academic operations, and research tracking, with a focus on clean architecture, scalability, and maintainability.

Team Members
Assan Bakhyt - 24B031652
Assimzhanov Radmir - 24B031653
Yussupzhanov Nizhat - 24B030323
Akhali Kasymzhomart - 24B031661

Project Objectives
Design a complete system architecture using Use Case and UML Class Diagrams
Implement role-based authentication with intuitive console navigation
Manage academic workflows: course registration, grading, and transcripts
Track research activities with strict validation (e.g., h-index, supervisor rules)
Apply multiple design patterns and custom exception handling
Ensure reliable data persistence via serialization
Provide synchronized documentation, reverse-engineered diagrams, and presentation
Technologies
Java 17/21
Object-Oriented Design & Layered Architecture
Java Collections & Serialization API
IntelliJ IDEA, Git, GitHub, StarUML
Javadoc & PDF Documentation
Key Features
🔐 Authentication & Roles
Secure login system with role-based access
Dedicated console menus for: Admin, Teacher, Student, Manager, Researcher
🎓 Academic Management
Course creation and registration (credit limit ≤ 21)
Multi-instructor assignment
Major/year-based course targeting
Grading system: Attestation 1, Attestation 2, Final
Transcript generation with GPA and sorting options
🔬 Research System
Researcher interface with decorator extension
Validation rules (e.g., h-index ≥ 3 for supervision)
Research paper and project tracking
Analytics: top-cited researchers, sorting by citations/date/length
⚙️ System Design & Logic
Business rule enforcement via custom exceptions
Credit limit restrictions
Maximum 3 failed courses
Supervisor validation
Data persistence using Java Serialization
Implementation of key design patterns:
Factory
Singleton
Strategy
Observer / Decorator

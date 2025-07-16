# BTO Housing Management System

## Overview
The BTO Housing Management System streamlines housing workflows through a modular, extensible architecture. Each user role is granted specific functionality:
- **Applicants** apply for flats and manage enquiries
- **HDB Officers** handle registration, project enquiries, and flat allocation
- **HDB Managers** oversee project creation, visibility, officer approval, and reporting

## Key Features
- **Role-Based Access Control (RBAC)**: Custom menus and access rights for each user role
- **Modular Architecture**: Controllers, UI classes, Repositories, and Entities follow SRP and high cohesion
- **Status-Driven Workflow**: Visibility toggles and logical handoffs between roles
- **UML Class & Sequence Diagrams**: Clear documentation of class relationships and interactions
- **Comprehensive Testing**: Validated over 25 user scenarios, from login to flat booking and receipt generation

## System Insights & Impact
- **Streamlined Workflow**: Reduces ambiguity and errors in flat selection, officer assignment, and application approvals
- **Robust Error Handling**: Prevents unauthorised access and invalid status transitions (eg. booking before approval)
- **Code Maintainability**: SOLID design allows for scalable updates like new flat types or dashboards
- **Real-World Alignment**: Application logic mirrors real HDB workflows, including officer-approval dependencies and flat visibility controls

## Design Principles & Patterns
### OOP Concepts Applied
- **Abstraction**: Abstract `User` class with role-specific implementations
- **Encapsulation**: Controlled access to sensitive data (e.g., flat prices)
- **Inheritance & Polymorphism**: Shared behavior across subclasses like `Applicant`, `HDBOfficer`, and `HDBManager`

### SOLID Principles
- **SRP**: Each class handles one responsibility (eg. `ProjectController`, `ManagerUI`)
- **OCP**: Easily extend project filtering logic via modular filter layers
- **LSP**: All user types inherit from `User`, enabling polymorphic interaction
- **ISP**: Repository interfaces serve specific tasks (eg. `ApplicationRepository`, `OfficerRepository`)
- **DIP**: High-level modules rely on abstractions, not concrete implementations

## Project Structure
- `src/` – Java source files
- `lib/` – External libraries (JAR files)
- `data/` – CSV files used for data persistence
- `Main.class` – Entry point of the program (compiled)

# JPA Fetch Type Guide

## What is `fetchType`?

`fetchType` controls **WHEN** JPA loads related entities.
In JPA, relationships (@ManyToOne, @OneToMany, etc.) can load data in two ways:
- **LAZY** → Load only when accessed (recommended default)
- **EAGER** → Load immediately with parent (dangerous if misused)

---

## Default Fetch Types (Important to Memorize)

| Relationship  | Default     |
|---------------|-------------|
| `@ManyToOne`  | **EAGER** ❌ |
| `@OneToOne`   | **EAGER** ❌ |
| `@OneToMany`  | **LAZY** ✅  |
| `@ManyToMany` | **LAZY** ✅  |

---

## When to Use WHAT (Rule of Thumb)

### Use LAZY when:

- Entity has relationships
- Large data sets
- REST APIs
- Microservices
- Production systems
- ✅ **95% of cases**

### Use EAGER when:

- Small, immutable reference data
- Single table joins
- Very controlled use-case

---

## Common Interview Question (⚡)

**Q: Why is LAZY recommended even though EAGER is default for ManyToOne?**

**Answer:**  
Because:

- EAGER causes hidden joins
- Performance issues
- N+1 problems that are hard to detect

## n+1 problem
*  1 query to load parents + N extra queries to load children.
* It happens with LAZY relationships accessed in a loop.
* Why it happens:
* -> It usually occurs with lazy-loaded associations (@OneToMany, @ManyToOne, @ManyToMany) in JPA/Hibernate.
* -> If you access the associated entities in a loop, Hibernate triggers additional queries for each record.
### solved by:
* Fetch Join
* DTO Projection
* EntityGraph
* LAZY + Explicit Fetch = Best Design
* You solve it explicitly, not by switching to EAGER.
* You fetch all students, then access their departments.
### Why LAZY Is NOT the Problem

* LAZY is correct

* Access pattern is wrong

* Solution is controlled fetching
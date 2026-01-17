# Entity Indexing Strategy

## Why Indexes?

### Performance
Queries filtering by the following columns become **O(log n)** instead of **O(n)**:

- `courseCode`
- `departmentId`
- `teacherId`
- `semester`

These fields are frequently used in search and filter operations.

---

## How Indexes Improve Performance

### Without Indexes
- Database performs a **full table scan**
- Time complexity: **O(n)**

### With Indexes
- Database uses an **index (binary search)**
- Time complexity: **O(log n)**

---

## Example

**10,000 courses in database**  
Search: `CS101`

- ❌ Without index → checks all 10,000 rows (slow)
- ✅ With index → binary search, ~14 checks (fast)

---

## Indexing Guidelines

- Index columns used in **WHERE**, **JOIN**, and **ORDER BY** clauses
- **Rule of thumb**: Add an index if a column is used in **more than 30% of queries**

---


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

## cascade
- Cascading = propagate JPA operations

- Parent action → automatically applied to child

- Prevents manual save/delete of child entities


```java
@OneToMany(
    mappedBy = "course",
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
@Builder.Default
private List<Enrollment> enrollments = new ArrayList<>();
```



### What Each Part Means

### `@OneToMany`

✅ **One `Course` → many `Enrollments`**


---

### `mappedBy = "course"`

✅ **`Enrollment` owns the foreign key**
✅ This tells JPA that the relationship is managed on the `Enrollment` side via its `course` field.

---

### `cascade = CascadeType.ALL`

✅ **All operations on `Course` propagate to `Enrollment`**

Any lifecycle operation performed on `Course` will automatically be applied to its `Enrollment`s.

---

### `orphanRemoval = true`

✅ **Removing an enrollment from the list deletes it from the database**

If an `Enrollment` is removed from the `enrollments` collection, JPA will delete it from the DB.

---

### `@Builder.Default`

✅ **Ensures the builder does not set the list to `null`**
When using Lombok’s `@Builder`, this guarantees `enrollments` is initialized with an empty list.

---

## CascadeType Values (Must Know)

### `CascadeType.PERSIST`

Saving parent → saves children

```java
entityManager.persist(course);
```

---

### `CascadeType.MERGE`

Updating parent → updates children

```java
entityManager.merge(course);
```

---

### `CascadeType.REMOVE`

Deleting parent → deletes children

```java
entityManager.remove(course);
```

---

### `CascadeType.REFRESH`

Reloading parent → reloads children from database

---

### `CascadeType.DETACH`

Detaching parent → detaches children from persistence context

---

### `CascadeType.ALL`

Shortcut for:

* `PERSIST`
* `MERGE`
* `REMOVE`
* `REFRESH`
* `DETACH`

---

## When to Use `CascadeType.ALL` ✅

Use it when:

* Child **cannot exist without** parent
* Parent is the **aggregate root**
* Child lifecycle is **fully owned** by parent

### Your Case

* `Enrollment` without `Course` ❌
* Correct usage ✔

---

## When **NOT** to Use Cascade ❌

Avoid cascading for:

* Shared entities (e.g. `User`, `Department`)
* Reference / lookup data
* Core many-to-many relationships

---

✅ **Summary**: This configuration is ideal for strict parent–child relationships where the child’s lifecycle is fully dependent on the parent.

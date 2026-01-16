#### Derived Query Methods
````
javaOptional<Course> findByCourseCode(String courseCode);
boolean existsByCourseCode(String courseCode);
Page<Course> findByDepartmentId(Long departmentId, Pageable pageable);
````
#### How Spring Generates Queries:

Parses method name using keywords (findBy, existsBy, etc.)
Maps field names to entity properties
Generates JPQL/SQL automatically

### Interview Question:
* "What's the difference between find, get, and read in Spring Data JPA?"

Answer: They're identical! Spring Data uses them interchangeably as synonyms.


- Tricky Note: Page always executes 2 queries. Use Slice if you don't need total count.
---

## pageable pagination
```java
Pageable pageable = PageRequest.of(page, size, sort);
Page<Course> coursePage = courseRepository.findAll(pageable);
```

---

## What Happens Internally

When this code runs, Spring Data JPA performs **two SQL queries** internally (in most cases).

---

### 1️⃣ Data Query (LIMIT / OFFSET)

Fetches only the requested slice of data:

```sql
SELECT * FROM course
ORDER BY ...
LIMIT size OFFSET page * size;
```

This query populates the **actual result list**.

---

### 2️⃣ Count Query (TOTAL RECORDS)

Calculates total number of matching rows:

```sql
SELECT COUNT(*) FROM course;
```

This query is required to compute pagination metadata.

---

## What `Page<T>` Contains

### `List<T> content`

✅ Actual data for the current page

```java
coursePage.getContent();
```

---

### `long totalElements`

✅ Total number of records in the table (or matching criteria)

```java
coursePage.getTotalElements();
```

---

### `int totalPages`

✅ Calculated as:

```text
totalPages = ceil(totalElements / size)
```

```java
coursePage.getTotalPages();
```

---

### Navigation Helpers

```java
coursePage.hasNext();
coursePage.hasPrevious();
coursePage.isFirst();
coursePage.isLast();
```

These are derived **purely from metadata**, not extra queries.

---

## Tricky / Important Things to Know ⚠️

### 1️⃣ Always Executes COUNT Query

Even if you only need the data list, `Page<T>` **still runs the COUNT query**.

❌ Can be expensive for large tables

---

### 2️⃣ Complex Queries = Expensive COUNT

For queries with:

* `JOIN`
* `GROUP BY`
* `DISTINCT`
* Subqueries

The generated COUNT query can be **slow or incorrect**.

---

### 3️⃣ Sorting Happens in Database

```java
PageRequest.of(page, size, sort);
```

Sorting is translated into `ORDER BY` — not in-memory sorting.

---

### 4️⃣ Page vs Slice (Very Important)

| Feature       | Page  | Slice |
| ------------- | ----- | ----- |
| COUNT query   | ✅ Yes | ❌ No  |
| totalElements | ✅ Yes | ❌ No  |
| totalPages    | ✅ Yes | ❌ No  |
| hasNext       | ✅ Yes | ✅ Yes |

👉 Use `Slice<T>` when total count is **not required**.

---

### 5️⃣ Zero-Based Page Index

```java
PageRequest.of(0, 10); // first page
PageRequest.of(1, 10); // second page
```

Passing `page = 1` from UI without adjustment is a **common bug**.

---

## Best Practices ✅

* Use `Page<T>` for admin panels, reports
* Use `Slice<T>` or `List<T>` for infinite scroll
* Avoid heavy joins in pageable queries
* Add proper indexes for sorted columns

---

✅ **Summary**: `Page<T>` is powerful but not free — understand the hidden COUNT query before using it at scale.

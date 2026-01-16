```java
@Transactional
public CourseResponse createCourse(CreateCourseRequest request) {
    securityUtils.verifyAdminAccess();  // 1. Authorization

    if (courseRepository.existsByCourseCode(request.getCourseCode())) {
        throw new DuplicateResourceException(...);  // 2. Validation
    }
    
    Department department = departmentRepository.findById(...)
        .orElseThrow(...);  // 3. Foreign Key Validation
    
    Course course = Course.builder()
        ...
        .build();  // 4. Entity Creation

    Course savedCourse = courseRepository.save(course);  // 5. Persistence
    
    return dtoMapper.toCourseResponse(savedCourse);  // 6. DTO Mapping
}
```

---

## Why This Order?

1. **Fail Fast** – Authorization first (cheapest operation)
2. **Database Validation** – Check uniqueness early
3. **Referential Integrity** – Ensure related entities exist
4. **Builder Pattern** – Clean, readable entity creation
5. **Persistence** – Save within transaction
6. **DTO Conversion** – Hide entity internals from API consumers

---

## Update Course with Business Rules

```java
if (request.getMaxCapacity() != null) {
    int currentEnrollment = course.getEnrolledCount();
    if (request.getMaxCapacity() < currentEnrollment) {
        throw new BusinessException(
            "Cannot reduce max capacity to " + request.getMaxCapacity() +
            ". Current enrollment: " + currentEnrollment
        );
    }
    course.setMaxCapacity(request.getMaxCapacity());
}
```

---

## Why This Validation?

* **Data Integrity** – Prevents invalid enrollment state
* **User-Friendly Error** – Clear reason for failure
* **Business Logic in Service Layer** – Not in controller or entity

---

## Why is `save()` Needed if Hibernate Tracks Changes?

👉 Hibernate tracks changes, but `save()` defines **when and what** to persist.

✔ Managed entities are auto-flushed inside a transaction

---

## When `save()` **IS Required**

| Scenario                  | `save()` |
| ------------------------- | -------- |
| New entity                | ✅        |
| Detached entity           | ✅        |
| Outside transaction       | ✅        |
| Entity not loaded via JPA | ✅        |

`````@Transactional
public CourseResponse createCourse(CreateCourseRequest request) {
securityUtils.verifyAdminAccess();  // 1. Authorization

    if (courseRepository.existsByCourseCode(request.getCourseCode())) {
        throw new DuplicateResourceException(...);  // 2. Validation
    }
    
    Department department = departmentRepository.findById(...)
        .orElseThrow(...);  // 3. Foreign Key Validation
    
    Course course = Course.builder()...build();  // 4. Entity Creation
    Course savedCourse = courseRepository.save(course);  // 5. Persistence
    
    return dtoMapper.toCourseResponse(savedCourse);
`````
Why This Order?

Fail Fast: Authorization checks first (cheapest operation)
Database Validation: Check uniqueness before creating entity
Referential Integrity: Ensure related entities exist
Builder Pattern: Clean, readable object construction
DTO Conversion: Hide entity internals from API consumers

````
Update Course with Business Rules
javaif (request.getMaxCapacity() != null) {
    int currentEnrollment = course.getEnrolledCount();
    if (request.getMaxCapacity() < currentEnrollment) {
        throw new BusinessException(
            "Cannot reduce max capacity to " + request.getMaxCapacity() +
            ". Current enrollment: " + currentEnrollment);
    }
    course.setMaxCapacity(request.getMaxCapacity());
}
````
Why This Validation?

Data Integrity: Prevents orphaned enrollments
User-Friendly Error: Explains why operation failed
Business Logic in Service: Not in controller or entity

### Why is save() needed if Hibernate tracks changes?
* 👉 Hibernate tracks changes, but save() tells it when and what to persist.
* ✔ Hibernate auto-saves managed entities inside a transaction

When save() IS Required
Scenario	                 save()
New entity	                  ✅
Detached entity	             ✅
Outside transaction	          ✅
Entity not loaded via JPA	    ✅
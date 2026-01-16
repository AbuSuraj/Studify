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

````
Pageable pageable = PageRequest.of(page, size, sort);
Page<Course> coursePage = courseRepository.findAll(pageable);
````
What Happens Internally:

Page contains:

List<T> content - Actual results
long totalElements - Total records (COUNT query)
int totalPages - Calculated from total/size
boolean hasNext(), boolean hasPrevious() - Navigation helpers

- Tricky Note: Page always executes 2 queries. Use Slice if you don't need total count.
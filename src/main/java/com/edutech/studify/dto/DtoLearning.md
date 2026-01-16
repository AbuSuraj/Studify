## Lombok Annotations
| Annotation            | What it generates                            |
| --------------------- | -------------------------------------------- |
| `@Data`               | getters, setters, toString, equals, hashCode |
| `@NoArgsConstructor`  | empty constructor                            |
| `@AllArgsConstructor` | constructor with all fields                  |
| `@Builder`            | builder pattern                              |

## import java.time.LocalDateTime;
- ➡️ Used to store response time
- ➡️ Better than Date (immutable & modern)

# @Builder.Default
 * private LocalDateTime timestamp = LocalDateTime.now();
 * Why @Builder.Default?
 
## Without it:
 * ❌ Builder will ignore default value
 
## With it:
 * ✔ Timestamp auto-generated
 * 📌 Records response time

## 🏗️ Lombok-generated code (hidden)

Because of Lombok, this class automatically has:

* getData()
* setData()
* getMessage()
* isSuccess()

➡️ You don’t write them manually
***

## 🧠 Why NOT return Entity directly?

❌ Entity exposes DB structure
❌ Lazy loading issues
❌ Security risks

DTO gives:
* ✔ Control
* ✔ Consistency
* ✔ Safety

### Why Separate Mapper? (Important Points Only)

Keeps entities and API contracts separate

Prevents exposing JPA entities to clients

Centralizes mapping logic (single source of truth)

Improves security by controlling exposed fields

Avoids lazy-loading & serialization issues

Keeps services and controllers clean

Makes code easier to maintain and refactor


# JWT Authentication Flow

## Initial Login Flow

1. **Client sends credentials** (email / password)
   ↓
2. **AuthController** receives `LoginRequest`
   ↓
3. **AuthenticationManager** validates credentials
   ↓
4. **CustomUserDetailsService** loads user from database
   ↓
5. **PasswordEncoder** compares hashed passwords
   ↓
6. **JwtUtils** generates JWT token
   ↓
7. **AuthResponse** returned with token

---

## Subsequent Requests Flow

1. **Client sends JWT token** in `Authorization` header
2. **JwtAuthenticationFilter** intercepts request
3. **JwtUtils** validates token and extracts username
4. **CustomUserDetailsService** loads user details
5. **UsernamePasswordAuthenticationToken** is created
6. **SecurityContext** is populated with authentication
7. **Request proceeds** to the controller


## SecurityConfig.java
Defines how Spring Security behaves:
* Which endpoints are public

* How authentication happens

* Which filters run

* Stateless JWT setup

### Password Encoder Bean

#### What it does:
* BCrypt is a one-way hashing algorithm (you can't reverse it)
* It automatically adds salt (random data) to prevent rainbow table attacks
* When a user registers, you hash their password: passwordEncoder.encode("password123")
* When they log in, you verify: passwordEncoder.matches("password123", hashedPassword)

Why BCrypt?

* One-way hashing (can't reverse)
*  Includes salt automatically (prevents rainbow table attacks)
* Slow by design (prevents brute-force attacks)
* Adaptive (can increase work factor as computers get faster)
### Stored value looks like:
````
$2a$10$W9hZKz...Qn9z0
````
This string contains:

* Algorithm version (`$2a$`)

* Strength (`10$`)

* Salt

* Hash

## Passwords are verified, not compared
### Verifying a Password (Login) 
* ❌ Never compare strings directly

* ✅ Always use matches()

### Why?
- ➡️ BCrypt adds a random salt each time
- ➡️ Hash output changes even for the same password
### What matches() Does
- Extracts the salt + cost from storedHash

- Re-hashes rawPassword using the same salt

- Compares results using constant-time comparison

### authenticationProvider()
Tells Spring:
* How to load users → CustomUserDetailsService
* How to verify passwords → BCryptPasswordEncoder

### authenticationManager()

* Central component that performs authentication

* Used in your login endpoint

* Delegates to AuthenticationProvider

### filterChain(HttpSecurity)

This is the core configuration:

Order of execution:

1. Disable CSRF → JWT is stateless

2. Exception handling → JwtAuthenticationEntryPoint

3. Session policy → STATELESS

4. Authorization rules

5. Register AuthenticationProvider

6. Add JwtAuthenticationFilter before username/password filter
---
# JwtAuthenticationFilter
## purposes:
* runs on every http request
* If JWT exists → authenticate user.
* **JWT is not automatic**.
* Spring Security won’t read headers unless you do it.

## Execution flow

- Extract Authorization header

-  Parse Bearer token

- Validate JWT

- Load user from DB

- Create Authentication

- Set into SecurityContext

Spring Boot starts
 * → @Component scanned
 * → JwtAuthenticationFilter bean created
 * → SecurityFilterChain built
 * → addFilterBefore() places this filter in the chain

2️⃣ Runtime (EVERY HTTP REQUEST)
HTTP request arrives
- Tomcat
- DelegatingFilterProxy
- FilterChainProxy
- JwtAuthenticationFilter.doFilterInternal()  👈 HERE

This happens:
* Before controller
* Before authorization
* Before @PreAuthorize
* Before permitAll is evaluated

### Who Calls It? (Exact Classes)
Tomcat
* DelegatingFilterProxy
* FilterChainProxy
* VirtualFilterChain
* OncePerRequestFilter.doFilter()
* JwtAuthenticationFilter.doFilterInternal()

### Why It Runs for EVERY Request

Because:
```
.addFilterBefore(jwtAuthenticationFilter,UsernamePasswordAuthenticationFilter.class)
```
Spring inserts it into the global security filter chain.

Even for:

* /api/auth/login

* /api/auth/register

* /swagger-ui/**

It still runs — it just does nothing if no JWT exists.

#### Why OncePerRequestFilter

Guarantees:

Runs once per HTTP request

Even with forwards / error dispatches

Prevents:

Duplicate authentication

Context corruption

#### What Happens If Token Is Missing?
parseJwt() → null
if condition fails
SecurityContext remains empty
request continues

Later:

authorizeHttpRequests()

Decides:

permitAll() → allow

authenticated() → 401

### Why Filter Must Come Before Authorization
* Authorization needs to know who the user is and what roles/permissions they have

* Authentication filters extract credentials (JWT, username/password, etc.)

* Filters create an Authentication object

* Filters store authentication data in SecurityContextHolder

* Authorization reads from SecurityContextHolder

If filters run after authorization:

* SecurityContextHolder is empty

* User is treated as anonymous

* Access is denied

* Therefore, filters must run before authorization so Spring can correctly decide permissions


Authorization checks:

SecurityContextHolder.getContext().getAuthentication()


* Spring Security calls filters, filters never call Spring Security.

#### One-Line Mental Model (Memorize)

* Spring Security calls filters, filters never call Spring Security.

Final Flow Diagram
* Request
* ↓
* JwtAuthenticationFilter.doFilterInternal()
* ↓
* SecurityContext populated (or not)
* ↓
* AuthorizationFilter
* ↓
* Controller

# JwtUtils — Short & Important Notes

---

## What `JwtUtils` Is
`JwtUtils` is a **stateless helper component** responsible for **JWT creation, parsing, and validation**.

> It does NOT authenticate users  
> It does NOT interact with Spring Security directly  
> It only handles **token cryptography**

---

## When `JwtUtils` Is Called
`JwtUtils` is **never called automatically** by Spring Security.

It is called **manually** from:
- **AuthController** → during login (token creation)
- **JwtAuthenticationFilter** → on every request (token validation)

---

## Lifecycle
- Spring Boot starts
-   @Component scanned
 -  JwtUtils bean created (ONCE)
---

## What Is JWT?

**JWT (JSON Web Token)** is a **signed JSON token** that proves:

- **Who you are** → Authentication
- **What you can do** → Authorization

### Key Characteristics

JWT is:
- URL-safe
- Compact
- Self-contained

---

## JWT Structure (VERY IMPORTANT)

A JWT has **3 parts**, separated by dots:

* xxxxx.yyyyy.zzzzz
* header.payload.signature

---

### 1️⃣ Header

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Contains:
- Algorithm used for signing
- Token type

➡️ Base64 encoded

### 2️⃣ Payload (Claims)
````
{
  "sub": "john",
  "role": "USER",
  "iat": 1700000000,
  "exp": 1700003600
}
````

#### Common Claims
* sub → username

* iat → issued at

* exp → expiration time

* roles → authorities

#### ⚠️ Payload is NOT encrypted
#### ❌ Never store passwords or sensitive data

### 3️⃣ Signature

```HMACSHA256(
  base64(header) + "." + base64(payload),
  secret_key
)
```
#### Purpose:

* Ensures token integrity

* Prevents tampering

### How JWT Works (Request Flow)
* User logs in (username / password)

* Spring Boot validates credentials

* Server generates JWT

* Client stores JWT (localStorage / cookie)

* Client sends JWT with every request

* Server verifies: Signature & Expiration

* Access granted

### Where JWT Lives in Spring Security
#### JWT Replaces
* ❌ HttpSession

* ❌ JSESSIONID

#### JWT Integrates With
✔ OncePerRequestFilter

✔ SecurityFilterChain

✔ AuthenticationManager

### JWT Structure:
```
eyJhbGciOiJIUzUxMiJ9.eyJzdWxfQ.6DmLrg_1RCcUZFHddL4_VKB4HCVFoD8K3ZMA5tL59eEizTDD7lx4jHfXs3EVi8GL...
     HEADER            PAYLOAD                    SIGNATURE
```

* Header: {"alg":"HS256","typ":"JWT"}
* Payload: {"sub":"user@example.com","iat":1736...,"exp":1736...}
* Signature: HMACSHA256(base64(header) + "." + base64(payload), secret)

- Signature ensures token hasn't been modified
- Anyone can decode payload (it's Base64), but can't forge signature without secret.

## JWT in Spring Boot (High-Level Steps)
### 1️⃣ User Login Endpoint

- Accepts username/password

- Authenticates via AuthenticationManager

- Generates JWT

### 2️⃣ JWT Utility

- Generate token

- Validate token

- Extract username

### 3️⃣ JWT Filter

- Runs before Spring Security

- Extracts token from header

- Validates token

- Sets authentication in SecurityContext

### 4️⃣ Security Configuration

- Disable session

- Add JWT filter

- Protect endpoints

## signing key
```
private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    return Keys.hmacShaKeyFor(keyBytes);
}
```
## Why Base64?

- Secret needs to be binary data (bytes)
- Base64 encoding lets you store binary data as text in properties files
- Keys.hmacShaKeyFor(): Creates HMAC key for HS256 algorithm

 --- 

# CustomUserDetailsService.java
#### purpose:
* Loads user data from DB for Spring Security.
* Spring Security never talks directly to your repository.
*  It only knows UserDetailsService.

### Key methods
#### loadUserByUsername(String email)

* Called during login

* Called during JWT validation

* Returns UserDetailsImpl

#### loadUserById(Long id)

* Optional helper

* Useful if token stores user ID instead of email

---

# UserDetailsImpl.java
- Security-Compatible User
- Your entity stores data.
- UserDetailsImpl explains that data to Spring Security.

### 🔑 Core idea (very simple)

* 👉 Your **User** entity = database model
* 👉 **UserDetails** = security model

* They are not the same, so we need a bridge.

* That bridge is **UserDetailsImpl**.

#### purpose:
##### Adapter between:
* Your User entity
* Spring Security’s UserDetails. 

### Why we need it



## Why Spring Security needs `UserDetails`

Spring Security only cares about **authentication & authorization**.  
So it asks only these questions:

- What is the **username**?
- What is the **password**?
- What are the **roles / authorities**?
- Is the account **enabled / locked / expired**?

It does **NOT** care about:

- `firstName`
- `lastName`
- `age`
- `profile picture`
- etc.

#### Spring Security requires:

* getUsername()

* getPassword()

* getAuthorities()

* Account state flags

* Your entity doesn’t match this interface.

## Why your `User` entity cannot be used directly

### Your entity

```java
class User {
    Long id;
    String email;
    String username;
    String password;
    Role role;
    Boolean isActive;
}
```
### What Spring Security expects

```
public interface UserDetails {
    String getUsername();
    String getPassword();
    Collection<? extends GrantedAuthority> getAuthorities();
    boolean isEnabled();
}
```

#### Problem

* ❌ These two do not match

* role ≠ Collection<GrantedAuthority>

* isActive ≠ isEnabled()

* No contract with Spring Security

### public static UserDetailsImpl build(User user)
This method:
* Converts Role → ROLE_ADMIN

* Maps email → username

* Maps isActive → enabled
---

```
┌──────────────────────────────────────────────────────────────┐
│ CLIENT SENDS REQUEST                                         │
│ POST /api/auth/register                                      │
│ { username, email, password, role }                          │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ STEP 1: DispatcherServlet (Spring MVC)                       │
│ • Receives HTTP request                                      │
│ • Routes to appropriate controller                           │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ STEP 2: CorsFilter                                           │
│ • Checks origin (http://localhost:3000)                      │
│ • Adds CORS headers to response                              │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ STEP 3: SecurityFilterChain                                  │
│ • Checks URL: /api/auth/register                             │
│ • Matches permitAll() → No authentication required           │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ STEP 4: JwtAuthenticationFilter                              │
│ • Looks for Authorization header                             │
│ • No token found (registration request)                      │
│ • Continues to next filter                                   │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ STEP 5: AuthController.registerUser()                        │
│ • Maps URL to method                                         │
│ • Deserializes JSON → RegisterRequest object                 │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ STEP 6: Bean Validation (@Valid)                             │
│ ✓ Username: 3-20 chars, alphanumeric                         │
│ ✓ Email: valid format                                        │
│ ✓ Password: min 6 chars, complex                             │
│ ✓ Role: not null                                             │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ STEP 7: AuthService.register()                               │
│ ┌──────────────────────────────────────────────────────────┐ │
│ │ 7A: Check email exists                                   │ │
│ │     userRepository.existsByEmail()                       │ │
│ │     SQL: SELECT EXISTS(SELECT 1 FROM users WHERE...)     │ │
│ └──────────────────────────────────────────────────────────┘ │
│                         │                                    │
│                         ▼                                    │
│ ┌──────────────────────────────────────────────────────────┐ │
│ │ 7B: Check username exists                                │ │
│ │     userRepository.existsByUsername()                    │ │
│ └──────────────────────────────────────────────────────────┘ │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ STEP 8: Create User Entity                                   │
│ • User.builder()                                             │
│ • passwordEncoder.encode(password)                           │
│   Input:  "SecurePass123"                                    │
│   Output: "$2a$10$N9qo8uLO..."                               │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ STEP 9: Save to Database                                     │
│ • userRepository.save(user)                                  │
│ • JPA generates INSERT SQL                                   │
│ • Database auto-generates ID                                 │
│ • Transaction NOT committed yet (@Transactional)             │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ STEP 10: Generate JWT Token                                  │
│ • jwtUtils.generateTokenFromUsername(email)                  │
│ • Creates JWT with:                                          │
│   - Header: { alg: HS256, typ: JWT }                         │
│   - Payload: { sub: email, iat: now, exp: +24h }             │
│   - Signature: HMAC-SHA256(header.payload, secret)           │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ STEP 11: Build AuthResponse                                  │
│ • AuthResponse.builder()                                     │
│ • Sets: token, id, username, email, role                     │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ STEP 12: Transaction Commits                                 │
│ • @Transactional method exits                                │
│ • JPA flushes changes to database                            │
│ • SQL COMMIT executed                                        │
│ • User permanently saved                                     │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ STEP 13: Response Serialization                              │
│ • Jackson converts AuthResponse → JSON                       │
│ • Sets Content-Type: application/json                        │
│ • HTTP Status: 201 Created                                   │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│ STEP 14: Response Returns to Client                          │
│ • Passes through security filters                            │
│ • CORS headers added                                         │
│ • Client receives JWT token                                  │
└──────────────────────────────────────────────────────────────┘
```

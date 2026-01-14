# JwtAuthenticationFilter
Spring Boot starts
 → @Component scanned
 → JwtAuthenticationFilter bean created
 → SecurityFilterChain built
 → addFilterBefore() places this filter in the chain

2️⃣ Runtime (EVERY HTTP REQUEST)
HTTP request arrives
→ Tomcat
→ DelegatingFilterProxy
→ FilterChainProxy
→ JwtAuthenticationFilter.doFilterInternal()  👈 HERE

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

###Why It Runs for EVERY Request

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
-  → @Component scanned
 - → JwtUtils bean created (ONCE)
##
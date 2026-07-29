# 📅 Week 1 Commit Schedule

## ✅ Wednesday July 22 — DONE
**Commit:** `feat: initialize Spring Boot backend with JWT security and JPA configuration`
Files committed:
- `backend/pom.xml` (project dependencies)
- `backend/src/main/resources/application.properties` (DB + JWT config)
- `backend/src/main/java/.../ContactmanagerApplication.java` (main entry point)
- `backend/src/main/java/.../security/SecurityConfig.java` (Spring Security rules)
- `backend/src/main/java/.../security/JwtTokenProvider.java` (JWT token maker)
- `backend/src/main/java/.../security/JwtAuthenticationFilter.java` (request guard)
- `backend/src/main/java/.../security/CustomUserDetailsService.java` (user loader)

---

## ⏳ Thursday July 24 — TODO
**Commit:** `feat: add User entity, repository, DTOs and service layer`
Files to commit:
- `backend/src/main/java/.../model/User.java`
- `backend/src/main/java/.../repository/UserRepository.java`
- `backend/src/main/java/.../dto/RegisterRequest.java`
- `backend/src/main/java/.../dto/LoginRequest.java`
- `backend/src/main/java/.../dto/AuthResponse.java`
- `backend/src/main/java/.../dto/UserResponse.java`
- `backend/src/main/java/.../dto/ChangePasswordRequest.java`
- `backend/src/main/java/.../service/UserService.java`
- `backend/src/main/java/.../service/UserServiceImpl.java`

**Command to run on Thursday (from the repository root):**
```bash
git add backend/src/main/java/com/tenpearls/contactmanager/model/ \
        backend/src/main/java/com/tenpearls/contactmanager/repository/ \
        backend/src/main/java/com/tenpearls/contactmanager/dto/ \
        backend/src/main/java/com/tenpearls/contactmanager/service/
git commit -m "feat: add User entity, repository, DTOs and service layer"
git push origin feature/setup-auth-backend
```

---

## ⏳ Friday July 25 — TODO (before your meeting!)
**Commit:** `feat: add auth REST controller, exception handling and unit tests`
Files to commit:
- `backend/src/main/java/.../controller/AuthController.java`
- `backend/src/main/java/.../exception/GlobalExceptionHandler.java`
- `backend/src/main/java/.../exception/BadRequestException.java`
- `backend/src/main/java/.../exception/ErrorResponse.java`
- `backend/src/main/java/.../exception/ResourceAlreadyExistsException.java`
- `backend/src/main/java/.../exception/UnauthorizedException.java`
- `backend/src/test/.../ContactmanagerApplicationTests.java`
- `backend/src/test/.../controller/AuthControllerTest.java`
- `backend/src/test/.../service/UserServiceImplTest.java`

**Command to run on Friday (from the repository root):**
```bash
git add backend/src/main/java/com/tenpearls/contactmanager/controller/ \
        backend/src/main/java/com/tenpearls/contactmanager/exception/ \
        backend/src/test/
git commit -m "feat: add auth REST controller, exception handling and unit tests"
git push origin feature/setup-auth-backend
```

Then open the Pull Request at:
https://github.com/TalhaNiazi27/cohort-9-java-11568-talha/pull/new/feature/setup-auth-backend

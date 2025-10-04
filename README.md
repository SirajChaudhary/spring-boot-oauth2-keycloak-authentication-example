# Spring Boot + Keycloak OAuth2 Example

## Overview

This project demonstrates how to secure a Spring Boot application using **Keycloak** as an OAuth2 / OpenID Connect provider. Users log in with Keycloak, receive an access token, and can access protected REST APIs.

---

## What is Keycloak?

Keycloak is an open-source Identity and Access Management (IAM) solution. It provides:
- Single Sign-On (SSO)
- User Federation
- Social login integration
- Role-based access control
- Standard OAuth2 / OpenID Connect support

### Why use Keycloak instead of social login?

- Social logins (GitHub, Google, Facebook) only provide basic identity info and require you to manage roles/permissions separately.
- Keycloak allows full **centralized user management**, role-based authorization, and token customization.
- You can generate your **own custom tokens** from Keycloak-issued access tokens if needed for API-specific claims.

### Important points about Keycloak

- Supports **realm-based isolation** of users and clients.
- Provides **standard OIDC endpoints**: token, userinfo, logout, JWKS.
- Can manage **roles, groups, and service accounts**.
- Supports multiple grant types (authorization code, client credentials, refresh token).
- Start-dev mode is for local development; production mode requires a persistent database and secure credentials.

---

## Quick Architecture

```
[User] ---> [Spring Boot App: OAuth2 Client] ---> [Keycloak: OAuth2 Server]
                     |
                     v
             [Protected REST APIs]
```

- Spring Boot acts as an OAuth2 client.
- Keycloak issues access tokens after login.
- Spring Boot validates Keycloak JWTs automatically.
- Optionally, Spring Boot can issue **custom JWTs** using claims from Keycloak’s token.

### Assumptions

- Keycloak runs locally: `http://localhost:8080`
- Spring Boot app runs locally: `http://localhost:8081`
- Realm: `demo`
- Client: `spring-app`
- OAuth2 redirect URI: `http://localhost:8081/login/oauth2/code/keycloak`

---

## 1) Start Keycloak (local dev mode)

```bash
docker run -p 127.0.0.1:8080:8080 \
  -e KC_DB=dev-file \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:26.1.1 start-dev
```

- Admin console: [http://localhost:8080](http://localhost:8080)
- Default admin user: `admin / admin`

---

## 2) Create Realm & Client

#### Step 1: Log into Keycloak Admin Console
- Open **Keycloak Admin Console** (e.g., http://localhost:8080/)
- Log in with your **admin username and password**

#### Step 2: Create a Realm
- On the top-left dropdown (it may say Master by default), click it
- Click **Create Realm**
- Enter:
    - **Realm Name:** demo
- Click **Create**
- You are now inside the **demo** realm

#### Step 3: Create a Client
- In the left menu, click **Clients**
- Click **Create client** (top-right button)
- Fill in:
    - **Client ID:** spring-app
    - **Client type / Protocol:** choose **OpenID Connect**
- Click **Next** (or **Save** depending on version)

#### Step 4: Configure Client Settings
- Inside your new client (**spring-app**):
    - **Access Type / Client Authentication:** set to **Confidential** (requires client secret)
    - **Valid Redirect URIs:** http://localhost:8081/login/oauth2/code/keycloak
    - **Root URL / Base URL (optional):** http://localhost:8081
- Scroll down and click **Save**

#### Step 5: Find the Client Secret
- In Keycloak 18+ (new Admin Console), secrets here:
    - Go to **Clients → spring-app → Credentials** (or in newer versions, **Clients → spring-app → Settings → Client Authentication**)
    - Toggle **Client Authentication** to **ON** if not already
- The **Client Secret** will be shown under **Credentials tab** or **Keys** section depending on Keycloak version
- **Copy this Client Secret** — you’ll need it in your Spring Boot `application.properties`

#### Step 6: (Optional) Service Accounts
- If using **client-credentials flow** (machine-to-machine):
    - Go to **Clients → spring-app → Service Account Roles**
    - Enable **Service Accounts**
    - Assign roles as needed
- Not required for browser logins

#### Step 7: (Optional) Token Settings
- Go to **Realm Settings → Tokens**
- Adjust token lifetimes (Access Token, Refresh Token, etc.) if required

#### At this point:
- You have a **realm demo**
- You have a **client spring-app** with redirect URI and client secret
- Spring Boot can now be configured to use **spring-app** as an OAuth2 client
---

## 3) Configure Spring Boot

### application.properties

```properties
server.port=8081

spring.security.oauth2.client.registration.keycloak.client-id=spring-app
spring.security.oauth2.client.registration.keycloak.client-secret=<your-client-secret>
spring.security.oauth2.client.registration.keycloak.scope=openid,profile,email
spring.security.oauth2.client.registration.keycloak.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

spring.security.oauth2.client.provider.keycloak.issuer-uri=http://localhost:8080/realms/demo
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/demo
```

- Replace `<your-client-secret>` with the value from Keycloak.
- Ensure ports/URLs match Keycloak configuration.

---

## 4) Run & Test

1. Build and start Spring Boot:

```bash
mvn clean package
mvn spring-boot:run
```

2. Open browser and login:

[http://localhost:8081/oauth2/authorization/keycloak](http://localhost:8081/oauth2/authorization/keycloak)

- **Login with the Keycloak realm user**: `e.g. testuser / password`
- After successful login, you will be redirected to `/api/v1/auth/success`
- The controller returns the Keycloak access token and user attributes

3. Access protected endpoints:

- Example: `GET http://localhost:8081/api/v1/employees`
- If not authenticated, you’ll be redirected to Keycloak login
- Spring Security validates the Keycloak JWT automatically
- You can also use the access token in `Authorization: Bearer <token>` headers to call APIs directly

This demonstrates a full OAuth2 login flow with Keycloak.

---

## 5) Notes

- **Issuer auto-discovery**: Spring fetches `.well-known/openid-configuration` using `issuer-uri`.
- **UserInfo endpoint**: `/realms/{realm}/protocol/openid-connect/userinfo` returns normalized claims.
- **Token endpoint**: `/realms/{realm}/protocol/openid-connect/token` is used for authorization code / client credentials flows.
- **CORS / Hosts**: Add frontend URLs in Keycloak client settings (Redirect URIs, Web Origins).
- **Dev vs Prod**: `start-dev` is for development only. Production requires a persistent database, strong credentials, and proper TLS configuration.

### Optional: Custom JWT

You can issue your own JWT from the Keycloak access token to:
- Add app-specific claims
- Simplify token validation in microservices
- Decouple API security from Keycloak

This is useful if you want more control over roles or token structure.

---

## License

Free Software, by [Siraj Chaudhary](https://www.linkedin.com/in/sirajchaudhary/)
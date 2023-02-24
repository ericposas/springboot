# Setup

## Keycloak

### Configuration
- access all keycloak endpoint and configuration settings here
- http://localhost:8000/auth/realms/dev/.well-known/openid-configuration
- use these to configure Postman for API testing
- get the Keycloak Client Secret in the Keycloak dashboard
    - in your Client settings 'spring-api', make sure that "Access Type" is set to "confidential"
    - after that's done, you should see your Secret in the "Credentials" tab


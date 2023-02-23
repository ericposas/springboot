
POST to auth endpoint to get the access_token and refresh_token (used to get new access_token later):
```
curl -d "client_id=$KEYCLOAK_CLIENT_ID" \
     -d "client_secret=$KEYCLOAK_CLIENT_SECRET" \
	 -d "scope=openid test:create scope:other" \
     -d "grant_type=password" \
     -d "password=$USER_PASSWORD" \
     -d "username=$USER" "http://localhost:8000/auth/realms/dev/protocol/openid-connect/token" | jq .access_token, .refresh_token
```

POST refresh_token to get a new access_token:
```
curl -d "client_id=$KEYCLOAK_CLIENT_ID" \
     -d "client_secret=$KEYCLOAK_CLIENT_SECRET" \
	 -d "grant_type=refresh_token" \
	 -d "refresh_token=$JWT_TOKEN" \
	 -d "username=$USERNAME" "http://localhost:8000/auth/realms/dev/protocol/openid-connect/token" | jq .access_token, .refresh_token
```

example to use access_token in protected endpoint in application (resource server):

```
curl -XGET http://localhost:8080/api/test/admin \
	-H 'Content-type: application/json' \
	-H 'Authorization: Bearer $ACCESS_TOKEN'
```

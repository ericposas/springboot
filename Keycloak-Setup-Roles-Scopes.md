
in Keycloak Admin:

Add a Role:

Add an app role, something like "app_admin" or "app_user"
Click on "Add Role"
Type the name of the role (i.e. "app_user") in *Role 
Optionally, add a Description
Click "Save"
After the role is created, go to the "Details" tab
Toggle on the "Composite Roles" switch 
A new "Composite Roles" menu will appear
Click on the "Client Roles" dropdown and select your Client ID
From the "Available Roles", add your Client-specific role or multiple role(s) (in case of an Admin or Superuser)
  (i.e. "app_admin" will get the "admin" and "user" roles association)


Add a Scope:

Go to "Client Scopes"
Click on "Create"
Add a *Name for the client scope (i.e. "create:comment" or "view:item")
Add an optional Description
Click on the "Scope" tab to map the scope to a role
Click on the "Client Roles"  dropdown and select your Client ID
Add your newly defined scope (i.e. "view:item") to an appropriate predefined Role, like "user" (from your Client's available roles)
  (this will allow the scope to be attached to the Role to be used as a scope in the application)

Go to "Clients", Select your Client ID
Click on the "Client Scopes" tab
Add your new scope to the "Assigned Default Client Scopes"
  (we need to add the scope by default, or otherwise request it in the "scope=" curl param)
  (since the scope was added to the "user" role, it can now show up by default if a person has the "user" role assigned to them)

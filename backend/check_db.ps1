# Database connection details
$mysql = "mysql -u root -p@run2007 expense_tracker"

# Check users table
Write-Host "Checking users table..." -ForegroundColor Cyan
& cmd /c "$mysql -e 'SELECT * FROM users;'"

# Check roles table
Write-Host "`nChecking roles table..." -ForegroundColor Cyan
& cmd /c "$mysql -e 'SELECT * FROM roles;'"

# Check user roles
Write-Host "`nChecking user roles..." -ForegroundColor Cyan
& cmd /c "$mysql -e 'SELECT u.username, r.name as role FROM users u JOIN user_roles ur ON u.id = ur.user_id JOIN roles r ON ur.role_id = r.id;'"

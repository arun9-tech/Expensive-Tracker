-- Make sure the database exists
CREATE DATABASE IF NOT EXISTS expense_tracker;
USE expense_tracker;

-- Create roles if they don't exist
INSERT IGNORE INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_MODERATOR'), ('ROLE_ADMIN');

-- Create test user (password: Test123!)
INSERT IGNORE INTO users (username, email, password) 
VALUES ('admin', 'admin@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');

-- Assign admin role to the user
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- Verify the user was created
SELECT u.username, u.email, r.name as role 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id 
WHERE u.username = 'admin';

-- Make sure the database exists
CREATE DATABASE IF NOT EXISTS expense_tracker;
USE expense_tracker;

-- Clear existing data (be careful with this in production!)
DELETE FROM user_roles;
DELETE FROM users;
DELETE FROM roles;

-- Create roles
INSERT INTO roles (name) VALUES 
    ('ROLE_USER'),
    ('ROLE_MODERATOR'),
    ('ROLE_ADMIN');

-- Create admin user with password: Admin@123
-- Password is bcrypt hashed version of 'Admin@123'
INSERT INTO users (username, email, password, created_at, updated_at) 
VALUES (
    'admin', 
    'admin@example.com', 
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
    NOW(), 
    NOW()
);

-- Assign admin role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- Verify the user was created
SELECT u.username, u.email, r.name as role 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id 
WHERE u.username = 'admin';

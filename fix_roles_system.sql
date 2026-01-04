-- Fix Roles System Migration Script
-- Run these commands in your PostgreSQL identity_db database

-- Step 1: Create the user_roles junction table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Step 2: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

-- Step 3: Remove the user_id column from roles table (it's no longer needed)
-- First, migrate existing relationships to the junction table
INSERT INTO user_roles (user_id, role_id)
SELECT r.user_id, r.id
FROM roles r
WHERE r.user_id IS NOT NULL;

-- Step 4: Drop the foreign key constraint and column
ALTER TABLE roles DROP CONSTRAINT IF EXISTS fk_roles_user_id;
ALTER TABLE roles DROP COLUMN IF EXISTS user_id;

-- Step 5: Clean up roles table - keep only unique role definitions
-- Create a temporary table with unique roles
CREATE TEMP TABLE unique_roles AS
SELECT DISTINCT name
FROM roles;

-- Delete all existing roles
DELETE FROM roles;

-- Insert back only unique role definitions
INSERT INTO roles (name, created_at, updated_at)
SELECT name, NOW(), NOW()
FROM unique_roles;

-- Step 6: Re-establish relationships in user_roles table
-- This maps users to the correct role IDs based on role names
INSERT INTO user_roles (user_id, role_id)
SELECT DISTINCT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE r.name = 'USER'  -- Assign USER role to all existing users
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);

-- Step 7: Verify the migration
SELECT
    'Users count' as info,
    COUNT(*) as count
FROM users
UNION ALL
SELECT
    'Roles count' as info,
    COUNT(*) as count
FROM roles
UNION ALL
SELECT
    'User-Role relationships' as info,
    COUNT(*) as count
FROM user_roles;

-- Optional: Show the final structure
SELECT
    u.email,
    r.name as role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
ORDER BY u.email, r.name;

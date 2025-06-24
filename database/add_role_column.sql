-- SQL script to add role column to existing usuarios table
-- Execute this script on your database to update the schema

-- Add role column to usuarios table
ALTER TABLE usuarios 
ADD COLUMN role VARCHAR(20) DEFAULT 'CAJERO' AFTER nombre;

-- Update existing users to have default CAJERO role
UPDATE usuarios 
SET role = 'CAJERO' 
WHERE role IS NULL;

-- Optional: Set the first user as ADMINISTRADOR if needed
-- UPDATE usuarios 
-- SET role = 'ADMINISTRADOR' 
-- WHERE id = 1;

-- Verify the changes
SELECT id, nombreUsuario, nombre, role, activo FROM usuarios;

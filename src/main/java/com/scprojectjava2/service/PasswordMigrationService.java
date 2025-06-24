package com.scprojectjava2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class PasswordMigrationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[ayb]\\$\\d{2}\\$.{53}$");

    public boolean isBCryptHash(String password) {
        return password != null && BCRYPT_PATTERN.matcher(password).matches();
    }

    public List<Map<String, Object>> findUsersWithPlaintextPasswords() {
        String sql = "SELECT id, nombre_usuario, contrasena, nombre FROM usuarios";
        List<Map<String, Object>> allUsers = jdbcTemplate.queryForList(sql);
        
        return allUsers.stream()
                .filter(user -> !isBCryptHash((String) user.get("contrasena")))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public int migratePasswordsToBeforeAddingEncryption() {
        List<Map<String, Object>> usersWithPlaintextPasswords = findUsersWithPlaintextPasswords();
        int migratedCount = 0;

        for (Map<String, Object> user : usersWithPlaintextPasswords) {
            String plaintextPassword = (String) user.get("contrasena");
            String hashedPassword = passwordEncoder.encode(plaintextPassword);
            Long userId = ((Number) user.get("id")).longValue();
            
            String updateSql = "UPDATE usuarios SET contrasena = ? WHERE id = ?";
            jdbcTemplate.update(updateSql, hashedPassword, userId);
            migratedCount++;
            
            System.out.println("Migrada contraseña para usuario: " + user.get("nombre_usuario"));
        }

        return migratedCount;
    }

    public MigrationStats getMigrationStats() {
        String sql = "SELECT id, nombre_usuario, contrasena, nombre FROM usuarios";
        List<Map<String, Object>> allUsers = jdbcTemplate.queryForList(sql);
        
        long totalUsers = allUsers.size();
        long encryptedUsers = allUsers.stream()
                .filter(user -> isBCryptHash((String) user.get("contrasena")))
                .count();
        long plaintextUsers = totalUsers - encryptedUsers;

        return new MigrationStats(totalUsers, encryptedUsers, plaintextUsers);
    }

    public static class MigrationStats {
        private final long totalUsers;
        private final long encryptedUsers;
        private final long plaintextUsers;

        public MigrationStats(long totalUsers, long encryptedUsers, long plaintextUsers) {
            this.totalUsers = totalUsers;
            this.encryptedUsers = encryptedUsers;
            this.plaintextUsers = plaintextUsers;
        }

        public long getTotalUsers() { return totalUsers; }
        public long getEncryptedUsers() { return encryptedUsers; }
        public long getPlaintextUsers() { return plaintextUsers; }
        public double getEncryptionPercentage() { 
            return totalUsers == 0 ? 0 : (double) encryptedUsers / totalUsers * 100; 
        }

        @Override
        public String toString() {
            return String.format(
                "Total usuarios: %d, Cifrados: %d (%.1f%%), En texto plano: %d",
                totalUsers, encryptedUsers, getEncryptionPercentage(), plaintextUsers
            );
        }
    }
}

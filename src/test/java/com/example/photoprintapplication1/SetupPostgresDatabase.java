package com.example.photoprintapplication1;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Создание БД photoprint и таблиц лабы 2.
 * Запуск: mvnw test -Dtest=SetupPostgresDatabase
 */
class SetupPostgresDatabase {

    private static final String HOST = "localhost:5432";
    private static final String DB_NAME = "photoprint";
    private static final String DB_USER = "photoprint_user";
    private static final String DB_PASS = "photoprint_pass";

    @Test
    void createDatabaseUserAndTables() throws Exception {
        if (!canConnectPhotoprint()) {
            ensureDatabaseAndUser();
        } else {
            System.out.println("[setup] Already connected to " + DB_NAME + " as " + DB_USER);
        }
        runSchemaOnPhotoprint();
        listTables();
        printRowCounts();
    }

    private boolean canConnectPhotoprint() {
        try (Connection c = DriverManager.getConnection(
                "jdbc:postgresql://" + HOST + "/" + DB_NAME, DB_USER, DB_PASS)) {
            return c.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    private void ensureDatabaseAndUser() throws Exception {
        List<String> adminPasswords = List.of(
                System.getenv("POSTGRES_PASSWORD"),
                "postgres",
                "admin",
                "root",
                ""
        );

        Exception last = null;
        for (String pass : adminPasswords) {
            if (pass == null) {
                continue;
            }
            try (Connection admin = DriverManager.getConnection(
                    "jdbc:postgresql://" + HOST + "/postgres", "postgres", pass)) {
                admin.setAutoCommit(true);
                try (Statement st = admin.createStatement()) {
                    var roleRs = st.executeQuery(
                            "SELECT 1 FROM pg_roles WHERE rolname = '" + DB_USER + "'");
                    if (!roleRs.next()) {
                        st.executeUpdate("CREATE USER " + DB_USER + " WITH PASSWORD '" + DB_PASS + "'");
                        System.out.println("[setup] Created user " + DB_USER);
                    }

                    var rs = st.executeQuery(
                            "SELECT 1 FROM pg_database WHERE datname = '" + DB_NAME + "'");
                    if (!rs.next()) {
                        st.executeUpdate("CREATE DATABASE " + DB_NAME + " OWNER " + DB_USER);
                        System.out.println("[setup] Created database " + DB_NAME);
                    } else {
                        System.out.println("[setup] Database " + DB_NAME + " already exists");
                    }
                    st.executeUpdate("GRANT ALL PRIVILEGES ON DATABASE " + DB_NAME + " TO " + DB_USER);
                }
                System.out.println("[setup] Connected as postgres");
                return;
            } catch (Exception e) {
                last = e;
            }
        }
        throw new IllegalStateException("Cannot connect as postgres. Set POSTGRES_PASSWORD env.", last);
    }

    private void runSchemaOnPhotoprint() throws Exception {
        Path sqlFile = Path.of("docs/pgadmin-setup.sql");
        String sql = Files.readString(sqlFile);
        sql = sql.replaceAll("(?m)^--.*$", "");
        sql = sql.replaceAll("DO \\$\\$[\\s\\S]*?END \\$\\$;", "");

        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://" + HOST + "/" + DB_NAME, DB_USER, DB_PASS)) {
            conn.setAutoCommit(true);
            try (Statement st = conn.createStatement()) {
                for (String part : sql.split(";")) {
                    String stmt = part.trim();
                    if (stmt.isEmpty()) {
                        continue;
                    }
                    if (stmt.toUpperCase().startsWith("SELECT")) {
                        continue;
                    }
                    st.execute(stmt);
                }
            }
            try (Statement st = conn.createStatement()) {
                st.execute("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO " + DB_USER);
                st.execute("GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO " + DB_USER);
            }
        }
        System.out.println("[setup] Tables created/verified on " + DB_NAME);
    }

    private void listTables() throws Exception {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://" + HOST + "/" + DB_NAME, DB_USER, DB_PASS);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("""
                     SELECT table_name FROM information_schema.tables
                     WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
                     ORDER BY table_name
                     """)) {
            List<String> names = new ArrayList<>();
            while (rs.next()) {
                names.add(rs.getString(1));
            }
            System.out.println("[setup] Tables (" + names.size() + "): " + String.join(", ", names));
            if (names.size() < 8) {
                throw new IllegalStateException("Expected 8 tables, found " + names.size());
            }
        }
    }

    private void printRowCounts() throws Exception {
        String[] tables = {
                "users", "product", "license_type", "license",
                "device", "device_license", "license_history", "user_sessions"
        };
        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://" + HOST + "/" + DB_NAME, DB_USER, DB_PASS);
             Statement st = conn.createStatement()) {
            for (String table : tables) {
                try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    rs.next();
                    System.out.println("[setup] " + table + ": " + rs.getInt(1) + " rows");
                }
            }
        }
    }
}

package net.burngames.jafig.types;

import net.burngames.jafig.Jafig;
import net.burngames.jafig.annotations.Options;

import java.lang.reflect.Field;
import java.sql.*;

/**
 * @author PaulBGD
 */
public class JafigMySQL extends Jafig {

    private static final String[] invalidCharacters = new String[]{".", "/", "\\", "`"};

    private Connection connection;
    private String url;
    private String database;
    private String user;
    private String password;

    public JafigMySQL(Object[] parameters) {
        super(parameters);
        if (parameters.length != 4) {
            throw new IllegalArgumentException("Parameters must be URL, database, user, and password");
        }
        this.url = String.valueOf(parameters[0]);
        this.database = String.valueOf(parameters[1]);
        if (!validDatabase()) {
            throw new IllegalArgumentException("Invalid database name");
        }
        this.user = String.valueOf(parameters[2]);
        this.password = String.valueOf(parameters[3]);

        try (PreparedStatement stmt = prepare("CREATE DATABASE IF NOT EXISTS `%s`", database)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public <T> T load(Class<T> clazz) {
        String table = classToTable(clazz);
        boolean exists = false;
        try (PreparedStatement stmt = prepare("SHOW TABLES LIKE `%s`.`%s`", database, table)) {
            exists = stmt.executeQuery().first();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!exists) {
            StringBuilder create = new StringBuilder("_id INT(8) AUTO_INCREMENT PRIMARY KEY");
            for (Field field : clazz.getFields()) {
                String name = field.getName();
                Options options = field.getAnnotation(Options.class);
                if (options != null) {
                    if (!options.name().equals("")) {
                        name = options.name(); // they specified a name for this field
                    }
                }
                create.append(",").append(name).append(" ").append(classToType(field.getType()));
            }
            try (PreparedStatement stmt = prepare("CREATE TABLE `%s`.`%s` (%s)", database, table, create.toString())) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void save(Object object) {

    }

    private PreparedStatement prepare(String statement, String... arguments) throws SQLException {
        return getConnection().prepareStatement(String.format(statement, arguments));
    }

    private Connection getConnection() throws SQLException {
        if (connection != null) {
            if (connection.isValid(1)) {
                return connection;
            } else {
                connection.close();
            }
        }
        connection = DriverManager.getConnection(url, user, password);
        return connection;
    }

    private String classToTable(Class<?> clazz) {
        return clazz.getSimpleName().replace('.', '_');
    }

    private String classToType(Class<?> clazz) {
        if (clazz == int.class || clazz == long.class) {
            return "BIGINT(9223372036854775807)";
        } else if (clazz == short.class) {
            return "INT(200)";
        } else if (clazz == double.class || clazz == float.class) {
            return "VARCHAR(255)";
        } else if (clazz == String.class) {
            return "TEXT";
        } else if (clazz == boolean.class) {
            return "BIT(1)";
        } else if (clazz == char.class) {
            return "CHAR(1)";
        }
        return null;
    }

    private boolean validDatabase() {
        int length = database.length();
        while (length-- != 0) {
            for (String invalid : invalidCharacters) {
                if (database.charAt(length) == invalid.charAt(0)) {
                    return false;
                }
            }
        }
        return true;
    }

}

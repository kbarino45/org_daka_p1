package com.revature.daka.repositories;

import com.revature.daka.models.TestOne;
import com.revature.daka.models.TestTwo;
import com.revature.daka.persistence.Column;
import com.revature.daka.persistence.Id;
import com.revature.daka.persistence.Table;
import com.revature.daka.util.JdbcConnection;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.*;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.reflections.scanners.Scanners.TypesAnnotated;

public class ModelRepoImplTest implements ModelRepoTest {
    static Connection conn = JdbcConnection.getConnection();
    static Reflections reflections = new Reflections("com.revature");

    @Test
    @Override
    public void addRecord() {
        // Creating an object here only for testing purposes. The real method will have an object passed in.
        Object greatObject = new TestOne(1, "omega_test", "action", false, 12345);
        assertNotNull(greatObject);

        Class<?> c = greatObject.getClass();
        Field[] fields = c.getDeclaredFields();
        int numOfFields = fields.length;
        Table table = c.getAnnotation(Table.class);
        String tableName = table.name();
        assertEquals("test_1", tableName);

        // Build column names and ?'s for the SQL query.
        String cn = "";
        String id = "";
        String qm = "";

        // Removing the field marked with @Id from fields[] so that JDBC doesn't think it has a parameterized value.
        for (int i = 0; i < numOfFields; i++) {
            if (fields[i].isAnnotationPresent(Id.class)) {
                Column column = fields[i].getAnnotation(Column.class);

                Id idField = fields[i].getAnnotation(Id.class);
                id = idField.type();
                fields = ArrayUtils.remove(fields, i);
                numOfFields = fields.length;

                try {
                    cn += column.name();

                    if (i < numOfFields - 1) {
                        cn += ", ";
                        id += ",";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 0; i < numOfFields; i++) {
            fields[i].setAccessible(true);
            Column column = fields[i].getAnnotation(Column.class);

            try {
                cn += column.name();
                qm += "?";

                if (i < numOfFields - 1) {
                    cn += ", ";
                    qm += ",";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String sql = "INSERT INTO " + tableName + " (" + cn + ") VALUES (" + id + qm + ")";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            for (int i = 0; i < numOfFields; i++) {
                switch (fields[i].getType().toString()) {
                    case "int":
                        ps.setInt(i + 1, fields[i].getInt(greatObject));
                        break;
                    case "long":
                        ps.setLong(i + 1, fields[i].getLong(greatObject));
                        break;
                    case "short":
                        ps.setShort(i + 1, fields[i].getShort(greatObject));
                        break;
                    case "byte":
                        ps.setByte(i + 1, fields[i].getByte(greatObject));
                        break;
                    case "class java.lang.String":
                        ps.setString(i + 1, (String) fields[i].get(greatObject));
                        break;
                    case "boolean":
                        ps.setBoolean(i + 1, fields[i].getBoolean(greatObject));
                        break;
                    case "double":
                        ps.setDouble(i + 1, fields[i].getDouble(greatObject));
                        break;
                    case "float":
                        ps.setFloat(i + 1, fields[i].getFloat(greatObject));
                        break;
                    default:
                        System.out.println("Unsupported: " + fields[i].getType());
                }
            }
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Override
    public void getRecord() {
        // Setting params for testing purposes.
        String tableName = "test_1";
        int id = 2;

        String primaryKeyName = "";
        Set<Class<?>> entities = reflections.get(TypesAnnotated.with(Table.class).asClass());

        for (Class<?> e : entities) {
            String fqcn = e.getName();
            Table entity = e.getAnnotation(Table.class);

            if (entity.name().equals(tableName)) {
                Class<?> c = null;
                try {
                    c = Class.forName(fqcn);
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }

                assert c != null;
                Field[] fields = c.getDeclaredFields();
                int numOfFields = fields.length;

                for (Field field : fields) {
                    field.setAccessible(true);
                    Column column = field.getAnnotation(Column.class);

                    if (field.isAnnotationPresent(Id.class)) {
                        primaryKeyName = column.name();
                    }
                }

                Constructor<?> ctor = null;
                try {
                    ctor = c.getConstructor();
                } catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                }

                Object output = null;
                try {
                    assert ctor != null;
                    output = ctor.newInstance();
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }

                assert output != null;
                Field[] outputFields = output.getClass().getDeclaredFields();

                for (Field f : outputFields) {
                    f.setAccessible(true);
                }

                String sql = "SELECT * FROM " + tableName + " WHERE " + primaryKeyName + " = ?";

                try {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, id);
                    ResultSet rs = ps.executeQuery();
                    ResultSetMetaData rsMetaData = rs.getMetaData();

                    while (rs.next()) {
                        for (int i = 0; i < numOfFields; i++) {
                            try {
                                switch (fields[i].getType().toString()) {
                                    case "int":
                                        outputFields[i].setInt(output, rs.getInt(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    case "long":
                                        outputFields[i].setLong(output, rs.getLong(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    case "short":
                                        outputFields[i].setShort(output, rs.getShort(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    case "byte":
                                        outputFields[i].setByte(output, rs.getByte(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    case "class java.lang.String":
                                        outputFields[i].set(output, rs.getString(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    case "boolean":
                                        outputFields[i].setBoolean(output, rs.getBoolean(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    case "double":
                                        outputFields[i].setDouble(output, rs.getDouble(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    case "float":
                                        outputFields[i].setFloat(output, rs.getFloat(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    default:
                                        System.out.println("Unsupported: " + fields[i].getType());
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                assertNotNull(output);
                System.out.println(output);
                // return objects;
            }
        }
        // return null;
    }

    @Test
    @Override
    public void getAllRecords() {
        // Setting params for testing purposes.
        String tableName = "test_2";
        Set<Class<?>> entities = reflections.get(TypesAnnotated.with(Table.class).asClass());
        assertNotNull(entities);
        List<Object> objects = null;

        for (Class<?> e : entities) {
            String fqcn = e.getName();
            Table entity = e.getAnnotation(Table.class);

            if (entity.name().equals(tableName)) {
                assertEquals("test_2", entity.name());
                Class<?> c = null;
                try {
                    c = Class.forName(fqcn);
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                assert c != null;
                Field[] fields = c.getDeclaredFields();
                int numOfFields = fields.length;

                for (Field field : fields) {
                    field.setAccessible(true);
                }

                String sql = "SELECT * FROM " + tableName;

                try {
                    objects = new ArrayList<>();
                    List<Integer> dataTypes = new ArrayList<>();
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery();
                    ResultSetMetaData rsMetaData = rs.getMetaData();

                    while (rs.next()) {
                        Constructor<?> ctor = null;
                        try {
                            ctor = c.getConstructor();
                        } catch (NoSuchMethodException ex) {
                            ex.printStackTrace();
                        }

                        Object output = null;
                        try {
                            assert ctor != null;
                            output = ctor.newInstance();
                        } catch (InstantiationException | InvocationTargetException | IllegalAccessException ex) {
                            ex.printStackTrace();
                        }

                        assert output != null;
                        Field[] outputFields = output.getClass().getDeclaredFields();

                        for (Field f : outputFields) {
                            f.setAccessible(true);
                        }

                        for (int i = 0; i < numOfFields; i++) {
                            try {
                                switch (fields[i].getType().toString()) {
                                    case "int":
                                        outputFields[i].setInt(output, rs.getInt(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    case "long":
                                        outputFields[i].setLong(output, rs.getLong(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    case "short":
                                        outputFields[i].setShort(output, rs.getShort(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    case "byte":
                                        outputFields[i].setByte(output, rs.getByte(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    case "class java.lang.String":
                                        outputFields[i].set(output, rs.getString(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    case "boolean":
                                        outputFields[i].setBoolean(output, rs.getBoolean(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    case "double":
                                        outputFields[i].setDouble(output, rs.getDouble(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    case "float":
                                        outputFields[i].setFloat(output, rs.getFloat(rsMetaData.getColumnName(i + 1)));
                                        break;
                                    default:
                                        System.out.println("Unsupported: " + fields[i].getType());
                                }
                            } catch (SQLException | IllegalAccessException ex) {
                                ex.printStackTrace();
                            }
                        }
                        objects.add(output);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                // return objects;
            }
        }
        System.out.println(objects);
        // return null;
    }

    @Test
    @Override
    public void updateRecord() {
        //TODO: String Object
        Object greatObject = new TestOne(1, "alpha_test", "action", false, 69);

        Class<?> c = greatObject.getClass();
        Field[] fields = c.getDeclaredFields();
        int numOfFields = fields.length;
        Table table = c.getAnnotation(Table.class);
        String tableName = table.name();

        // Remove id from fields array
        String cn = ""; //column name
        String id = ""; //id (if present)
        Field id_field = null;
        for (int i = 0; i < numOfFields; i++) {
            if (fields[i].isAnnotationPresent(Id.class)) {
                Column column = fields[i].getAnnotation(Column.class);
                fields[i].setAccessible(true);
                id_field = fields[i];

                fields = ArrayUtils.remove(fields, i);
                numOfFields = fields.length;

                try {
                    id = column.name();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Build column names for the SQL query.
        for (int i = 0; i < numOfFields; i++) {
            fields[i].setAccessible(true);
            Column column = fields[i].getAnnotation(Column.class);

            try {
                cn += column.name() + "=?";

                if (i < numOfFields - 1) {
                    cn += ", ";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String sql = "UPDATE " + tableName + " SET " + cn + " WHERE " + id + "=? RETURNING *";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            for (int i = 0; i < numOfFields; i++) {
                switch (fields[i].getType().toString()) {
                    case "int":
                        ps.setInt(i + 1, fields[i].getInt(greatObject));
                        break;
                    case "long":
                        ps.setLong(i + 1, fields[i].getLong(greatObject));
                        break;
                    case "short":
                        ps.setShort(i + 1, fields[i].getShort(greatObject));
                        break;
                    case "byte":
                        ps.setByte(i + 1, fields[i].getByte(greatObject));
                        break;
                    case "class java.lang.String":
                        ps.setString(i + 1, (String) fields[i].get(greatObject));
                        break;
                    case "boolean":
                        ps.setBoolean(i + 1, fields[i].getBoolean(greatObject));
                        break;
                    case "double":
                        ps.setDouble(i + 1, fields[i].getDouble(greatObject));
                        break;
                    case "float":
                        ps.setFloat(i + 1, fields[i].getFloat(greatObject));
                        break;
                    default:
                        System.out.println("Unsupported: " + fields[i].getType());
                } //end switch
            } //end for

            //final parameter - id; switch to determine id type
            switch (id_field.getType().toString()) {
                case "int":
                    ps.setInt(fields.length + 1, id_field.getInt(greatObject));
                    break;
                case "long":
                    ps.setLong(fields.length + 1, id_field.getLong(greatObject));
                    break;
                case "short":
                    ps.setShort(fields.length + 1, id_field.getShort(greatObject));
                    break;
                case "byte":
                    ps.setByte(fields.length + 1, id_field.getByte(greatObject));
                    break;
                case "class java.lang.String":
                    ps.setString(fields.length + 1, (String) id_field.get(greatObject));
                    break;
                case "boolean":
                    ps.setBoolean(fields.length + 1, id_field.getBoolean(greatObject));
                    break;
                case "double":
                    ps.setDouble(fields.length + 1, id_field.getDouble(greatObject));
                    break;
                case "float":
                    ps.setFloat(fields.length + 1, id_field.getFloat(greatObject));
                    break;
                default:
                    System.out.println("Unsupported: " + id_field.getType());
            } //end switch

            ps.executeQuery();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    @Override
    public void deleteRecord() {
        //TODO: String Object
        Object greatObject = new TestTwo(2, "omega_test", true);

        Class<?> c = greatObject.getClass();
        Field[] fields = c.getDeclaredFields();
        int numOfFields = fields.length;
        Table table = c.getAnnotation(Table.class);
        String tableName = table.name();
        int result_id = -1;
        String primaryKeyName = "";

        for (int i = 0; i < numOfFields; i++) {
            if (fields[i].isAnnotationPresent(Id.class)) {
                Column idColumn = fields[i].getAnnotation(Column.class);
                primaryKeyName = idColumn.name();
            }

            try {
                fields[i] = c.getDeclaredField(primaryKeyName);
                fields[i].setAccessible(true);
                result_id = fields[i].getInt(greatObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String sql = "DELETE FROM " + tableName + " WHERE "+ primaryKeyName +"=?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,result_id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

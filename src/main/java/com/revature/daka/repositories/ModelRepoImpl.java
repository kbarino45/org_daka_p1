package com.revature.daka.repositories;

import com.revature.daka.persistence.Column;
import com.revature.daka.persistence.Table;
import com.revature.daka.util.JdbcConnection;
import com.revature.daka.persistence.Id;
import com.revature.daka.util.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;

import static org.reflections.scanners.Scanners.TypesAnnotated;

public class ModelRepoImpl implements ModelRepo {
    static Connection conn = JdbcConnection.getConnection();
    static Reflections reflections = new Reflections("com.revature");

    @Override
    public void addRecord(Object o) {
        Logger.logger.info("addRecord() started");
        Class<?> c = o.getClass();
        Field[] fields = c.getDeclaredFields();
        int numOfFields = fields.length;
        Table table = c.getAnnotation(Table.class);
        String tableName = table.name();

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
                        ps.setInt(i + 1, fields[i].getInt(o));
                        break;
                    case "long":
                        ps.setLong(i + 1, fields[i].getLong(o));
                        break;
                    case "short":
                        ps.setShort(i + 1, fields[i].getShort(o));
                        break;
                    case "byte":
                        ps.setByte(i + 1, fields[i].getByte(o));
                        break;
                    case "class java.lang.String":
                        ps.setString(i + 1, (String) fields[i].get(o));
                        break;
                    case "boolean":
                        ps.setBoolean(i + 1, fields[i].getBoolean(o));
                        break;
                    case "double":
                        ps.setDouble(i + 1, fields[i].getDouble(o));
                        break;
                    case "float":
                        ps.setFloat(i + 1, fields[i].getFloat(o));
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

    @Override
    public Object getRecord(String tableName, int id) {
        Logger.logger.info("getRecord() started");
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
                return output;
            }
        }
        return null;
    }

    @Override
    public List<?> getAllRecords(String tableName) {
        Logger.logger.info("getAllRecord() started");
        Set<Class<?>> entities = reflections.get(TypesAnnotated.with(Table.class).asClass());
        List<Object> objects = null;

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
                }

                String sql = "SELECT * FROM " + tableName;

                try {
                    objects = new ArrayList<>();
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
                return objects;
            }
        }
        return null;
    }

    @Override
    public void updateRecord(Object o) {
        Logger.logger.info("updateRecord() started");
        Class<?> c = o.getClass();
        Field[] fields = c.getDeclaredFields();
        int numOfFields = fields.length;
        Table table = c.getAnnotation(Table.class);
        String tableName = table.name();

        // Remove @Id field from fields[].
        String cn = "";
        String id = "";
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
                        ps.setInt(i + 1, fields[i].getInt(o));
                        break;
                    case "long":
                        ps.setLong(i + 1, fields[i].getLong(o));
                        break;
                    case "short":
                        ps.setShort(i + 1, fields[i].getShort(o));
                        break;
                    case "byte":
                        ps.setByte(i + 1, fields[i].getByte(o));
                        break;
                    case "class java.lang.String":
                        ps.setString(i + 1, (String) fields[i].get(o));
                        break;
                    case "boolean":
                        ps.setBoolean(i + 1, fields[i].getBoolean(o));
                        break;
                    case "double":
                        ps.setDouble(i + 1, fields[i].getDouble(o));
                        break;
                    case "float":
                        ps.setFloat(i + 1, fields[i].getFloat(o));
                        break;
                    default:
                        System.out.println("Unsupported: " + fields[i].getType());
                }
            }

            //final parameter - id; switch to determine id type.
            switch (id_field.getType().toString()) {
                case "int":
                    ps.setInt(fields.length + 1, id_field.getInt(o));
                    break;
                case "long":
                    ps.setLong(fields.length + 1, id_field.getLong(o));
                    break;
                case "short":
                    ps.setShort(fields.length + 1, id_field.getShort(o));
                    break;
                case "byte":
                    ps.setByte(fields.length + 1, id_field.getByte(o));
                    break;
                case "class java.lang.String":
                    ps.setString(fields.length + 1, (String) id_field.get(o));
                    break;
                case "boolean":
                    ps.setBoolean(fields.length + 1, id_field.getBoolean(o));
                    break;
                case "double":
                    ps.setDouble(fields.length + 1, id_field.getDouble(o));
                    break;
                case "float":
                    ps.setFloat(fields.length + 1, id_field.getFloat(o));
                    break;
                default:
                    System.out.println("Unsupported: " + id_field.getType());
            }
            ps.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteRecord(Object o) {
        Logger.logger.info("deleteRecord() started");
        Class<?> c = o.getClass();
        Field[] fields = c.getDeclaredFields();
        int numOfFields = fields.length;
        Table table = c.getAnnotation(Table.class);
        String tableName = table.name();
        int result_id = -1;
        String primaryKeyName = "";
        String primaryKeyField = "";

        for (int i = 0; i < numOfFields; i++) {
            if (fields[i].isAnnotationPresent(Id.class)) {
                Column idColumn = fields[i].getAnnotation(Column.class);
                primaryKeyName = idColumn.name();
                primaryKeyField = fields[i].getName();
            }

            try {
                fields[i] = c.getDeclaredField(primaryKeyField);
                fields[i].setAccessible(true);
                result_id = fields[i].getInt(o);
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
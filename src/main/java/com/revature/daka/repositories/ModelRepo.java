package com.revature.daka.repositories;

import java.util.List;

public interface ModelRepo {
    public void addRecord(Object o);
    public Object getRecord(String tableName, int id);
    public List<?> getAllRecords(String tableName);
    public void updateRecord(Object o);
    public void deleteRecord(Object o);
}

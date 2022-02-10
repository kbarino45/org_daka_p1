package com.revature.daka.repositories;

public interface ModelRepoTest {
    //Paramter: Object o
    public void addRecord();
    //Parameter String tableName, int id
    public void getRecord();
    //Parameter String tableName
    public void getAllRecords();
    //Parameter Object o
    public void updateRecord();
    //Parameter Object o
    public void deleteRecord();
}
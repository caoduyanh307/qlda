package com.ddangkhoa.trafficfx.controller;

import java.util.List;

import com.ddangkhoa.trafficfx.dao.GenericCrudDao;
import com.ddangkhoa.trafficfx.model.DataRecord;

public class GenericManagementController {
    private final GenericCrudDao dao;

    public GenericManagementController(String tableName, String primaryKeyColumn, List<String> columns, String accountId) {
        this.dao = new GenericCrudDao(tableName, primaryKeyColumn, columns, accountId);
    }

    public List<DataRecord> searchRecords(String keyword) {
        return dao.search(keyword);
    }

    public void insertRecord(DataRecord record) {
        dao.insert(record);
    }

    public List<DataRecord> findAllRecords() {
        return dao.findAll();
    }

    public int importRecords(List<DataRecord> records, String prefix, int digits) {
        int success = 0;
        for (DataRecord record : records) {
            if (record.get(dao.getPrimaryKeyColumn()).isBlank()) {
                record.put(dao.getPrimaryKeyColumn(), dao.generateNextId(prefix, digits));
            }
            dao.insert(record);
            success++;
        }
        return success;
    }

    public void updateRecord(DataRecord record) {
        dao.update(record);
    }

    public void deleteRecord(String id) {
        dao.deleteById(id);
    }

    public String generateNextId(String prefix, int digits) {
        return dao.generateNextId(prefix, digits);
    }
}

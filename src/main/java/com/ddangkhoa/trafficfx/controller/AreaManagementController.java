package com.ddangkhoa.trafficfx.controller;

import java.util.List;

import com.ddangkhoa.trafficfx.dao.AreaDao;
import com.ddangkhoa.trafficfx.model.Area;

public class AreaManagementController {
    private final AreaDao areaDao;

    public AreaManagementController() {
        this.areaDao = new AreaDao();
    }

    public List<Area> searchAreas(String keyword) {
        return areaDao.search(keyword);
    }

    public List<Area> findAllAreas() {
        return areaDao.findAll();
    }

    public void insertArea(Area area) {
        areaDao.insert(area);
    }

    public void updateArea(Area area) {
        areaDao.update(area);
    }

    public void deleteArea(String areaId) {
        areaDao.deleteById(areaId);
    }

    public String generateNextAreaId() {
        int maxNumber = findAllAreas().stream()
                .map(Area::getAreaId)
                .filter(id -> id != null && id.matches("KV\\d{3,}"))
                .mapToInt(id -> Integer.parseInt(id.substring(2)))
                .max()
                .orElse(0);
        return String.format("KV%03d", maxNumber + 1);
    }
}

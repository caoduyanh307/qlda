package com.ddangkhoa.trafficfx.model;

public class Area {
    private String areaId;
    private String areaName;
    private String areaType;
    private String status;
    private String createdDate;

    public Area(String areaId, String areaName, String areaType, String status, String createdDate) {
        this.areaId = areaId;
        this.areaName = areaName;
        this.areaType = areaType;
        this.status = status;
        this.createdDate = createdDate;
    }

    public String getAreaId() { return areaId; }
    public void setAreaId(String areaId) { this.areaId = areaId; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public String getAreaType() { return areaType; }
    public void setAreaType(String areaType) { this.areaType = areaType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}

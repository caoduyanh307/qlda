package com.ddangkhoa.trafficfx.model;

public class AccountInfo {
    private final String accountId;
    private final String fullName;
    private final String username;
    private final String email;
    private final String role;
    private final String accountStatus;
    private final String createdAt;
    private final String updatedAt;
    private final String accountRoleId;
    private final String assignedAt;
    private final String roleActive;
    private final String employeeId;
    private final String phone;
    private final String dateOfBirth;
    private final String gender;
    private final String address;
    private final String salary;
    private final String employeeStatus;

    public AccountInfo(String accountId, String fullName, String username, String email, String role,
                       String accountStatus, String createdAt, String updatedAt, String accountRoleId,
                       String assignedAt, String roleActive, String employeeId, String phone,
                       String dateOfBirth, String gender, String address, String salary, String employeeStatus) {
        this.accountId = accountId;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.role = role;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.accountRoleId = accountRoleId;
        this.assignedAt = assignedAt;
        this.roleActive = roleActive;
        this.employeeId = employeeId;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.salary = salary;
        this.employeeStatus = employeeStatus;
    }

    public String getAccountId() { return accountId; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getAccountStatus() { return accountStatus; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getAccountRoleId() { return accountRoleId; }
    public String getAssignedAt() { return assignedAt; }
    public String getRoleActive() { return roleActive; }
    public String getEmployeeId() { return employeeId; }
    public String getPhone() { return phone; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getAddress() { return address; }
    public String getSalary() { return salary; }
    public String getEmployeeStatus() { return employeeStatus; }
}

package com.example.controller;

import com.example.dto.Employee;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/* Log in with a Keycloak realm user to get an access token: http://localhost:8081/oauth2/authorization/keycloak */

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final Map<Long, Employee> employeeMap = new HashMap<>();
    private long counter = 1;

    @GetMapping
    public Collection<Employee> getAllEmployees() {
        return employeeMap.values();
    }

    @GetMapping("/{id}")
    public Employee getEmployee(@PathVariable Long id) {
        return employeeMap.getOrDefault(id, null);
    }

    @PostMapping
    public Employee createEmployee(@RequestBody Employee employee) {
        long id = counter++;
        employee.setId(id);
        employeeMap.put(id, employee);
        return employee;
    }

    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        if (!employeeMap.containsKey(id)) return null;
        employee.setId(id);
        employeeMap.put(id, employee);
        return employee;
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteEmployee(@PathVariable Long id) {
        if (employeeMap.remove(id) != null) {
            return Map.of("message", "Deleted employee " + id);
        } else {
            return Map.of("error", "Employee not found");
        }
    }
}

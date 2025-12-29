package com.moretolearn.service;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moretolearn.entity.Employee;
import com.moretolearn.repository.EmployeeRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> loadEmployees() {
        return repository.streamAll();
//                .doOnNext(e ->
//                        System.out.println("DB → " + e.id())
//                );
    }
    
//    @Transactional(readOnly = true)
    public Stream<Employee> loadEmployees1() {
        return repository.streamAll1();
    }
    
    @Transactional(readOnly = true)
    public List<Employee> loadEmployeesPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(pageable).getContent();
    }
}

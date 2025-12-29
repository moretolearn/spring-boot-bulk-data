package com.moretolearn;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

@Query("SELECT e FROM Employee e")
List<Employee> streamAll();

@Query("SELECT e FROM Employee e")
Stream<Employee> streamAll1();

}


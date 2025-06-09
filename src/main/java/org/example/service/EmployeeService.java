package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;

@Service
public class EmployeeService {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public EmployeeService(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource);
    }

    public List<String> listEmail(){
        return jdbcTemplate.queryForList("select email from employee", String.class);
    }
}

package org.example.service;

import lombok.SneakyThrows;
import org.example.ds.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class EmployeeService {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public EmployeeService(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource);
    }

    
    // RowMapper (stateless)
    @SneakyThrows
    private Employee mapToEmployee(ResultSet rs, int i){
        return new Employee(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getDate("start_date"),
                rs.getFloat("salary")
        );
    }

    public List<Employee> findAllEmployees() {
        return jdbcTemplate.query(
                "select * from employee",
                (a,b) -> mapToEmployee(a, b)
               // this::mapToEmployee;
        );
    }

    //RowCallbackHandler (stateful)
    private class AverageRowCallbackHandler implements RowCallbackHandler {
        private int count;
        private double sum;
        @Override
        public void processRow(ResultSet rs) throws SQLException {
            sum += rs.getDouble("salary");
            count++;
        }
        public double averageSalary(){
            return sum / count;
        }
    }

    public double averageByRowCallbackHandler(){
        AverageRowCallbackHandler averageRowCallbackHandler =
                new AverageRowCallbackHandler();
        jdbcTemplate.query(
          "select salary from employee",
          averageRowCallbackHandler
        );
        return averageRowCallbackHandler.averageSalary();
    }

    //ResultSetExtractor
    private class AverageResultSetExtractor implements ResultSetExtractor<Double> {

        @Override
        public Double extractData(ResultSet rs) throws SQLException, DataAccessException {
            int count = 0;
            double sum = 0;
            while (rs.next()) {
                sum += rs.getDouble("salary");
                count++;
            }
            return sum / count;
        }
    }

    public double averageByResultSetExtractor(){
        AverageResultSetExtractor resultSetExtractor = new AverageResultSetExtractor();
        return jdbcTemplate.query(
                "select salary from employee",
                new AverageResultSetExtractor()
        );
    }

    // call back
    public int findEmployeeIdByEmail(String email){
        return jdbcTemplate.query(new PreparedStatementCreator() {
                                      @Override
                                      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                                          return con.prepareCall("select id from employee where email = ?");
                                      }
                                  },
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setString(1, email);
                    }
                },
                new ResultSetExtractor<Integer>() {
                    @Override
                    public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                        if(rs.next()){
                            return rs.getInt("id");
                        }else{
                            throw new RuntimeException("Employee id not found");
                        }
                    }
                }
        );
    }

    public List<String> listEmail(){
        return jdbcTemplate.queryForList("select email from employee", String.class);
    }
}

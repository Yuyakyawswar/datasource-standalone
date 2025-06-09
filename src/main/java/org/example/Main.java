package org.example;

import org.example.service.EmployeeService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);
        context.registerShutdownHook();

        EmployeeService service = context.getBean(EmployeeService.class);
        service.listEmail().forEach(System.out::println);
    }

}
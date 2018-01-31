package io.github.tesla.ops;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@MapperScan(value = {"io.github.tesla.ops.*.dao",
    "com.quancheng.saluki.gateway.persistence.*.dao"})
@SpringBootApplication
public class TeslaOpslApplication {
  public static void main(String[] args) {
    SpringApplication.run(TeslaOpslApplication.class, args);
  }

}

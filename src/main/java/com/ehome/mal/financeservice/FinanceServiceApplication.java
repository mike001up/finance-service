package com.ehome.mal.financeservice;

import com.pig4cloud.pig.common.datasource.annotation.EnableDynamicDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDynamicDataSource
@EnablePigFeignClients
@EnablePigResourceServer
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.pig4cloud.pig.common.core","com.pig4cloud.pig.common.mybatis","com.pig4cloud.pig.common.datasource","com.ehome.mal.financeservice"})
public class FinanceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceServiceApplication.class, args);
	}

}

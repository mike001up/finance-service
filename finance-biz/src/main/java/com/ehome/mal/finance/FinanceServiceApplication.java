package com.ehome.mal.finance;

import com.pig4cloud.pig.common.datasource.annotation.EnableDynamicDataSource;
import com.pig4cloud.pig.common.feign.annotation.EnablePigFeignClients;
import com.pig4cloud.pig.common.security.annotation.EnablePigResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableDynamicDataSource
@EnablePigFeignClients
@EnablePigResourceServer
@EnableDiscoveryClient
@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.pig4cloud.pig.common.core", "com.pig4cloud.pig.common.mybatis", "com.pig4cloud.pig.common.datasource", "com.ehome.mal.finance", "com.ehome.mal.finance.api"})
public class FinanceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceServiceApplication.class, args);
	}

}

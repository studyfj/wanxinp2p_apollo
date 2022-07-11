package cn.itcast.wanxinp2p.account;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(scanBasePackages = {"org.dromara.hmily","cn.itcast.wanxinp2p.account"})
@EnableDiscoveryClient
@EnableSwagger2
@MapperScan(basePackages = "cn.itcast.wanxinp2p.account.mapper")
public class AccountService {

    public static void main(String[] args) {
        SpringApplication.run(AccountService.class, args);
    }

}

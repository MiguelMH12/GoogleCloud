package com.example.demo.swaggerconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class SwaggerConfiguration {
	@Bean
	public Docket googleAPI() {
		return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(googleApiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .build()
                .useDefaultResponseMessages(false)
                .useDefaultResponseMessages(false);
	}
	
	private ApiInfo googleApiInfo() {

        return new ApiInfoBuilder()
                .title("Google APIs Service")
                .version("1.0")
                .license("Apache License Version 2.0")
                .build();

    }
	
}

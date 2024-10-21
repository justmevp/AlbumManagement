package org.studyeasy.SpringRestDemo.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Demo API ", // Tiêu đề API
        version = "Version 1.0", // Phiên bản API
        contact = @Contact(name = "justmevp", // Tên người liên hệ
                email = "vpek215@gmail.com", // Email liên hệ
                url = "http://localhost:8080/swagger-ui/index.html" // URL liên hệ
        ), license = @License(name = "Apache 2.0", // Tên giấy phép sử dụng
                url = "https://www.apache.org/license/LICENSE-2.0" // URL đến giấy phép
        ), termsOfService = "http://localhost:8080/swagger-ui/index.html", description = "Spring Boot Restfull API Demo by VP"))
public class SwaggerConfig {

}

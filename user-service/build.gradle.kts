plugins {
	id("org.springframework.boot")
}

extra["springCloudVersion"] = "2025.0.0"

dependencies {
	implementation(project(":Common"))
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	testImplementation("org.springframework.security:spring-security-test")
	implementation("org.springframework.boot:spring-boot-starter-web")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.mysql:mysql-connector-j")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

// Cấu hình cho Spring Boot để tạo file JAR thực thi được
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("com.Huy.user_service.UserServiceApplication")
}
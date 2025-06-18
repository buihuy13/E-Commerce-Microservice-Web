plugins {
    id ("java")
    id("org.springframework.boot") version "3.5.0" apply false // Đổi phiên bản mới hơn nếu muốn, ví dụ 3.3.0
    id("io.spring.dependency-management") version "1.1.7" apply false 
} 

// các project, bao gồm cả project gốc.
allprojects {
    group = "com.Huy"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    // Áp dụng plugin quản lý dependency của Spring cho các module con
    apply(plugin = "io.spring.dependency-management")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    configurations {
	    compileOnly {
		    extendsFrom(configurations.annotationProcessor.get())
	    }
    }

    // Tránh để `implementation("org.springframework.boot:spring-boot-starter")` ở đây
    dependencies {
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	    implementation("org.springframework.boot:spring-boot-starter-validation")
        compileOnly("org.projectlombok:lombok")
	    annotationProcessor("org.projectlombok:lombok")
        implementation("io.github.cdimascio:dotenv-java:3.2.0")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("io.micrometer:micrometer-tracing-bridge-brave")
        implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    }  

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
plugins {
	id("org.springframework.boot") version "2.7.17"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	java
}

group = "com.shishaoqi"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	
	// MySQL
	implementation("mysql:mysql-connector-java:8.0.33")
	
	// MyBatis Plus
	implementation("com.baomidou:mybatis-plus-boot-starter:3.5.3.1")
	implementation("com.baomidou:mybatis-plus-annotation:3.5.3.1")
	implementation("com.baomidou:mybatis-plus-core:3.5.3.1")
	implementation("com.baomidou:mybatis-plus-extension:3.5.3.1")
	
	// Jackson for JSON
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	
	// OpenAPI 3.0 Documentation
	implementation("org.springdoc:springdoc-openapi-ui:1.6.15")
	
	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	
	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}

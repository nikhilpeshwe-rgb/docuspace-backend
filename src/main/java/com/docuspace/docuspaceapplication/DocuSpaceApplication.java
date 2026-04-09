package com.docuspace.docuspaceapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.docuspace")
public class DocuSpaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocuSpaceApplication.class, args);
	}

}

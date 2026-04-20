package com.solution;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@SpringBootApplication
public class WebhookSolutionApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebhookSolutionApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ApplicationRunner run(RestTemplate restTemplate) {
        return args -> {
            // Step 1: Generate webhook
            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            Map<String, String> requestBody = Map.of(
                "name", "John Doe",
                "regNo", "REG12347",
                "email", "john@example.com"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map> response = restTemplate.exchange(
                generateUrl,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                Map.class
            );

            Map<?, ?> body = response.getBody();
            String webhook = (String) body.get("webhook");
            String accessToken = (String) body.get("accessToken");

            System.out.println("Webhook URL: " + webhook);

            // Step 2: Submit SQL solution
            // regNo = REG12347 → last two digits = 47 → ODD → Question 1
            // Question 1: Find the 2nd highest salary without using LIMIT/TOP
            String finalQuery =
                "SELECT DISTINCT p.AMOUNT AS SALARY, e.FIRST_NAME, e.LAST_NAME, d.DEPARTMENT_NAME " +
                "FROM PAYMENTS p " +
                "JOIN EMPLOYEE e ON e.EMP_ID = p.EMP_ID " +
                "JOIN DEPARTMENT d ON d.DEPARTMENT_ID = e.DEPARTMENT_ID " +
                "WHERE p.AMOUNT = (" +
                    "SELECT MAX(AMOUNT) FROM PAYMENTS " +
                    "WHERE AMOUNT < (SELECT MAX(AMOUNT) FROM PAYMENTS)" +
                ")";

            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.setContentType(MediaType.APPLICATION_JSON);
            submitHeaders.set("Authorization", accessToken);

            Map<String, String> submitBody = Map.of("finalQuery", finalQuery);

            ResponseEntity<String> submitResponse = restTemplate.exchange(
                webhook,
                HttpMethod.POST,
                new HttpEntity<>(submitBody, submitHeaders),
                String.class
            );

            System.out.println("Submission status: " + submitResponse.getStatusCode());
            System.out.println("Submission response: " + submitResponse.getBody());
        };
    }
}

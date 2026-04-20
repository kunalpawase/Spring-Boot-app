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
                "name", "Kunal",
                "regNo", "ADT23SOCB0553",
                "email", "kunalpawase@gmail.com"
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
            // Question 1: Highest salary NOT credited on the 1st day of any month,
            // with employee name (FIRST_NAME + LAST_NAME), age, and department name
            String finalQuery =
                "SELECT p.AMOUNT AS SALARY, " +
                "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
                "d.DEPARTMENT_NAME " +
                "FROM PAYMENTS p " +
                "JOIN EMPLOYEE e ON e.EMP_ID = p.EMP_ID " +
                "JOIN DEPARTMENT d ON d.DEPARTMENT_ID = e.DEPARTMENT_ID " +
                "WHERE DAY(p.PAYMENT_TIME) != 1 " +
                "AND p.AMOUNT = (" +
                    "SELECT MAX(AMOUNT) FROM PAYMENTS WHERE DAY(PAYMENT_TIME) != 1" +
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

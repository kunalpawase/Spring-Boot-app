# Webhook Solution - Spring Boot

Spring Boot app that on startup:
1. POSTs to `generateWebhook/JAVA` to get a webhook URL and access token
2. Submits the SQL solution to the webhook URL using the access token as JWT

## RegNo: REG12347 → last two digits = 47 (ODD) → Question 1

### Question 1 - SQL Query
Find employees with the second highest salary:

```sql
SELECT DISTINCT p.AMOUNT AS SALARY, e.FIRST_NAME, e.LAST_NAME, d.DEPARTMENT_NAME
FROM PAYMENTS p
JOIN EMPLOYEE e ON e.EMP_ID = p.EMP_ID
JOIN DEPARTMENT d ON d.DEPARTMENT_ID = e.DEPARTMENT_ID
WHERE p.AMOUNT = (
    SELECT MAX(AMOUNT) FROM PAYMENTS
    WHERE AMOUNT < (SELECT MAX(AMOUNT) FROM PAYMENTS)
)
```

## Run

```bash
java -jar target/webhook-solution-0.0.1-SNAPSHOT.jar
```

## Build

```bash
mvn package
```

# Course Waitlist

## Requirements
- JDK 17+
- Maven 3.6+

## Run tests and application
```bash
mvn clean test
mvn spring-boot:run
```

The default offering is `COURSE-101` with capacity `2`. Change `waitlist.offering-id` or `waitlist.capacity` in `src/main/resources/application.properties` if needed.

## Example API flow
First mark a learner as prerequisite-satisfied:
```http
PUT /api/learners/alice/prerequisite
Content-Type: application/json

{"satisfied":true}
```
Enroll:
```http
POST /api/offerings/COURSE-101/enrollments
Content-Type: application/json

{"learnerId":"alice"}
```
Inspect state:
```http
GET /api/offerings/COURSE-101
```
Drop:
```http
DELETE /api/offerings/COURSE-101/enrollments/alice
```

All state is in memory and resets when the application restarts.

# AI-assisted engineering use record

## Prompt used
"Implement the service layer for the supplied Spring Boot course waitlist starter: enroll, drop, FIFO waitlist promotion, prerequisite eligibility, duplicate prevention, safe capacity handling, and focused tests. Follow the starter's package structure and controller API."

## Response summary
The AI proposed a service interface, in-memory enrolled and waitlist collections, synchronized state transitions, prerequisite checks, FIFO promotion, and focused unit tests.

## Review and decisions
- Accepted: `LinkedHashSet` for enrolled learners, `ArrayDeque` for FIFO waitlist order, and synchronized service operations.
- Accepted with explicit policy: unknown learners are ineligible; ineligible waitlisted learners are skipped for promotion but retained in their original relative queue order.
- Edited: aligned all Java packages beneath `com.example.coursewaitlist` so Spring Boot component scanning discovers controllers and services; aligned the implementation to the starter's actual application package.
- Rejected: adding a database, frontend, deployment infrastructure, or configurable policy engine because these are out of scope.

## Verification evidence
The unit test suite covers free capacity, full capacity, duplicates, FIFO promotion, prerequisite failure, ineligible queue retention, repeated drops, invalid offering/learner requests, and concurrent enrollments. Run `mvn clean test` locally and retain the Maven test summary as submission evidence. Maven was not available in the generation environment, so no passing test result is claimed here.

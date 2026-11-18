# Design specification

## Service boundary
`OfferingEnrollmentService` exposes snapshot, enroll, and drop operations for one configured offering. REST controllers delegate to this service. `InMemoryPrerequisiteEligibility` owns the demo prerequisite state.

## State and invariants
- The offering ID and positive capacity are configured in `application.properties`.
- `LinkedHashSet` stores enrolled learners; `ArrayDeque` stores the FIFO waitlist.
- A learner is present at most once and cannot be both enrolled and waitlisted.
- Enrolled count never exceeds capacity.
- Unknown offering IDs and blank learner IDs are rejected before state changes.

## Prerequisite and waitlist policy
A learner is eligible only when the prerequisite registry explicitly contains `true`; unknown learners default to ineligible. An ineligible learner cannot enroll or join the waitlist. If a learner becomes ineligible while waiting, promotion scans past them and retains them in their original relative queue order. The earliest eligible learner is promoted.

## Concurrency and persistence
Synchronized service methods make each offering-state read or mutation atomic within one application instance. Prerequisite values are held in a `ConcurrentHashMap`. State is process-local and resets on restart. A multi-instance or durable production implementation would need transactional persistence and database-level coordination.

## Limitations
This is intentionally a single-offering, single-instance in-memory implementation. Prerequisite updates and promotion do not share a cross-component transaction; a production implementation should coordinate those operations under one transactional boundary.

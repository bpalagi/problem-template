# [ACME-1234] Investigate degraded API performance reported by customers

**Status:** In Progress  
**Assignee:** You  
**Priority:** High

---

## Description

Multiple enterprise customers have reported slow response times in the application over the past week. The Customer Success team has escalated this as it's affecting our largest accounts.

From the support tickets:
> "The app has been really sluggish lately dealing with orders. Sometimes it takes seconds to load." — Foo Corp

> "Performance has degraded significantly since last month. Please investigate ASAP." — GlobalTrade Ltd

## Acceptance Criteria

- [ ] Identify which endpoint(s) are experiencing performance issues
- [ ] Determine root cause of the slowdown
- [ ] Implement a fix that brings response times to acceptable levels (<100ms p95)
- [ ] Ensure existing tests continue to pass

## Technical Context

- Application uses SQLite database
- Database has ~50,000 orders with line items

## Getting Started

See [app/README.md](app/README.md) for setup instructions.

## Notes

_Add your investigation notes here as you debug._

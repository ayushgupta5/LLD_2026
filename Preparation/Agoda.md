Agoda Software Engineer Interview Questions - Comprehensive Analysis
Based on the interview experiences provided, here's a complete breakdown of questions and topics:

1. DSA (Data Structures & Algorithms)
Stack-Based Problems

Min Stack - Design a stack that supports push, pop, top, and retrieving the minimum element in constant time
Next Greater Element - Find the next greater element for each element in an array

Follow-up: Solve for a circular array


Monotonic Stack variation - Similar to next greater element (asked multiple times)
Chemical Formula Weight Calculator - Given atoms C=12, H=1, O=8, calculate total weight of formula (e.g., CH4=16, H(CH4)2=33). Solved using stack-based approach

Binary Search Problems

Koko Eating Bananas variation (asked multiple times across different rounds)
Binary search variations

Array & String Problems

Two Sum, Three Sum, Four Sum
Reverse Each Word in String

Input: "Hello Bangkok:)"
Output: "Bangkok:) Hello"


Flower Bed Placement - Given array of 0s and 1s (0=empty, 1=flower bed), determine if you can place k more flower beds such that no two are adjacent

Multiple follow-ups on optimizations and early termination


Stock Buy/Sell variation
Pairs with Min Absolute Difference = K
String Comparison with Backspace - Given strings S1 (with # backspace) and S2, determine if equal after processing backspaces

Dynamic Programming

Jump Game variation
Count Palindromic Substrings - Medium to hard difficulty
Robot Path Counting - Robot starts at (0,0) in matrix with obstacles (denoted by 1), find number of ways to reach (n-1, m-1) with only right and down movements

Graph Problems

Topological Sort - Course Schedule I variation

Must solve using both DFS and BFS approaches
Explain time and space complexity for both



Greedy Problems

Eliminate Maximum Number of Monsters variation - Airplanes landing problem

Each plane has starting position and descending speed
Player can shoot one aircraft per second
Game ends if any plane lands
Find maximum planes that can be prevented from landing



String/Palindrome Problems

Complex Palindrome Problem - Given two strings s1 and s2:

Find longest palindrome from both strings
Merge them
Find shortest palindrome from merged string



Sliding Window

LC medium question based on sliding window technique

Hash Map

Medium-difficulty hash map problem
Hash map implementation with concurrency handling (low-level design aspect)


2. Low Level Design (LLD)
Object-Oriented Design

Banking System - Implement using OOP principles in Java
Elevator System - Design elevator system for a building
Flashcard System - Machine coding round to create flashcard application
HashMap Implementation - Design with concurrency support

Design Patterns

Strategy Pattern - Suggested during code review for booking API
General design patterns discussion
Proper error and exception handling implementation


3. High Level Design (HLD / System Design)
E-commerce & Booking Systems

Hotel Search System - Basic hotel searching functionality

How to add/remove functionalities
Scaling strategies
Caching mechanisms
Rate limiting
Tradeoffs discussion


Hotel Booking System (asked multiple times)

Similar to Booking.com
Emphasis on proximity-based search
RESTful service design
How to prevent double bookings
Scalability and fault tolerance


Flight Search System

Basic input parameters
Price from 3rd party (each call bears cost)
Dynamic pricing (changes in real-time)
Multiple suppliers providing inventory via metered APIs
Handling frequent price/seat availability updates
Show cheapest option when same flight from multiple suppliers
No push mechanism from suppliers


Flight Booking System - Scenario-based with existing HLD

"How will you...?" type questions
"What if...?" scenarios


Flight Price Management System

Manage and retrieve prices from different providers
Rate limiting based on different provider limits
Challenges in distributed DC environment


Concert Booking System - Similar to flash sale

Test overall design skills
Trade-offs discussion
Knowledge of bottlenecks
Scaling considerations
Metrics and telemetry (asked throughout)



Content & Media Systems

Music Streaming Application

Fetch top trending songs
Regional trending songs
Handle millions of requests
Scalability and fault tolerance
Data schema design
Database choice justification


YouTube Design

Registered users can upload videos
Any user can search and view videos


WhatsApp Design - Architecture round

Storage & Data Systems

File Storage Service

Simple requirements (no conflict handling needed)


Log/Media Storage System

Accept data from multiple sources (API, CSV, events)
How to get and handle all data
Database schema and LLD


URL Shortener Webapp

Database for storing original and short URLs
Service to generate unique short URLs
Scalability and performance considerations
Analytics for tracking usage



Specialized Systems

Location Sharing App

Database design focus
User constraints (time-based, location-based restrictions)
Example: user1 can restrict user2 based on time and location


Payment Gateway System

Design for scalability with company growth
Feature on top of Kafka for exactly-once message processing (important for payments)
Older Kafka versions only guarantee at-least-once


Distributed Scheduler

MySQL table with 500 million records
Each record defines task to perform indefinitely
Schema: ID (PK), URL (HTTP), Frequency (gap between consecutive calls)
URL returns different results on each hit
Schedule jobs based on frequency


Hotel Inventory System

Query internal Agoda data
Call external services (MakeMyTrip, Booking.com)
Aggregate inventory data


Rate Limiter Design

For various APIs (mobile, web, APIs)
Discussed signature databases and ML-based algorithms


Reconciliation System - Car dealer showroom

Vague problem statement requiring clarification
HLD, component breakdown, trade-offs



Technical Deep Dives & Concepts
Caching

Local vs. global caching
Caching strategies and techniques
Where to implement caching in architecture

Database & Storage

Sharding and Federation

How it improves availability
Challenges involved


DBMS indexes
Database internals
Redis internals
Indexing strategies
Transaction handling in microservice environments requiring high consistency
Database schema design (asked across multiple rounds)

Scalability & Distribution

Load balancing (LB)
Message queues
Strong vs. Eventual Consistency
Maintaining consistency across distributed sites (EU, North America, Asia-Pacific regions)

System Improvements

Given existing design, identify improvements
Code review of booking API with suggestions

API Design

REST API design principles
Types of authentication
Managing robust, secure connectivity sessions
Connectivity management
Plain vanilla HTTP client basics (no Spring/abstract frameworks)

Adapters & Integration

File and FTP adapters functionality


4. Behavioral Questions
Motivation & Career

Why Agoda? (asked in almost every round)
Reason for job switch? (asked multiple times)
Are you okay with relocation? / Relocation motivation
Open to moving to Bangkok?
Salary expectations

Work Experience & Projects

Tell me about your current role
Discuss your recent projects and underlying architecture
Deep dive into tech stack used (Kafka, databases, etc.)
What would you improve in your current project design?
Explain architecture of current organization (e.g., end-to-end order flow for food delivery)
Tasks completed, challenges faced, and solutions implemented

Team & Leadership

Experience with mentoring
Mentoring junior engineers
Working under tight deadlines
Conflict resolution / Handling conflicts
Experience with experiments/experimentation
Trade-off decisions made in past
Mistakes made and learnings
Work style preferences

Agile & Process

Concepts around Agile methodology


5. Culture Fit Questions
Values Assessment (Longest round - 1.5 hours)

Mentorship experiences and approach
Significant mistakes and what you learned
Experiments you've run and outcomes
Trade-off decisions and reasoning
Conflict situations and resolution
Working under pressure/tight deadlines

Team Compatibility

Team compatibility assessment
Leadership questions and scenarios
How you handle disagreements with team members

Situational Questions

Scenario-based questions testing cultural alignment
Questions assessing alignment with Agoda's values


6. Interview Tips & Preparation Insights
Round Structure (Typical Process)

Recruiter Screening (30 mins) - Intro, salary expectations, relocation willingness
Online Assessment (HackerRank/CodeSignal) - 1 DSA (can be LC Hard) + 1 API design
DSA Round(s) (1-1.5 hours) - 1-2 coding questions (easy to medium, sometimes hard)
Platform Round (1 hour) - System design + code review + REST API design + QA
System Design/HLD Round(s) (1 hour each, can be 2 rounds)
Architecture Round (1 hour) - Deep technical, trade-offs, bottlenecks, metrics
Culture Fit/Values Round (1-1.5 hours) - Longest behavioral round
Hiring Manager Round (1 hour) - Behavioral + technical + compensation discussion

Platform Round Specifics

System Design: Given existing design, suggest improvements (LB, rate limiter, message queue)
Connectivity Management: REST API improvements, authentication types, secure sessions
Code Review: Review booking API implementation, suggest patterns (e.g., strategy pattern), error handling

Coding Round Expectations

Write full executable code on HackerRank/CodeSignal
Pass test cases live (sometimes need to pass 10+ out of 18 test cases)
Multiple follow-ups on optimizations
Early termination possibilities
Explain time and space complexity clearly

System Design Approach

Start small and tell interviewer you'll consider scale as you move along
Discuss trade-offs extensively - this is critical
Focus on metrics and telemetry throughout discussion (brownie points)
Ask clarifying questions especially for vague problem statements
Present HLD, component breakdown, trade-offs
Consider bottlenecks and scaling strategies
Justify technology choices (databases, caching, etc.)
Think about fault tolerance and high availability

Code Quality Expectations

No Spring/abstract magic in API design rounds - plain vanilla HTTP basics
Use built-in classes appropriately (e.g., AtomicLong in Java for concurrency)
Demonstrate thread safety and concurrency control
Proper error and exception handling
Knowledge of design patterns

Technical Depth Areas to Prepare

Kafka internals and use cases
Database internals, indexing, transaction handling
Redis internals
Concurrency control and thread safety
Exactly-once vs. at-least-once message processing
REST API design principles
Authentication mechanisms
Metrics and observability

General Tips

Agoda recruiters provide excellent feedback after each round
They share detailed preparation docs and links
Entire interview loop takes approximately 1 week
Interviewers are generally friendly and engaging
They test thought process, not just solutions
For API design: Know plain vanilla HTTP client basics thoroughly
Strong performance may lead to skipping rounds
Rejection can happen even after good technical performance if cultural fit isn't strong

Red Flags to Avoid

Weak fundamentals/basics - can fail machine coding even with good problem-solving
Not asking enough clarifying questions for vague requirements
Over-engineering or under-engineering solutions
Poor communication of trade-offs and decision-making
Not demonstrating cultural alignment in behavioral rounds

Self-Assessment Verdicts (Common Pattern)
Candidates often self-rate as:

Strong Hire - Exceptional performance
Hire - Solid performance
OK-OK - Average, uncertain outcome

Focus Areas for Success

Solid DSA fundamentals - especially stack, binary search, DP, graphs
System design breadth - booking systems, distributed systems, caching strategies
Concurrency and threading knowledge
Database design and trade-offs
Clear communication of thought process
Cultural preparation - be ready to discuss why Agoda, relocation, values
Metrics and observability mindset throughout design discussions
Trade-off analysis - this comes up constantly


Total Time Investment: Plan for 4-6 hours of interviews spread across 1 week, with the culture fit round being the longest (1.5 hours).
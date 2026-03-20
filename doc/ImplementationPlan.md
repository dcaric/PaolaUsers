# Project Implementation Plan: ReqRes Users Information System (Topic #17)

## 1. Project Foundation & Tech Stack Overview

This document outlines the architectural blueprint for an interoperable system 
centered on the ReqRes Users domain. The primary objective is to ensure seamless 
data exchange across heterogeneous protocols while maintaining strict schema 
adherence and role-based security. The system bridges a legacy-style SOAP interface, 
modern gRPC streaming, and flexible GraphQL queries into a unified ecosystem.

Technical Requirements & Communication Protocols

Component	Technology	Communication Protocol
==============================================
* Backend Framework	Jakarta EE (Java EE)	JAX-RS, JAX-WS, gRPC
* Persistence Layer	JPA / Hibernate	SQL
* Data Binding	JAXB (Jakarta XML Binding)	XML / JSON
* External Integration 1	ReqRes Users API	REST (JSON)
* External Integration 2	DHMZ Weather Feed	REST (XML)
* Service Discovery	gRPC Proto Definitions	HTTP/2
* Security	JWT (Access/Refresh Tokens)	Bearer Authentication
* Frontend	C# .NET Desktop Client	Multi-protocol (REST/SOAP/gRPC)

Dual-Role Security Model

The system enforces a strict Role-Based Access Control (RBAC) model. 
The backend will issue JWTs containing specific "Role" claims:

* Read-only: Authorized only for idempotent retrieval operations (GET).
* Full Access: Authorized for all operations, including state-changing methods 
* (POST, PUT, DELETE).


--------------------------------------------------------------------------------


## 2. Day 1: Backend Setup, Data Validation, and Persistence

The foundation involves establishing a robust persistence layer and a strict 
validation gateway for incoming data.

Database & Entity Design

Utilizing JPA (Jakarta Persistence API), we will design a User entity mirroring 
the ReqRes domain. To ensure transactional integrity during persistence, 
the entity will include:

* id (Primary Key, Auto-incremented)
* email, first_name, last_name, avatar (String fields)

JAX-RS POST Endpoint with Strict Validation

We will implement a high-interoperability endpoint using JAX-RS that accepts 
both application/xml and application/json.

* Arbitrary Data Support: The endpoint must accept arbitrary user data payloads as long as they conform to the required User domain schema.
* Validation Gateways:
    * Incoming XML is validated against an XSD (XML Schema Definition).
    * Incoming JSON is validated against a JSON Schema.
* Error Handling: If validation fails, the service must return a 404 Bad Request status with a structured error payload detailing the specific validation violations to the client.


--------------------------------------------------------------------------------


## 3. Day 2: XML Interoperability & SOAP Services

This phase demonstrates protocol bridging by transforming REST data into a local XML snapshot for legacy search capabilities.

JAXB XML Generation

We will develop a service that invokes the public ReqRes REST API, retrieves the user list, and uses JAXB to marshal this data into a local XML file. This file acts as a local "snapshot," decoupling our SOAP search performance from the external API's latency.

Programmatic XML Validation

Before any search operations are permitted, the system must use Jakarta XML to programmatically validate the generated local snapshot. If the file violates established structural rules, the system must return detailed validation messages.

SOAP Implementation (JAX-WS)

We will expose a JAX-WS SOAP service to facilitate searching.

* Logic: The service receives a search string and utilizes the javax.xml.xpath API to query the local XML snapshot.
* XPath Filtering: The implementation must execute an XPath expression to filter only those records where fields match the search term.
* Output: The filtered list is returned as a SOAP response.


--------------------------------------------------------------------------------


## 4. Day 3: gRPC Weather Service & Security Foundations

Integration of real-time weather data and the establishment of a secure identity layer.

gRPC Server (DHMZ Integration)

A gRPC server will be implemented to handle high-performance weather queries.

* Protocol Definition: Define a .proto file specifying the GetTemperature service, request messages (city name), and response messages (temperature list).
* External Logic: The backend fetches the DHMZ XML feed.
* Filtering Rule: The service must support partial city name matching. Crucial: If multiple entries match a partial name (e.g., "Zag" matching "Zagreb-Maksimir" and "Zagreb-Grič"), the service must return all matching records to the client.

Security: JWT Implementation

Implement a comprehensive security provider to issue and verify JSON Web Tokens.

* Token Pair: Generate both a short-lived Access Token and a long-lived Refresh Token.
* Claim Mapping: The "Role" claim (Read-only vs. Full Access) must be embedded in the token to allow the C# client to perform client-side UI state management.


--------------------------------------------------------------------------------


## 5. Day 4: Custom REST API, GraphQL, and Configuration Switch

Providing local alternatives to external services while maintaining schema parity.

Custom REST API & GraphQL Integration

We will develop a local REST API that mirrors the ReqRes public schema exactly. This ensures the client remains agnostic of the data source.

* Full CRUD: Support GET, POST, PUT, and DELETE via JAX-RS.
* GraphQL Endpoint: Implement a GraphQL layer over the same JPA entities. Define a Root Query for flexible data fetching and Mutations for user updates, allowing the client to request specific fields (e.g., just email and avatar).

The "Switch" Mechanism

To demonstrate true interoperability, we will implement a configuration "Switch" (likely via a property file or environment variable).

* Architectural Pattern: Using Dependency Injection or the Strategy Pattern, this switch will toggle the Base URL used by the system.
* Behavior: When toggled, the application seamlessly redirects all traffic from the Public ReqRes API to our Custom Local REST API without requiring a recompile of the C# client.


--------------------------------------------------------------------------------


## 6. Day 5: C# .NET Desktop Client & System Integration

The final layer is the C# client, which serves as the consumer for all previously established protocols.

GUI Development & Functionality

The C# application will provide a centralized interface for the following operations:

* Data Upload: Send XML/JSON user data to the Day 1 POST endpoint.
* SOAP/XPath: Execute and display results from the SOAP search service.
* gRPC Weather: Connect to the gRPC server and list all matching temperatures from the DHMZ feed.
* Advanced Querying: Interface with the Custom REST and GraphQL endpoints.

Client-Side RBAC Logic

Upon login, the client receives the JWT. The application must parse the claims and dynamically adjust the UI:

* Read-only Role: The application must programmatically disable or hide all UI components associated with POST, PUT, and DELETE operations.
* Full Access Role: All buttons and interactive elements for data modification are enabled.

Final Validation

The final phase involves validating the Configuration Switch. We will verify that toggling the backend configuration correctly redirects the client's data flow between the public ReqRes cloud and our local persistence layer, ensuring zero breaking changes in the user experience.

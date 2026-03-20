# PaolaRestApi Guide

## 1. What is Jakarta EE?
Jakarta EE (formerly known as Java EE or J2EE) is a set of specifications and guidelines for developing enterprise-level Java applications. It extends the standard Java SE (Standard Edition) with libraries and APIs designed for building distributed, scalable, and secure web and enterprise applications. 

Several key Jakarta EE specifications include:
*   **Jakarta RESTful Web Services (JAX-RS):** Used to build REST APIs (e.g., using annotations like `@Path`, `@GET`).
*   **Jakarta Servlet:** The foundational API for handling HTTP requests and responses in Java web servers (like Tomcat).

The transition from "Java EE" to "Jakarta EE" occurred when Oracle transferred the project to the Eclipse Foundation. This is why newer packages start with `jakarta.*` instead of the older `javax.*`.

## 2. What is jersey-servlet?
The `jersey-servlet` is typically configured in a `web.xml` file using the class `org.glassfish.jersey.servlet.ServletContainer`. 

*   **Jersey** is an open-source framework and the official reference implementation of the Jakarta RESTful Web Services (JAX-RS) specification. It provides the mechanism for mapping HTTP requests to Java methods.
*   The **jersey-servlet** (`ServletContainer`) acts as the bridge or "front controller" between a web server (like Tomcat) and the JAX-RS application code. 
*   When the server receives an HTTP request that matches the configured `<url-pattern>` (e.g., `/api/*`), it delegates that request to the Jersey `ServletContainer`. 
*   Jersey then processes the configuration (such as `jersey.config.server.provider.packages`), scans the specified packages for classes with `@Path` annotations, and routes the incoming request to the appropriate Java method.

## 3. Project Configuration
The project uses **Maven** for dependency management and build automation.

*   **pom.xml:** Defines the project dependencies, including the Jakarta EE APIs and the Jersey implementation. It also specifies the packaging type as `war` (Web Application Archive), which is the standard format for deploying Java web applications.
*   **web.xml:** The Deployment Descriptor. It tells the Servlet Container (Tomcat) how to initialize the application. By defining the `ServletContainer` and its mapping, it eliminates the need for a custom Java `Application` class.
*   **Resource Classes:** Classes like `HelloResource.java` serve as the API endpoints. By using JAX-RS annotations, these classes define the URI paths and HTTP methods they respond to, allowing for a declarative style of programming.

## 4. `PaolaRestApi:war` vs `PaolaRestApi:war exploded`
The difference between `PaolaRestApi:war` and `PaolaRestApi:war exploded` comes down to how your project is packaged and how quickly you can see code changes during development.

### `PaolaRestApi:war` (The Archive)
**What it is:** A single, compressed `.war` (Web Application Archive) file. It is essentially a ZIP containing your entire application.

**Best for:** Production or final testing.

**Drawback:** Every time you change one line of code, the entire archive must be rebuilt and the server must unpack it again, which is slower.

### `PaolaRestApi:war exploded` (The Folder)
**What it is:** An uncompressed directory structure that looks like a deployed web app.

**Best for:** Active development (Day 1). This is usually the best option while coding.

**Advantage:** It allows partial updates ("hot swap"-style workflow). If you change a Java method or an HTML file, IntelliJ can update only the changed file, making restart/update cycles faster.

### Comparison Table
| Feature | `war` (Archive) | `war exploded` |
|---|---|---|
| Structure | Single compressed file | Uncompressed directory |
| Deployment speed | Slower | Faster (partial updates) |
| Typical use case | Final release / production | Daily coding / debugging |
| IntelliJ location | Under **Artifacts** | Under **Artifacts** |

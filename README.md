<h1 align="center"> Biolink Site API </h1>

![JAR Build Status](https://github.com/george/biolink-site/actions/workflows/build-and-upload-jar.yml/badge.svg)

Powerful and scalable biolink REST API for biolink sites in Spring.

**Requirements**

- Java 17 and Maven for development
- Docker for local development with a Postgres image, as well as for deployment
- PostgreSQL for deployment

**Features**

- Well-designed Postgres data storage, focused on maintainability and minimum-redundancy
- Custom authentication system with MFA support through TOTP
- Secure login IP verification, automatically requiring an MFA code from the email to continue
- Moderation and administration panel with fully customizable user group system
- Highly scalable and maintainable, with a focus on expandability
- Containerized with Docker, including JAR building in image building
- Easy to add onto, with a clearly defined project structure

**Licensing**

This project is licensed under the MIT license.
# Greengage Keycloak Authenticator

This project contains a custom Keycloak authenticator that redirects users to a specific URL if certain user attributes are not set during the authentication process.

## Requirements

- Java Development Kit (JDK) 8 or higher
- Apache Maven
- Keycloak server (version 11.0.0 or higher)

## Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/your-repo/greengage-keycloak-authenticator.git
    cd greengage-keycloak-authenticator
    ```

2. Build the project using Maven:
    ```sh
    mvn clean package
    ```

3. Copy the generated JAR file to the Keycloak `standalone/deployments` directory. This directory is typically found in your Keycloak installation path:
    ```sh
    cp target/greengage-keycloak-authenticator.jar /path/to/keycloak/standalone/deployments/
    ```

4. Restart the Keycloak server to load the new authenticator:
    ```sh
    /path/to/keycloak/bin/standalone.sh
    ```

## Usage

1. Log in to the Keycloak admin console.

2. Navigate to `Authentication` -> `Flows`.

3. Create a new authentication flow or modify an existing one.

4. Add a new execution to your flow and select `GreengageLoginForm` as the provider.

5. Configure the execution as required.

## Development

To make changes to the authenticator, edit the Java files in the `src/main/java/com/greengage/spi/authentication` directory.

After making changes, recompile the project with Maven:
```sh
mvn clean package

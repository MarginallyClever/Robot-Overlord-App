### Coding Standards for Robot Overlord

This document outlines the coding standards to be followed for this project. These standards are derived from the existing codebase and are intended to ensure consistency and maintainability.

#### 1. General Principles
- **Clarity over Conciseness**: Write code that is easy to read and understand.
- **Follow Existing Patterns**: Maintain consistency with the surrounding code and established project structures.
- **Don't be a jerk**: As per `CONTRIBUTING.md`, maintain a professional and respectful tone in comments and documentation.

#### 2. Java Language Standards
- **Indentation**: Use 4 spaces for indentation. Do not use tabs.
- **Naming Conventions**:
    - **Classes**: `PascalCase` (e.g., `TextureChooserDialog`, `Node`).
    - **Methods**: `camelCase` (e.g., `setSelectedItem`, `addChild`).
    - **Variables**: `camelCase` (e.g., `selectedItem`, `listeners`).
    - **Constants**: `UPPER_SNAKE_CASE` (e.g., `ICON`, `JFileChooser.APPROVE_OPTION`).
- **Braces**: Use K&R style (Egyptian braces) where the opening brace is on the same line as the statement.
    ```java
    if (condition) {
        // ...
    } else {
        // ...
    }
    ```
- **Line Length**: Aim for a maximum of 120 characters per line.

#### 3. Documentation
- **Javadoc**: Use Javadoc for all public classes and complex public/protected methods.
    - Include `@param`, `@return`, and `@throws` tags where appropriate.
    - Use `<p>` tags for paragraphs and `{@link}` for referencing other code elements.
- **Comments**: Use comments to explain "why" something is done, especially for non-obvious logic. Match the existing frequency of comments in the file.

#### 4. Error Handling and Logging
- **Logging**: Use SLF4J for logging.
    - Initialize the logger as: `private static final Logger logger = LoggerFactory.getLogger(YourClass.class);`
- **Exceptions**: Use descriptive exception messages. Prefer standard Java exceptions where they fit.

#### 5. Testing
- **Framework**: Use JUnit 5 (JUnit Jupiter) for testing.
- **Requirements**:
    - New features should include unit tests.
    - Bug fixes should include a reproduction test case.
    - Tests should be placed in the `src/test/java` directory, following the same package structure as the production code.

#### 6. Dependencies and Build
- **Maven**: This project uses Maven for build and dependency management.
- **pom.xml**: Add new dependencies to `pom.xml` only when necessary and ensure they are compatible with the existing GPLv2 license.

#### 7. UI Development (Swing)
- **Consistency**: Follow the existing patterns for Swing components and dialogs (e.g., using `BorderLayout`, `JToolBar`, and `JOptionPane` where appropriate).
- **Icons**: Use existing icons from the `resources` directory when possible to maintain visual consistency.

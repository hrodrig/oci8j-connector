# Contributing to Oracle 8i Connector

Thank you for your interest in contributing to the Oracle 8i Connector project! This document provides guidelines for contributing to this project.

## Development Setup

### Prerequisites
- Java 8+
- Maven 3.6+
- Docker (optional)
- Oracle 8i database access (for testing)

### Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/oci8j-connector.git
   cd oci8j-connector
   ```

3. **Set up the development environment**:
   ```bash
   # Copy example configuration
   cp config.example.yaml config.yaml
   cp docker-compose.example.yml docker-compose.yml
   
   # Edit with your Oracle connection details
   nano config.yaml
   nano docker-compose.yml
   ```

4. **Build and test**:
   ```bash
   mvn clean compile
   mvn test
   ```

## Security Guidelines

### Sensitive Files
- **Never commit** `docker-compose.yml` with real credentials
- **Never commit** `config.yaml` with real credentials
- **Always use** environment variables for sensitive data
- **Use example files** as templates

### Configuration Files
- `config.example.yaml` - Safe to commit (uses environment variables)
- `docker-compose.example.yml` - Safe to commit (placeholder values)
- `src/main/resources/config.yaml` - Safe to commit (uses environment variables)

## Code Style

### Java
- Follow standard Java naming conventions
- Use meaningful variable and method names
- Add Javadoc comments for public methods
- Keep methods focused and small

### Configuration
- Use YAML format for configuration files
- Include comments explaining configuration options
- Use environment variables for sensitive data

## Testing

### Running Tests
```bash
# Unit tests
mvn test

# Integration tests with Docker
docker-compose up -d
./test-api.sh
```

### Test Coverage
- Write unit tests for new features
- Test error scenarios
- Verify security configurations

## Pull Request Process

1. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**:
   - Follow the coding style guidelines
   - Add tests for new functionality
   - Update documentation if needed

3. **Test your changes**:
   ```bash
   mvn clean test
   ./test-api.sh
   ```

4. **Commit your changes**:
   ```bash
   git add .
   git commit -m "feat: Add your feature description"
   ```

5. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request** on GitHub

## Commit Message Format

Use conventional commit messages:

- `feat:` - New features
- `fix:` - Bug fixes
- `docs:` - Documentation changes
- `style:` - Code style changes
- `refactor:` - Code refactoring
- `test:` - Test additions or changes
- `chore:` - Build process or auxiliary tool changes

Examples:
- `feat: Add Basic Authentication support`
- `fix: Resolve connection timeout issue`
- `docs: Update README with Docker instructions`

## Security Considerations

### SQL Injection Prevention
- Always use parameterized queries
- Validate input parameters
- Implement keyword filtering for dangerous SQL operations

### Authentication
- Use Basic Authentication for API access
- Store credentials in environment variables
- Never hardcode passwords in source code

### Configuration Security
- Use example files for templates
- Document security requirements
- Include security warnings in documentation

## Questions or Issues?

- **Bug reports**: Use GitHub Issues
- **Feature requests**: Use GitHub Issues
- **Security issues**: Contact maintainers directly
- **General questions**: Use GitHub Discussions

## License

By contributing to this project, you agree that your contributions will be licensed under the MIT License.

# 🤝 Contributing to Fake System Update

Thank you for your interest in contributing to the Fake System Update project! This document provides guidelines and instructions for contributing to the codebase.

---

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [Development Setup](#development-setup)
4. [Development Workflow](#development-workflow)
5. [Coding Standards](#coding-standards)
6. [Commit Guidelines](#commit-guidelines)
7. [Pull Request Process](#pull-request-process)
8. [Issue Reporting](#issue-reporting)
9. [Feature Requests](#feature-requests)
10. [Testing](#testing)
11. [Documentation](#documentation)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inspiring community for all. By participating in this project, you agree to:

- Be respectful and inclusive
- Accept constructive criticism gracefully
- Focus on what is best for the community
- Show empathy toward other community members

### Unacceptable Behavior

- Harassment or discriminatory language
- Trolling or insulting comments
- Public or private harassment
- Publishing others' private information
- Unethical or unprofessional conduct

---

## Getting Started

### Prerequisites

Before contributing, ensure you have:

- **Android Studio** Arctic Fox (2020.3.1) or newer
- **JDK 11** or higher
- **Git** for version control
- **Android SDK** with API levels 24-36
- Basic knowledge of Kotlin and Android development

### First Contribution

Looking for a good first issue? Check our issue tracker for:
- Issues labeled `good first issue`
- Issues labeled `help wanted`
- Documentation improvements
- UI/UX enhancements

---

## Development Setup

### 1. Fork the Repository

Click the "Fork" button on GitHub to create your own copy of the repository.

### 2. Clone Your Fork

```bash
git clone https://github.com/YOUR_USERNAME/FakeSystemUpdate.git
cd FakeSystemUpdate
```

### 3. Add Upstream Remote

```bash
git remote add upstream https://github.com/ORIGINAL_OWNER/FakeSystemUpdate.git
```

### 4. Set Up Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to your cloned repository
4. Wait for Gradle sync to complete
5. Verify the project builds successfully

### 5. Create a Branch

```bash
git checkout -b feature/your-feature-name
```

**Branch Naming Convention:**
- `feature/add-ios-style` - New features
- `bugfix/fix-progress-calculation` - Bug fixes
- `docs/update-readme` - Documentation updates
- `refactor/simplify-setup-activity` - Code refactoring
- `test/add-unit-tests` - Test additions

---

## Development Workflow

### Standard Workflow

1. **Sync with Upstream**
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Make Your Changes**
   - Write clean, documented code
   - Follow coding standards (see below)
   - Add tests for new functionality

3. **Test Your Changes**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

4. **Commit Your Changes**
   ```bash
   git add .
   git commit -m "feat: add iOS 18 update style"
   ```

5. **Push to Your Fork**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create Pull Request**
   - Go to GitHub
   - Click "New Pull Request"
   - Fill out the PR template
   - Wait for review

---

## Coding Standards

### Kotlin Style Guide

We follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).

#### Key Points

**Naming Conventions:**
```kotlin
// Classes: PascalCase
class FakeUpdateActivity : AppCompatActivity()

// Functions: camelCase
fun navigateToReveal()

// Constants: UPPER_SNAKE_CASE
const val EXTRA_STYLE = "extra_style"

// Variables: camelCase
val prankDuration = 10
var currentProgress = 0
```

**Indentation:**
- Use 4 spaces (no tabs)
- Continuation indent: 8 spaces

**Line Length:**
- Maximum 120 characters
- Break long lines appropriately

**Imports:**
```kotlin
// Order: Android, AndroidX, Third-party, Java/Kotlin, Internal
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlin.math.min
import io.softexforge.fakesysupdate.PrankSessionData
```

**Documentation:**
```kotlin
/**
 * Calculates non-linear progress for realistic update simulation.
 * 
 * Progress stalls at specific points to maximize prank effect.
 * 
 * @param fraction Time fraction (0.0 to 1.0)
 * @return Progress percentage (0 to 99)
 */
fun computeNonLinearProgress(fraction: Double): Int {
    // Implementation
}
```

### XML Style Guide

**Layout Files:**
```xml
<!-- Use kebab-case for IDs -->
<TextView
    android:id="@+id/text_prank_title"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/title_setup"
    android:textSize="24sp"
    android:textColor="?attr/colorOnSurface" />
```

**String Resources:**
```xml
<!-- Use snake_case for string names -->
<string name="onboarding_welcome_title">Welcome to Fake Update</string>
<string name="error_past_time">Selected time is in the past.</string>
```

**Dimensions:**
```xml
<!-- Use dp for dimensions, sp for text sizes -->
<dimen name="spacing_normal">16dp</dimen>
<dimen name="text_size_large">20sp</dimen>
```

### Code Organization

**Activity Structure:**
```kotlin
class SetupActivity : AppCompatActivity() {
    // 1. Companion object & constants
    companion object {
        const val EXTRA_STYLE = "extra_style"
    }
    
    // 2. Properties
    private lateinit var styleDropdown: AutoCompleteTextView
    private var selectedStyle: String = "stock"
    
    // 3. Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        // Implementation
    }
    
    // 4. UI setup methods
    private fun setupStyleDropdown() {
        // Implementation
    }
    
    // 5. Event handlers
    private fun onStartButtonClick() {
        // Implementation
    }
    
    // 6. Helper methods
    private fun validateConfiguration(): Boolean {
        // Implementation
    }
    
    // 7. Inner classes
    private class StyleAdapter : BaseAdapter() {
        // Implementation
    }
}
```

---

## Commit Guidelines

### Commit Message Format

We use [Conventional Commits](https://www.conventionalcommits.org/).

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes

**Examples:**
```
feat(update-styles): add Windows 11 update screen

Implemented Windows 11 update style with:
- Blue accent colors
- Microsoft logo
- Progress ring animation

Closes #42
```

```
fix(exit-methods): correct shake detection threshold

The shake detection was too sensitive, causing false
positives. Increased threshold from 12f to 15f.

Fixes #38
```

```
docs(readme): update installation instructions

Added missing step for SDK installation and clarified
Gradle sync process.
```

### Commit Best Practices

- Write clear, concise commit messages
- Use present tense ("add feature" not "added feature")
- Reference issues and pull requests when applicable
- Keep commits atomic (one logical change per commit)
- Don't commit commented-out code
- Don't commit debug logs

---

## Pull Request Process

### Before Submitting

- [ ] Code follows the style guidelines
- [ ] All tests pass locally
- [ ] New code has appropriate test coverage
- [ ] Documentation is updated
- [ ] Commit messages follow guidelines
- [ ] No merge conflicts with main branch

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
Describe how you tested your changes

## Screenshots (if applicable)
Add screenshots for UI changes

## Checklist
- [ ] Code follows style guidelines
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No breaking changes (or documented)
```

### Review Process

1. **Automated Checks**
   - CI/CD pipeline runs automatically
   - Lint checks must pass
   - Unit tests must pass

2. **Code Review**
   - At least one maintainer must approve
   - Address all review comments
   - Keep discussions professional

3. **Merge**
   - Squash and merge (usually)
   - Maintainer will merge after approval

---

## Issue Reporting

### Bug Reports

Use the bug report template:

```markdown
**Describe the bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce:
1. Go to '...'
2. Click on '...'
3. See error

**Expected behavior**
What you expected to happen.

**Screenshots**
If applicable, add screenshots.

**Device Info:**
 - Device: [e.g. Pixel 7]
 - Android Version: [e.g. Android 14]
 - App Version: [e.g. 1.0]

**Additional context**
Any other relevant information.
```

### Good Bug Report Example

```markdown
**Describe the bug**
Triple tap exit method doesn't work on Samsung devices.

**To Reproduce**
1. Configure prank with triple tap exit
2. Launch prank on Samsung Galaxy S23
3. Triple tap the screen
4. Nothing happens

**Expected behavior**
Prank should exit to reveal screen after 3 taps.

**Device Info:**
 - Device: Samsung Galaxy S23
 - Android Version: Android 14 (One UI 6)
 - App Version: 1.0

**Additional context**
Works fine on Pixel devices. Might be related to Samsung's
touch sensitivity settings.
```

---

## Feature Requests

### Proposing New Features

1. **Check Existing Issues**
   - Search for similar requests
   - Avoid duplicates

2. **Use Feature Request Template**
   ```markdown
   **Is your feature request related to a problem?**
   Describe the problem.
   
   **Describe the solution**
   What you want to happen.
   
   **Describe alternatives**
   Alternative solutions you've considered.
   
   **Additional context**
   Screenshots, mockups, etc.
   ```

3. **Discuss First**
   - For major features, open a discussion before coding
   - Get feedback from maintainers
   - Ensure alignment with project goals

### Feature Ideas

We're particularly interested in:
- New update style variants (iOS, Windows, etc.)
- Accessibility improvements
- Performance optimizations
- Localization/translations
- Enhanced UI/UX
- Better analytics insights

---

## Testing

### Unit Tests

Located in `app/src/test/`

**Writing Tests:**
```kotlin
class ProgressCalculationTest {
    @Test
    fun `progress starts below 10 percent`() {
        val progress = FakeUpdateActivity.computeNonLinearProgress(0.05)
        assertTrue(progress < 10)
    }
    
    @Test
    fun `progress never reaches 100`() {
        val progress = FakeUpdateActivity.computeNonLinearProgress(0.99)
        assertTrue(progress < 100)
    }
}
```

**Run Tests:**
```bash
./gradlew test
```

### Instrumented Tests

Located in `app/src/androidTest/`

**Writing Tests:**
```kotlin
@RunWith(AndroidJUnit4::class)
class SetupActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(SetupActivity::class.java)
    
    @Test
    fun startButton_isDisplayed() {
        onView(withId(R.id.btn_start_prank))
            .check(matches(isDisplayed()))
    }
}
```

**Run Tests:**
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist

Before submitting a PR, manually test:
- [ ] Onboarding flow completes
- [ ] All update styles render correctly
- [ ] Each exit method works
- [ ] Scheduling modes function properly
- [ ] Share/receipt generation works
- [ ] No crashes or ANRs
- [ ] UI looks good on different screen sizes
- [ ] App works on Android 7.0 (API 24)

---

## Documentation

### Code Documentation

**Document:**
- All public APIs
- Complex algorithms
- Non-obvious logic
- TODOs and FIXMEs

**Example:**
```kotlin
/**
 * Generates a shareable image receipt from prank session data.
 * 
 * This renderer inflates the selected template layout, populates
 * it with session data, and renders it to a bitmap.
 * 
 * @param data The prank session data to visualize
 * @return A bitmap containing the rendered receipt
 * 
 * @throws IllegalStateException if template inflation fails
 */
fun generate(data: PrankSessionData): Bitmap {
    // Implementation
}
```

### Updating Documentation

When making changes, update:
- [ ] Inline code comments
- [ ] README.md (if user-facing changes)
- [ ] DOCUMENTATION.md (if architecture changes)
- [ ] Changelog (for releases)

---

## Project Vision

### Goals

1. **Entertainment First** - Create the most realistic prank experience
2. **Safety & Ethics** - Ensure responsible use with clear disclaimers
3. **Quality** - Maintain high code quality and performance
4. **Privacy** - Never collect user data

### Non-Goals

- Real system modifications
- Malicious functionality
- Data collection or tracking
- Deceptive practices

---

## Community & Support

### Getting Help

- **Documentation:** Start with [DOCUMENTATION.md](DOCUMENTATION.md)
- **Issues:** Search existing issues first
- **Discussions:** Use GitHub Discussions for questions
- **Website:** Visit [fakesysupdate.softexforge.io](https://fakesysupdate.softexforge.io/)

### Staying Updated

- Watch the repository for notifications
- Check the changelog for new releases
- Follow project milestones

---

## Recognition

Contributors will be:
- Listed in the project contributors
- Mentioned in release notes for significant contributions
- Credited in the app's About section (for major features)

---

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.

---

## Questions?

If you have questions about contributing:
1. Check this guide thoroughly
2. Search existing issues and discussions
3. Open a new discussion if needed
4. Contact via [feedback form](https://fakesysupdate.softexforge.io/feedback)

---

**Thank you for contributing to Fake System Update!** 🎭

Your efforts help make this the best entertainment app for harmless pranks.

---

<p align="center">
  Made with 💙 by <a href="https://www.nasimstg.dev">Nasim STG</a> at <a href="https://www.softexforge.io">SoftexForge</a>
</p>

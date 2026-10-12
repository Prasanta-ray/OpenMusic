# Contributing to OpenMusic

Thank you for your interest in contributing to OpenMusic! We welcome contributions from developers of all experience levels.

---

## Code of Conduct

We are committed to providing a welcoming, inclusive, and harassment-free environment for all contributors. Please be respectful and constructive in all discussions, issues, and pull requests.

---

## How to Contribute

### Reporting Bugs
Before filing an issue, search the issue tracker to ensure the bug hasn't already been reported. When submitting a bug report:
1. Provide a clear, descriptive title.
2. Include the Android OS version and device model.
3. Detail reproducible steps to trigger the bug.
4. Include relevant logcat output if applicable (`adb logcat -s OpenMusic ExoPlayer`).
5. Describe expected vs. actual behavior.

### Suggesting Enhancements
Feature requests are tracked via GitHub Discussions and Issues:
1. Explain the problem or limitation the feature solves.
2. Describe the proposed solution and workflow.
3. Outline any alternatives or considerations considered.

### Submitting Pull Requests
1. **Fork the repository** and create your branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. **Adhere to the coding standards**:
   * Kotlin code style following official Android guidelines.
   * Format all code with `./gradlew ktlintCheck` or standard Android Studio formatting.
   * Write clean, self-documenting code with meaningful variable and function names.
   * Maintain unidirectional data flow (UDF) in Compose components.
3. **Verify builds**:
   * Ensure the project compiles cleanly:
     ```bash
     ./gradlew assembleDebug
     ```
   * Ensure all unit tests pass:
     ```bash
     ./gradlew test
     ```
4. **Commit messages**:
   * Use clear, imperative commit messages (e.g., `feat: add sleep timer dialog`, `fix: resolve audio stutter on track skip`).
5. **Open a Pull Request**:
   * Fill out the PR template with a description of the changes, affected components, and testing steps.
   * Link related issues.

---

## Architecture Guidelines

* **Architecture Pattern**: Unidirectional Data Flow (UDF / MVI) with ViewModel and StateFlow.
* **UI**: 100% Jetpack Compose with Material 3 and custom glassmorphic theming. Avoid legacy View references.
* **Audio Engine**: AndroidX Media3 `MediaSessionService` with custom LRU Opus audio cache and `ExoPlayer`.
* **Networking**: Ktor HTTP Client configured with lightweight InnerTube protocol serialization.
* **Persistence**: Room Database with Coroutines Flow for cached playlists, favorites, and history.

---

## License

By contributing to OpenMusic, you agree that your contributions will be licensed under the project's [GNU General Public License v3.0](LICENSE).

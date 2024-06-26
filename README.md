# ollama-commit-summarizer

![Build](https://github.com/itishermann/ollama-commit-summarizer/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
<!-- Plugin description -->
The Ollama Commit Summarizer Plugin enhances your development workflow by automatically generating meaningful and context-aware commit messages. Powered by Ollama's advanced language model, this plugin analyzes your code changes and provides precise, human-readable commit messages that save time and ensure consistency across your projects.

## Features

- **Automated Commit Messages:** Generate descriptive commit messages based on code changes. Supports various commit styles (e.g., Conventional Commits, Gitmoji).
- **Context-Aware Analysis:** Understands the context of your code changes for accurate message generation. Incorporates relevant code snippets and file names for clarity.
- **Customizable Templates:** Customize commit message templates to fit your team's standards. Easily toggle between different templates as needed.
- **Integration with Git:** Seamless integration with Git and popular VCS tools. Compatible with GitHub, GitLab, Bitbucket, and more.
- **Interactive Suggestions:** Provides multiple commit message suggestions for you to choose from. Edit and refine suggested messages before committing.
- **Language Support:** Supports multiple programming languages and frameworks. Continually updated to recognize new languages and libraries.

## Benefits

- **Increased Productivity:** Save time by automating the commit message writing process.
- **Consistency:** Ensure commit messages are uniform and adhere to your project's guidelines.
- **Improved Collaboration:** Facilitate better collaboration and understanding among team members with clear and descriptive commit messages.
- **Enhanced Workflow:** Smooth integration with your existing development tools and workflows.

## Requirements

- Compatible with IntelliJ IDEA, PyCharm, WebStorm, PhpStorm, RubyMine, and other JetBrains IDEs.
- Requires a valid Git installation and configured repository.
- Ollama must be installed and running locally or remotely. Visit [Ollama][ollama-github-url] to learn more.

## Installation

1. Navigate to the JetBrains Marketplace.
2. Search for "Ollama Commit Summarizer."
3. Click "Install" and follow the prompts to add the plugin to your IDE.
4. Configure the plugin settings to match your preferences and start generating commit messages instantly.

## Contributing

Contributions are welcome! If you'd like to contribute to this project, please follow these steps:

1. Fork the repository from [GitHub][repo-url].
2. Create a new branch (`git checkout -b feature-branch`).
3. Make your changes and commit them (`git commit -am 'Add new feature'`).
4. Push to the branch (`git push origin feature-branch`).
5. Create a new Pull Request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

Enhance your development process with the Ollama Commit Summarizer Plugin and experience the future of automated commit messaging!
<!-- Plugin description end -->
---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
[repo-url]: https://github.com/itishermann/ollama-commit-summarizer
[ollama-github-url]:https://github.com/ollama/ollama

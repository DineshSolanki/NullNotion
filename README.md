# NullNotion IntelliJ Plugin

NullNotion is your ultimate companion in IntelliJ that automagically annotates fields in your entity classes with `@NonNull` or `@Nullable` based on their database constraints. No more tedious manual annotations!

## Installation

[![Get NullNotion from the JetBrains Plugin Marketplace](https://camo.githubusercontent.com/d1e8ac9d3ba6c06ad1d03019aae5e2c7e8d85245f0f38f1bfbd5ecdc6cee0e58/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f596969477578696e672f5472616e736c6174696f6e506c7567696e406d61737465722f696d616765732f696e7374616c6c6174696f6e5f627574746f6e2e737667)](https://plugins.jetbrains.com/plugin/20902-nullnotion)

- **Installing from the plugin repository within the IDE:**
    - Go to <kbd>Preferences(Settings)</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd>.
    - Search for <b>"NullNotion"</b> and install the plugin.

- **Installing manually:**
    - Download the plugin package from [GitHub Releases][gh:releases] or the [JetBrains Plugin Repository][plugin-versions].
    - Go to <kbd>Preferences(Settings)</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>.
    - Select the downloaded plugin package and install (no need to unzip).

## Features

* 🎯 Automatically annotates fields in an entity class with `@NonNull` or `@Nullable` based on database constraints.
* 🛡️ Supports MSSQL, MySQL, PostgreSQL, and Oracle database types.
* 📂 Saves database connection information for each project.

## How to Use

1. Install the NullNotion plugin from the [JetBrains Plugin Marketplace](https://plugins.jetbrains.com/plugin/20902-nullnotion).
2. Right-click on an entity Java class annotated with `@Table`, or right-click inside the class.
3. Choose "Process NullNotion" from the menu.
4. On the first run of each project, you will be asked to select a database type and enter the connection string for the database.
5. The plugin will run in the background, and you will be notified when it's completed.

## Benefits

- 🚀 Provides the developer with IDE static analysis hints on potential NPE problems.
- ⏱️ Saves time and effort in manually annotating fields with `@NonNull` or `@Nullable`.
- 🎭 Works in the background, allowing you to continue working while it processes.
- 🌱 Ideal for Spring Boot projects.

## Screenshots

![Screenshot 1](https://user-images.githubusercontent.com/15937452/216801786-edb05d1e-79de-4fa3-9d68-7628fff94146.png)
![Screenshot 2](https://user-images.githubusercontent.com/15937452/216801839-ca2ad078-aa46-41fa-bd68-380cb41aefc7.png)

## Support Us

You can support us in various ways:

* ⭐ Star this project on GitHub.
* 🔄 Share this plugin with your friends and colleagues.
* 🌟 Rate this plugin on the [JetBrains Plugin Repository](https://plugins.jetbrains.com/plugin/20902-nullnotion).
* 🐞 Make pull requests to improve the plugin.
* 🚩 Report bugs if you encounter any.
* 💡 Share your ideas with us.
* 💖 Become a [sponsor](https://github.com/sponsors/DineshSolanki) to help further development.

## Contribution

We welcome contributions to NullNotion! If you want to contribute, please fork the repository, make your changes, and submit a pull request.

## License

NullNotion is open-source software licensed under the [GPL-3 License](https://opensource.org/licenses/GPL-3.0).

Development powered by [JetBrains](https://www.jetbrains.com/?from=NullNotion).
---
[![https://www.jetbrains.com/?from=NullNotion](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)](https://www.jetbrains.com/?from=NullNotion)


Let NullNotion enhance your IntelliJ experience! 🚀✨

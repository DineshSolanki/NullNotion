# NullNotion Intellij Plugin

NullNotion is an IntelliJ plugin that helps you automatically annotate fields in an entity class with `@NonNull` or `@Nullable` based on if they are set as not null in the database. 

## Installation 

The plugin can be installed directly from the JetBrains marketplace using the following link: 
https://plugins.jetbrains.com/plugin/20902-nullnotion 

## Features

* Automatically annotates fields in an entity class with @NonNull or @Nullable based on database not null constraints.
* Supports MSSQL, MySQL, PostgreSQL, and Oracle database types.
* Saves database connection information for each project.

## Usage

* Install the NullNotion plugin from the [JetBrains Plugin Marketplace](https://plugins.jetbrains.com/plugin/20902-nullnotion).
* Right-click on an entity Java class annotated with @Table or right-click inside the class.
    Choose "Process NullNotion" from the menu.
* On the first run of each project, you will be asked to select a database type and enter the connection string for the database.
* The plugin will run in the background and you will be notified when it's completed.

## Benefits 

- Provides the developer with IDE static analysis hints on potential NPE problems. 
- Saves time and effort in manually annotating fields with `@NonNull` or `@Nullable`. 
- Works in the background, allowing you to continue working while it processes. 
- Ideal for Spring Boot projects. 

## Screenshots

<img alt="context menu" height="400" src="https://user-images.githubusercontent.com/15937452/216801786-edb05d1e-79de-4fa3-9d68-7628fff94146.png" title="NullNotion"/>
<img alt="context menu" height="400" src="https://user-images.githubusercontent.com/15937452/216801839-ca2ad078-aa46-41fa-bd68-380cb41aefc7.png" title="NullNotion"/>

![image](https://user-images.githubusercontent.com/15937452/216802075-14ceb597-eef1-4c0d-96db-91141517c3d7.png)
![image](https://user-images.githubusercontent.com/15937452/216802078-2dd91164-c789-424a-b7e2-ae5ebc25778f.png)


## Contribution

We welcome contributions to NullNotion! If you want to contribute, please fork the repository, make your changes, and submit a pull request.

## License

NullNotion is open source software licensed under the [GPL-3 License](https://opensource.org/licenses/GPL-3.0).


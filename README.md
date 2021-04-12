# Background
This project is meant an interview task for programmers who are applying for a position in Accountor Finago Oy.

# Task
The idea is to create a simple batch processor. Your program receives a bunch of PDF and XML files. Within the given XML files, you're supposed to find the receivers for PDF files and move them accordingly in the folder structure.

## Functional requirements
Within the provided files, you should see a `data` folder:
```
└── data
    ├── archive
    ├── error
    ├── in
    └── out
```

The program receives XML and PDF files to the `in` folder. PDF are to be delivered to the customers and XML files provide information to make it possible:
```
<receivers>
    <receiver>
        <receiver_id>8842</receiver_id>
        <first_name>John</first_name>
        <last_name>Smith</last_name>
        <file>6723451234.pdf</file>
        <file_md5>d41d8cd98f00b204e9800998ecf8427e</file_md5>
    </receiver>
    <receiver>
        ...
    </receiver>
    ...
</receivers>
```

You've been provided with example files. They are in the `in` folder.

Your program should:
1. Monitor the `in` folder.
1. Once an XML file and its PDF files are received, process them.
1. When all files have been processed, resume to monitoring the `in` folder for new files.

Processing an XML file:
1. If the XML file is invalid, move it to the `error` folder and stop processing it.
1. Otherwise, read a `receiver` block from the received XML file.
1. Find the PDF file belonging to that receiver.
1. Check if the PDF file is uncorrupted with the given MD5 checksum.
1. If the PDF file is OK:
   1. Copy it to a subfolder in the `out` folder. The subfolder should be `receiver_id mod 100 / receiver_id`.
   1. Write to the same subfolder an XML file containing only corresponding `receiver` block. Use the name of the PDF file.
   1. See the example below.
1. If the PDF file is corrupted, do the same steps as above but create the subfolder in the `error` folder.
1. If the PDF file is missing, do the same steps as with a corrupted file but write only the XML file.
1. Once the received XML file has been completely processed: 
   1. Move the XML file from the `in` folder to the `archive` folder.
   1. Delete all PDF files used in the XML file from the `in` folder.


A starting situation (with one file and receiver) for the examples:
```
└── data
    ├── archive
    ├── error
    ├── in
    |   ├── 6723450001.xml
    |   └── 6723451234.pdf
    └── out
```

An example of a successfully received and handled PDF file:
```
└── data
    ├── archive
    |   └── 6723450001.xml
    ├── error
    ├── in
    └── out
        └── 42
            └── 8842
                ├── 6723451234.pdf
                └── 6723451234.xml
```

An example if the PDF file is corrupted:
```
└── data
    ├── archive
    |   └── 6723450001.xml
    ├── error
    |   └── 42
    |       └── 8842
    |           ├── 6723451234.pdf
    |           └── 6723451234.xml
    ├── in
    └── out
```

An example if the PDF file is missing:
```
└── data
    ├── archive
    |   └── 6723450001.xml
    ├── error
    |   └── 42
    |       └── 8842
    |           └── 6723451234.xml
    ├── in
    └── out
```

It is guaranteed to you that:
1. All files delivered to the `in` folder have a unique name.
1. PDF files are delivered before the XML file they belong to. This means that once an XML file is readable, we will not provide more PDF files for it.
1. When a file becomes accessible, it's completely written. I.e. you don't have to worry about other processes modifying the files while your program is reading them.
1. One or more receivers can receive the same PDF file, but only within the same XML file. Two XML files will never share the same PDF files.

## Technical requirements
The result should adhere to following requirements:
1. The project should be build, packaged and managed by Apache Maven.
1. The application should be "Dockerized", i.e. it should be run from within a Docker container.
1. The application should run in JVM.

As a starting point, you're provided with a Dockerfile that will:
1. Use a base image that provides you a Java 14 JRE installation.
1. Creates a folder in the container for the application.
1. Creates "data" folder into the programs's folder.
   1. The files to be processed are mounted to this folder.
1. Copies the application's JAR with dependencies into ("fat/über jar") it and runs the JAR file.
   1. "batch-processor-1.0-SNAPSHOT-jar-with-dependencies.jar" in the target folder.

Also, there is a simple Maven project that provides a starting location for the project. It's pom.xml will:
1. Set the Java version to 14.
1. Source encoding to UTF-8.
1. Create the JAR with dependencies during the package phase.

Feel free to modify both Dockerfile and pom.xml to fit your needs. Just notice two things:
1. Use Docker Official Images.
1. Use the Maven default repository (Maven Central).
1. We should be able to mount our `data` folder to be processed by the application.

## Other requirements/tips
You can use whatever IDE, formatting/coding style, dependencies you like to.

Notice that solving the problem is only a part of the task. We're also interested to see your coding style, for example how you've done error handling, logging, program flow, class/method naming, dependency selection etc.

# Running the "skeleton" project
If you want to run the provided example project, do the following:
1. Navigate to the folder containing the `pom.xml` file.
1. Run: `mvn package`
1. Run: `docker build -t batch-processor .`
1. Run: `docker run batch-processor`
1. You should see in the console this message: `*beep boop* ...processing data... *beep boop*`

Notice that you will have to install Java, Maven, Docker etc. tools to your computer yourself. Those are not instructed here.

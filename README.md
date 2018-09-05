# Loguerro

This application reads log entries from a file whose path has been provided as an argument and writes the data 
to a HSQL-based file database.

## How to Build and Run the App

1. Clone the repository from this location:

   `git clone https://github.com/mariuszkawa/loguerro.git`
   
2. Change the directory:

   `cd loguerro`
   
3. Generate a runnable JAR by executing this:

   `./gradlew shadowJar`
   
   It may take a while (10-20 seconds).
   
4. The JAR file is located in `build/libs/loguerro.jar`:

   `cd build/libs`
   
5. Copy the input file into that directory. Alternatively, you can provide the full path to any location 
   on your hard drive.

6. Execute the application. Issue a command (provided that the input file name is `my_input_file.log`):

   `java -jar loguerro.jar my_input_file.log`
   
7. If the execution finishes successfully, your current directory should contain a couple of event.db.* files:

   - `event.db.log`
   - `event.db.properties`
   - `event.db.script`   
   
   Additionally, there should be a file named `loguerro.log`. It contains a detailed log from the execution 
   and may be useful in debugging if anything went wrong.

## Additional Info

Type:

`java -jar loguerro.jar --help`

to print the usage info screen.


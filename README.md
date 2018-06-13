# sangria-demo

Copyright 2018 Daniel Brice, except for portions by Facebook that are otherwise noted.

This demonstraction project serves as a guide to help you implement your own services.
This program is not suitable for use as a production server (though it wouldn't be too hard to productionalize it), nor is it suitable for direct reuse as a library.

This is a maven project.
To compile it, make sure `mvn` is installed and use `mvn package` from the same directory as `pom.xml`.
To run the compiled program, use `java -jar target/sangria-demo-1.0.0-SNAPSHOT-jar-with-dependencies.jar` from the same directory as `pom.xml`.
The service will be accessible in your browser via `http://localhost:8080/`.
If you have trouble running the code locally, please submit an issue, or DM @fried_brice on Twitter.

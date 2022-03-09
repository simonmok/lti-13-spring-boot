# lti-13-spring-boot
This project is a Java-based LTI 1.3 Tool tested with Blackboard Learn Ultra experience.


### Setup Instructions

- Register a new application in https://developer.blackboard.com.
- Register a new LTI tool provider in Blackboard Learn. Create a new placement in the LTI tool provider.
- Configure the client_id in src/main/resources/application.properties.
- Start up the Spring Boot program with Gradle.
- The program by default runs in port 8080. Setup a reverse proxy (e.g. nginx) that routes port 443 to port 8080. Alternatively, use ngrok (https://ngrok.com) to provide the tunneling.
- You can now launch the LTI tool via Blackboard Learn. You should see the current user information, course information, and course role information. Take a look at the console log to understand the LTI payload and the full JSON claim payload.
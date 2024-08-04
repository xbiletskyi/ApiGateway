# Use the official Gradle image to create a build artifact.
# This is a multi-stage build. In the first stage, we build the JAR file.
FROM gradle:jdk21 as builder

# Copy in the build script and source code
COPY --chown=gradle:gradle . /home/gradle/src

# Set the working directory
WORKDIR /home/gradle/src

# Use Gradle to build the application.
RUN gradle build --no-daemon

# Now that we have a JAR file, we can use a smaller base image to reduce size.
FROM openjdk:21-jdk

# Copy the JAR file from the previous stage into this new stage
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/application.jar

# Copy the .env file into the new stage
COPY --from=builder /home/gradle/src/.env /app/.env

# Set the working directory for the application
WORKDIR /app

# Run the application
CMD ["java", "-jar", "application.jar"]

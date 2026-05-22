FROM openjdk:11-jdk-slim

WORKDIR /app

# Copy all project files
COPY . .

# Compile the code using the jars in the libs folder
RUN javac -cp "libs/*" src/main/java/com/email/*.java

# Run the API server
CMD ["java", "-cp", "libs/*:src/main/java", "com.email.ApiServer"]

FROM openjdk:17-jdk-slim

WORKDIR /app

# Copie de l'API JAR
COPY ../api/build/libs/api-*.jar /app/api.jar

# Copie des fichiers de l'exemple
COPY src/ /app/src/
COPY build.gradle /app/
COPY settings.gradle /app/

# Exposition du port
EXPOSE 8080

# Commande de démarrage
CMD ["java", "-jar", "api.jar"]

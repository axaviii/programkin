# Базовый образ содержащий java 17
FROM openjdk:17-oracle

# директория приложения внутри контейнера
WORKDIR /app

# копирование JAR-файла приложения в контейнер
COPY target/socketserver-0.0.1-SNAPSHOT.jar app.jar

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
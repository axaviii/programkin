# Базовый образ содержащий java 17
FROM openjdk:17-oracle

# директория приложения внутри контейнера
WORKDIR /app

# копирование JAR-файла приложения в контейнер
COPY target/socket-client-0.0.1-SNAPSHOT.jar app.jar

#Определение переменной среды
ENV SOCKET_SERVER_HOST=localhost
ENV SOCKET_SERVER_PORT=8888

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
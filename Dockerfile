FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN javac DoubleTransposition.java
ENV PORT=8080
EXPOSE 8080
CMD ["java", "DoubleTransposition", "web"]

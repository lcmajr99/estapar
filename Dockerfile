# ================================
# Stage 1 — Build
# ================================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia apenas arquivos necessários para resolver dependências
COPY parking/pom.xml ./pom.xml
RUN mvn dependency:go-offline

# Copia o restante do código
COPY parking/src ./src

# Gera o JAR
RUN mvn clean package -DskipTests

# ================================
# Stage 2 — Runtime
# ================================
FROM eclipse-temurin:21-jre

# Cria usuário não-root
RUN useradd -ms /bin/bash appuser

WORKDIR /app

# Copia o JAR gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# Porta padrão (documentação)
EXPOSE 3003

# Usa usuário não-root
USER appuser

# Comando de execução
ENTRYPOINT ["java", "-jar", "app.jar"]

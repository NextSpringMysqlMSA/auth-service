# 빌드 스테이지
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Gradle 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드 (캐싱 활용)
RUN chmod +x ./gradlew
RUN ./gradlew dependencies

# 소스 복사 및 빌드
COPY src src
RUN ./gradlew clean bootJar -Pvaadin.productionMode -x test --no-daemon

# 실행 스테이지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 타임존 설정
RUN apk add --no-cache tzdata
ENV TZ=Asia/Seoul

# MySQL 클라이언트 및 필요한 라이브러리 추가
RUN apk add --no-cache mysql-client

# 빌드된 JAR 파일 복사
COPY --from=build /workspace/app/build/libs/*.jar app.jar

# 실행
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"] 
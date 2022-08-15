FROM openjdk:17
WORKDIR /var/app
COPY ./build/libs/project87-1.0.0.jar /var/app/project87-1.0.0.jar
ENV _JAVA_OPTIONS '-Dspring.datasource.host=db-server'
CMD [ "java", "-jar", "/var/app/project87-1.0.0.jar" ]
EXPOSE 8080

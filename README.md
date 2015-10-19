# uploader
Service for uploading data

# Local development

This command will enable local development and set local directory ("../uploader/target/uploads" - to retrieve this path we are using "user.dir" system property) for file upload:

    $ SPRING_PROFILES_ACTIVE=local mvn clean spring-boot:run

For generating proper request to uploader you could use: src/test/resources/upload_test.html
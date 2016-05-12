[![Build Status](https://travis-ci.org/trustedanalytics/uploader.svg?branch=master)](https://travis-ci.org/trustedanalytics/uploader)
[![Dependency Status](https://www.versioneye.com/user/projects/5729c2a7a0ca35005083fc0f/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5729c2a7a0ca35005083fc0f)

# uploader
Service for uploading data

# Local development

This command will enable local development and set local directory ("../uploader/target/uploads" - to retrieve this path we are using "user.dir" system property) for file upload:

    $ SPRING_PROFILES_ACTIVE=local mvn clean spring-boot:run

For generating proper request to uploader you could use: src/test/resources/upload_test.html

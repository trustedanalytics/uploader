[![Build Status](https://travis-ci.org/trustedanalytics/uploader.svg?branch=master)](https://travis-ci.org/trustedanalytics/uploader)
[![Dependency Status](https://www.versioneye.com/user/projects/5729c2a7a0ca35005083fc0f/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5729c2a7a0ca35005083fc0f)

# uploader
Service for uploading data

# Local development

This command will enable local development and set local directory ("../uploader/target/uploads" - to retrieve this path we are using "user.dir" system property) for file upload:

    $ SPRING_PROFILES_ACTIVE=local mvn clean spring-boot:run

For generating proper request to uploader you could use: src/test/resources/upload_test.html and multiple_files_upload_test.html

# Calling upload with REST API

* Upload single file

  Path: `/rest/upload/{orgGuid}`

Example response:
```
{
    "idInObjectStore": "4ad99778-1ce2-4dcf-a84a-26a08de63b9f",
    "objectStoreId": "hdfs://nameservice1/org/31e3c350-e937-4c59-ae15-25ddf227babd/brokers/userspace/da31788b-fad1-4b26-9772-722e90d7f057",
    "publicAccess": false,
    "category": "business",
    "title": "upload-data",
    "orgUUID": "31e3c350-e937-4c59-ae15-25ddf227babd",
    "source": "test1.txt"
}
```
* Upload multiple files

  Path: `/rest/v1/files/{orgGuid}`

Example response:
```
[
  {
    "idInObjectStore": "4ad99778-1ce2-4dcf-a84a-26a08de63b9f",
    "objectStoreId": "hdfs://nameservice1/org/31e3c350-e937-4c59-ae15-25ddf227babd/brokers/userspace/da31788b-fad1-4b26-9772-722e90d7f057",
    "publicAccess": false,
    "category": "business",
    "title": "test1",
    "orgUUID": "31e3c350-e937-4c59-ae15-25ddf227babd",
    "source": "test1.txt"
  },
  {
    "idInObjectStore": "442cd341-4886-4939-be81-25c187320b82",
    "objectStoreId": "hdfs://nameservice1/org/31e3c350-e937-4c59-ae15-25ddf227babd/brokers/userspace/da31788b-fad1-4b26-9772-722e90d7f057",
    "publicAccess": false,
    "category": "business",
    "title": "test2",
    "orgUUID": "31e3c350-e937-4c59-ae15-25ddf227babd",
    "source": "test2.txt"
  },
  {
    "idInObjectStore": "92869b23-db9d-4015-b91e-5d1d70aec9fa",
    "objectStoreId": "hdfs://nameservice1/org/31e3c350-e937-4c59-ae15-25ddf227babd/brokers/userspace/da31788b-fad1-4b26-9772-722e90d7f057",
    "publicAccess": false,
    "category": "business",
    "title": "test",
    "orgUUID": "31e3c350-e937-4c59-ae15-25ddf227babd",
    "source": "test.txt"
  }
]

```
Files are stored on HDFS and title of the dataset is the same as filename without extension.
In repository bash script was placed which helps sending requests for upload multiple files.
To use it locally:
 1. Log in to Cloud Foundry
 2. Give script executable permissions
 3. Run the script and specify organization and catalog with files

```
./multiple_upload_curl --org=<organization name> --dir=<path>
```
If you provide environment domain as argument, you can upload files using uploader service on environment

```
./multiple_upload_curl --org=<organization name> --dir=<path> --domain=<domain>
```
Default files are uploaded on localhost.

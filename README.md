# Employee Import Project

This project is used to import employees from CSV/text files to Veeva Vault.

## Features

WonderHealth Inc. will provide the employee data files based on different departments of WonderDrugs, WonderPharma, and WonderBio. 
The import process will obtain the employee files, validate the employee data, and import the employees into the Vault system.

## Design Architecture

### Basic Design Architecture Diagram 

![image](https://github.com/user-attachments/assets/04ce4e8d-0b79-4d28-9a79-4d2c944736e7)

### How it works

The solution is built on AWS. The core of the project is a scheduled job that reads the data from the target S3 bucket and validates and imports the data into Vault with REST API.


The following AWS services are used in this solution:

- **Amazon S3**: WonderHealth uses S3 to upload employee data to be processed by our solution. 
  - **Note** that different departments will have different folders in our case. Currently, only CSV file is supported.
    
- **Amazon EventBridge**: This serverless event bus will schedule and trigger file processing events daily, triggering our lambda function.
- **AWS Lambda**: We use AWS Lambda to validate the presence of employee files in the S3 bucket. If new files are detected, our Lambda function will trigger a batch processing job to process the employee data import.
  - **Note** that, per the design architecture, there is another git repository for the event handler used by Lambda. Please refer to the link below for the related code.
    - https://github.com/Annielz1223/ScheduledEventHandler
    The event handler will be triggered by the EventBridge.
- **AWS Batch**: The batch computing service will retrieve employee files, data validation, and import data into the target Vault. The job runs in a Docker container that is supported by AWS Fargate.
- **AWS Fargate**: Virtual machine to run the docker container.
- **Amazon ECR**: This is for storing the docker image containers. 
- **Cloudwatch**: used to track the running status of our project.
- **Others**: Except for these main resources used, we also need the following AWS resources:
  - VPC(Virtual Private Cloud for the whole project)
  - IAM Role(Different role-based control for different users and resources)
  - Security Group(virtual firewalls to control inbound and outbound traffic for our VPC)
  - Secret Manager or Parameter Store(AWS Secrets, Vault user information and other credentials should be well stored and protected)

## Basic Workflow

### Workflow Diagram

![image](https://github.com/user-attachments/assets/da47d102-4acf-4761-9226-ad377131455a)

### Sequence Diagram 

![image](https://github.com/user-attachments/assets/8e1f7735-712a-4517-aa83-a371455fdaf5)

## Dependency

- Java 17
- SpringBoot
- AWS: Refer to **Brief Resources Introduction** section for more details.
- Gradle




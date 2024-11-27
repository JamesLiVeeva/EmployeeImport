# Employee Import Project

This project is used to import employees from client's system to Vault system.

## Features

WonderHealth Inc. will provide the employee data files based on different departments of WonderDrugs, WonderPharma and WonderBio. 
The import process will get the employee files, validate the employee data and import the employees to Vault system.

## Design Architecture

### Basic Design Architecture Diagram 

![image](https://github.com/user-attachments/assets/04ce4e8d-0b79-4d28-9a79-4d2c944736e7)

### Brief Resources Introduction 

We choose to leverage AWS for our cloud-based solution because of its reliability, security and cost-effectiveness. The whole project is designed based on an event-driven architecture. When the event occurs on the scheduled time, it will trigger the followed processing steps automatically. The core services used are listed below:

- **Amazon S3**: We will use Amazon S3 for cloud object storage. WonderHealth Inc. will upload employee files to S3 and we will get the files for processing later.
  - **Note** that different departments will have different folders in our case.
    
- **Amazon EventBridge**: This serverless event bus will be used to schedule and trigger file processing events on a daily basis. It will trigger our lambda function.
- **AWS Lambda**: We will utilize AWS Lambda to validate the presence of employee files in the S3 bucket. If new files are detected, our Lambda function will trigger a batch processing job to process the employee data import.
  - **Note** that there is another git repository for the event handler used by Lambda per the design architecture. Refer to below link for the related code please.
    - https://github.com/Annielz1223/ScheduledEventHandler
    - The event handler will be triggered on a fixed time on a daily basis. And it will check if there are new employee files uploaded to S3 or not. The batch job will be triggered when new file(s) are detected.
- **AWS Batch**: The batch computing service will be used to handle the retrieval of employee files, perform the necessary data validation, and import processed data into Vault system.
- **AWS Fargate**: We could run our batch jobs using AWS Fargate, a serverless compute engine for containers.
- **Amazon ECR**: We will use Amazon Elastic Container Registry (ECR) to store and manage our Docker images, enabling seamless pushing and pulling of container images for our batch job.
- **Cloudwatch**: This monitoring and logging service could be used to track the runing status of our project.
- **Others**: Except for these main resources used, we also need to consider for
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




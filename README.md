# Employee Import Project

This project is used to import employees from CSV/text files to Veeva Vault.

## Features

WonderHealth Inc. will provide the employee data files for their departments of WonderDrugs, WonderPharma, and WonderBio. 
The import process will obtain the employee files, validate the employee data, and import the employees into the Vault system.

## Architecture Diagram 

![image](https://github.com/user-attachments/assets/04ce4e8d-0b79-4d28-9a79-4d2c944736e7)

## How it works

The solution is built on AWS. The core of the project is a scheduled job that reads the data from the target S3 bucket and validates and imports the data into Vault with REST API.


The following AWS services are used in this solution:

- **Amazon S3**: WonderHealth uses S3 to upload employee data to be processed by our solution. The home folder has 3 folders, each used by a different department.
- **Amazon EventBridge**: Schedule and trigger the lambda service to process client files daily.
- **AWS Lambda**: The AWS lambda service detects whether new employee files need to be imported. If new files are detected, our Lambda function triggers a batch processing job to process the employee data import. The lambda function is in its [repository](https://github.com/JamesLiVeeva/ScheduledEventHandler).
- **AWS Batch**: The batch service is used to run the main program with a Docker container hosted by Fargate.
- **AWS Fargate**: Virtual machine to run the docker container.
- **Amazon ECR**: This is for storing the docker image containers. 
- **Cloudwatch**: used to track the running status of our project.
- **Others**: Except for these main resources used, we also need the following AWS resources:
  - VPC(Virtual Private Cloud for the whole project)
  - IAM Role(Different role-based control for different users and resources)
  - Security Group(virtual firewalls to control inbound and outbound traffic for our VPC)
  - Secret Manager or Parameter Store(AWS Secrets, Vault user information and other credentials should be well stored and protected)

## Workflow Diagram

![image](https://github.com/user-attachments/assets/da47d102-4acf-4761-9226-ad377131455a)

## Sequence Diagram 

![image](https://github.com/user-attachments/assets/8e1f7735-712a-4517-aa83-a371455fdaf5)

## Dependency

- Java 17
- SpringBoot
- Gradle




# Terraform Prompt: GitHub Actions OIDC + AWS ECS Infrastructure

Use this prompt with your Terraform repository to set up the AWS infrastructure required for GitHub Actions to deploy the Restaurant Service to ECS.

---

## Copy-Paste Prompt for AI

```
I need you to create Terraform configuration to set up AWS infrastructure for GitHub Actions CI/CD deployment to ECS using OIDC authentication.

## Context

I have a Restaurant Service with a GitHub Actions workflow that needs to:
1. Authenticate to AWS using GitHub OIDC (no long-lived credentials)
2. Push Docker images to Amazon ECR
3. Deploy to Amazon ECS on Fargate

Repository: `kodegravity/restaurant-service`
Branch: `staging` and `main`

## Required Infrastructure

Create Terraform modules/files for:

### 1. GitHub OIDC Identity Provider

```hcl
# Create AWS IAM OIDC Identity Provider for GitHub Actions
# - Provider URL: https://token.actions.githubusercontent.com
# - Audience: sts.amazonaws.com
# - Thumbprint: Use GitHub's current thumbprint
```

### 2. IAM Role for GitHub Actions

```hcl
# Create IAM role that GitHub Actions will assume
# Role name: github-actions-restaurant-service-role
# Trust policy: Allow GitHub Actions from kodegravity/restaurant-service repo
# Permissions needed:
# - ECR: Push/pull images (ecr:GetAuthorizationToken, ecr:BatchCheckLayerAvailability, ecr:PutImage, ecr:InitiateLayerUpload, ecr:UploadLayerPart, ecr:CompleteLayerUpload)
# - ECS: Update service, register task definitions
# - IAM: Pass role to ECS tasks
# - Logs: Create and write to CloudWatch logs
```

Trust policy should restrict to:
- Organization: `kodegravity`
- Repository: `restaurant-service`
- Branches: `main`, `staging`

### 3. ECR Repository

```hcl
# Create ECR repository
# Name: restaurant-service
# Image tag mutability: MUTABLE (for :latest tag)
# Scan on push: true
# Lifecycle policy: Keep last 10 images, delete older ones
```

### 4. ECS Cluster

```hcl
# Create or reference existing ECS cluster
# Name: food-delivery-dev-cluster
# Capacity providers: FARGATE, FARGATE_SPOT
# Container insights: enabled
```

### 5. ECS Task Definition

```hcl
# Create initial ECS task definition
# Family: restaurant-service
# Requires compatibilities: FARGATE
# Network mode: awsvpc
# CPU: 512 (0.5 vCPU)
# Memory: 1024 (1 GB)
# Execution role: Create role with permissions to pull from ECR and write logs
# Task role: Create role for application permissions (access to Secrets Manager, RDS, etc)

# Container definition:
# - Name: restaurant-service
# - Image: <account>.dkr.ecr.ca-central-1.amazonaws.com/restaurant-service:latest
# - Port: 8080
# - Health check: /actuator/health
# - Environment variables from: AWS Secrets Manager
# - Logging: CloudWatch logs
```

### 6. ECS Service

```hcl
# Create ECS service
# Name: restaurant-service
# Cluster: food-delivery-dev-cluster
# Task definition: restaurant-service (latest)
# Desired count: 1 (for dev), 2+ for prod
# Launch type: FARGATE
# Network configuration:
#   - Subnets: private subnets
#   - Security groups: Allow inbound 8080 from ALB
#   - Assign public IP: false (use NAT gateway)
# Load balancer: Target group for ALB (if you have one)
# Health check grace period: 60 seconds
# Deployment configuration:
#   - Type: ROLLING
#   - Min healthy percent: 100
#   - Max percent: 200
```

### 7. Security Groups

```hcl
# ECS task security group
# - Inbound: 8080 from ALB security group
# - Outbound: 443 (HTTPS for AWS APIs), 5432 (PostgreSQL RDS), 9092 (Kafka)

# ALB security group (if creating new ALB)
# - Inbound: 80, 443 from 0.0.0.0/0
# - Outbound: 8080 to ECS security group
```

### 8. CloudWatch Log Group

```hcl
# Create log group for ECS tasks
# Name: /ecs/restaurant-service
# Retention: 7 days (dev), 30 days (prod)
```

### 9. Secrets Manager Secrets

```hcl
# Create placeholder secrets (to be populated manually or by another process)
# Name: restaurant-service/dev/database
# Keys needed:
#   - DB_HOST
#   - DB_PORT
#   - DB_NAME
#   - DB_USERNAME
#   - DB_PASSWORD
#   - KAFKA_BOOTSTRAP_SERVERS
#   - JWT_ISSUER_URI
#   - REDIS_HOST
#   - REDIS_PORT
```

### 10. GitHub Repository Variables & Secrets

Create a Terraform output that provides values to set in GitHub:

```hcl
output "github_actions_config" {
  description = "Configuration values for GitHub Actions"
  value = {
    AWS_REGION       = var.aws_region
    AWS_ACCOUNT_ID   = data.aws_caller_identity.current.account_id
    AWS_ROLE_TO_ASSUME = aws_iam_role.github_actions_role.arn
    ECR_REPOSITORY   = aws_ecr_repository.restaurant_service.name
    ECS_CLUSTER      = aws_ecs_cluster.food_delivery.name
    ECS_SERVICE      = aws_ecs_service.restaurant_service.name
  }
}
```

## Requirements

- Use Terraform 1.5+
- AWS Provider 5.0+
- Region: `ca-central-1` (Canada Central)
- Use variables for environment (dev/staging/prod)
- Use consistent naming: `{service-name}-{environment}`
- Add appropriate tags to all resources:
  - Service: restaurant-service
  - Environment: dev/staging/prod
  - ManagedBy: terraform
  - Team: platform-engineering

## File Structure

Organize Terraform code as:

```
terraform/
├── main.tf                 # Main resource definitions
├── variables.tf            # Input variables
├── outputs.tf              # Output values
├── providers.tf            # Provider configuration
├── iam.tf                  # IAM roles and policies
├── ecr.tf                  # ECR repository
├── ecs.tf                  # ECS cluster, service, task definition
├── security_groups.tf      # Security groups
├── cloudwatch.tf           # Log groups
├── secrets.tf              # Secrets Manager
└── data.tf                 # Data sources
```

## Constraints

1. **GitHub OIDC Trust Policy** must restrict to specific repo and branches
2. **Least privilege** - IAM roles should have minimum permissions needed
3. **Secrets** - Never hardcode credentials, use Secrets Manager
4. **Cost optimization** - Use FARGATE_SPOT for dev, reserved for prod
5. **High availability** - Prod should span multiple AZs

## After Terraform Apply

Show me commands to:
1. Set GitHub repository variables using `gh` CLI
2. Set GitHub repository secrets using `gh` CLI
3. Verify the OIDC connection works

Example:
```bash
# Set variables
gh variable set AWS_REGION --body "ca-central-1" --repo kodegravity/restaurant-service
gh variable set AWS_ACCOUNT_ID --body "123456789012" --repo kodegravity/restaurant-service

# Set secret
gh secret set AWS_ROLE_TO_ASSUME --body "arn:aws:iam::123456789012:role/github-actions-restaurant-service-role" --repo kodegravity/restaurant-service
```

## Deliverables

1. Complete Terraform configuration files
2. README with setup instructions
3. GitHub Actions variable/secret setup commands
4. Verification steps to test the OIDC connection

Please create this infrastructure following AWS and Terraform best practices.
```

---

## Expected Terraform Outputs

After running `terraform apply`, you should get outputs like:

```
Outputs:

github_actions_config = {
  AWS_ACCOUNT_ID = "123456789012"
  AWS_REGION = "ca-central-1"
  AWS_ROLE_TO_ASSUME = "arn:aws:iam::123456789012:role/github-actions-restaurant-service-role"
  ECR_REPOSITORY = "restaurant-service"
  ECS_CLUSTER = "food-delivery-dev-cluster"
  ECS_SERVICE = "restaurant-service"
}

# GitHub CLI commands to configure repository:
github_setup_commands = <<EOT
gh variable set AWS_REGION --body "ca-central-1" --repo kodegravity/restaurant-service
gh variable set AWS_ACCOUNT_ID --body "123456789012" --repo kodegravity/restaurant-service
gh secret set AWS_ROLE_TO_ASSUME --body "arn:aws:iam::123456789012:role/github-actions-restaurant-service-role" --repo kodegravity/restaurant-service
EOT
```

---

## Quick Reference

**What gets created:**
- ✅ GitHub OIDC Identity Provider
- ✅ IAM Role with trust policy for GitHub Actions
- ✅ ECR Repository for Docker images
- ✅ ECS Cluster (if not exists)
- ✅ ECS Task Definition
- ✅ ECS Service on Fargate
- ✅ Security Groups
- ✅ CloudWatch Log Group
- ✅ Secrets Manager placeholders
- ✅ IAM execution and task roles

**What you need to do after:**
1. Run `terraform apply`
2. Copy output values
3. Set GitHub variables and secrets using `gh` CLI
4. Populate Secrets Manager with actual database/Kafka credentials
5. Push to staging/main branch to trigger deployment

**Cost estimate (ca-central-1):**
- ECS Fargate (1 task, 0.5 vCPU, 1GB): ~$15-20/month
- ECR storage: ~$1/month (10 images)
- CloudWatch Logs: ~$1-2/month
- NAT Gateway: ~$32/month (if needed)
- **Total: ~$50-55/month for dev environment**

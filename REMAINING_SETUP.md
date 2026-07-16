# Remaining Configuration Needed

The Restaurant Service now uses the existing database. Here's what still needs to be configured:

## ✅ Already Configured

- **Database:** Uses existing RDS `food-delivery-dev-postgres` in `us-east-2`
- **Credentials:** Reads from Secrets Manager `food-delivery/dev/db/credentials`
- **Region:** Updated to `us-east-2`
- **ECS Task Definition:** Updated to use Secrets Manager

---

## 🔧 Still Needs Configuration

### 1. AWS SSM Parameters (Other Services)

These parameters need to be created in AWS Systems Manager Parameter Store (region: `us-east-2`):

```bash
# Kafka Bootstrap Servers
aws ssm put-parameter \
  --name /restaurant-service/kafka-bootstrap-servers \
  --value "your-kafka-broker:9092" \
  --type SecureString \
  --region us-east-2

# Redis Host
aws ssm put-parameter \
  --name /restaurant-service/redis-host \
  --value "your-redis-endpoint" \
  --type String \
  --region us-east-2

# Redis Port
aws ssm put-parameter \
  --name /restaurant-service/redis-port \
  --value "6379" \
  --type String \
  --region us-east-2

# JWT Issuer URI
aws ssm put-parameter \
  --name /restaurant-service/jwt-issuer-uri \
  --value "https://your-auth-server/realms/food-delivery" \
  --type String \
  --region us-east-2

# CORS Allowed Origins
aws ssm put-parameter \
  --name /restaurant-service/cors-allowed-origins \
  --value "https://app.yourcompany.com" \
  --type String \
  --region us-east-2
```

---

### 2. GitHub Repository Variables

Set these in your GitHub repository settings:

```bash
# You only need AWS_ACCOUNT_ID now (region is hardcoded to us-east-2)
gh variable set AWS_ACCOUNT_ID --body "YOUR_ACCOUNT_ID" --repo kodegravity/restaurant-service
```

---

### 3. GitHub Repository Secret

Set the IAM role ARN:

```bash
gh secret set AWS_ROLE_TO_ASSUME \
  --body "arn:aws:iam::YOUR_ACCOUNT_ID:role/github-actions-restaurant-service-role" \
  --repo kodegravity/restaurant-service
```

---

### 4. IAM Role for GitHub Actions

Create an IAM role that GitHub Actions can assume via OIDC:

**Role Name:** `github-actions-restaurant-service-role`

**Trust Policy:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::YOUR_ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:kodegravity/restaurant-service:*"
        }
      }
    }
  ]
}
```

**Permissions Needed:**
- ECR: Push/pull images
- ECS: Update service, register task definitions
- IAM: Pass role to ECS tasks
- SSM: Read parameters (for Kafka, Redis, JWT configs)

---

### 5. ECS Task Execution Role Permissions

The `ecsTaskExecutionRole` needs permission to:

**a) Read from Secrets Manager:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:us-east-2:YOUR_ACCOUNT_ID:secret:food-delivery/dev/db/credentials*"
      ]
    }
  ]
}
```

**b) Read from SSM Parameter Store:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ssm:GetParameters",
        "ssm:GetParameter"
      ],
      "Resource": [
        "arn:aws:ssm:us-east-2:YOUR_ACCOUNT_ID:parameter/restaurant-service/*"
      ]
    }
  ]
}
```

**c) Pull from ECR:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage"
      ],
      "Resource": "*"
    }
  ]
}
```

**d) Write to CloudWatch Logs:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": [
        "arn:aws:logs:us-east-2:YOUR_ACCOUNT_ID:log-group:/ecs/restaurant-service:*"
      ]
    }
  ]
}
```

---

### 6. ECR Repository

Create the ECR repository if it doesn't exist:

```bash
aws ecr create-repository \
  --repository-name restaurant-service \
  --region us-east-2 \
  --image-scanning-configuration scanOnPush=true
```

---

### 7. ECS Cluster and Service

Create these if they don't exist:

**ECS Cluster:**
```bash
aws ecs create-cluster \
  --cluster-name food-delivery-dev-cluster \
  --region us-east-2
```

**Note:** The ECS service will be created by the GitHub Actions workflow on first deployment.

---

### 8. CloudWatch Log Group

Create the log group:

```bash
aws logs create-log-group \
  --log-group-name /ecs/restaurant-service \
  --region us-east-2

aws logs put-retention-policy \
  --log-group-name /ecs/restaurant-service \
  --retention-in-days 7 \
  --region us-east-2
```

---

### 9. Security Group for ECS Tasks

Create a security group that:
- Allows inbound on port 8080 from ALB (if using load balancer)
- Allows outbound to:
  - RDS on port 5432
  - Kafka on port 9092
  - Redis on port 6379
  - Internet (443) for AWS APIs

---

### 10. VPC and Subnets

Ensure:
- ECS tasks run in subnets that can reach RDS
- NAT Gateway or public IPs for internet access
- VPC endpoints for AWS services (optional, cost optimization)

---

## Quick Setup Checklist

Use this to track what's done:

- [ ] Create SSM parameters (Kafka, Redis, JWT, CORS)
- [ ] Set GitHub variable AWS_ACCOUNT_ID
- [ ] Set GitHub secret AWS_ROLE_TO_ASSUME
- [ ] Create/verify IAM role for GitHub Actions (OIDC)
- [ ] Update ECS task execution role with Secrets Manager + SSM permissions
- [ ] Create ECR repository
- [ ] Create/verify ECS cluster
- [ ] Create CloudWatch log group
- [ ] Create/verify security groups
- [ ] Test database connection from ECS subnet
- [ ] Run first deployment from GitHub Actions
- [ ] Verify service is running and healthy
- [ ] Check logs for Flyway migration success

---

## Testing After Setup

Once everything is configured:

1. **Trigger deployment:**
   ```bash
   git push origin staging
   ```

2. **Watch workflow:**
   ```bash
   gh run watch
   ```

3. **Check ECS service:**
   ```bash
   aws ecs describe-services \
     --cluster food-delivery-dev-cluster \
     --services restaurant-service \
     --region us-east-2
   ```

4. **Check logs:**
   ```bash
   aws logs tail /ecs/restaurant-service --follow --region us-east-2
   ```

5. **Test health endpoint:**
   ```bash
   curl http://your-alb-endpoint/actuator/health
   ```

---

## Need Help?

See these files for more details:
- `DATABASE_CONFIG.md` - Database connection details
- `SSM_PARAMETERS_NEEDED.md` - All SSM parameters
- `DEPLOYMENT_SETUP.md` - Complete deployment guide
- `NEXT_STEPS.md` - Quick getting started

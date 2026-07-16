# Complete Deployment Setup Guide

This guide shows you how to go from a working build to a fully deployed service on AWS ECS.

## Current Status ✅

- ✅ Restaurant Service code complete (all 6 phases)
- ✅ GitHub Actions workflow fixed
- ✅ Build and tests pass on staging branch
- ❌ AWS deployment fails (missing infrastructure)

## What's Missing

The GitHub Actions workflow needs these values to deploy:
- `AWS_REGION` - GitHub repository variable
- `AWS_ACCOUNT_ID` - GitHub repository variable  
- `AWS_ROLE_TO_ASSUME` - GitHub repository secret (ARN of IAM role)

These values come from AWS infrastructure that needs to be created.

---

## Step-by-Step Setup

### Step 1: Set Up AWS Infrastructure with Terraform

**Go to your Terraform repository** and use the prompt in `TERRAFORM_SETUP_PROMPT.md`

This will create:
- GitHub OIDC Identity Provider in AWS
- IAM role for GitHub Actions
- ECR repository for Docker images
- ECS cluster, task definition, and service
- Security groups, log groups, secrets

**Time:** ~15-20 minutes

### Step 2: Get Terraform Outputs

After `terraform apply`, you'll get outputs like:

```bash
AWS_REGION = "ca-central-1"
AWS_ACCOUNT_ID = "123456789012"
AWS_ROLE_TO_ASSUME = "arn:aws:iam::123456789012:role/github-actions-restaurant-service-role"
```

### Step 3: Configure GitHub Repository

Use the `gh` CLI to set variables and secrets:

```bash
# Set variables (non-sensitive)
gh variable set AWS_REGION --body "ca-central-1" --repo kodegravity/restaurant-service
gh variable set AWS_ACCOUNT_ID --body "123456789012" --repo kodegravity/restaurant-service

# Set secret (sensitive)
gh secret set AWS_ROLE_TO_ASSUME \
  --body "arn:aws:iam::123456789012:role/github-actions-restaurant-service-role" \
  --repo kodegravity/restaurant-service
```

### Step 4: Populate Secrets Manager

Add actual credentials to AWS Secrets Manager:

```bash
aws secretsmanager put-secret-value \
  --secret-id restaurant-service/dev/database \
  --secret-string '{
    "DB_HOST": "your-rds-endpoint",
    "DB_PORT": "5432",
    "DB_NAME": "restaurant_service",
    "DB_USERNAME": "restaurant_user",
    "DB_PASSWORD": "your-secure-password",
    "KAFKA_BOOTSTRAP_SERVERS": "your-kafka-endpoint:9092",
    "JWT_ISSUER_URI": "http://your-auth-server/realms/food-delivery",
    "REDIS_HOST": "your-redis-endpoint",
    "REDIS_PORT": "6379"
  }'
```

### Step 5: Trigger Deployment

Push to staging or main branch, or manually trigger the workflow:

```bash
# Option 1: Push code
git push origin staging

# Option 2: Manual trigger via GitHub UI
# Go to Actions → restaurant-service – Build & Deploy → Run workflow

# Option 3: Manual trigger via CLI
gh workflow run restaurant-service-deploy.yml --ref staging
```

### Step 6: Verify Deployment

```bash
# Check workflow status
gh run list --workflow=restaurant-service-deploy.yml --limit 1

# Check ECS service
aws ecs describe-services \
  --cluster food-delivery-dev-cluster \
  --services restaurant-service \
  --region ca-central-1

# Test the service (after deployment completes)
curl http://your-alb-endpoint/actuator/health
```

---

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Terraform Repository                                     │
│    └─ Use TERRAFORM_SETUP_PROMPT.md                         │
│    └─ Create AWS infrastructure                             │
│    └─ Get output values                                     │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. GitHub Repository Configuration                          │
│    └─ Set AWS_REGION variable                               │
│    └─ Set AWS_ACCOUNT_ID variable                           │
│    └─ Set AWS_ROLE_TO_ASSUME secret                         │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. AWS Secrets Manager                                      │
│    └─ Populate database credentials                         │
│    └─ Add Kafka, Redis, JWT configs                         │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. Push Code or Trigger Workflow                            │
│    └─ GitHub Actions runs                                   │
│    └─ Authenticates via OIDC                                │
│    └─ Builds and pushes Docker image                        │
│    └─ Deploys to ECS                                        │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. Service Running on ECS ✅                                │
│    └─ Health check: http://alb/actuator/health              │
│    └─ Swagger UI: http://alb/swagger-ui.html                │
│    └─ API endpoints: http://alb/api/v1/...                  │
└─────────────────────────────────────────────────────────────┘
```

---

## Files in This Repository

| File | Purpose |
|------|---------|
| `TERRAFORM_SETUP_PROMPT.md` | Copy-paste prompt for Terraform repo to create AWS infrastructure |
| `WORKFLOW_FIX_PROMPT.md` | Detailed guide to fix GitHub Actions path issues (for other repos) |
| `QUICK_WORKFLOW_FIX.md` | Quick copy-paste prompt to fix workflow issues (for other repos) |
| `DEPLOYMENT_SETUP.md` | This file - complete setup guide |

---

## Troubleshooting

### Workflow still fails at "Configure AWS credentials"

**Symptom:** Error about missing `aws-region` or invalid OIDC token

**Solution:**
1. Verify GitHub variables are set: `gh variable list --repo kodegravity/restaurant-service`
2. Verify secret is set: `gh secret list --repo kodegravity/restaurant-service`
3. Check IAM role trust policy allows your repo and branch
4. Verify OIDC provider thumbprint is correct

### ECS deployment fails

**Symptom:** "Service did not stabilize" or "Task stopped"

**Solution:**
1. Check ECS task logs: `aws logs tail /ecs/restaurant-service --follow`
2. Verify secrets are populated in Secrets Manager
3. Check security groups allow required ports
4. Verify subnets have NAT gateway or public IPs
5. Check task definition has correct CPU/memory

### Service starts but health check fails

**Symptom:** ECS keeps restarting tasks

**Solution:**
1. Check application logs for startup errors
2. Verify database connectivity (security groups, credentials)
3. Check JWT issuer URI is reachable
4. Increase health check grace period in ECS service

### Docker image push fails

**Symptom:** "denied: requested access to the resource is denied"

**Solution:**
1. Verify ECR repository exists
2. Check IAM role has ECR push permissions
3. Verify repository name matches workflow

---

## Cost Estimate

**Dev environment (ca-central-1):**
- ECS Fargate (1 task, 0.5 vCPU, 1GB): ~$15-20/month
- ECR storage (~10 images): ~$1/month
- CloudWatch Logs (7 day retention): ~$1-2/month
- NAT Gateway: ~$32/month (if needed)
- **Total: ~$50-55/month**

**Production optimizations:**
- Use FARGATE_SPOT for 70% cost reduction
- Use CloudWatch Logs Insights for better log analysis
- Enable Container Insights for monitoring
- Set up Auto Scaling based on CPU/memory

---

## Next Steps

1. **Now:** Go to Terraform repo with `TERRAFORM_SETUP_PROMPT.md`
2. **Then:** Set GitHub variables and secrets
3. **Then:** Populate AWS Secrets Manager
4. **Finally:** Deploy! 🚀

---

## Support

If you encounter issues:
1. Check the troubleshooting section above
2. Review CloudWatch logs: `/ecs/restaurant-service`
3. Check ECS service events in AWS console
4. Verify all prerequisites are met

For other services, you can reuse:
- `WORKFLOW_FIX_PROMPT.md` - Fix GitHub Actions paths
- `TERRAFORM_SETUP_PROMPT.md` - Adapt for other services (change service name)

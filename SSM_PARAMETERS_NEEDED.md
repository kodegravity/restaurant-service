# SSM Parameters Required for ECS Deployment

The ECS task definition expects these SSM Parameter Store parameters to exist:

## Database Connection
```
/restaurant-service/db-host              (String)       - RDS endpoint
/restaurant-service/db-username          (SecureString) - Database username
/restaurant-service/db-password          (SecureString) - Database password
```

## Kafka
```
/restaurant-service/kafka-bootstrap-servers  (SecureString) - Kafka brokers (comma-separated)
```

## Redis
```
/restaurant-service/redis-host           (String)       - Redis/ElastiCache endpoint
/restaurant-service/redis-port           (String)       - Redis port (usually 6379)
```

## Authentication
```
/restaurant-service/jwt-issuer-uri       (String)       - JWT issuer URI (Cognito/Auth0/Keycloak)
```

## CORS
```
/restaurant-service/cors-allowed-origins (String)       - Comma-separated allowed origins
```

---

## Terraform Will Create These

The `TERRAFORM_SETUP_PROMPT.md` now includes creation of all these parameters.

## Manual Creation (if needed)

```bash
# Database
aws ssm put-parameter --name /restaurant-service/db-host \
  --value "restaurant-db.xxxxxx.ca-central-1.rds.amazonaws.com" \
  --type String --region ca-central-1

aws ssm put-parameter --name /restaurant-service/db-username \
  --value "restaurant_user" \
  --type SecureString --region ca-central-1

aws ssm put-parameter --name /restaurant-service/db-password \
  --value "your-secure-password" \
  --type SecureString --region ca-central-1

# Kafka
aws ssm put-parameter --name /restaurant-service/kafka-bootstrap-servers \
  --value "b-1.msk-cluster.xxx.kafka.ca-central-1.amazonaws.com:9092" \
  --type SecureString --region ca-central-1

# Redis
aws ssm put-parameter --name /restaurant-service/redis-host \
  --value "restaurant-cache.xxxxx.cac1.cache.amazonaws.com" \
  --type String --region ca-central-1

aws ssm put-parameter --name /restaurant-service/redis-port \
  --value "6379" \
  --type String --region ca-central-1

# JWT
aws ssm put-parameter --name /restaurant-service/jwt-issuer-uri \
  --value "https://cognito-idp.ca-central-1.amazonaws.com/ca-central-1_xxxxx" \
  --type String --region ca-central-1

# CORS
aws ssm put-parameter --name /restaurant-service/cors-allowed-origins \
  --value "https://app.yourcompany.com,https://admin.yourcompany.com" \
  --type String --region ca-central-1
```

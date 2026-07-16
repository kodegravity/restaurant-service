# Database Configuration - Existing AWS RDS

The Restaurant Service uses an existing AWS RDS PostgreSQL database.

## Database Details

**Region:** `us-east-2` (Ohio)

**RDS Endpoint:**
```
food-delivery-dev-postgres.c1ys20sk00ud.us-east-2.rds.amazonaws.com
```

**Connection Details:**
- **Host:** food-delivery-dev-postgres.c1ys20sk00ud.us-east-2.rds.amazonaws.com
- **Port:** 5432
- **Database Name:** fooddelivery
- **Username:** fooddelivery
- **Password:** Stored in AWS Secrets Manager

**JDBC URL:**
```
jdbc:postgresql://food-delivery-dev-postgres.c1ys20sk00ud.us-east-2.rds.amazonaws.com:5432/fooddelivery
```

---

## AWS Secrets Manager

All database credentials are stored in AWS Secrets Manager:

**Secret Name:** `food-delivery/dev/db/credentials`

**Secret Structure (JSON):**
```json
{
  "dbname": "fooddelivery",
  "host": "food-delivery-dev-postgres.c1ys20sk00ud.us-east-2.rds.amazonaws.com",
  "password": "<secure-password>",
  "port": "5432",
  "url": "jdbc:postgresql://food-delivery-dev-postgres.c1ys20sk00ud.us-east-2.rds.amazonaws.com:5432/fooddelivery",
  "username": "fooddelivery"
}
```

---

## ECS Task Configuration

The ECS task definition reads database credentials from Secrets Manager:

```json
"secrets": [
  { 
    "name": "DB_HOST", 
    "valueFrom": "arn:aws:secretsmanager:us-east-2:YOUR_ACCOUNT_ID:secret:food-delivery/dev/db/credentials:host::"
  },
  { 
    "name": "DB_USERNAME", 
    "valueFrom": "arn:aws:secretsmanager:us-east-2:YOUR_ACCOUNT_ID:secret:food-delivery/dev/db/credentials:username::"
  },
  { 
    "name": "DB_PASSWORD", 
    "valueFrom": "arn:aws:secretsmanager:us-east-2:YOUR_ACCOUNT_ID:secret:food-delivery/dev/db/credentials:password::"
  },
  { 
    "name": "DB_PORT", 
    "valueFrom": "arn:aws:secretsmanager:us-east-2:YOUR_ACCOUNT_ID:secret:food-delivery/dev/db/credentials:port::"
  },
  { 
    "name": "DB_NAME", 
    "valueFrom": "arn:aws:secretsmanager:us-east-2:YOUR_ACCOUNT_ID:secret:food-delivery/dev/db/credentials:dbname::"
  }
]
```

---

## IAM Permissions Required

The ECS task execution role needs permission to read from Secrets Manager:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue",
        "secretsmanager:DescribeSecret"
      ],
      "Resource": [
        "arn:aws:secretsmanager:us-east-2:YOUR_ACCOUNT_ID:secret:food-delivery/dev/db/credentials*"
      ]
    }
  ]
}
```

---

## Database Schema Initialization

On first startup, Flyway will automatically:

1. ✅ Connect to the `fooddelivery` database
2. ✅ Create the `restaurant_service` schema
3. ✅ Run migration `V1__create_restaurant_schema.sql`
   - Creates all tables (Restaurant, MenuItem, MenuCategory, BusinessHour, etc.)
   - Creates indexes and foreign keys
4. ✅ Run migration `V2__insert_default_cuisines.sql`
   - Inserts 15 default cuisines

**Important:** Make sure the `fooddelivery` user has permissions to:
- Create schemas
- Create tables
- Create indexes
- Insert data

---

## Verifying Database Connection

### From Local Machine

If you have access to the RDS instance (security groups allow your IP):

```bash
# Using psql
psql -h food-delivery-dev-postgres.c1ys20sk00ud.us-east-2.rds.amazonaws.com \
     -p 5432 \
     -U fooddelivery \
     -d fooddelivery

# Check if restaurant_service schema exists
\dn

# Check tables in restaurant_service schema
\dt restaurant_service.*
```

### From ECS Task

Check CloudWatch Logs:

```bash
aws logs tail /ecs/restaurant-service --follow --region us-east-2
```

Look for Flyway migration logs:
```
Flyway Community Edition by Redgate
Database: jdbc:postgresql://food-delivery-dev-postgres.c1ys20sk00ud.us-east-2.rds.amazonaws.com:5432/fooddelivery
Successfully validated 2 migrations
Creating Schema History table "restaurant_service"."flyway_schema_history"
Current version of schema "restaurant_service": << Empty Schema >>
Migrating schema "restaurant_service" to version "1 - create restaurant schema"
Migrating schema "restaurant_service" to version "2 - insert default cuisines"
Successfully applied 2 migrations
```

---

## Security Groups

Ensure the ECS tasks can reach the RDS instance:

**RDS Security Group Inbound Rules:**
- Type: PostgreSQL
- Protocol: TCP
- Port: 5432
- Source: ECS task security group

---

## Connection Pooling

HikariCP is configured in `application.yml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
```

For production, increase pool size if needed:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # for prod
```

---

## Troubleshooting

### Connection Timeout

**Symptom:** `Connection timed out`

**Solutions:**
1. Check security groups allow ECS → RDS on port 5432
2. Verify ECS tasks are in the correct VPC/subnets
3. Check network ACLs
4. Ensure RDS is not in a private subnet without NAT

### Authentication Failed

**Symptom:** `password authentication failed for user "fooddelivery"`

**Solutions:**
1. Verify credentials in Secrets Manager are correct
2. Check IAM role has permission to read Secrets Manager
3. Ensure secret ARN is correct in task definition
4. Check username/password haven't expired

### Schema Not Found

**Symptom:** `schema "restaurant_service" does not exist`

**Solutions:**
1. Flyway migrations failed - check logs
2. User doesn't have CREATE SCHEMA permission
3. Run migrations manually if needed

### Cannot Connect to Database

**Symptom:** `org.postgresql.util.PSQLException: The connection attempt failed`

**Solutions:**
1. Check RDS endpoint is correct
2. Verify port 5432 is accessible
3. Check VPC DNS resolution
4. Ensure RDS instance is running

---

## Cost Optimization

Since this is a shared RDS instance:
- ✅ No additional RDS costs for restaurant service
- ✅ Only pay for ECS compute and storage
- ✅ Shared connection pooling reduces overhead

---

## Backup & Recovery

The shared RDS instance should have:
- Automated backups enabled
- Point-in-time recovery
- Multi-AZ for high availability (production)

**Note:** Coordinate with platform team for any database schema changes or maintenance windows.

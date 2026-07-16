
---

## Additional Note: Database Setup

The Terraform configuration should also include:

### RDS PostgreSQL Database (Option 1: Create New)

```hcl
# Create RDS PostgreSQL instance
resource "aws_db_instance" "restaurant_db" {
  identifier           = "restaurant-service-db"
  engine              = "postgres"
  engine_version      = "15.3"
  instance_class      = "db.t3.micro"  # for dev
  allocated_storage   = 20
  storage_type        = "gp3"
  
  db_name  = "restaurant_db"
  username = "restaurant_user"
  password = random_password.db_password.result  # Generate secure password
  
  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name   = aws_db_subnet_group.main.name
  
  backup_retention_period = 7
  skip_final_snapshot    = true  # for dev only
  
  tags = {
    Name        = "restaurant-service-db"
    Environment = var.environment
  }
}

# Store credentials in SSM Parameter Store (to match ecs-task-definition.json)
resource "aws_ssm_parameter" "db_host" {
  name  = "/restaurant-service/db-host"
  type  = "String"
  value = aws_db_instance.restaurant_db.address
}

resource "aws_ssm_parameter" "db_username" {
  name  = "/restaurant-service/db-username"
  type  = "SecureString"
  value = aws_db_instance.restaurant_db.username
}

resource "aws_ssm_parameter" "db_password" {
  name  = "/restaurant-service/db-password"
  type  = "SecureString"
  value = random_password.db_password.result
}
```

### RDS Connection (Option 2: Use Existing Database)

If you already have an RDS instance, just create the SSM parameters:

```hcl
resource "aws_ssm_parameter" "db_host" {
  name  = "/restaurant-service/db-host"
  type  = "String"
  value = "your-existing-rds.xxxxxxxxx.ca-central-1.rds.amazonaws.com"
}

resource "aws_ssm_parameter" "db_username" {
  name  = "/restaurant-service/db-username"
  type  = "SecureString"
  value = "restaurant_user"
}

resource "aws_ssm_parameter" "db_password" {
  name  = "/restaurant-service/db-password"
  type  = "SecureString"
  value = var.db_password  # Pass as variable
}
```

### Update ECS Task Role Permissions

The task role needs permission to read SSM parameters:

```hcl
resource "aws_iam_role_policy" "ecs_task_ssm" {
  name = "ecs-task-ssm-access"
  role = aws_iam_role.ecs_task_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ssm:GetParameters",
          "ssm:GetParameter"
        ]
        Resource = [
          "arn:aws:ssm:${var.region}:${data.aws_caller_identity.current.account_id}:parameter/restaurant-service/*"
        ]
      }
    ]
  })
}
```

### Database Schema Initialization

After RDS is created, Flyway will automatically run migrations on first startup:
- Creates `restaurant_service` schema
- Creates all tables
- Inserts default cuisines

**No manual database setup required!** ✅

# IAM role which dictates what other AWS services the Lambda function
# may access.
data "aws_iam_policy_document" "assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "lvapi_lambda_role" {
  name = "LaundryViewApi-lambda-role"

  assume_role_policy = data.aws_iam_policy_document.assume_role_policy.json
}

data "aws_iam_policy_document" "lvapi_lambda_policy" {
  statement {
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "cloudwatch:Describe*",
      "cloudwatch:Get*",
      "cloudwatch:List*",
    ]
    resources = ["*"]
  }

  statement {
    actions = [
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:DeleteItem",
      "dynamodb:Scan",
    ]
    resources = [
      aws_dynamodb_table.api_cache_table.arn,
      aws_dynamodb_table.api_notifications_table.arn,
    ]
  }

  statement {
    actions   = ["secretsmanager:*"]
    resources = [aws_secretsmanager_secret.firebase_secret.arn]
  }
}

resource "aws_iam_role_policy" "lvapi_lambda_policy" {
  name = "lambda_policy"
  role = aws_iam_role.lvapi_lambda_role.id

  policy = data.aws_iam_policy_document.lvapi_lambda_policy.json
}

resource "aws_lambda_function" "lvapi_func" {
  function_name = "LaundryViewApi"
  filename      = "${var.lvapi_func_filename}"

  source_code_hash = "${base64sha256(file(var.lvapi_func_filename))}"

  handler = "${var.lvapi_func_handler}"
  runtime = "java8"
  timeout = "300"
  memory_size = "512"

  role = "${aws_iam_role.lvapi_lambda_role.arn}"

  environment {
    variables = {
      cache_table = "${aws_dynamodb_table.api_cache_table.name}"
      notification_table = "${aws_dynamodb_table.api_notifications_table.name}"
      firebase_secret_name = "${aws_secretsmanager_secret.firebase_secret.name}"
    }
  }
}

resource "aws_lambda_permission" "lvapi_gateway" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.lvapi_func.arn}"
  principal     = "apigateway.amazonaws.com"

  # The /*/* portion grants access from any method on any resource
  # within the API Gateway "REST API".
  source_arn = "${aws_api_gateway_rest_api.laundry_view_api.execution_arn}/*/*"
}

resource "aws_cloudwatch_event_rule" "every_minute" {
  name = "every_minute"
  description = "Fires every minute"
  schedule_expression = "rate(1 minute)"
  is_enabled = true
}

resource "aws_cloudwatch_event_target" "lvperiodic_every_minute" {
  rule = "${aws_cloudwatch_event_rule.every_minute.name}"
  arn = "${aws_lambda_function.lvapi_func.arn}"
}

resource "aws_lambda_permission" "lvperiod_cloudwatch" {
  statement_id = "AllowExecutionFromCloudWatch"
  action = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.lvapi_func.function_name}"
  principal = "events.amazonaws.com"
  source_arn = "${aws_cloudwatch_event_rule.every_minute.arn}"
}


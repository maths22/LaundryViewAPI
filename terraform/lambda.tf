resource "aws_lambda_function" "lvapi_func" {
  function_name = "LaundryViewApi"
  filename      = "${var.lvapi_func_filename}"

  source_code_hash = "${base64sha256(file(var.lvapi_func_filename))}"

  handler = "${var.lvapi_func_handler}"
  runtime = "java8"
  timeout = "15"
  memory_size = "512"

  role = "${aws_iam_role.lvapi_lambda_role.arn}"

  environment {
    variables = {
      cache_table = "${aws_dynamodb_table.api_cache_table.name}"
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
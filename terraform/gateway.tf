resource "aws_api_gateway_rest_api" "laundry_view_api" {
  name        = "laundry_view_api"
  description = "LaundryView API"
}

resource "aws_api_gateway_resource" "laundry_view_api_proxy" {
  rest_api_id = aws_api_gateway_rest_api.laundry_view_api.id
  parent_id   = aws_api_gateway_rest_api.laundry_view_api.root_resource_id
  path_part   = "lv_api"
}

resource "aws_api_gateway_method" "laundry_view_api_proxy" {
  rest_api_id   = aws_api_gateway_rest_api.laundry_view_api.id
  resource_id   = aws_api_gateway_resource.laundry_view_api_proxy.id
  http_method   = "ANY"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "lambda" {
  rest_api_id = aws_api_gateway_rest_api.laundry_view_api.id
  resource_id = aws_api_gateway_method.laundry_view_api_proxy.resource_id
  http_method = aws_api_gateway_method.laundry_view_api_proxy.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.lvapi_func.invoke_arn
}

resource "aws_api_gateway_deployment" "laundry_view_api" {
  depends_on = [aws_api_gateway_integration.lambda]

  //    "aws_api_gateway_integration.lambda_root",

  rest_api_id = aws_api_gateway_rest_api.laundry_view_api.id
  stage_name  = "prod"
}

resource "aws_api_gateway_domain_name" "laundry_view_api" {
  domain_name = "lvapi.maths22.com"

  regional_certificate_arn = "arn:aws:acm:us-west-2:902151335766:certificate/7cf8aede-66a0-4389-9892-e57915ff78fa"

  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

resource "aws_api_gateway_base_path_mapping" "test" {
  api_id      = aws_api_gateway_rest_api.laundry_view_api.id
  stage_name  = aws_api_gateway_deployment.laundry_view_api.stage_name
  domain_name = aws_api_gateway_domain_name.laundry_view_api.domain_name
}

output "base_url" {
  value = aws_api_gateway_deployment.laundry_view_api.invoke_url
}

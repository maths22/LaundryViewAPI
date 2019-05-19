resource "aws_dynamodb_table" "api_cache_table" {
  name           = "LaundryViewAPI-Cache"
  read_capacity  = 10
  write_capacity = 10
  hash_key       = "Key"

  attribute {
    name = "Key"
    type = "S"
  }

  ttl {
    attribute_name = "TimeToExist"
    enabled        = true
  }
}

resource "aws_dynamodb_table" "api_notifications_table" {
  name           = "LaundryViewAPI-Notifications"
  read_capacity  = 10
  write_capacity = 10
  hash_key       = "RequesterId"
  range_key      = "MachineId"

  attribute {
    name = "RequesterId"
    type = "S"
  }

  attribute {
    name = "MachineId"
    type = "S"
  }

  ttl {
    attribute_name = "TimeToExist"
    enabled        = true
  }
}

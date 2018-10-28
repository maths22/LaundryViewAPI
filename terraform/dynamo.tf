resource "aws_dynamodb_table" "api_cache_table" {
  name           = "LaundryViewAPI-Cache"
  read_capacity  = 20
  write_capacity = 20
  hash_key       = "Key"

  attribute {
    name = "Key"
    type = "S"
  }

  ttl {
    attribute_name = "TimeToExist"
    enabled = true
  }
}
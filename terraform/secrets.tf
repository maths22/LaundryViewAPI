resource "aws_secretsmanager_secret" "firebase_secret" {
  name = "laundryview_firebase_key"
}

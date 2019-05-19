variable "lvapi_func_filename" {
  default = "../java/target/LaundryViewAPI-1.0-SNAPSHOT.jar"
}

variable "lvapi_func_handler" {
  default = "com.maths22.laundryviewapi.LaundryViewEndpoint"
}

variable "lvperiodic_func_handler" {
  default = "com.maths22.laundryviewapi.NotificationManager"
}

provider "aws" {
  region  = "us-west-2"
  version = "~> 2.7.0"
}

terraform {
  backend "s3" {
    bucket = "maths22-remote-tfstate"
    region = "us-west-2"
    key    = "laundryview-api.tfstate"
  }

  required_version = "> 0.12.0-rc1"
}


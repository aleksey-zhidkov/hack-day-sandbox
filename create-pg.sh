#!/bin/zsh
sudo docker rm hds-pg
sudo docker run -p 5432:5432 -d -name hds-pg hds_postgresql

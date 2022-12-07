#!/bin/sh

### PostgreSQL Admin ###
podman pod create --name postgre-sql -p 9876:80 -p 5432:5432

podman run --pod postgre-sql \
   -e 'PGADMIN_DEFAULT_EMAIL=admin@mohashi.io' \
   -e 'PGADMIN_DEFAULT_PASSWORD=admin123'  \
   --name pgadmin \
   -d docker.io/dpage/pgadmin4:latest

### PostgreSQL 14 ###
podman run --name db --pod=postgre-sql -d \
   -e POSTGRES_USER=db \
   -e POSTGRES_PASSWORD=db123 \
   -v ~/.local/storage/data:/var/lib/postgresql/data:Z \
   docker.io/library/postgres:14

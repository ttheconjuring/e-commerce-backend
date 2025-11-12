#!/bin/bash
set -e

# This script is used by psql, which will connect using
# the POSTGRES_USER and POSTGRES_DB variables.
# We can just use the 'postgres' user and default 'postgres' db.
# The 'psql' command is available in the container.

# Function to create a database
create_database() {
  local db_name=$1
  echo "  Creating database '$db_name'"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE "$db_name";
EOSQL
}

# Check if the POSTGRES_MULTIPLE_DBS variable is set
if [ -n "$POSTGRES_MULTIPLE_DBS" ]; then
  echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DBS"
  # Loop through the comma-separated list
  for db in $(echo $POSTGRES_MULTIPLE_DBS | tr ',' ' '); do
    create_database $db
  done
  echo "Databases created"
fi
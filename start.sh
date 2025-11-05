if [ "$(docker network ls | grep homework1)" == "" ]; then
  docker network create homework1
fi

if [ ! "$(docker ps -a -q -f name=postgresdb)" ]; then
    docker run --name postgresdb \
      --network homework1 \
      -e POSTGRES_USER=admin \
      -e POSTGRES_PASSWORD=secret \
      -e POSTGRES_DB=bookings_db \
      -p 5432:5432 \
      -d postgres:latest
else
    docker start postgresdb 2>/dev/null || true
fi
docker network connect homework1 postgresdb 2>/dev/null || true

echo "Waiting for PostgreSQL to start..."
until docker exec postgresdb pg_isready -U admin > /dev/null 2>&1; do
  sleep 1
done
echo "PostgreSQL is ready!"

if [ ! "$(docker ps -a -q -f name=sonarqube)" ]; then
    docker run -d --name sonarqube \
        --network homework1 \
        -p 9000:9000 \
        -v sonarqube_data:/opt/sonarqube/data \
        -v sonarqube_logs:/opt/sonarqube/logs \
        -v sonarqube_extensions:/opt/sonarqube/extensions \
        sonarqube:latest
else
    docker start sonarqube 2>/dev/null || true
fi
docker network connect homework1 sonarqube 2>/dev/null || true

sleep 2

./mvnw spring-boot:run

docker stop postgresdb 2>/dev/null || true
docker stop sonarqube 2>/dev/null || true
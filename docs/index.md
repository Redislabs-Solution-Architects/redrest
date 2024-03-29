## Download

[redrest-0.0.1-SNAPSHOT.jar](redrest-0.0.1-SNAPSHOT.jar)

## Running the API server

```sh
java -jar redrest-0.0.1-SNAPSHOT.jar
```

## Configuration

| Property      | Description           | Default  |
| ------------- |-----------------------| -----|
| server.port | Web server port | 8080 |
| spring.redis.host | Redis Enterprise database endpoint | localhost |
| spring.redis.port | Redis Enterprise database port | 6379 |

e.g. `java -jar redrest-0.0.1-SNAPSHOT.jar --server.port=8081 --spring.redis.host=redis-12000.example.com --spring.redis.port=12000`

## API

See [API Documentation](api/)

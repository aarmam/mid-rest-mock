# Mobile ID (MID) REST API mock service

[Mobile ID (MID) REST API Documentation](https://github.com/SK-EID/MID)

## Implemented endpoints

| Process                                                                                | Method |  URL   |
|----------------------------------------------------------------------------------------|--------|-----|
| [Authenticate](https://github.com/SK-EID/MID#32-initiating-signing-and-authentication) | POST   |  BASE/authentication   |
| [Authentication status](https://github.com/SK-EID/MID#33-status-of-signing-and-authentication)                                                                  | GET    |  BASE/authentication/session/:sessionId?timeoutMs=:timeoutMs   |
| [Signing](https://github.com/SK-EID/MID#32-initiating-signing-and-authentication)      | POST   |  BASE/signature   |
| [Signing status](https://github.com/SK-EID/MID#33-status-of-signing-and-authentication)                                                                         | GET    |  BASE/signature/session/:sessionId?timeoutMs=:timeoutMs   |

## Build docker image

```shell
./mvnw spring-boot:build-image
```

## Run Docker image

```shell
docker run -p 8080:8080 mid-rest-mock:latest
```
## Chaos monkey

[Chaos monkey](https://codecentric.github.io/chaos-monkey-spring-boot/) latency assaults are enabled on Authentication/Signing status endpoints to simulate random result status times. Use chaos monkey [HTTP endpoints](https://codecentric.github.io/chaos-monkey-spring-boot/latest/#_http_endpoint) to change this behaviour at runtime. 

## Metrics

Application and [chaos monkey metrics](https://codecentric.github.io/chaos-monkey-spring-boot/latest/#metrics) are published under BASE/actuator/metrics and BASE/actuator/prometheus
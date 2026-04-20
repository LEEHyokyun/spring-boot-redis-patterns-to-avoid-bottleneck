## 1.  개요

- Java ver 21.
- Spring Boot ver 3.5.6
- Redis ver 8.2.1
- MySQL ver 8.0.42

## 2. Projects

- redis 사상 및 목적에 맞는, 그러면서도 병목 현상을 줄이기 위한 다양한 pattern 적용 방안
- spring boot project


## 3. Architecturing

- Cache Strategies

![img_4.png](img_4.png)

- domain models

![img_2.png](img_2.png)

## 3. Redis cache for what?

> Redis Cache
> - DataSource의 데이터 접근 및 부하의 속도와 병목을 보완하기 위한 캐싱 전략 도구

![img.png](img.png)

Redis를 도입한 순간 부터, 기본적인 부하의 진입점은 Cache이다.
- 모든 데이터를 캐싱할 필요는 없지만, 기본적인 부하 분산 및 조회 성능 향상을 확보해야 함.
- 비싼 비용을 감수하면서 도입하는 체계이다.

## 4. Redis Patterns to avoid bottlenecks

## 4-1. Cache Penetration

- DB에 존재하지 않은 데이터 요청이 지속적으로 발생하여, cache miss 및 DB call이 지속적으로 발생하는 현상

![img_5.png](img_5.png)

## Appendix. Redis Architecturing

- RedisConfig / DataSerializer / DataDeserializer

![img_1.png](img_1.png)

- Entity / Record(DTO)

![img_3.png](img_3.png)

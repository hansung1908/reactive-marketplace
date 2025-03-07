# Reactive Marketplace - 비동기 중고거래 플랫폼

http://hansung1908.site:8081/

# 📌 목차

[1. 프로젝트 소개](#프로젝트-소개)

[2. 기술 스택](#기술-스택)

[3. ERD](#ERD)

[4. 주요 기능](#주요-기능)

[5. 성능 최적화 및 트러블 슈팅](#성능-최적화-및-트러블-슈팅)

[6. 프로젝트 구조](#프로젝트-구조)

[7. 개발 상세 정보](#개발 상세 정보)

# 🚀 프로젝트 소개
Reactive Marketplace는 Spring WebFlux 및 Reactive MongoDB를 기반으로 한 비동기 중고거래 플랫폼입니다.

리액티브 프로그래밍 패러다임을 통해 동시성을 효율적으로 처리하고, MongoDB와 Redis의 리액티브 버전을 활용하여 높은 처리 속도와 효율적인 리소스 관리를 실현했습니다.

# 🛠️ 기술 스택

### Backend
- Java 17
- Spring Boot 3.x
- Spring WebFlux (리액티브 프로그래밍)
- Spring Data MongoDB Reactive
- Spring Data Redis Reactive
- Spring Security (JWT 기반 인증)

### Database
- MongoDB (Reactive)
- Redis (Reactive)

### Frontend
- Thymeleaf
- JavaScript
- SSE(Server-Sent Events)

### Tools & Libraries
- Lombok
- Thumbnailator (이미지 리사이징)
- Jackson (JSON 직렬화/역직렬화)
- Blockhound (블로킹 코드 탐지)
- Mockito, StepVerifier (테스트 코드 작성)

### Infrastructure & Deployment
- AWS EC2 (Ubuntu 환경 배포)

# ✨ 주요 기능

### 사용자 관리 및 인증
- JWT 기반 인증 및 보안 설정

### 상품 관리 및 거래 기능
- 상품 CRUD 기능 구현 및 이미지 업로드 지원
- 실시간 채팅 기능 (MongoDB capped collection + SSE 활용)

### 실시간 알림 서비스
- Redis Pub/Sub과 SSE를 이용한 실시간 알림 서비스 구현

### 캐싱 기능
- Redis Reactive Template을 활용한 캐싱 기능 구현

### 이미지 처리 및 리사이징
- Thumbnailator를 통한 이미지 크기 및 품질 조정

# 🚧 성능 최적화 및 트러블 슈팅
MongoDB LocalDateTime 이슈 해결 시도 및 결론
MongoDB는 LocalDateTime 저장 시 UTC로 고정되는 이슈가 있습니다. 이를 해결하기 위해 DateTimeProvider를 통해 한국 시간대로 설정했으나, 출력 시 추가 조정 문제가 발생하여 기본 UTC 저장 방식을 유지하는 것으로 결정했습니다.

MongoDB Aggregation 최적화
파이프라인 초기에 $match 사용, 인덱스 활용, 메모리 사용량 제한 고려 등 최적화를 진행했습니다.

Redis 직렬화 문제 해결
GenericJackson2JsonRedisSerializer를 사용하여 다양한 객체 직렬화를 지원하도록 설정했습니다.

파일 업로드(Content-Type) 문제 해결
WebFluxConfigurer에서 Decoder 설정을 통해 multipart/form-data와 application/octet-stream 문제를 해결했습니다.

#📂 프로젝트 구조

```text
src
├── main
│   ├── java
│   │   └── com
│   │       └── reactivemarketplace
│   │           ├── config          # 환경설정 클래스들
│   │           ├── controller      # 웹 요청 컨트롤러 클래스들
│   │           ├── domain          # 도메인 모델 클래스들
│   │           ├── dto             # 데이터 전송 객체(DTO) 클래스들
│   │           ├── exception       # 전역 예외처리 핸들러 클래스들
│   │           ├── jwt
│   │           ├── redis     
│   │           ├── model           
│   │           ├── repository      # 데이터 접근 레이어 클래스들
│   │           ├── security
│   │           ├── service         # 비즈니스 로직 클래스들
│   │           └── util            # 유틸리티 클래스들
│   └── resources
│       ├── static
│       └── templates
└── test
```

src/main/java/com/reactivemarketplace/


# 🔗 개발 상세 정보

<details>
  <summary>auto reload 활성화 하는법</summary>

1. springboot devtools dependency 추가
2. file > settings > build, execution, deployment > compiler > build project autiomaically 체크
3. file > settings > advanced settings > allow auto-make to start even if developed application is currently running 체크 (IntelliJ 2021.2 이후 버전부터)
4. application.yml에 devtools, resources, thymeleaf 설정 추가
5. 브라우저에 livereload 확장 프로그램 설치

</details>

<details>
  <summary>webflux 간단 소개</summary>

- 적은 수의 스레드로 동시성을 처리 (비동기 지원)
- Mono(0 ~ 1개의 값 반환) 타입 + Flux(1개 이상의 값 반환) 타입
- Netty, Undertow, Tomcat 등의 비동기 웹 서버와 통합
- WebClient를 통해 비동기 HTTP 요청
</details>

<details>
    <summary>리액티브 오퍼레이션</summary>

- flux와 mono로 파이프라인을 만들기 위한 메소드
---
- just() : 리액티브 타입 생성, static 메소드
- range(n, m) : n부터 m까지 숫자의 리액티브 타입 생성 (카운터), static 메소드
- subscribe() : 리액티브 타입 호출
- defer() : supplier를 구독하여 반환되는 값을 전달, 지연 실행 (구독하지 않으면 인스턴스화 진행 x), 다른 mono 반환
- fromCallable() : Callable supplier를 구독하여 반환되는 값을 전달, 지연 전달, 단일 값 반환
---
- interval(), delayElements() : Duration.ofSeconds()를 통해 초 단위로 값 방출
- delaySubscription() : Duration.ofSeconds()를 통해 구독 지연 설정
---
- A.mergeWith(B) : 두 flux A와 B를 결합, 별도에 설정이 없으면 순서 보장 x
- A.zip(B) : 두 flux A와 B를 결합, 각 소스로부터 한 항목씩 묶어서 새로운 flux 생성
- first() : 두 flux중 느린 flux는 제외하고 빠른 flux만 발행
---
- from~() : 각 컬렉션을 리액티브 타입으로 변환 (fromArray(), fromStream() ..)
- skip() : 주어진 숫자에 맞게 처음 항목을 건너뛰고 발행
- take() : 주어진 숫자에 맞게 처음 항목부터 발행
- filter() : 조건식을 통해 원하는 값만 발행
- distinct() : 중복 제거하여 발행
---
- map() : 지정된 함수를 통해 매핑, 동기적 실행,  
- flatmap() : 지정된 함수를 통해 매핑, 비동기적 실행, 병렬 처리
- concatmap() : 지정된 함수를 통해 매핑, 비동기적 실행, 순차적 처리
- buffer() : 주어진 숫자에 맞게 소스를 List 컬렉션으로 묶은 flux 발행, flatMap()을 통해 병행 처리 가능
- collectList() : flux를 list로 묶어 mono<list> 발행
- collectMap() : flux를 매핑하여 mono<map> 발행
---
- all() : 조건식을 통해 모든 값이 만족하는지 체크, expectNext(true)로 검증
- any() : 조건식을 통해 하나의 값이라도 만족하는지 체크, expectNext(true)로 검증
- stepVerifier : assertion을 적용하는 리액티브 타입 테스트 도구
  - create() : 테스트 데이터 등록
  - expectNext() : 각 항목과 데이터 비교
  - verifyComplete() : 데이터가 완전한지 검사, 마무리 메소드
---
- doOn~() : 로깅 + api 콜과 같은 부수적인 작업에 사용
  - 스트림을 전달받으나 반환 x (비동기적 처리까지 겹쳐 db작업 x)
  - 각 트리거에 맞게 발동 (doOnNext() : 발행, doOnSuccess() : 완료, doOnError() : 에러 ..)
- then() : doOnSuccess()와 발동 조건이 같음, 이전 스트림 전달 x, 기존 스트림만 변경 가능
</details>

<details>
  <summary>spring-data-mongodb-reactive</summary>

- MongoDB는 BSON(Binary JSON)을 사용해 데이터를 저장하는 NoSQL 데이터베이스
- JPA 스프링에서 db를 다루면서 NoSQL 특성을 이용한 비동기적 상호작용을 지원
- @CreatedDate와 같은 자동 추가 기능을 사용하려면 @EnableMongoAuditing를 설정
- reactive-mongodb 환경이라면 @EnableReactiveMongoAuditing를 설정
- @Id는 선언없이도 자동 생성되지만 커스텀 가능성과 명시화의 이유로 설정하는 것이 좋음
- collection 생성시 tailable cursor를 사용하려면 capped 설정을 true하고, size를 지정해야함
```shell
db.createCollection("chat", { capped: true, size: 1048576 });
```
- 이후 바로 연결하면 데이터가 없어 dead 상태가 되므로 더미 데이터를 추가하여 연결 유지
```shell
db.chat.insertOne({ roomId: "dummyId" })
```
</details>

<details>
  <summary>mongodb localdatetime 문제</summary>

- mongodb는 localdatetime 저장시 지역 시간대를 지원하지 않아 무조건 utc로 저장

### 해결 시도
- 처음 @CreatedDate를 설정하면 utc 시간대로 설정되어 9시간의 차이가 발생
- 시간대를 변경하기 위해선 DateTimeProvider를 구현하여 utc+9 시간대(한국 시간대)로 설정
- 해당 provider를 @EnableReactiveMongoAuditing에 dateTimeProviderRef로 설정
- 결과 -> 저장시 제대로 9시간 추가되어 저장되지만 db에서 객체로 출력시 해당 시간대로 조정되서 9시간이 또 추가되어 출력

### 결론
- 기존 설정대로 utc 시간대로 저장
- 이를 잘 인지하여 향후 개발 과정에서 해당 문제에 맞춰 개발하는 것이 최선이라 판단
</details>

<details>
  <summary>mongodb-aggregation</summary>

### 기본 명령어

- $match 
  - 도큐먼트 필터링 (SQL의 WHERE절과 유사)
  - 특정 조건에 맞는 도큐먼트 선택
```shell
{ $match: { status: "active" } }
{ $match: { age: { $gt: 25 } } }
```

- $group
  - 데이터 그룹화 및 집계
  - _id 필드로 그룹화 기준 지정
```shell
{
  $group: {
    _id: "$department",
    totalSalary: { $sum: "$salary" },
    avgAge: { $avg: "$age" },
    count: { $sum: 1 }
  }
}
```

- $sort
  - 결과 정렬
  - 1: 오름차순, -1: 내림차순
```shell
{ $sort: { age: -1, name: 1 } }
```

- $project
  - 출력할 필드 선택 (SQL의 SELECT와 유사)
  - 1: 포함, 0: 제외
```shell
{
  $project: {
    name: 1,
    age: 1,
    _id: 0,
    fullName: { $concat: ["$firstName", " ", "$lastName"] }
  }
}
```

- $limit / $skip
  - 결과 수 제한 및 건너뛰기
```shell
{ $limit: 5 }  # 상위 5개 결과만
{ $skip: 10 }  # 처음 10개 건너뛰기
```

### 집계 연산자

- 수학 연산자
```shell
$sum: # 합계 계산
$avg: # 평균값 계산
$min: # 최솟값 찾기
$max: # 최댓값 찾기
$count: # 개수 세기
```

- 배열 연산자
  - $unwind: 배열을 개별 도큐먼트로 분리
```shell
{ $unwind: "$tags" }
```

- 조인 연산자
  - $lookup: 다른 컬렉션과 조인
```shell
{
  $lookup: {
    from: "orders",          // 조인할 컬렉션
    localField: "user_id",   // 현재 컬렉션의 필드
    foreignField: "user_id", // 대상 컬렉션의 필드
    as: "user_orders"        // 결과를 저장할 필드명
  }
}
```

### 사용 시 주의사항
- $match는 가능한 파이프라인 초기에 사용하여 처리할 데이터 양을 줄이기
- 인덱스는 파이프라인의 첫 번째 $match 단계에서만 사용 가능
- 메모리 사용량 제한 (기본 100MB)을 고려하여 설계
- 복잡한 집계는 성능에 영향을 줄 수 있으므로 최적화 필요
</details>

<details>
  <summary>mongodb-transaction</summary>

- mongodb에서 트랜잭션을 하려면 기존 db를 레플리카 셋으로 변경해야 가능
- 레플리카 셋은 기본 노드(db) 하나와 부하 노드 둘 이상을 결합한 db cluter
- 로컬에서 설정
```shell
# 설정 전 mongod.cfg 파일에 해당 설정 추가
replication:
replSetName: rs0

# 설정 적용을 위한 mongodb 재시작
net stop MongoDB
net start MongoDB

# 안될 경우 (cmd에서 직접 설정)
echo replication: >> "C:\Program Files\MongoDB\Server\6.0\bin\mongod.cfg"
echo   replSetName: rs0 >> "C:\Program Files\MongoDB\Server\6.0\bin\mongod.cfg"

# cmd에서 설정 파일 확인
type "C:\Program Files\MongoDB\Server\6.0\bin\mongod.cfg"
```
```shell
# 데이터 디렉토리 생성
mkdir -p "C:\Program Files\MongoDB\Server\6.0\27017"
mkdir -p "C:\Program Files\MongoDB\Server\6.0\27018"
mkdir -p "C:\Program Files\MongoDB\Server\6.0\27019"

# MongoDB 인스턴스 시작 (각각 다른 cmd에서 관리자 권한 실행)
# 각 cmd는 계속 켜져있어야 함
start mongod --replSet rs0 --port 27017 --dbpath "C:\Program Files\MongoDB\Server\6.0\27017"
start mongod --replSet rs0 --port 27018 --dbpath "C:\Program Files\MongoDB\Server\6.0\27018"
start mongod --replSet rs0 --port 27019 --dbpath "C:\Program Files\MongoDB\Server\6.0\27019"

# 레플리카 셋 초기화
# --eval 옵션은 자바스크립트 표현식을 직접 실행 가능하게 해주는 명령줄 옵션
mongosh --eval 'rs.initiate({
  _id: "rs0",
  members: [
    {_id: 0, host: "localhost:27017"},
    {_id: 1, host: "localhost:27018"},
    {_id: 2, host: "localhost:27019"}
  ]
})'

# db 및 collection 생성
mongosh --eval 'use market;
db.createCollection("chat", {
  capped: true,
  size: 104857,
  max: 10000
});
db.createCollection("user");
db.createCollection("product");
db.createCollection("chatroom");
db.createCollection("image");
print("Collections created in market database:");
db.getCollectionNames();'

# 레플리카 셋 확인
mongosh --eval 'rs.status()'
```
</details>

<details>
  <summary>spring-data-redis-reactive</summary>

- webflux 같은 논블로킹 방식으로 동작하는 reactive 버전 redis
- 실시간 알람 서비스나 캐시 기능 구현시 빠른 처리 속도와 효율적인 리소스 관리를 보장
- redis를 사용하면 localdatetime 호환성 오류가 발생

### 실시간 알람 서비스
- redis sub / pub 기능을 활용하여 구현
- redisPublisher의 convertAndSend 기능을 구현해 알람 토픽을 생성
- redisSubscriber의 listenTo과 sse(server-sent-events)를 통해 적은 리소스로 알람을 발행
- controller에 produces = MediaType.TEXT_EVENT_STREAM_VALUE를 설정하여 실시간성을 확보

### 캐시 기능
- application.yml에 redis host, port 설정
- redis configuration 파일을 따로 만들어 user 객체에 대한 직렬화 template 설정
- service에 redisTemplate 주입하고 .opsForValue()를 시작으로 .get(), .set(), .delete() 등 오퍼레이션 실행

### 주의할 점
- 적용시 아래의 오류들이 발생

##### java.time.LocalDateTime not Supported
- redis 기능을 구현하여 다른 서비스에 주입할 경우 localdatetime 직렬화/역직렬화 호환성 오류가 발생
- 각 서비스의 도메인 객체에 @JsonSerialize, @JsonDeserialize 설정을 추가

##### LinkedHasmap cannot be cast to class DTO Object
- 환경에 따라 어떤 Serializer를 사용해야 될지 고려해야 함
- Jackson2JsonRedisSerializer
  - Class Type을 지정해야 하며, redis에 객체를 저장할 때 class 값 대신 Classy Type 값을 JSON 형태로 저장
  - pacakge 등의 정보 일치를 고려할 필요 x
  - 하지만, class type을 지정해야 하기 때문에 특정 클래스에 종속적이며, redisTemplate을 여러 쓰레드에서 접근하게 될 때 serializer 타입의 문제가 발생
- GenericJackson2JsonRedisSerializer
  - 객체의 클래스 지정 없이 모든 Class Type을 JSON 형태로 저장할 수 있는 Serializer
  - Class Type에 상관 없이 모든 객체를 직렬화해준다는 장점
  - 하지만, 단점으로는 Object의 class 및 package까지 전부 함께 저장하게 되어 다른 프로젝트에서 redis에 저장되어 있는 값을 사용하려면 package까지 일치
  - 따라서 MSA 구조의 프로젝트 같은 경우 문제 발생 가능성 있음
- 여러 객체를 캐싱해야 했기 때문에, 여러 객체를 직렬화/역직렬화 사용할 수 있는 GenericJackson2JsonRedisSerializer를 사용
</details>

<details>
  <summary>json 직렬화 + 역직렬화</summary>

- 객체를 JSON 문자열로 변환하는 과정
- 일반적으로 Jackson이나 Gson과 같은 라이브러리를 사용
- Java 클래스의 필드가 JSON으로 변환되려면, 해당 필드에 대한 getter가 필요
- 반대로, 역직렬화 시에는 setter가 필요
</details>

<details>
  <summary>Rendering</summary>

- 리액티브 환경에서 뷰를 렌더링하는 방식
- 기존 mvc방식으로 처리하면 동기적으로 값을 처리하여 리액티브의 장점을 잃음
- Rendering 객체는 비동기적으로 데이터 처리를 관리 가능
- 또한 리액티브 프로그래밍 패턴을 따르므로, webflux의 이점을 최대한 활용 가능
- Mono나 Flux 타입을 해당 객체에서 ThymeleafReactiveViewResolver로 자동으로 처리하여 뷰에 렌더링
</details>

<details>
    <summary>리액티브 환경에서 파일 처리</summary>

- MultipartFile 대신 비동기를 지원하는 FilePart 사용
- content-type은 multipart/form-data로 mvc 방식과 똑같이 받음
---
- 파일과 json 데이터를 같이 보낼때 생기는 octet stream 타입 문제
- WebMvcConfigurer 대신 WebFluxConfigurer을 사용하여 설정
- mvc 방식에선 octet stream을 jackson 라이브러리를 통해 json 형태로 바꿔주는 converter를 등록
- flux에선 decoder를 통해 json 변환을 구현하고 codec 설정을 통해 등록
</details>

<details>
    <summary>이미지 리사이징</summary>

- 이미지의 크기나 화질을 조정하여 용량을 낮추는 방식
- thumbnailator 라이브러리를 이용하면 리사이징부터 저장까지 간단하게 구현 가능
- size()로 크기 조정 (사진 비율에 따라 비율이 달라질 수 있음), outputQuality()로 화질 조정
- java.io,File로 이미지 데이터를 불러오거나 저장
</details>

<details>
  <summary>외부 경로 정적 리소스 허용</summary>

- 기본적으로 외부 경로를 통해 정적 리소스를 불러오는 것은 보안적으로 막혀있음
- 이를 허용하기 위해서는 해당 경로를 어떤 요청을 보냈을때 허용할지 설정이 필요
- WebFluxConfigurer에서 addResourceHandlers에서 설정 가능
</details>

<details>
  <summary>유틸리티 클래스</summary>

- 코드 재사용성을 높이기 위해 특정 기능을 제공하는 도구들을 모은 클래스
- stateless 상태(객체 상태 변경 x)를 유지하며 thread-safe(다중 스레드에서 작업 보장)하게 설계
- Math 클래스 처럼 모든 메소드를 static으로 제공
- immutable(불변성) 유지를 위해 list나 map을 불변 객체로 처리 + 생성자는 private로 처리 혹은 설정 x
- service나 component와 분리하여 독립적인 역할을 하므로 어노테이션 설정 x
</details>

<details>
  <summary>response dto 설계 패턴: repository vs service</summary>

- response dto를 작성할때 repository와 service 중에서 어떤 곳에서 사용하는게 나은지 비교

### repository에서 사용하는 경우
- 장점:
  - 성능 최적화
  - 불필요한 데이터 제외
- 단점:
  - 비지니스 로직 분리의 어려움 (도메인 객체의 정보를 감춰 처리시 어려움)
  - 테스트와 유지보수의 어려움

### service에서 dto로 변환하는 경우
- 장점:
  - 단일 책임 원칙 (각 계층간의 책임을 명확히 분리)
  - 비즈니스 로직 분리
  - 테스트 용이성
  - 유연성 (클라이언트 요구사항에 맞게 데이터 형식 변환 쉬움)
- 단점:
  - 약간의 오버헤드 (추가적인 코드와 성능 오버헤드를 발생시킬 수 있음)

### 추천 방법
- Repository는 엔티티를 반환하고, 서비스 계층에서 dto로 변환하는 방식이 대부분의 경우 가장 바람직
- 실무시 기존의 코드를 수정하거나 각자 맞는 파트가 다름으로 비즈니스 로직이 분리가 중요
- 오버헤드의 경우 복잡한 비즈니스 로직을 구현하는데 비해 큰 비용 발생 x
</details>

<details>
  <summary>record 타입</summary>

- 불변 객체를 간결하게 정의하도록 도와주는 타입
- 불변 객체이므로 데이터 변경 x + 불필요한 객체 복사 x
- toString(), equals(), hashcode(), getter 메소드를 자동 생성
- 상속이 불가능하며, 메소드 오버라이드를 통해 자동 생성된 메서드를 커스터마이즈
- 객체 직렬화를 지원
- dto 같은 단순 데이터 전송 객체에 유용
</details>

<details>
  <summary>jwt 도입</summary>

- json web token
- 구조 :
  - 헤더: 토큰 타입, 암호화 알고리즘 명시
  - 페이로드: JWT에 넣을 데이터, JWT 발급 / 만료일 등 명시
  - 시그니처: 헤더, 페이로드가 변조 되었는지를 확인하는 역할
- 장점 :
  - 서버의 확장성이 높으며 대량의 트래픽이 발생해도 대처할 수 있음
  - (서버가 분리되어 있는 경우) 특정 DB/서버에 의존하지 않아도 인증할 수 있음
  - -> userId를 받던 코드를 authentication 객체로 받아 처리하도록 수정
- 단점 : 
  - state-ful(세션) 방식보다 비교적 많은 양의 데이터가 반복적으로 전송되므로 네트워크 성능저하가 될 수 있음
  - 데이터 노출로 인한 보안적인 문제 존재
  - -> 후술할 보안 옵션을 통해 토큰 보안 구축

### 개발 사항
- 토큰 관련 사항
  - 편의성과 보안을 위해 토큰을 쿠키에 등록
  - 쿠키에 토큰을 등록하면 요청마다 자동으로 포함되어 별도의 등록 코드가 필요없음
  - 다만, 자동 등록으로 csrf 공격에 취약
  - 그래서 http-onlu 옵션을 추가해 js 접근을 막고
  - secure 옵션을 추가해 https 프로토콜에서만 전송하도록 설계
  - 추가로 SameSite 옵션을 추가하여 xss 공격 제한
- webflux에서 구현시 알아둬야할 사항
  - session을 stateless 상태로 만들기 위해 NoOpServerSecurityContextRepository.getInstance()를 securityWebFilterChain에 등록
  - mvc와 다르게 ReactiveAuthenticationManager와 ServerAuthenticationConverter가 필요
  - 각각 인증 절차와 토큰 변환 절차를 구현후 AuthenticationWebFilter에 설정
  - 그후 securityWebFilterChain에 등록
  - /login 엔드포인트를 컨트롤러에 설정하여 로그인 성공시 토큰 발급 절차 구현
  - converter 부분에서 토큰을 가져오는 부분을 justOrEmpty로 하여 로그인을 안한 상태에서 첫 페이지 접속이 가능하도록 설계
</details>

<details>
  <summary>리액티브 환경에서 전역 예외 처리</summary>

- 기존 동기 방식에선 @ControllerAdvice와 @ExceptionHandler를 이용하여 전역 예외 처리 구현
- 비동기 방식에선 WebExceptionHandler 인터페이스를 구현하여 전역 예외 처리 코드 구성
- 이때 기존에 작동하던 DefaultErrorWebExceptionHandler가 @Order(-1)에 우선순위를 가져 먼저 실행됨
- 그래서 보다 높은 우선순위를 부여하기 위해 @Order(-2) 설정
- 이때 우선순위가 바뀌면서 SecurityConfig.class에서 설정한 exceptionHandling이 작동하지 않음
- 그래서 aop를 사용하여 컨트롤러에서 발생하는 authentication null exception을 따로 처리
---
- enum 타입을 통해 각 서비스 api에서 발생할 수 있는 오류에 이름 지정
- 어디서 어떤 예외가 발생했는지 확인하기 편함
- 각 서비스에 switchIfEmpty 오퍼레이션이나 onErrorResume 오퍼레이션을 통해 예외 트리거를 설정
</details>

<details>
  <summary>리액티브 환경에서 재시도 처리</summary>

- 일시적인 오류나 네트워크 문제가 발생하여 재요청이 필요한 경우 retry 관련 오퍼레이션을 통해 구현

##### 선형 대기 전략
```shell
Retry.fixedDelay(long maxAttempts, Duration fixedDelay)
```
- 고정된 대기시간을 두고 재요청
- 1s -> 2s -> 3s

##### 지수 백오프 전략
```shell
Retry.backoff(long maxAttempts, Duration minBackoff)
```
- 초기 대기시간에서 지수적으로 증가하여 재요청
- 100ms -> 200ms -> 400ms
- 선형 대기 전략보다 부하를 분산하면서 불필요한 요청을 감소

##### 지터
- 지수 백오프 전략만 사용하면 여러 클라이언트가 동시에 실패하면 모두 같은 시간대에 재시도
- 그럼 동시 요청으로 인한 서버 부하로 연쇄적인 실패 발생 가능성이 생김
```shell
RetryBackoffSpec.jitter(double jitterFactor)
```
- 지수 백오프 전략에 같이 사용
- 0.0 ~ 1.0 (0% ~ 100%)로 범위를 설정하여 해당 요청에 무작위성을 부여
- 100ms에 50% 지터를 설정하면 100ms ± 50ms 사이의 시간중 무작위로 요청
- 서비스 복구 시간을 확보함과 동시에 시스템 과부하를 방지할 수 있음

##### 주의사항
- 재시도 횟수 제한 설정 필요
- 최대 대기 시간 설정 필요
</details>

<details>
  <summary>리액티브 환경에서 테스트 코드 작성</summary>

- 기본적으로 단위 테스트를 위해 mockito 사용
- 통합 테스트가 필요하다면 @SpringBootTest를 사용하여 통합 컨텍스트 적용

##### repository
- @Createdate 같은 config 파일이 필요한 경우 @Import를 통해 해당 config 파일을 추가
- db에 테스트 데이터 i/o시 비동기 특성상 순서를 보장할 수 없어 테스트가 먼저 실행될 수 있음
- 그래서 StepVerifier를 사용하여 db i/o 작업을 보장

##### service
- filter를 통한 인증 과정을 거쳐 authentication 객체를 매개변수로 받을때 별도의 given 설정 필요
- then() 연산의 경우 즉시 평가로 인해 switchIfEmpty로 null 처리 전에 실행되므로 defer()를 통해 지연 평가로 수정

##### controller
</details>

<details>
  <summary>리액티브 환경에서 적용에 주의해야 할 것</summary>

##### aop
- aop는 webflux에서 완전히 호환되지 않아 비동기 동작을 보장할 수 없음
- if문은 filter + switchifempty나 justorempty로 리액티브하게 변경
- 여러 작업에서 한 곳에서라도 오류가 나면 전체 롤백이 필요한 경우 zip()을 이용하여 하나의 스트림으로 합침

##### evaluation
- switchifempty는 자바의 즉시 평가(eager evaluation) 특성으로 empty가 아닌 상황에도 불필요한 실행이 됨
- 그래서 mono.defer()로 supplier에 넘겨 실제 호출 시점으로 실행을 지연 평가(lazy evaluation)해야 함
- error는 mono.error()를 통해 지연 평가로 에러 처리를 구현
- then() 역시 즉시 평가하므로 조치 필요

##### dataBuffer
- 단순히 filePart의 헤더에서 getContentLength()를 통해 파일의 크기값을 얻으려고 하였으나
- -1을 반환하여 값을 찾을 수 없다고 뜸
- 원인을 찾아보니 다양한 메타데이터가 같이 있어 정확한 파일 크기를 알 수 없다는 문제를 발견
---
- 해결책으로 content()로 부터 dataBuffer 스트림을 얻어 크기를 계산하기로 결정
- 이때 DataBufferUtils이라는 좋은 유틸리티 클래스가 있어 이를 이용
- DataBufferUtils.join()을 통해 버퍼를 하나로 묶어줌
- netty 서버 기반으로 버퍼가 풀링되며 참조 카운팅됨
- 이때 참조로 인한 메모리 누수를 방지하기 위해 크기 계산이 끝나면 release()로 해제해야 함
---
- webflux에서 메모리 문제 방지로 기본 in-memory buffer 크기가 256KB로 제한되므로 조정 필요
- 압축을 통한 파일 전송 과정에서 헤더값이 붙어 실제 크기보다 클 수 있음

</details>

<details>
  <summary>리액티브 환경에서 도움되는 툴</summary>

##### blockhound
- 자동으로 블로킹 코드를 찾아주는 라이브러리
- 해당 기능이 동작할시 블로킹 코드를 감지해 에러를 발생
</details>

<details>
  <summary>배포 진행</summary>

- jar 파일 배포로 thymeleaf 설정에서 경로를 classpath 경로로 변경
- http 배포 환경이라 cookie.secure(true) 설정은 임시 제외
- localhost로 설정된 부분은 배포 ip로 재설정
  - chat.js에 eventSource 경로 수정 (hansung1908.site:8081)
- 이미지 경로는 배포 환경(ubuntu)에 따라 해당 환경에 맞는 절대 경로로 설정 (/home/ubuntu/image)
  - WebfluxConfig static resouce 경로 등록
  - ImageServiceImpl.java의 File 객체 생성 부분 수정
---
- ec2 free tier는 ram가 1gib만 있어 프로젝트 빌드 과정에서 cpu 과부하로 서버가 다운될 수 있음
- swap 메모리 설정을 통해 남는 공간을 ram으로 대체 가능하도록 설정
```shell
# 1. Swap 메모리 추가하기
$ sudo dd if=/dev/zero of=/swapfile bs=128M count=16
$ sudo chmod 600 /swapfile

# EC2는 기본 램 1GB를 갖고 있는데 + 쉘에 해당 명령어를 입력해 2GB 스왑파일을 생성한다.
# 권한부여는 잊지말자!

# 2. Swap 메모리를 Swap 파일로 포맷
$ sudo mkswap /swapfile

# 해당 명령어를 입력해서 Swap 메모리를 Swap 파일로 포맷할 수 있다.

# 3. Swap 메모리 활성화
$ sudo swapon /swapfile
$ sudo swapon -s
# 해당 명령어를 입력해 Swap 메모리를 활성화 시킨다.
# 마지막 명령어를 통해 출력은 활성화된 스왑 파일의 정보와 크기 등을 보여주고, 출력은 활성화된 스왑 파일의 정보와 크기 등을 나타낸다.

# 4. Swap 메모리 시스템이 재시작되더라도 자동 활성화
$ sudo vi /etc/fstab 

# 마지막 행에 추가하기
/swapfile swap swap defaults 0 0

# vi 명령어를 이용해 설정 파일로 들어가 마지막 행에 해당 구문을 추가해주자.

#5. 현재의 메모리 사용 및 가용 메모리에 대한 정보 확인
$ sudo free -h 

# 해당 명령어를 입력하면 현재 메모리에 관한 정보를 확인 할 수 있는데 우리가 생성한 Swap 메모리가 잘 작동하고 있는지 확인해주자.

# 기타 정보
# Swap 메모리 삭제
sudo rm -r swapfile

# 단일 Swap 메모리 비활성화
$ sudo swapoff swapfile

# 모든 Swap 메모리 비활성화
$ sudo swapoff -a
```

</details>
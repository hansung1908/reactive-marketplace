# reactive-marketplace
비동기 방식을 활용한 중고거래 사이트 프로젝트

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
- flux<..>같이 여러개의 데이터를 렌더링하려면 collectList()를 사용하여 Mono<List<..>>로 변환

</details>
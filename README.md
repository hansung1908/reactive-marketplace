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
    <summary>리액티브 오퍼레이션</summary>

- flux와 mono로 파이프라인을 만들기 위한 메소드
---
- just() : 리액티브 타입 생성, static 메소드
- range(n, m) : n부터 m까지 숫자의 리액티브 타입 생성 (카운터), static 메소드
- subscribe() : 리액티브 타입 호출
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
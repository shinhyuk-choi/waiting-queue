```mermaid
sequenceDiagram
    User->>+MVC: 페이지 진입 요청
    MVC->>+Webflux: 페이지 진입 가능 여부 확인 요청
    Webflux->>+Redis: 진입 허용 여부 확인 요청
    Redis->>-Webflux: 
    Webflux->>+Redis: 대기열 등록 요청
    Redis-->>-Webflux: 
    Webflux-->>-MVC: 
    MVC-->>-User: 대기 페이지 이동
```

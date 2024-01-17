# HISTORY
#### 2024-01-17
---
- static 정적자원 경로 지정
  - WebMvcConfig에 정적자원경로 등록
  - SecurityConfig 에 정적자원소스 permit

- resource>static 경로 파일 정리
  - js / css / images 로 정리
  - 각 페이지별 css/js는 하위폴더를 만들어서 관리
- html 페이지에서 정적자원 link 설정 수정
  - ex. <link href="/css/common.css" />
  - ex. <script defer href="/js/common.js"></script>
  

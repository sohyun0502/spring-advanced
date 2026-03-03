## 1. [문제 인식 및 정의]

현재 프로젝트에서는 UserRoleCheckInterceptor에서
HttpServletRequest로부터 JWT 토큰에 포함된 사용자 권한(Role)을 추출하여
어드민 API 접근 여부를 검증하고 있다.

문제는 다음과 같다.

changeUserRole() 서비스 로직이 실행되면 DB의 권한은 변경된다.

하지만 기존 JWT 토큰은 변경되지 않는다.

따라서 동일한 토큰으로 재요청 시, 변경된 권한이 아닌 토큰에 저장된 기존 권한을 기준으로 인가 검증이 수행된다.

즉, 권한 변경 이후에도 기존 JWT를 사용하는 한, 변경 사항이 즉시 반영되지 않는 문제가 발생한다.

---

## 2. [해결 방안]

2-1. [의사결정 과정]

① 인터셉터에서 매 요청마다 DB 조회
요청마다 사용자 권한을 DB에서 조회하여 최신 상태 반영

② 권한 변경 시 JWT 재발급
changeUserRole() 실행 후 새로운 JWT 발급
클라이언트는 이후 요청에서 새 토큰 사용

2-2. [해결 과정]

서비스 로직 수정
```
@Transactional
public UserSaveResponse changeUserRole(long userId, UserRoleChangeRequest userRoleChangeRequest) {
    User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
    user.updateRole(UserRole.of(userRoleChangeRequest.getRole()));
    
    // 변경된 권한 기준으로 새 토큰 발급
    String newToken = jwtUtil.createToken(user.getId(), user.getEmail(), UserRole.of(userRoleChangeRequest.getRole()));
    return new UserSaveResponse(newToken);
}
```

컨트롤러 수정
```
@PatchMapping("/admin/users/{userId}")
public ResponseEntity<UserSaveResponse> changeUserRole(@PathVariable long userId, @RequestBody UserRoleChangeRequest userRoleChangeRequest) {
    return ResponseEntity.ok(userAdminService.changeUserRole(userId, userRoleChangeRequest));
}
```
---

## 3. [해결 완료]

3-1. [회고]
이번 문제를 통해 JWT의 가장 중요한 특성인 Stateless 구조를 깊이 이해하게 되었다.

초기에는 인터셉터에서 DB를 조회하는 방식도 고려했지만,
이는 JWT를 사용하는 목적과 맞지 않는다는 점을 깨달았다.

권한 변경은 서버 상태 변경이 아닌, 토큰 재발급 문제라는 점을 인지하게 되었고,
JWT의 설계 철학에 맞는 방향으로 구조를 개선할 수 있었다.

이를 통해 다음을 학습했다:

JWT는 발급 시점의 사용자 상태를 담는 스냅샷이다.
권한 변경은 인증 정보 재발급 문제이다.
Stateless 아키텍처는 일관성을 유지하는 것이 중요하다.

3-2. [전후 데이터 비교]

| 구분          | 변경 전  | 변경 후 |
|:------------|:------|:---|
| 권한 변경 즉시 반영 | 반영 안됨 | 즉시 반영 |
| 인터셉터 DB 조회  | 없음    | 없음 |
| JWT 구조 유지   | 의미 약화 | 유지 |




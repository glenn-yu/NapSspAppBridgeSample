# iOS xcframework 관리 가이드

이 샘플의 iOS 의존성은 현재 `ios/Vendor/AdMixer/AdMixer.xcframework`와 `ios/Vendor/AdMixerMediation/AdMixerMediation.xcframework`를 기준으로 동작합니다. 샘플 UI에는 **Use vendored / Use remote SPM** 토글이 있고, 기본 흐름은 vendored xcframeworks를 우선합니다. 원격 SPM 경로를 쓰더라도, fetch 실패 시에는 vendored 경로가 재현성 있는 fallback 역할을 합니다.

## 권장 운영 방식

- **개발/CI 재현성 우선**: `ios/Vendor/` 아래의 xcframework를 남겨 둡니다.
- **업데이트가 잦은 경우**: 파일 크기와 변경 이력을 줄이기 위해 Git LFS 또는 외부 아티팩트 호스팅을 고려합니다.
- **배포가 명확해야 할 때**: 버전별 zip을 GitHub Releases, S3, 혹은 사내 artifact 저장소로 분리합니다.

## Git LFS로 옮길 때

- 큰 바이너리(`*.xcframework`, `*.zip`)가 자주 바뀐다면 Git LFS가 적합합니다.
- 저장소에는 포인터만 남고 실제 바이너리는 LFS에 보관됩니다.
- 장점: repo 크기 관리가 쉬움.
- 단점: clone/checkout 환경에 LFS 설정이 필요합니다.

## 외부 artifact 호스팅으로 옮길 때

- `Package.swift`의 binary target을 로컬 `path:` 대신 `url:` + checksum 방식으로 바꿀 수 있습니다.
- zip 파일명과 checksum을 버전별로 고정해 두면 롤백이 쉽습니다.
- 배포 채널은 GitHub Releases, S3, Artifactory, 사내 CDN 중 하나를 권장합니다.

## 이 샘플에서 확인할 것

- `ios/Package.swift`
  - vendored binary target을 참조하는지 확인
- `ios/Vendor/`
  - xcframework 구조와 Info.plist가 유지되는지 확인
- `xcodebuild`
  - vendored 빌드가 simulator 대상에서 통과하는지 확인

## 빌드 검증 명령

```bash
cd ios
xcodebuild -scheme NapSspIOSSample -destination 'generic/platform=iOS Simulator' build
```

## 링크er / build settings 메모

- 현재 vendored xcframework를 쓰는 기본 경로에서는 별도의 추가 linker flag가 필요하지 않습니다.
- SPM / xcframework 충돌이 생기면 `Package.swift`에서 의존성 소스를 한쪽으로 고정하고, 중복 모듈 import를 피합니다.
- 필요 시에만 `OTHER_LDFLAGS`를 추가하고, 기본값은 유지하는 편이 안전합니다.

## 추천 의사결정

- **샘플/데모**: vendored xcframework 유지
- **팀 배포 자동화**: 외부 artifact 호스팅 + checksum
- **바이너리 변경 빈도 높음**: Git LFS

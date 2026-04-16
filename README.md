# NapSsp App Bridge Sample

NapSsp(Nasmedia AdMixer SSP) SDK를 WebView 하이브리드 방식으로 연동하는 Android/iOS 샘플 앱입니다.

## 구조

```
android/     - Android 샘플 앱 (Kotlin + Compose)
docs/        - 가이드 문서
examples/    - 포맷별 코드 예시
```

## 빠른 시작

### Android

```bash
cd android
# Gradle 빌드 (JDK 11 이상 필요)
./gradlew assembleDebug
```

### 문서 요약

| 파일 | 내용 |
|---|---|
| `docs/quickstart.md` | 빠른 시작 — 초보자용 단계별 안내 (5분) |
| `docs/guide.md` | 메인 가이드 — 전체 흐름과 상세 예시 |
| `docs/hybrid-webview.md` | WebView ↔ Native 브릿지 상세 |
| `docs/nap-ssp-android-sdk-native.md` | Android SDK 공식 연동 가이드 |

## 핵심 포인트 (요약)

- SDK 초기화: AdMixer.getInstance().initialize(context, MEDIA_KEY, ArrayList(...))
- 포맷별 호출: AdView / NativeAdView / VideoAdView / RewardInterstitialVideoAd / InterstitialVideoAd
- 이벤트 수신: 콜백에서 loaded/displayed/clicked 등의 이벤트를 받아 웹에 전달

## 먼저 읽을 것 (초보자 빠른 시작)

1. `docs/quickstart.md` — 바로 따라하기(5분)
2. `docs/guide.md` — 전체 흐름 및 상세
3. `docs/start-here.md`
4. `docs/install.md`
5. `docs/format-matrix.md`
6. `docs/hybrid-webview.md`

## 설정 파일

`android/app/src/main/java/.../NapSspConfig.kt` 에서 Media Key와 AdUnit ID를 교체하세요.

## 문의

- 이메일: nap_adx@nasmedia.co.kr
- 공식 가이드: `docs/nap-ssp-android-sdk-native.md`


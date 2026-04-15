# 브리지 개념

이 프로젝트에서 말하는 브리지는 **웹페이지와 네이티브 앱이 서로 말하는 통로**다.

## 방향 1: 웹 → 네이티브

웹에서 JavaScript로 메시지를 보낸다.

예:

```js
window.AppBridge.postMessage({
  type: 'ping',
  payload: 'hello from web'
})
```

## 방향 2: 네이티브 → 웹

네이티브에서 웹의 함수를 호출한다.

예:

```js
window.onNativeMessage({
  type: 'pong',
  payload: 'hello from native'
})
```

## 이 샘플의 규칙

- 웹은 메시지만 보낸다.
- 네이티브는 메시지를 받아 화면에 보여준다.
- 응답은 다시 웹으로 돌려준다.
- 복잡한 광고 로직은 나중에 넣는다.

window.AppBridge = {
  postMessage(message) {
    if (window.webkit?.messageHandlers?.AppBridge) {
      window.webkit.messageHandlers.AppBridge.postMessage(message)
      return
    }

    if (window.AndroidBridge?.postMessage) {
      window.AndroidBridge.postMessage(JSON.stringify(message))
      return
    }

    console.log('AppBridge not available', message)
  }
}

window.onNativeMessage = function (message) {
  console.log('message from native', message)
}

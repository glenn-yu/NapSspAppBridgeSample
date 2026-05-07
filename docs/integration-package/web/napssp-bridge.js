/**
 * NapSSP App-WebView 브릿지 유틸리티
 *
 * 사용법:
 *   1. 이 파일을 HTML에 <script src="napssp-bridge.js"></script>로 포함합니다.
 *   2. NapSspBridge.init()으로 SDK를 초기화합니다.
 *   3. NapSspBridge.loadAd('banner')처럼 광고를 요청합니다.
 *   4. NapSspBridge.onEvent = function(event, format, detail) { ... }로 이벤트를 수신합니다.
 */

(function (global) {
    'use strict';

    // ── 내부 상태 ────────────────────────────────────────────────────────────

    var _lastRequestTime = 0;
    var DEBOUNCE_MS = 600; // Native 브릿지의 500ms debounce보다 여유를 둠

    // ── Native 호출 ──────────────────────────────────────────────────────────

    function _callNative(action, params) {
        var now = Date.now();
        if (now - _lastRequestTime < DEBOUNCE_MS) {
            console.warn('[NapSsp] 요청 간격이 너무 짧습니다. ' + DEBOUNCE_MS + 'ms 후 다시 시도하세요.');
            return false;
        }
        _lastRequestTime = now;

        var payload = JSON.stringify({ action: action, params: params || {} });

        try {
            if (window.NapSspBridge && typeof window.NapSspBridge.postMessage === 'function') {
                // Android (JavascriptInterface)
                window.NapSspBridge.postMessage(payload);
            } else if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.NapSspBridge) {
                // iOS (WKScriptMessageHandler)
                window.webkit.messageHandlers.NapSspBridge.postMessage(payload);
            } else {
                console.warn('[NapSsp] Native 브릿지를 찾을 수 없습니다. 앱 WebView에서 실행 중인지 확인하세요.');
                return false;
            }
        } catch (e) {
            console.error('[NapSsp] Native 호출 오류:', e);
            return false;
        }
        return true;
    }

    // ── Native → JS 응답 수신 ────────────────────────────────────────────────
    //
    // action 종류:
    //   "init"            - SDK 초기화 응답
    //   "loadAd"          - 광고 요청 ACK (로드 성공이 아님)
    //   "clearAds"        - 광고 해제 응답
    //   "event"           - SDK 광고 이벤트 (loaded / displayed / clicked / rewarded / closed / failed)
    //   "error"           - 오류 응답
    //
    // event data 형식: "[format] eventName: detail"
    //   예: "[banner] loaded: 104704"
    //   예: "[rewardVideo] rewarded: 103722"
    //   예: "[banner] failed: [-1] No fill"

    window.onNapSspMessage = function (responseStr) {
        var response;
        try {
            response = JSON.parse(responseStr);
        } catch (e) {
            console.error('[NapSsp] 응답 파싱 오류:', responseStr);
            return;
        }

        var action = response.action;
        var status = response.status;
        var data = response.data || '';

        if (action === 'event') {
            // "[format] eventName: detail" 파싱
            var match = data.match(/^\[([^\]]+)\]\s+(\w+):\s*(.*)$/);
            if (match) {
                var format = match[1];
                var event  = match[2];
                var detail = match[3];
                if (typeof NapSspBridge.onEvent === 'function') {
                    NapSspBridge.onEvent(event, format, detail);
                }
            }
        } else {
            if (typeof NapSspBridge.onResponse === 'function') {
                NapSspBridge.onResponse(action, status, data);
            }
        }

        if (typeof NapSspBridge.onRawMessage === 'function') {
            NapSspBridge.onRawMessage(response);
        }
    };

    // ── 공개 API ─────────────────────────────────────────────────────────────

    var NapSspBridge = {

        /**
         * SDK 초기화. 광고 요청 전에 반드시 먼저 호출해야 합니다.
         */
        init: function () {
            return _callNative('init', {});
        },

        /**
         * 광고 로드 요청.
         * @param {string} format - "banner" | "native" | "video" | "rewardVideo" | "interstitialVideo" | "interstitialBanner"
         * @param {string} [adUnitId] - 선택. 기본 설정 대신 특정 Ad Unit ID를 사용할 때 전달.
         */
        loadAd: function (format, adUnitId) {
            var params = { format: format };
            if (adUnitId) params.adUnitId = String(adUnitId);
            return _callNative('loadAd', params);
        },

        /**
         * 현재 표시 중인 광고 해제.
         */
        clearAds: function () {
            return _callNative('clearAds', {});
        },

        /**
         * SDK 광고 이벤트 콜백.
         * @param {string} event  - "loaded" | "displayed" | "clicked" | "rewarded" | "completed" | "skipped" | "closed" | "failed"
         * @param {string} format - "banner" | "native" | "video" | "rewardVideo" | "interstitialVideo" | "interstitialBanner"
         * @param {string} detail - 이벤트 상세 메시지
         *
         * 사용 예:
         *   NapSspBridge.onEvent = function(event, format, detail) {
         *       if (event === 'rewarded') grantReward();
         *   };
         */
        onEvent: null,

        /**
         * init / loadAd / clearAds / error 응답 콜백.
         * @param {string} action
         * @param {string} status - "success" | "error"
         * @param {string} data
         */
        onResponse: null,

        /**
         * 모든 응답을 원본 그대로 수신하는 콜백 (디버깅용).
         * @param {Object} response - { action, status, data }
         */
        onRawMessage: null,

        /** 브릿지가 사용 가능한 환경인지 확인합니다. */
        isAvailable: function () {
            return !!(
                (window.NapSspBridge && typeof window.NapSspBridge.postMessage === 'function') ||
                (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.NapSspBridge)
            );
        },
    };

    global.NapSspBridge = NapSspBridge;

}(window));

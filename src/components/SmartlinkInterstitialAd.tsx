import React, { useState, useEffect } from 'react';
import { Lock, Crown } from 'lucide-react';
import { hideSystemNavigation } from '../utils/systemBars';

interface SmartlinkInterstitialAdProps {
  isOpen: boolean;
  onClose: () => void;
  smartlinkUrl?: string;
  wasFullscreen?: boolean;
  onOpenSubscription?: () => void;
}

export const NATIVE_BANNER_HTML = `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <style>
    * { box-sizing: border-box; }
    html, body {
      margin: 0;
      padding: 0;
      width: 100%;
      height: 100%;
      background: #0b0e14;
      color: #f3f4f6;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      overflow-x: hidden;
    }
    .ad-outer {
      width: 100%;
      max-width: 620px;
      padding: 16px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      margin: auto;
      text-align: center;
    }
    #container-d9cb834be4423ba7a81b28d974b0a61a {
      width: 100%;
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 250px;
    }
  </style>
</head>
<body>
  <div class="ad-outer">
    <div id="container-d9cb834be4423ba7a81b28d974b0a61a"></div>
    <script async="async" data-cfasync="false" src="https://bancadeltempoidea.org/21/d9cb834be4423ba7a81b28d974b0a61a"></script>
  </div>
</body>
</html>`;

export const SMARTLINK_DEFAULT_URL = "https://bancadeltempoidea.org/21/d9cb834be4423ba7a81b28d974b0a61a";

export const SmartlinkInterstitialAd: React.FC<SmartlinkInterstitialAdProps> = ({
  isOpen,
  onClose,
  wasFullscreen = false,
  onOpenSubscription,
}) => {
  const [countdown, setCountdown] = useState(15);
  const [canSkip, setCanSkip] = useState(false);

  useEffect(() => {
    if (!isOpen) return;

    // Exit any native fullscreen so modal is 100% visible on top
    if (document.fullscreenElement || (document as any).webkitFullscreenElement) {
      try {
        if (document.exitFullscreen) {
          document.exitFullscreen().catch(() => {});
        } else if ((document as any).webkitExitFullscreen) {
          (document as any).webkitExitFullscreen();
        }
      } catch {}
    }

    // Pause any playing YouTube iframes
    const iframes = document.querySelectorAll('iframe');
    iframes.forEach((ifr) => {
      try {
        if (ifr.src && (ifr.src.includes('youtube.com') || ifr.src.includes('youtube-nocookie.com')) && ifr.contentWindow) {
          ifr.contentWindow.postMessage(JSON.stringify({ event: 'command', func: 'pauseVideo', args: [] }), '*');
        }
      } catch {}
    });

    setCountdown(15);
    setCanSkip(false);

    const interval = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          setCanSkip(true);
          clearInterval(interval);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSkipOrClose = async () => {
    // Resume any paused YouTube iframes
    const iframes = document.querySelectorAll('iframe');
    iframes.forEach((ifr) => {
      try {
        if (ifr.src && (ifr.src.includes('youtube.com') || ifr.src.includes('youtube-nocookie.com')) && ifr.contentWindow) {
          ifr.contentWindow.postMessage(JSON.stringify({ event: 'command', func: 'playVideo', args: [] }), '*');
        }
      } catch {}
    });

    if (wasFullscreen) {
      await hideSystemNavigation().catch(() => {});

      try {
        if ((window as any).Android && typeof (window as any).Android.setLandscape === 'function') {
          (window as any).Android.setLandscape();
        }
      } catch {}

      try {
        if (screen.orientation && 'lock' in screen.orientation) {
          await (screen.orientation as any).lock('landscape').catch(() => {});
        }
      } catch {}

      try {
        const docEl = document.documentElement;
        if (!document.fullscreenElement && !(document as any).webkitFullscreenElement) {
          if (docEl.requestFullscreen) {
            await docEl.requestFullscreen({ navigationUI: 'hide' as const }).catch(() => {});
          } else if ((docEl as any).webkitRequestFullscreen) {
            await (docEl as any).webkitRequestFullscreen().catch(() => {});
          }
        }
      } catch {}
    }

    onClose();
  };

  return (
    <div className="fixed inset-0 z-[999999] bg-black/95 flex flex-col justify-between backdrop-blur-md animate-fadeIn">
      {/* Top Control Bar with VIP Subscription and Skip Ad button */}
      <div className="w-full bg-[#111622]/95 border-b border-gray-800 px-3 sm:px-6 py-2.5 flex items-center justify-between gap-2 shadow-xl shrink-0">
        {/* Left: VIP Subscription (No Ads) Button */}
        <button
          onClick={() => {
            onClose();
            if (onOpenSubscription) {
              onOpenSubscription();
            } else if (typeof (window as any).openSubscriptionSection === 'function') {
              (window as any).openSubscriptionSection();
            }
          }}
          className="flex items-center gap-1.5 bg-gradient-to-r from-amber-500 via-amber-400 to-yellow-400 hover:from-amber-400 hover:to-yellow-300 text-gray-950 text-xs font-black px-3 py-1.5 rounded-lg shadow-lg shadow-amber-500/25 active:scale-95 transition-all cursor-pointer border border-amber-300/60 group"
          title="VIP Subscription (No Ads)"
        >
          <Crown className="w-3.5 h-3.5 fill-current text-amber-950 group-hover:rotate-12 transition-transform" />
          <span>VIP (No Ads)</span>
        </button>

        <div className="flex items-center gap-2">
          {/* Sponsored Ad Badge */}
          <span className="flex items-center gap-1 text-[11px] font-bold text-cyan-400 bg-cyan-950/60 border border-cyan-500/30 px-2 py-1 rounded">
            Sponsored
          </span>

          {/* Skip Ad Button with 15s Countdown */}
          <button
            onClick={handleSkipOrClose}
            disabled={!canSkip}
            className={`flex items-center gap-1.5 text-xs font-bold px-3.5 py-1.5 rounded-lg transition-all shadow-md ${
              canSkip
                ? 'bg-emerald-600 hover:bg-emerald-500 text-white cursor-pointer active:scale-95 ring-2 ring-emerald-400/50 animate-pulse'
                : 'bg-gray-800 text-gray-500 cursor-not-allowed border border-gray-700'
            }`}
          >
            {canSkip ? (
              <>Skip Ad</>
            ) : (
              <>
                <Lock className="w-3 h-3 text-gray-400" />
                <span>{countdown}s</span>
              </>
            )}
          </button>
        </div>
      </div>

      {/* Native Banner Ad Body */}
      <div className="flex-1 w-full relative bg-[#0a0d14] overflow-hidden flex items-center justify-center p-2">
        <iframe
          srcDoc={NATIVE_BANNER_HTML}
          className="w-full h-full border-0 bg-[#0a0d14]"
          sandbox="allow-forms allow-modals allow-orientation-lock allow-pointer-lock allow-popups allow-popups-to-escape-sandbox allow-presentation allow-same-origin allow-scripts allow-top-navigation-by-user-activation"
          scrolling="yes"
          title="Sponsored Native Banner"
        />
      </div>
    </div>
  );
};

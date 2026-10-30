const __vite__mapDeps=(i,m=__vite__mapDeps,d=(m.f||(m.f=["./web-BM8W9WEm.js","./index-J1K1S-x6.js"])))=>i.map(i=>d[i]);
import { _ as __vitePreload } from "./main-CE_qmbre.js";
import { r as registerPlugin } from "./index-J1K1S-x6.js";
import "./no-internet-BRw4lL7h.js";
var Style;
(function(Style2) {
  Style2["Dark"] = "DARK";
  Style2["Default"] = "DEFAULT";
  Style2["Light"] = "LIGHT";
})(Style || (Style = {}));
const NavigationBar = registerPlugin("NavigationBar", {
  web: () => __vitePreload(() => import("./web-BM8W9WEm.js"), true ? __vite__mapDeps([0,1]) : void 0, import.meta.url).then((m) => new m.NavigationBarWeb())
});
export {
  NavigationBar,
  Style
};

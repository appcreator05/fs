const __vite__mapDeps=(i,m=__vite__mapDeps,d=(m.f||(m.f=["./web-I1Aj6Sei.js","./index-J1K1S-x6.js"])))=>i.map(i=>d[i]);
import { _ as __vitePreload } from "./main-CE_qmbre.js";
import { r as registerPlugin } from "./index-J1K1S-x6.js";
import "./no-internet-BRw4lL7h.js";
const App = registerPlugin("App", {
  web: () => __vitePreload(() => import("./web-I1Aj6Sei.js"), true ? __vite__mapDeps([0,1]) : void 0, import.meta.url).then((m) => new m.AppWeb())
});
export {
  App
};

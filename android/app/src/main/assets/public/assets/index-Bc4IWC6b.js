import { r as registerPlugin } from "./index-J1K1S-x6.js";
var Style;
(function(Style2) {
  Style2["Dark"] = "DARK";
  Style2["Light"] = "LIGHT";
  Style2["Default"] = "DEFAULT";
})(Style || (Style = {}));
var Animation;
(function(Animation2) {
  Animation2["None"] = "NONE";
  Animation2["Slide"] = "SLIDE";
  Animation2["Fade"] = "FADE";
})(Animation || (Animation = {}));
const StatusBarAnimation = Animation;
const StatusBarStyle = Style;
const StatusBar = registerPlugin("StatusBar");
export {
  Animation,
  StatusBar,
  StatusBarAnimation,
  StatusBarStyle,
  Style
};

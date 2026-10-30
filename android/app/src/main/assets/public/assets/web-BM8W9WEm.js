import { W as WebPlugin, C as CapacitorException, E as ExceptionCode } from "./index-J1K1S-x6.js";
class NavigationBarWeb extends WebPlugin {
  async getColor() {
    throw this.createUnimplementedError();
  }
  async getStyle() {
    throw this.createUnimplementedError();
  }
  async hide() {
    throw this.createUnimplementedError();
  }
  async setColor(_options) {
    throw this.createUnimplementedError();
  }
  async setStyle(_options) {
    throw this.createUnimplementedError();
  }
  async show() {
    throw this.createUnimplementedError();
  }
  createUnimplementedError() {
    return new CapacitorException("This plugin method is not implemented on this platform.", ExceptionCode.Unimplemented);
  }
}
export {
  NavigationBarWeb
};

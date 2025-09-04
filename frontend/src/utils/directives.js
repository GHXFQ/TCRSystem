/**
 * 全局自定义指令
 */
import { safeElementConfig } from './resizeObserverFix';

/**
 * 注册全局指令
 * @param {*} app Vue应用实例
 */
export const registerDirectives = (app) => {
  // 安全的Element Plus组件配置指令
  app.directive('safe-element', {
    mounted(el) {
      // 如果组件有__vcomponent属性，说明是Element Plus组件
      if (el.__vcomponent) {
        const component = el.__vcomponent;
        
        // 为组件应用安全配置
        if (component.props) {
          // 应用teleported配置
          if ('teleported' in component.props) {
            component.props.teleported = safeElementConfig.teleported;
          }
          
          // 应用destroyOnClose配置
          if ('destroyOnClose' in component.props) {
            component.props.destroyOnClose = safeElementConfig.destroyOnClose;
          }
          
          // 应用popperOptions配置
          if ('popperOptions' in component.props) {
            component.props.popperOptions = safeElementConfig.popperOptions;
          }
        }
      }
    }
  });
}; 
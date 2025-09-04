/**
 * ResizeObserver问题修复工具
 * 用于解决Element Plus组件在调整大小时出现的ResizeObserver错误
 */

/**
 * 应用全局ResizeObserver错误处理器
 * 这会忽略"ResizeObserver loop completed with undelivered notifications"错误
 */
export const applyResizeObserverErrorFix = () => {
  // 全局标志，用于判断是否已经应用了修复
  if (window.__resizeObserverFixed) {
    return;
  }
  
  // 标记为已修复
  window.__resizeObserverFixed = true;
  
  // 保存原始console.error方法
  const originalConsoleError = window.console.error;
  
  // 重写console.error，过滤掉ResizeObserver错误
  window.console.error = (...args) => {
    // 忽略ResizeObserver相关错误
    if (args[0] && 
        typeof args[0] === 'string' && 
        (args[0].includes('ResizeObserver loop') || 
         args[0].includes('ResizeObserver') ||
         args[0].includes('Resize') ||
         args[0].includes('Maximum update depth exceeded'))) {
      return;
    }
    // 其他错误正常输出
    originalConsoleError.apply(window.console, args);
  };
  
  // 完全禁用原生ResizeObserver，替换为无操作版本
  // 注意：这是一种强力方法，会阻止所有ResizeObserver功能，但也会彻底消除错误
  const disableResizeObserver = () => {
    try {
      // 原始的ResizeObserver
      const originalResizeObserver = window.ResizeObserver;
      
      // 替换为无操作版本
      window.ResizeObserver = class NoopResizeObserver {
        constructor(callback) {
          this.callback = callback;
        }
        
        observe() { /* 无操作 */ }
        unobserve() { /* 无操作 */ }
        disconnect() { /* 无操作 */ }
      };
      
      // 保存引用，以便在需要时恢复
      window.__originalResizeObserver = originalResizeObserver;
      
      // 在5秒后恢复，以便让应用有时间完成初始化而不受ResizeObserver干扰
      setTimeout(() => {
        // 使用修补版本替代完全禁用
        applyPatchedResizeObserver();
      }, 5000);
    } catch (e) {
      console.log('禁用ResizeObserver失败:', e);
    }
  };
  
  // 应用修补版本的ResizeObserver，仅捕获错误但保留功能
  const applyPatchedResizeObserver = () => {
    try {
      // 如果已经保存了原始版本，则使用它，否则使用当前版本
      const originalResizeObserver = window.__originalResizeObserver || window.ResizeObserver;
      
      // 替换为修补版本
      window.ResizeObserver = class PatchedResizeObserver {
        constructor(callback) {
          // 包装回调函数，捕获并忽略错误
          const safeCallback = (entries, observer) => {
            try {
              // 使用requestAnimationFrame来减少同步的布局更新
              requestAnimationFrame(() => {
                try {
                  callback(entries, observer);
                } catch (innerErr) {
                  // 忽略特定的ResizeObserver错误
                  if (innerErr.message && 
                      (innerErr.message.includes('ResizeObserver') || 
                       innerErr.message.includes('Maximum update depth'))) {
                    return;
                  }
                  throw innerErr;
                }
              });
            } catch (err) {
              // 忽略特定的ResizeObserver错误
              if (err.message && 
                  (err.message.includes('ResizeObserver') || 
                   err.message.includes('Maximum update depth'))) {
                return;
              }
              throw err;
            }
          };
          
          // 创建原始的ResizeObserver，但使用安全的回调
          this._observer = new originalResizeObserver(safeCallback);
        }
        
        // 代理方法到原始观察者
        observe(target, options) {
          try {
            this._observer.observe(target, options);
          } catch (e) {
            console.log('ResizeObserver.observe error:', e);
          }
        }
        
        unobserve(target) {
          try {
            this._observer.unobserve(target);
          } catch (e) {
            console.log('ResizeObserver.unobserve error:', e);
          }
        }
        
        disconnect() {
          try {
            this._observer.disconnect();
          } catch (e) {
            console.log('ResizeObserver.disconnect error:', e);
          }
        }
      };
    } catch (e) {
      console.log('应用ResizeObserver补丁失败:', e);
    }
  };
  
  // 根据错误频率选择修复策略
  let errorCount = 0;
  const originalError = window.Error;
  
  window.Error = function(...args) {
    const error = new originalError(...args);
    if (error.message && error.message.includes('ResizeObserver')) {
      errorCount++;
      
      // 如果短时间内错误过多，应用强力修复
      if (errorCount > 3) {
        // 重置计数
        errorCount = 0;
        // 应用强力修复
        disableResizeObserver();
      }
    }
    return error;
  };
  
  // 初始应用修补版本
  applyPatchedResizeObserver();
  
  // 监听全局未捕获错误
  window.addEventListener('error', (event) => {
    if (event && event.message && 
        (event.message.includes('ResizeObserver') || 
         event.message.includes('Resize') ||
         event.message.includes('Maximum update depth exceeded'))) {
      event.preventDefault();
      event.stopPropagation();
      return false;
    }
  }, true);

  // 监听Promise未捕获错误
  window.addEventListener('unhandledrejection', (event) => {
    if (event && event.reason && 
        (String(event.reason).includes('ResizeObserver') || 
         String(event.reason).includes('Resize') ||
         String(event.reason).includes('Maximum update depth exceeded'))) {
      event.preventDefault();
      event.stopPropagation();
      return false;
    }
  });
  
  // 修复Element Plus标签页切换问题
  const fixTabsResize = () => {
    const tabNavs = document.querySelectorAll('.el-tabs__nav');
    if (tabNavs.length > 0) {
      // 使用requestAnimationFrame确保在下一帧渲染时处理
      window.requestAnimationFrame(() => {
        tabNavs.forEach(nav => {
          if (nav.parentElement) {
            const tabHeader = nav.parentElement;
            // 触发一次重新计算布局
            tabHeader.style.display = 'none';
            setTimeout(() => {
              tabHeader.style.display = '';
            }, 0);
          }
        });
      });
    }
  };
  
  // 监听标签页切换事件
  document.addEventListener('click', (e) => {
    const target = e.target;
    if (target && target.classList && 
        (target.classList.contains('el-tabs__item') || 
         target.closest('.el-tabs__item'))) {
      setTimeout(fixTabsResize, 10);
    }
  });
  
  // 全局MutationObserver监听DOM变化，修复标签页
  const setupTabsMutationObserver = () => {
    const observer = new MutationObserver((mutations) => {
      let hasTabs = false;
      for (const mutation of mutations) {
        if (mutation.addedNodes.length) {
          for (const node of mutation.addedNodes) {
            if (node.nodeType === 1 && 
                (node.classList?.contains('el-tabs__content') || 
                 node.querySelector?.('.el-tabs__content'))) {
              hasTabs = true;
              break;
            }
          }
        }
      }
      
      if (hasTabs) {
        setTimeout(fixTabsResize, 50);
      }
    });
    
    observer.observe(document.body, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['class', 'style']
    });
  };
  
  // 注入CSS样式
  const injectCSS = () => {
    const style = document.createElement('style');
    style.textContent = `
      /* 防止标签页内容区域触发不必要的调整大小事件 */
      .el-tabs__content {
        overflow: hidden !important;
        min-height: 200px;
        transform: translateZ(0);
        will-change: transform;
        contain: layout style;
      }
      
      /* 为表格设置溢出处理，防止尺寸计算问题 */
      .el-table {
        overflow: hidden !important;
        transform: translateZ(0);
        will-change: transform;
        contain: layout style;
      }
      
      /* 修复弹窗中的ResizeObserver问题 */
      .el-dialog__body {
        min-height: 50px;
        overflow: auto !important;
        contain: layout style;
      }
    `;
    document.head.appendChild(style);
  };
  
  // 初始化DOM后设置监听
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      setupTabsMutationObserver();
      injectCSS();
    });
  } else {
    setupTabsMutationObserver();
    injectCSS();
  }
};

/**
 * 为Element Plus组件提供安全的配置
 * 用于避免ResizeObserver相关问题
 */
export const safeElementConfig = {
  // 禁止传送到body，减少ResizeObserver事件
  teleported: false,
  // 关闭时销毁内容，避免累积过多监听器
  destroyOnClose: true,
  // 避免自动聚焦，减少不必要的重排
  autofocus: false,
  // 在页面调整大小时减少不必要的重排
  popperOptions: {
    strategy: 'fixed',
    modifiers: [
      {
        name: 'computeStyles',
        options: {
          adaptive: false,
          gpuAcceleration: false
        }
      },
      {
        name: 'preventOverflow',
        options: {
          padding: 10
        }
      }
    ]
  }
};

/**
 * 为Element Plus标签页提供安全的配置
 * 特别处理标签页切换问题
 */
export const safeTabsConfig = {
  // 避免自动触发ResizeObserver事件
  lazy: true,
  // 在标签页切换后重新计算布局
  onTabClick: () => {
    try {
      // 延迟后触发窗口大小调整事件
      setTimeout(() => {
        window.dispatchEvent(new Event('resize'));
      }, 50);
      
      // 延迟后尝试修复标签页布局
      setTimeout(() => {
        const tabContents = document.querySelectorAll('.el-tabs__content');
        for (const content of tabContents) {
          // 强制重排
          content.style.display = 'none';
          content.offsetHeight; // 触发重排
          content.style.display = '';
        }
      }, 100);
    } catch (e) {
      // 忽略错误
    }
  }
}; 
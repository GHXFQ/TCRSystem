<template>
  <router-view />
</template>

<script>
export default {
  name: 'App',
  created() {
    // 全局错误处理，抑制 ResizeObserver 警告
    const originalConsoleError = console.error;
    console.error = function() {
      const args = Array.from(arguments);
      const firstArg = args[0];
      
      // 更精确地匹配 ResizeObserver 错误
      if (firstArg && 
          typeof firstArg === 'string' && 
          (firstArg.includes('ResizeObserver loop') || 
           firstArg.includes('ResizeObserver Loop') || 
           firstArg.includes('ResizeObserver was created'))) {
        // 忽略 ResizeObserver 相关的错误
        return;
      }
      return originalConsoleError.apply(console, args);
    };
    
    // 添加全局错误处理
    window.addEventListener('error', (event) => {
      if (event && event.message && 
          (event.message.includes('ResizeObserver') || 
           event.message.includes('Script error'))) {
        // 阻止错误继续传播
        event.stopPropagation();
        event.preventDefault();
        return true;
      }
    }, true);
  }
}
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100%;
}
</style> 
<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <el-menu
        :router="true"
        :default-active="$route.path"
        class="el-menu-vertical"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <span>首页</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              {{ userInfo?.realName || '用户' }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup name="LayoutIndex">
import { computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Monitor, ArrowDown } from '@element-plus/icons-vue'

const store = useStore()
const router = useRouter()

const userInfo = computed(() => store.state.user.userInfo)

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await store.dispatch('user/logout')
      ElMessage.success('退出成功')
      router.push('/login')
    } catch (error) {
      console.error('退出失败:', error)
    }
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;

  .el-aside {
    background-color: #304156;
    
    .el-menu {
      border-right: none;
    }
  }

  .el-header {
    background-color: #fff;
    border-bottom: 1px solid #dcdfe6;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 0 20px;

    .header-right {
      .el-dropdown-link {
        cursor: pointer;
        color: #409eff;
      }
    }
  }

  .el-main {
    background-color: #f0f2f5;
    padding: 20px;
  }
}
</style> 
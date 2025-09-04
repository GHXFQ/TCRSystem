import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: () => import('@/views/UserProfile.vue'),
    meta: { title: '个人信息', requiresAuth: true }
  },
  {
    path: '/student',
    name: 'StudentLayout',
    component: () => import('@/layouts/StudentLayout.vue'),
    meta: { title: '学生端', requiresAuth: true, role: 0 },
    children: [
      {
        path: 'home',
        name: 'StudentHome',
        component: () => import('@/views/student/Home.vue'),
        meta: { title: '学生首页', requiresAuth: true, role: 0 }
      },
      {
        path: 'courses',
        name: 'StudentCourses',
        component: () => import('@/views/student/Courses.vue'),
        meta: { title: '课程选择', requiresAuth: true, role: 0 }
      },
      {
        path: 'materials',
        name: 'StudentMaterials',
        component: () => import('../views/student/Materials.vue'),
        meta: { title: '课程资料', requiresAuth: true, role: 0 }
      },
      {
        path: 'feedback',
        name: 'StudentFeedback',
        component: () => import('../views/student/Feedback.vue'),
        meta: { title: '课程反馈', requiresAuth: true, role: 0 }
      },
      {
        path: 'assignments',
        name: 'StudentAssignments',
        component: () => import('../views/student/Assignments.vue'),
        meta: { title: '课程作业', requiresAuth: true, role: 0 }
      },
      {
        path: 'assignment/:id',
        name: 'StudentAssignmentDetail',
        component: () => import('../views/student/AssignmentDetail.vue'),
        meta: { title: '作业详情', requiresAuth: true, role: 0 }
      }
    ]
  },
  {
    path: '/teacher',
    name: 'TeacherLayout',
    component: () => import('@/layouts/TeacherLayout.vue'),
    meta: { title: '教师端', requiresAuth: true, role: 1 },
    children: [
      {
        path: 'home',
        name: 'TeacherHome',
        component: () => import('@/views/teacher/Home.vue'),
        meta: { title: '教师首页', requiresAuth: true, role: 1 }
      },
      {
        path: 'material-upload',
        name: 'MaterialUpload',
        component: () => import('@/views/teacher/MaterialUpload.vue'),
        meta: { title: '材料上传', requiresAuth: true, role: 1 }
      },
      {
        path: 'material-review',
        name: 'MaterialReview',
        component: () => import('@/views/teacher/MaterialReview.vue'),
        meta: { title: '材料审核', requiresAuth: true, role: 1 }
      },
      {
        path: 'course-management',
        name: 'CourseManagement',
        component: () => import('@/views/teacher/CourseManagement.vue'),
        meta: { title: '课程管理', requiresAuth: true, role: 1 }
      },
      {
        path: 'resource-sharing',
        name: 'ResourceSharing',
        component: () => import('@/views/teacher/ResourceSharing.vue'),
        meta: { title: '课程资源协同', requiresAuth: true, role: 1 }
      },
      {
        path: 'assignment-management',
        name: 'AssignmentManagement',
        component: () => import('@/views/teacher/AssignmentManagement.vue'),
        meta: { title: '课程任务管理', requiresAuth: true, role: 1 }
      },
      {
        path: 'assignment-submissions',
        name: 'AssignmentSubmissions',
        component: () => import('@/views/teacher/AssignmentSubmissions.vue'),
        meta: { title: '作业提交列表', requiresAuth: true, role: 1 }
      },
      {
        path: 'course-feedback',
        name: 'TeacherCourseFeedback',
        component: () => import('@/views/teacher/CourseFeedback.vue'),
        meta: { title: '课程反馈', requiresAuth: true, role: 1 }
      }
    ]
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { title: '管理员端', requiresAuth: true, role: 2 },
    children: [
      {
        path: 'home',
        name: 'AdminHome',
        component: () => import('@/views/admin/Home.vue'),
        meta: { title: '管理员首页', roles: [2] }
      },
      {
        path: 'material-review',
        name: 'AdminMaterialReview',
        component: () => import('@/views/admin/MaterialReview.vue'),
        meta: { title: '材料审核', roles: [2] }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { title: '用户管理', roles: [2] }
      },
      {
        path: 'teacher-management',
        name: 'TeacherManagement',
        component: () => import('@/views/admin/TeacherManagement.vue'),
        meta: { title: '教师管理', roles: [2] }
      },
      {
        path: 'student-management',
        name: 'StudentManagement',
        component: () => import('@/views/admin/StudentManagement.vue'),
        meta: { title: '学生管理', roles: [2] }
      },
      {
        path: 'educational-admin',
        name: 'EducationalAdmin',
        component: () => import('@/views/admin/EducationalAdmin.vue'),
        meta: { title: '教务管理', roles: [2] }
      },
      {
        path: 'material-tasks',
        name: 'MaterialTasks',
        component: () => import('@/views/admin/MaterialTasks.vue'),
        meta: { title: '教学材料任务', roles: [2] }
      },
      {
        path: 'material-statistics',
        name: 'MaterialStatistics',
        component: () => import('@/views/admin/MaterialStatistics.vue'),
        meta: { title: '材料统计', roles: [2] }
      },
      {
        path: 'courses',
        name: 'AdminCourses',
        component: () => import('@/views/admin/CourseManagement.vue'),
        meta: { title: '课程管理', roles: [2] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 教学课程资源管理系统` : '教学课程资源管理系统'
  
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  
  // 已登录用户访问登录页，根据角色重定向到对应首页
  if (token && (to.path === '/login' || to.path === '/')) {
    if (userInfo && userInfo.role !== undefined) {
      if (userInfo.role === 0) {
        next('/student/home')
        return
      } else if (userInfo.role === 1) {
        next('/teacher/home')
        return
      } else if (userInfo.role === 2) {
        next('/admin/home')
        return
      }
    }
  }
  
  // 需要认证的页面
  if (to.meta.requiresAuth) {
    if (!token) {
      ElMessage.warning('请先登录')
      next({ path: '/login' })
      return
    }
    
    // 如果路由需要特定角色权限
    if (to.meta.role !== undefined) {
      if (!userInfo || userInfo.role === undefined) {
        ElMessage.warning('请先登录')
        next({ path: '/login' })
        return
      } else if (to.meta.role !== userInfo.role) {
        ElMessage.error('您没有权限访问该页面')
        
        // 根据用户角色重定向到对应首页
        if (userInfo.role === 0) {
          next('/student/home')
        } else if (userInfo.role === 1) {
          next('/teacher/home')
        } else if (userInfo.role === 2) {
          next('/admin/home')
        } else {
          next('/login')
        }
        return
      }
    }
  }
  
  next()
})

export default router 
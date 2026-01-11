import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register/band',
    name: 'RegisterBand',
    component: () => import('../views/RegisterBand.vue')
  },
  {
    path: '/register/fan',
    name: 'RegisterFan',
    component: () => import('../views/RegisterFan.vue')
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../layouts/AdminLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue')
      },
      {
        path: 'bands',
        name: 'AdminBands',
        component: () => import('../views/admin/Bands.vue')
      },
      {
        path: 'members',
        name: 'AdminMembers',
        component: () => import('../views/admin/Members.vue')
      },
      {
        path: 'albums',
        name: 'AdminAlbums',
        component: () => import('../views/admin/Albums.vue')
      },
      {
        path: 'songs',
        name: 'AdminSongs',
        component: () => import('../views/admin/Songs.vue')
      },
      {
        path: 'concerts',
        name: 'AdminConcerts',
        component: () => import('../views/admin/Concerts.vue')
      },
      {
        path: 'fans',
        name: 'AdminFans',
        component: () => import('../views/admin/Fans.vue')
      },
      {
        path: 'reviews',
        name: 'AdminReviews',
        component: () => import('../views/admin/Reviews.vue')
      }
    ]
  },
  {
    path: '/band',
    name: 'Band',
    component: () => import('../layouts/BandLayout.vue'),
    children: [
      {
        path: 'home',
        name: 'BandHome',
        component: () => import('../views/band/Home.vue')
      },
      {
        path: 'members',
        name: 'BandMembers',
        component: () => import('../views/band/Members.vue')
      },
      {
        path: 'albums',
        name: 'BandAlbums',
        component: () => import('../views/band/Albums.vue')
      },
      {
        path: 'songs',
        name: 'BandSongs',
        component: () => import('../views/band/Songs.vue')
      },
      {
        path: 'concerts',
        name: 'BandConcerts',
        component: () => import('../views/band/Concerts.vue')
      },
      {
        path: 'fans',
        name: 'BandFans',
        component: () => import('../views/band/Fans.vue')
      }
    ]
  },
  {
    path: '/fan',
    name: 'Fan',
    component: () => import('../layouts/FanLayout.vue'),
    children: [
      {
        path: 'home',
        name: 'FanHome',
        component: () => import('../views/fan/Home.vue')
      },
      {
        path: 'profile',
        name: 'FanProfile',
        component: () => import('../views/fan/Profile.vue')
      },
      {
        path: 'favorites',
        name: 'FanFavorites',
        component: () => import('../views/fan/Favorites.vue')
      },
      {
        path: 'discovery',
        name: 'FanDiscovery',
        component: () => import('../views/fan/Discovery.vue')
      },
      {
        path: 'reviews',
        name: 'FanReviews',
        component: () => import('../views/fan/Reviews.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userRole = localStorage.getItem('userRole')

  // 如果访问登录页或注册页，直接放行
  if (to.path === '/login' || to.path.startsWith('/register')) {
    next()
    return
  }

  // 如果没有用户角色信息，跳转到登录页
  if (!userRole) {
    next('/login')
    return
  }

  // 检查角色权限
  if (to.path.startsWith('/admin') && userRole !== 'ADMIN') {
    next('/login')
  } else if (to.path.startsWith('/band') && userRole !== 'BAND') {
    next('/login')
  } else if (to.path.startsWith('/fan') && userRole !== 'FAN') {
    next('/login')
  } else {
    next()
  }
})

export default router

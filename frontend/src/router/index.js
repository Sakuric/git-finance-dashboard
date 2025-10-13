import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import StockList from '../views/StockList.vue'
import StockDetail from '../views/StockDetail.vue'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Home
    },
    {
        path: '/login',
        name: 'Login',
        component: Login
    },
    {
        path: '/register',
        name: 'Register',
        component: Register
    },
    {
        path: '/stocks',
        name: 'StockList',
        component: StockList
    },
    {
        path: '/stocks/:stockCode',
        name: 'StockDetail',
        component: StockDetail
    }
]

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.path === '/login' || to.path === '/register') {
        next()
    } else {
        if (!token) {
            next('/login')
        } else {
            next()
        }
    }
})

export default router
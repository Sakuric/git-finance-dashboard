// 导入API服务
import { getMarketIndices, getStockDetail, getWatchlist, addToWatchlist, removeFromWatchlist } from './src/api/stock.js'
import { getUserInfo, logout as logoutApi } from './src/api/user.js'

document.addEventListener('DOMContentLoaded', function() {
    // 检查登录状态
    checkLoginStatus();
    
    let activeCharts = []; // 用于跟踪活动图表实例以便于销毁

    // === 页面导航功能 ===
    const menuItems = document.querySelectorAll('.menu-item');
    const pageContents = document.querySelectorAll('.page-content');
    
    // 设置退出登录功能
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            logout();
        });
    }
    
    // === 仪表盘K线图时间周期选择器 ===
    function setupDashboardTimeSelector() {
        const timeButtons = document.querySelectorAll('.time-selector button');
        timeButtons.forEach(button => {
            button.addEventListener('click', function() {
                // 如果点击的是已激活的按钮，不做任何操作
                if (this.classList.contains('active')) return;
                
                // 添加点击动画效果
                this.style.transform = 'scale(0.95)';
                
                // 更新按钮状态
                timeButtons.forEach(btn => {
                    btn.classList.remove('active');
                    // 为非活动按钮添加过渡效果
                    btn.style.transition = 'all 0.3s ease';
                });
                
                // 延迟添加active类，以便动画效果更明显
                setTimeout(() => {
                    this.classList.add('active');
                    // 重置按钮变换
                    this.style.transform = '';
                    
                    // 获取选择的时间周期
                    const period = this.getAttribute('data-period');
                    
                    // 重新初始化K线图
                    initMainKLineChart(period);
                }, 100);
            });
            
            // 添加鼠标悬停效果
            button.addEventListener('mouseenter', function() {
                if (!this.classList.contains('active')) {
                    this.style.transform = 'translateY(-2px)';
                }
            });
            
            button.addEventListener('mouseleave', function() {
                if (!this.classList.contains('active')) {
                    this.style.transform = '';
                }
            });
        });
    }
  
    menuItems.forEach(item => {
        // 跳过退出登录按钮，因为它有独立的事件处理
        if (item.id === 'logoutBtn') return;
        
        item.addEventListener('click', function(e) {
            e.preventDefault();
            console.log('菜单项被点击:', this.getAttribute('data-page'));
          
            menuItems.forEach(i => i.classList.remove('active'));
            this.classList.add('active');
          
            pageContents.forEach(p => p.classList.remove('active'));
          
            // 销毁所有旧图表实例
            activeCharts.forEach(chart => chart.dispose());
            activeCharts = [];

            const pageId = this.getAttribute('data-page') + '-page';
            const targetPage = document.getElementById(pageId);
            if (targetPage) {
                targetPage.classList.add('active');
              
                // 延迟初始化新页面的图表，确保DOM可见
                setTimeout(() => {
                    const page = this.getAttribute('data-page');
                    console.log('初始化页面:', page);
                    if (page === 'dashboard') {
                        initMainKLineChart('daily');
                        setupDashboardTimeSelector();
                        loadDashboardData();
                    } else if (page === 'market') {
                        setupMarketPage();
                    } else if (page === 'settings') {
                        setupSettingsPage();
                    } else if (page === 'watchlist') {
                        setupWatchlistPage();
                    } else if (page === 'advisor') {
                        setupAdvisorPage();
                    } else if (page === 'ai-models') {
                        setupAIModelsPage();
                    }
                }, 50);
            }
        });
    });

    // === 加载仪表盘数据 ===
    function loadDashboardData() {
        // 加载市场指数数据
        getMarketIndices()
            .then(response => {
                console.log('市场指数数据:', response.data);
                updateMarketIndices(response.data);
            })
            .catch(error => {
                console.error('加载市场指数失败:', error);
                // 使用模拟数据作为后备
                updateMarketIndices(getMockMarketIndices());
            });
        
        // 加载自选股数据
        getWatchlist()
            .then(response => {
                console.log('自选股数据:', response.data);
                updateWatchlist(response.data);
            })
            .catch(error => {
                console.error('加载自选股失败:', error);
                // 使用模拟数据作为后备
                updateWatchlist(getMockWatchlist());
            });
    }

    // === 更新市场指数显示 ===
    function updateMarketIndices(data) {
        const indexItems = document.querySelectorAll('.index-item');
        if (indexItems.length >= 3 && data && data.length >= 3) {
            for (let i = 0; i < 3; i++) {
                const item = indexItems[i];
                const index = data[i];
                
                const nameEl = item.querySelector('p');
                const priceEl = item.querySelector('h3');
                const changeEl = item.querySelector('span');
                
                if (nameEl) nameEl.textContent = index.name;
                if (priceEl) {
                    priceEl.textContent = index.current.toFixed(2);
                    priceEl.className = index.change >= 0 ? 'positive' : 'negative';
                }
                if (changeEl) {
                    const changeText = `${index.change >= 0 ? '+' : ''}${index.change.toFixed(2)} (${index.changePercent >= 0 ? '+' : ''}${index.changePercent.toFixed(2)}%)`;
                    changeEl.textContent = changeText;
                    changeEl.className = index.change >= 0 ? 'positive' : 'negative';
                }
            }
        }
    }

    // === 更新自选股显示 ===
    function updateWatchlist(data) {
        const watchlistEl = document.querySelector('.watchlist');
        if (watchlistEl && data) {
            watchlistEl.innerHTML = '';
            data.slice(0, 3).forEach(stock => {
                const li = document.createElement('li');
                li.className = 'watchlist-item';
                li.innerHTML = `
                    <div class="stock-info">
                        <span>${stock.name}</span><small>${stock.code}</small>
                    </div>
                    <div class="stock-price ${stock.change >= 0 ? 'positive' : 'negative'}">
                        <span>${stock.current.toFixed(2)}</span>
                        <small>${stock.change >= 0 ? '+' : ''}${stock.changePercent.toFixed(2)}%</small>
                    </div>
                `;
                watchlistEl.appendChild(li);
            });
        }
    }

    // === 仪表盘K线图 ===
    function initMainKLineChart(period) {
        const chartDom = document.getElementById('mainKLineChart');
        if (!chartDom) return;
        
        // 销毁可能存在的旧图表实例
        const existingChart = echarts.getInstanceByDom(chartDom);
        if (existingChart) {
            existingChart.dispose();
        }
        
        const mainChart = echarts.init(chartDom);
        activeCharts.push(mainChart);
        
        // 根据不同周期生成不同数量的模拟数据
        let dataCount;
        let periodName;
        switch(period) {
            case 'weekly':
                dataCount = 104; // 大约2年的周K数据
                periodName = '周K';
                break;
            case 'monthly':
                dataCount = 48; // 大约4年的月K数据
                periodName = '月K';
                break;
            default: // daily
                dataCount = 200; // 大约200天的日K数据
                periodName = '日K';
        }
        
        const rawData = generateKLineData(dataCount, period);
        const dates = rawData.map(item => item.time);
        const kData = rawData.map(item => item.k);
        
        const option = {
            backgroundColor: 'transparent',
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'cross'
                },
                backgroundColor: 'rgba(22, 27, 34, 0.9)',
                borderColor: '#30363D',
                textStyle: {
                    color: '#C9D1D9'
                }
            },
            grid: {
                left: '10%',
                right: '10%',
                bottom: '15%'
            },
            xAxis: {
                type: 'category',
                data: dates,
                axisLine: {
                    lineStyle: {
                        color: '#8B949E'
                    }
                },
                axisLabel: {
                    color: '#8B949E'
                }
            },
            yAxis: {
                scale: true,
                axisLine: {
                    lineStyle: {
                        color: '#8B949E'
                    }
                },
                splitLine: {
                    lineStyle: {
                        color: '#30363D'
                    }
                }
            },
            dataZoom: [
                {
                    type: 'inside',
                    start: 80,
                    end: 100
                },
                {
                    show: true,
                    type: 'slider',
                    bottom: 10,
                    start: 80,
                    end: 100,
                    handleStyle: {
                        color: '#00AFFF'
                    },
                    textStyle: {
                        color: '#8B949E'
                    }
                }
            ],
            series: [
                {
                    name: periodName,
                    type: 'candlestick',
                    data: kData,
                    itemStyle: {
                        color: 'var(--color-positive)',
                        color0: 'var(--color-negative)',
                        borderColor: 'var(--color-positive)',
                        borderColor0: 'var(--color-negative)'
                    }
                }
            ]
        };
        
        mainChart.setOption(option);
        window.addEventListener('resize', () => mainChart.resize());
    }

    // === 设置和初始化股票详情页 ===
    function setupMarketPage() {
        initStockCharts();
        initRealtimeDataSimulation();

        // 绑定图表标签页切换事件
        const tabButtons = document.querySelectorAll('.tab-button-pro');
        const tabContents = document.querySelectorAll('.tab-content-pro');
        tabButtons.forEach(button => {
            button.addEventListener('click', function() {
                tabButtons.forEach(btn => btn.classList.remove('active'));
                tabContents.forEach(content => content.classList.remove('active'));
                this.classList.add('active');
                const tabId = this.getAttribute('data-tab');
                document.getElementById(tabId.split('-')[0] + '-tab-content').classList.add('active');
              
                // 重新渲染对应图表
                initStockCharts();
            });
        });
    }

    // === 初始化个股页面的所有图表 ===
    function initStockCharts() {
        const activeTab = document.querySelector('.tab-button-pro.active').getAttribute('data-tab');
        if (activeTab === 'timeline') {
            initStockTimelineChart();
        } else if (activeTab.startsWith('kline')) {
            const period = activeTab.split('-')[1] || 'daily';
            initStockKLineChart(period);
        }
    }

    // --- 模拟生成K线和指标数据 ---
    function generateKLineData(count, period = 'daily') {
        let data = [];
        let time = new Date(2023, 0, 1);
        let basePrice = 200;
        
        for (let i = 0; i < count; i++) {
            let open = basePrice;
            
            // 根据不同周期调整价格波动范围
            let priceRange;
            switch(period) {
                case 'weekly':
                    priceRange = 40; // 周K波动范围更大
                    break;
                case 'monthly':
                    priceRange = 80; // 月K波动范围最大
                    break;
                default: // daily
                    priceRange = 20; // 日K波动范围
            }
            
            let close = open + (Math.random() - 0.5) * priceRange;
            let high = Math.max(open, close) + Math.random() * (priceRange / 4);
            let low = Math.min(open, close) - Math.random() * (priceRange / 4);
            
            // 根据不同周期调整成交量
            let volumeRange;
            switch(period) {
                case 'weekly':
                    volumeRange = 2000000; // 周K成交量更大
                    break;
                case 'monthly':
                    volumeRange = 8000000; // 月K成交量最大
                    break;
                default: // daily
                    volumeRange = 500000; // 日K成交量
            }
            
            data.push({
                time: time.toISOString().slice(0, 10),
                k: [open.toFixed(2), close.toFixed(2), low.toFixed(2), high.toFixed(2)],
                volume: Math.floor(Math.random() * volumeRange) + (volumeRange / 5)
            });
            basePrice = close;
            
            // 根据不同周期增加时间
            switch(period) {
                case 'weekly':
                    time.setDate(time.getDate() + 7);
                    break;
                case 'monthly':
                    time.setMonth(time.getMonth() + 1);
                    break;
                default: // daily
                    time.setDate(time.getDate() + 1);
            }
        }
        return data;
    }
  
    // --- 计算MACD指标 ---
    function calculateMACD(data) {
        let a = data.map(d => parseFloat(d.k[1]));
        let ema12 = [a[0]];
        let ema26 = [a[0]];
        let diff = [0];
        for (let i=1; i<a.length; i++) {
            ema12.push(ema12[i-1]*11/13 + a[i]*2/13);
            ema26.push(ema26[i-1]*25/27 + a[i]*2/27);
            diff.push(ema12[i]-ema26[i]);
        }
        let dea = [diff[0]];
        for (let i=1; i<diff.length; i++) {
            dea.push(dea[i-1]*8/10 + diff[i]*2/10);
        }
        return data.map((d, i) => ({
            diff: diff[i],
            dea: dea[i],
            macd: (diff[i] - dea[i]) * 2
        }));
    }

    // === 个股K线图 (带成交量和MACD) ===
    function initStockKLineChart(period) {
        const chartDom = document.getElementById('stockKLineChart');
        if (!chartDom) return;
        echarts.dispose(chartDom); // 确保销毁旧实例
        const myChart = echarts.init(chartDom);
        activeCharts.push(myChart);

        const dataCount = period === 'weekly' ? 104 : period === 'monthly' ? 48 : 200;
        const rawData = generateKLineData(dataCount);
        const dates = rawData.map(item => item.time);
        const kData = rawData.map(item => item.k);
        const volumes = rawData.map((item, index) => [index, item.volume, item.k[1] > item.k[0] ? 1 : -1]);
        const macdData = calculateMACD(rawData);

        const option = {
            animation: false,
            backgroundColor: 'transparent',
            tooltip: { trigger: 'axis', axisPointer: { type: 'cross' }, backgroundColor: 'rgba(22, 27, 34, 0.9)', borderColor: '#30363D', textStyle: { color: '#C9D1D9' } },
            axisPointer: { link: { xAxisIndex: 'all' } },
            grid: [
                { left: '10%', right: '8%', height: '50%' },
                { left: '10%', right: '8%', top: '65%', height: '10%' },
                { left: '10%', right: '8%', top: '80%', height: '12%' }
            ],
            xAxis: [
                { type: 'category', data: dates, axisLine: { lineStyle: { color: '#8B949E' } }, axisLabel: { show: false } },
                { type: 'category', data: dates, gridIndex: 1, axisLine: { lineStyle: { color: '#8B949E' } }, axisLabel: { show: false } },
                { type: 'category', data: dates, gridIndex: 2, axisLine: { lineStyle: { color: '#8B949E' } } }
            ],
            yAxis: [
                { scale: true, axisLine: { lineStyle: { color: '#8B949E' } }, splitLine: { lineStyle: { color: '#30363D' } }},
                { scale: true, gridIndex: 1, axisLabel: { show: false }, axisLine: { show: false }, axisTick: { show: false }, splitLine: { show: false } },
                { scale: true, gridIndex: 2, axisLabel: { show: false }, axisLine: { show: false }, axisTick: { show: false }, splitLine: { show: false } }
            ],
            dataZoom: [{ type: 'inside', xAxisIndex: [0, 1, 2], start: 80, end: 100 }],
            series: [
                { type: 'candlestick', name: '日K', data: kData, itemStyle: { color: 'var(--color-positive)', color0: 'var(--color-negative)', borderColor: 'var(--color-positive)', borderColor0: 'var(--color-negative)' } },
                { type: 'bar', name: 'Volume', data: volumes, xAxisIndex: 1, yAxisIndex: 1, itemStyle: { color: ({ value }) => (value[2] === 1 ? 'var(--color-positive)' : 'var(--color-negative)') } },
                { name: 'MACD', type: 'bar', data: macdData.map(d => d.macd), xAxisIndex: 2, yAxisIndex: 2, itemStyle: { color: ({ value }) => (value > 0 ? 'var(--color-positive)' : 'var(--color-negative)') } },
                { name: 'DIF', type: 'line', data: macdData.map(d => d.diff), symbol: 'none', lineStyle: { width: 1 }, xAxisIndex: 2, yAxisIndex: 2 },
                { name: 'DEA', type: 'line', data: macdData.map(d => d.dea), symbol: 'none', lineStyle: { width: 1 }, xAxisIndex: 2, yAxisIndex: 2 }
            ]
        };
        myChart.setOption(option);
        window.addEventListener('resize', () => myChart.resize());
    }

    // === 个股分时图（带成交量） ===
    function initStockTimelineChart() {
        const chartDom = document.getElementById('stockTimelineChart');
        if (!chartDom) return;
        echarts.dispose(chartDom); // 确保销毁旧实例
        const myChart = echarts.init(chartDom);
        activeCharts.push(myChart);
        
        // 生成分时数据和成交量数据
        const now = new Date();
        const priceData = [];
        const volumeData = [];
        let basePrice = 255.88;
        
        for (let i = 0; i < 240; i++) { // 4小时，每分钟一个数据点
            const time = new Date(now);
            time.setHours(9, 30 + i); // 从9:30开始
            
            // 随机波动
            const change = (Math.random() - 0.5) * 0.5;
            basePrice += change;
            
            // 生成成交量数据（手）
            const volume = Math.floor(Math.random() * 500) + 50;
            
            priceData.push([time.toTimeString().slice(0, 5), basePrice.toFixed(2)]);
            volumeData.push([time.toTimeString().slice(0, 5), volume]);
        }
        
        const option = {
            backgroundColor: 'transparent',
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'cross'
                },
                backgroundColor: 'rgba(22, 27, 34, 0.9)',
                borderColor: '#30363D',
                textStyle: { color: '#C9D1D9' },
                formatter: function(params) {
                    let result = `时间: ${params[0].name}<br/>`;
                    params.forEach(param => {
                        if (param.seriesName === '分时') {
                            result += `价格: ${param.value[1]}<br/>`;
                        } else if (param.seriesName === '成交量') {
                            result += `成交量: ${param.value[1]}手<br/>`;
                        }
                    });
                    return result;
                }
            },
            axisPointer: {
                link: { xAxisIndex: 'all' }
            },
            grid: [
                { left: '10%', right: '8%', height: '65%' },
                { left: '10%', right: '8%', top: '75%', height: '20%' }
            ],
            xAxis: [
                {
                    type: 'category',
                    data: priceData.map(item => item[0]),
                    boundaryGap: false,
                    axisLine: { lineStyle: { color: '#8B949E' } },
                    axisLabel: {
                        color: '#8B949E',
                        interval: 30 // 每30个标签显示一个
                    }
                },
                {
                    type: 'category',
                    gridIndex: 1,
                    data: volumeData.map(item => item[0]),
                    axisLine: { lineStyle: { color: '#8B949E' } },
                    axisLabel: {
                        color: '#8B949E',
                        interval: 30
                    }
                }
            ],
            yAxis: [
                {
                    type: 'value',
                    scale: true,
                    min: function(value) {
                        return value.min - 0.5;
                    },
                    max: function(value) {
                        return value.max + 0.5;
                    },
                    axisLine: { lineStyle: { color: '#8B949E' } },
                    splitLine: { lineStyle: { color: '#30363D' } }
                },
                {
                    type: 'value',
                    gridIndex: 1,
                    axisLine: { show: false },
                    axisTick: { show: false },
                    axisLabel: { show: false },
                    splitLine: { show: false }
                }
            ],
            series: [
                {
                    name: '分时',
                    type: 'line',
                    smooth: false,
                    data: priceData.map(item => item[1]),
                    showSymbol: false,
                    lineStyle: {
                        width: 1,
                        color: '#00AFFF'
                    },
                    areaStyle: {
                        color: {
                            type: 'linear',
                            x: 0,
                            y: 0,
                            x2: 0,
                            y2: 1,
                            colorStops: [{
                                offset: 0, color: 'rgba(0, 175, 255, 0.3)'
                            }, {
                                offset: 1, color: 'rgba(0, 175, 255, 0.05)'
                            }]
                        }
                    }
                },
                {
                    name: '成交量',
                    type: 'bar',
                    xAxisIndex: 1,
                    yAxisIndex: 1,
                    data: volumeData.map(item => item[1]),
                    itemStyle: {
                        color: '#00AFFF',
                        opacity: 0.7
                    }
                }
            ]
        };
        
        myChart.setOption(option);
        window.addEventListener('resize', () => myChart.resize());
    }

    // === 模拟实时数据和DOM更新 ===
    let simulationInterval;
    function initRealtimeDataSimulation() {
        if (simulationInterval) clearInterval(simulationInterval);
      
        let currentPrice = 255.88;
        const sellOrdersEl = document.getElementById('sell-orders');
        const buyOrdersEl = document.getElementById('buy-orders');
        const tickListEl = document.getElementById('tick-list-container');
        const priceDivider = document.querySelector('.current-price-divider');

        function updateOrderBook() {
            sellOrdersEl.innerHTML = '';
            buyOrdersEl.innerHTML = '';
            for (let i = 5; i >= 1; i--) {
                const price = currentPrice + i * 0.01 + Math.random() * 0.02;
                sellOrdersEl.innerHTML += `
                    <li class="order-list-item">
                        <span class="order-label">卖${i}</span>
                        <span class="order-price negative">${price.toFixed(2)}</span>
                        <span class="order-volume">${Math.floor(Math.random() * 200) + 10}</span>
                    </li>`;
            }
            for (let i = 1; i <= 5; i++) {
                const price = currentPrice - i * 0.01 - Math.random() * 0.02;
                buyOrdersEl.innerHTML += `
                    <li class="order-list-item">
                        <span class="order-label">买${i}</span>
                        <span class="order-price positive">${price.toFixed(2)}</span>
                        <span class="order-volume">${Math.floor(Math.random() * 300) + 20}</span>
                    </li>`;
            }
        }

        function addTick() {
            const lastPrice = currentPrice;
            const priceChange = (Math.random() - 0.5) * 0.1;
            currentPrice += priceChange;
          
            const colorClass = currentPrice > lastPrice ? 'positive' : 'negative';
            const time = new Date().toTimeString().slice(0, 8);
            const volume = Math.floor(Math.random() * 100) + 1;
          
            const tickItem = document.createElement('li');
            tickItem.className = 'tick-item';
            tickItem.innerHTML = `
                <span class="time">${time}</span>
                <span class="price ${colorClass}">${currentPrice.toFixed(2)}</span>
                <span class="volume">${volume}</span>`;
          
            tickListEl.prepend(tickItem);
            if (tickListEl.children.length > 50) {
                tickListEl.lastChild.remove();
            }
          
            // 更新中间分割线价格
            if(priceDivider) priceDivider.textContent = currentPrice.toFixed(2);
        }

        updateOrderBook(); // 首次加载
      
        simulationInterval = setInterval(() => {
            addTick();
            // 盘口不需要每秒刷新那么快，可以设置一个计数器
            if (new Date().getSeconds() % 5 === 0) {
                updateOrderBook();
            }
        }, 1500); // 1.5秒刷新一次分时成交
    }
  
    // 初始化时加载默认页面
    initMainKLineChart('daily');
    setupDashboardTimeSelector();
});

// === 登录状态管理 ===
function checkLoginStatus() {
    const isLoggedIn = localStorage.getItem('isLoggedIn');
    if (isLoggedIn !== 'true') {
        // 如果未登录，重定向到登录页面
        window.location.href = 'login.html';
        return false;
    }
  
    // 如果已登录，更新用户信息显示
    const userEmail = localStorage.getItem('userEmail');
    if (userEmail) {
        const userProfileElements = document.querySelectorAll('.user-profile');
        userProfileElements.forEach(element => {
            // 可以在这里更新用户头像或显示用户信息
            console.log('当前登录用户:', userEmail);
        });
    }
  
    return true;
}

// 退出登录功能
function logout() {
    // 显示确认对话框
    if (confirm('确定要退出登录吗？')) {
        // 调用后端API退出登录
        logoutApi()
            .then(() => {
                console.log('退出登录成功');
            })
            .catch(error => {
                console.error('退出登录API调用失败:', error);
            })
            .finally(() => {
                // 清除登录状态
                localStorage.removeItem('isLoggedIn');
                localStorage.removeItem('userEmail');
                localStorage.removeItem('loginTime');
                localStorage.removeItem('token');
              
                // 显示退出成功通知
                showNotification('已成功退出登录', 'success');
              
                // 延迟跳转到登录页面
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 1000);
            });
    }
}

// 显示通知函数（与auth.js中的函数类似）
function showNotification(message, type = 'success') {
    // 移除现有通知
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }
  
    // 创建新通知
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
  
    const icon = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';
  
    notification.innerHTML = `
        <div class="notification-content">
            <i class="fas ${icon}"></i>
            <span>${message}</span>
        </div>
    `;
  
    document.body.appendChild(notification);
  
    // 显示通知
    setTimeout(() => {
        notification.classList.add('show');
    }, 100);
  
    // 自动隐藏通知
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 3000);
}

// === 设置页面初始化 ===
function setupSettingsPage() {
    // 显示用户邮箱
    const userEmail = localStorage.getItem('userEmail');
    const userEmailDisplay = document.getElementById('userEmailDisplay');
    if (userEmail && userEmailDisplay) {
        userEmailDisplay.value = userEmail;
    }
  
    // 显示登录时间
    const loginTime = localStorage.getItem('loginTime');
    const loginTimeDisplay = document.getElementById('loginTime');
    if (loginTime && loginTimeDisplay) {
        const loginDate = new Date(loginTime);
        loginTimeDisplay.textContent = loginDate.toLocaleString('zh-CN');
    } else if (loginTimeDisplay) {
        // 如果没有登录时间，使用当前时间
        const now = new Date();
        localStorage.setItem('loginTime', now.toISOString());
        loginTimeDisplay.textContent = now.toLocaleString('zh-CN');
    }
  
    // 绑定设置项的变更事件
    const settingsSelects = document.querySelectorAll('.settings-section select');
    settingsSelects.forEach(select => {
        select.addEventListener('change', function() {
            const settingName = this.previousElementSibling.textContent;
            const settingValue = this.value;
            console.log(`设置变更: ${settingName} = ${settingValue}`);
          
            // 这里可以添加保存设置到localStorage的逻辑
            localStorage.setItem(`setting_${settingName}`, settingValue);
          
            // 显示保存成功通知
            showNotification('设置已保存', 'success');
        });
    });
  
    // 绑定开关按钮的变更事件
    const toggleSwitches = document.querySelectorAll('.settings-section input[type="checkbox"]');
    toggleSwitches.forEach(toggle => {
        toggle.addEventListener('change', function() {
            const settingName = this.closest('.setting-item').querySelector('label').textContent;
            const settingValue = this.checked;
            console.log(`设置变更: ${settingName} = ${settingValue}`);
          
            // 保存设置到localStorage
            localStorage.setItem(`setting_${settingName}`, settingValue);
          
            // 显示保存成功通知
            showNotification('设置已保存', 'success');
        });
    });
}

// === 我的自选页面初始化 ===
function setupWatchlistPage() {
    console.log('我的自选页面已加载');
    
    // 加载自选股数据
    getWatchlist()
        .then(response => {
            console.log('自选股数据:', response.data);
            updateWatchlistTable(response.data);
        })
        .catch(error => {
            console.error('加载自选股失败:', error);
            // 使用模拟数据作为后备
            updateWatchlistTable(getMockWatchlistTable());
        });
    
    // 初始化自选股行业分布图表
    const sectorChartDom = document.getElementById('watchlistSectorChart');
    if (sectorChartDom) {
        const sectorChart = echarts.init(sectorChartDom);
        activeCharts.push(sectorChart);
        
        const sectorOption = {
            backgroundColor: 'transparent',
            tooltip: {
                trigger: 'item',
                backgroundColor: 'rgba(22, 27, 34, 0.9)',
                borderColor: '#30363D',
                textStyle: { color: '#C9D1D9' }
            },
            series: [
                {
                    name: '行业分布',
                    type: 'pie',
                    radius: ['40%', '70%'],
                    avoidLabelOverlap: false,
                    itemStyle: {
                        borderRadius: 10,
                        borderColor: '#0D1117',
                        borderWidth: 2
                    },
                    label: {
                        show: false,
                        position: 'center'
                    },
                    emphasis: {
                        label: {
                            show: true,
                            fontSize: 14,
                            fontWeight: 'bold'
                        }
                    },
                    data: [
                        { value: 35, name: '科技', itemStyle: { color: '#00AFFF' } },
                        { value: 25, name: '金融', itemStyle: { color: '#00B894' } },
                        { value: 20, name: '消费', itemStyle: { color: '#F39C12' } },
                        { value: 15, name: '医疗', itemStyle: { color: '#6F42C1' } },
                        { value: 5, name: '能源', itemStyle: { color: '#D73A49' } }
                    ]
                }
            ]
        };
        
        sectorChart.setOption(sectorOption);
        window.addEventListener('resize', () => sectorChart.resize());
    }
    
    // 初始化自选股涨跌分布图表
    const performanceChartDom = document.getElementById('watchlistPerformanceChart');
    if (performanceChartDom) {
        const performanceChart = echarts.init(performanceChartDom);
        activeCharts.push(performanceChart);
        
        const performanceOption = {
            backgroundColor: 'transparent',
            tooltip: {
                trigger: 'axis',
                backgroundColor: 'rgba(22, 27, 34, 0.9)',
                borderColor: '#30363D',
                textStyle: { color: '#C9D1D9' }
            },
            grid: {
                left: '10%',
                right: '8%',
                top: '15%',
                bottom: '15%'
            },
            xAxis: {
                type: 'category',
                data: ['贵州茅台', '宁德时代', '比亚迪', '五粮液', '招商银行'],
                axisLine: { lineStyle: { color: '#8B949E' } },
                axisLabel: {
                    color: '#8B949E',
                    interval: 0,
                    rotate: 30
                }
            },
            yAxis: {
                type: 'value',
                axisLine: { lineStyle: { color: '#8B949E' } },
                splitLine: { lineStyle: { color: '#30363D' } },
                axisLabel: {
                    color: '#8B949E',
                    formatter: '{value}%'
                }
            },
            series: [
                {
                    name: '涨跌幅',
                    type: 'bar',
                    data: [
                        { value: 1.5, itemStyle: { color: '#00B894' } },
                        { value: -2.1, itemStyle: { color: '#D63031' } },
                        { value: 0.88, itemStyle: { color: '#00B894' } },
                        { value: 0.65, itemStyle: { color: '#00B894' } },
                        { value: -0.35, itemStyle: { color: '#D63031' } }
                    ],
                    label: {
                        show: true,
                        position: 'top',
                        formatter: '{c}%'
                    }
                }
            ]
        };
        
        performanceChart.setOption(performanceOption);
        window.addEventListener('resize', () => performanceChart.resize());
    }
}

// === 更新自选股表格 ===
function updateWatchlistTable(data) {
    const tbody = document.querySelector('.watchlist-table tbody');
    if (tbody && data) {
        tbody.innerHTML = '';
        data.forEach(stock => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${stock.code}</td>
                <td>${stock.name}</td>
                <td>${stock.current.toFixed(2)}</td>
                <td class="${stock.changePercent >= 0 ? 'positive' : 'negative'}">${stock.changePercent >= 0 ? '+' : ''}${stock.changePercent.toFixed(2)}%</td>
                <td>${stock.volume}</td>
                <td>
                    <button class="icon-btn" onclick="viewStockDetail('${stock.code}')"><i class="fas fa-chart-line"></i></button>
                    <button class="icon-btn" onclick="handleRemoveFromWatchlist('${stock.code}')"><i class="fas fa-trash"></i></button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }
}

// === 智能投顾页面初始化 ===
function setupAdvisorPage() {
    console.log('智能投顾页面已加载');
    
    // 初始化投资组合分析图表
    const portfolioChartDom = document.getElementById('portfolioAnalysisChart');
    if (portfolioChartDom) {
        const portfolioChart = echarts.init(portfolioChartDom);
        activeCharts.push(portfolioChart);
        
        const portfolioOption = {
            backgroundColor: 'transparent',
            tooltip: {
                trigger: 'axis',
                backgroundColor: 'rgba(22, 27, 34, 0.9)',
                borderColor: '#30363D',
                textStyle: { color: '#C9D1D9' }
            },
            legend: {
                data: ['当前组合', '建议组合'],
                textStyle: { color: '#8B949E' },
                top: 10
            },
            radar: {
                indicator: [
                    { name: '成长性', max: 100 },
                    { name: '价值性', max: 100 },
                    { name: '稳定性', max: 100 },
                    { name: '收益性', max: 100 },
                    { name: '流动性', max: 100 }
                ],
                radius: '65%',
                splitNumber: 4,
                axisLine: {
                    lineStyle: {
                        color: '#8B949E'
                    }
                },
                splitLine: {
                    lineStyle: {
                        color: '#30363D'
                    }
                },
                splitArea: {
                    show: false
                },
                axisName: {
                    color: '#8B949E'
                }
            },
            series: [
                {
                    name: '当前组合',
                    type: 'radar',
                    data: [
                        {
                            value: [85, 70, 90, 75, 80],
                            name: '当前组合',
                            areaStyle: {
                                color: 'rgba(0, 175, 255, 0.4)'
                            },
                            lineStyle: {
                                color: '#00AFFF',
                                width: 2
                            }
                        }
                    ]
                },
                {
                    name: '建议组合',
                    type: 'radar',
                    data: [
                        {
                            value: [90, 80, 85, 85, 90],
                            name: '建议组合',
                            areaStyle: {
                                color: 'rgba(0, 184, 148, 0.4)'
                            },
                            lineStyle: {
                                color: '#00B894',
                                width: 2
                            }
                        }
                    ]
                }
            ]
        };
        
        portfolioChart.setOption(portfolioOption);
        window.addEventListener('resize', () => portfolioChart.resize());
    }
}

// === AI模型管理页面初始化 ===
function setupAIModelsPage() {
    console.log('AI模型管理页面已加载');
    
    // 初始化模型性能对比图表
    const performanceChartDom = document.getElementById('modelPerformanceChart');
    if (performanceChartDom) {
        const performanceChart = echarts.init(performanceChartDom);
        activeCharts.push(performanceChart);
        
        const performanceOption = {
            backgroundColor: 'transparent',
            tooltip: {
                trigger: 'axis',
                backgroundColor: 'rgba(22, 27, 34, 0.9)',
                borderColor: '#30363D',
                textStyle: { color: '#C9D1D9' }
            },
            legend: {
                data: ['增长模型 V2', '技术分析模型 V1', '持仓分析模型 V3', '情绪分析模型'],
                textStyle: { color: '#8B949E' },
                top: 10
            },
            grid: {
                left: '10%',
                right: '8%',
                top: '15%',
                bottom: '15%'
            },
            xAxis: {
                type: 'category',
                data: ['1月', '2月', '3月', '4月', '5月', '6月'],
                axisLine: { lineStyle: { color: '#8B949E' } },
                axisLabel: { color: '#8B949E' }
            },
            yAxis: {
                type: 'value',
                name: '准确率(%)',
                min: 50,
                max: 100,
                axisLine: { lineStyle: { color: '#8B949E' } },
                splitLine: { lineStyle: { color: '#30363D' } },
                axisLabel: { color: '#8B949E' }
            },
            series: [
                {
                    name: '增长模型 V2',
                    type: 'line',
                    data: [72, 75, 78, 78.5, 79, 78.5],
                    smooth: true,
                    lineStyle: { width: 2, color: '#00AFFF' },
                    symbol: 'circle',
                    symbolSize: 6
                },
                {
                    name: '技术分析模型 V1',
                    type: 'line',
                    data: [68, 70, 71, 72.3, 73, 72.3],
                    smooth: true,
                    lineStyle: { width: 2, color: '#00B894' },
                    symbol: 'circle',
                    symbolSize: 6
                },
                {
                    name: '持仓分析模型 V3',
                    type: 'line',
                    data: [78, 79, 80, 81.2, 82, 81.2],
                    smooth: true,
                    lineStyle: { width: 2, color: '#F39C12' },
                    symbol: 'circle',
                    symbolSize: 6
                },
                {
                    name: '情绪分析模型',
                    type: 'line',
                    data: [60, 62, 64, 65.8, 67, 65.8],
                    smooth: true,
                    lineStyle: { width: 2, color: '#6F42C1' },
                    symbol: 'circle',
                    symbolSize: 6
                }
            ]
        };
        
        performanceChart.setOption(performanceOption);
        window.addEventListener('resize', () => performanceChart.resize());
    }
    
    // 初始化模型使用统计图表
    const usageChartDom = document.getElementById('modelUsageChart');
    if (usageChartDom) {
        const usageChart = echarts.init(usageChartDom);
        activeCharts.push(usageChart);
        
        const usageOption = {
            backgroundColor: 'transparent',
            tooltip: {
                trigger: 'axis',
                backgroundColor: 'rgba(22, 27, 34, 0.9)',
                borderColor: '#30363D',
                textStyle: { color: '#C9D1D9' }
            },
            legend: {
                data: ['增长模型 V2', '技术分析模型 V1', '持仓分析模型 V3', '情绪分析模型'],
                textStyle: { color: '#8B949E' },
                top: 10
            },
            grid: {
                left: '10%',
                right: '8%',
                top: '15%',
                bottom: '15%'
            },
            xAxis: {
                type: 'category',
                data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
                axisLine: { lineStyle: { color: '#8B949E' } },
                axisLabel: { color: '#8B949E' }
            },
            yAxis: {
                type: 'value',
                name: '使用次数',
                axisLine: { lineStyle: { color: '#8B949E' } },
                splitLine: { lineStyle: { color: '#30363D' } },
                axisLabel: { color: '#8B949E' }
            },
            series: [
                {
                    name: '增长模型 V2',
                    type: 'bar',
                    stack: 'total',
                    data: [120, 132, 101, 134, 90, 80],
                    itemStyle: { color: '#00AFFF' }
                },
                {
                    name: '技术分析模型 V1',
                    type: 'bar',
                    stack: 'total',
                    data: [220, 182, 191, 234, 290, 210],
                    itemStyle: { color: '#00B894' }
                },
                {
                    name: '持仓分析模型 V3',
                    type: 'bar',
                    stack: 'total',
                    data: [150, 232, 201, 154, 190, 120],
                    itemStyle: { color: '#F39C12' }
                },
                {
                    name: '情绪分析模型',
                    type: 'bar',
                    stack: 'total',
                    data: [80, 92, 91, 94, 90, 70],
                    itemStyle: { color: '#6F42C1' }
                }
            ]
        };
        
        usageChart.setOption(usageOption);
        window.addEventListener('resize', () => usageChart.resize());
    }
}

// === 模拟数据函数 ===
function getMockMarketIndices() {
    return [
        { name: '上证指数', current: 3145.80, change: 12.50, changePercent: 0.40 },
        { name: '深证成指', current: 10480.11, change: -25.30, changePercent: -0.24 },
        { name: '恒生指数', current: 18500.20, change: 210.70, changePercent: 1.15 }
    ];
}

function getMockWatchlist() {
    return [
        { name: '贵州茅台', code: '600519', current: 1850.00, change: 27.50, changePercent: 1.50 },
        { name: '宁德时代', code: '300750', current: 218.50, change: -4.70, changePercent: -2.10 },
        { name: '比亚迪', code: '002594', current: 255.88, change: 2.23, changePercent: 0.88 }
    ];
}

function getMockWatchlistTable() {
    return [
        { name: '贵州茅台', code: '600519', current: 1850.00, change: 27.50, changePercent: 1.50, volume: '2.3万' },
        { name: '宁德时代', code: '300750', current: 218.50, change: -4.70, changePercent: -2.10, volume: '5.8万' },
        { name: '比亚迪', code: '002594', current: 255.88, change: 2.23, changePercent: 0.88, volume: '15.2万' },
        { name: '五粮液', code: '000858', current: 168.50, change: 1.08, changePercent: 0.65, volume: '3.2万' },
        { name: '招商银行', code: '600036', current: 42.30, change: -0.15, changePercent: -0.35, volume: '8.7万' }
    ];
}

// === 全局函数 ===
function viewStockDetail(stockCode) {
    // 切换到股票详情页面
    const marketMenuItem = document.querySelector('[data-page="market"]');
    if (marketMenuItem) {
        marketMenuItem.click();
    }
    
    // 这里可以加载特定股票的详细信息
    console.log('查看股票详情:', stockCode);
}

function handleRemoveFromWatchlist(stockCode) {
    if (confirm(`确定要将 ${stockCode} 从自选股中移除吗？`)) {
        // 调用API移除自选股
        removeFromWatchlist(stockCode)
            .then(() => {
                showNotification('已从自选股中移除', 'success');
                // 重新加载自选股数据
                setupWatchlistPage();
            })
            .catch(error => {
                console.error('移除自选股失败:', error);
                showNotification('移除失败，请重试', 'error');
            });
    }
}
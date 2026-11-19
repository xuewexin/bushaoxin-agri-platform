<template>
  <div class="taobao-root">
    <!-- ============================================================== -->
    <!-- 1. 顶部全局功能条 (精简单行，字体11px小巧防折行，仿淘宝顶级质感)  -->
    <!-- ============================================================== -->
    <header class="pc-top-bar">
      <div class="site-container top-bar-inner">
        <div class="top-bar-left">
          <!-- 城市省份切换 (仿淘宝区域定位) -->
          <div class="region-dropdown">
            <el-dropdown @command="handleRegionChange">
              <span class="region-pill interactive">
                <el-icon><Location /></el-icon> {{ currentRegion }} [切换]
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="r in ['北京', '上海', '广州', '深圳', '杭州', '成都', '武汉', '西安', '南京', '重庆']" :key="r" :command="r">
                    {{ r }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <span class="brand-slogan-pill">
            <el-icon><CircleCheckFilled /></el-icon> 好产品，不烧心 · 源头直发
          </span>
          <!-- 登录/未登录状态 -->
          <div class="auth-bar-status">
            <template v-if="!isLoggedIn">
              <span class="login-trigger" @click="openAuthModal('login')">亲，请登录</span>
              <span class="register-trigger" @click="openAuthModal('register')">免费注册</span>
            </template>
            <template v-else>
              <span class="welcome-text">您好，<strong>{{ currentUser.realName.replace('不烧心金牌吃客', '') || currentUser.realName }}</strong></span>
              <span class="role-mini-pill">{{ getRoleName(currentRole) }}</span>
              <span class="logout-btn" @click="handleLogout">[退出]</span>
            </template>
          </div>
        </div>

        <div class="top-bar-right">
          <!-- 淘宝式快捷菜单 -->
          <div class="top-quick-links">
            <span class="quick-link" @click="activeTab = 'workbench'">我的淘宝</span>
            <span class="quick-link" @click="activeTab = 'cart'">
              购物车 <strong class="c-red">({{ cartList.length }})</strong>
            </span>
            <span class="quick-link" @click="couponCenterVisible = true">
              <el-icon><Ticket /></el-icon> 领券中心
            </span>
            <span class="quick-link" @click="wangwangVisible = true">
              <el-icon><Service /></el-icon> 掌柜客服
            </span>
            <!-- 手机逛二维码弹层 -->
            <el-popover placement="bottom" :width="180" trigger="hover">
              <template #reference>
                <span class="quick-link mobile-link">
                  <el-icon><Iphone /></el-icon> 手机逛
                </span>
              </template>
              <div class="qr-popover-box">
                <img src="https://api.qrserver.com/v1/create-qr-code/?size=130x130&data=http://localhost:3000" class="qr-img" />
                <div class="qr-text">扫码手机看直播</div>
                <div class="qr-sub">好产品，不烧心</div>
              </div>
            </el-popover>
          </div>

          <!-- 快捷身份模拟切换器 (6种角色，紧凑设计) -->
          <div class="role-switch-box">
            <span class="role-title"><el-icon><Switch /></el-icon> 角色:</span>
            <el-select v-model="currentRole" size="small" class="role-select-compact" style="width: 104px;" @change="handleFastRoleSwitch">
              <el-option label="普通买家" value="CONSUMER" />
              <el-option label="种植合作社" value="COOP" />
              <el-option label="产地主播" value="ANCHOR" />
              <el-option label="采购批发商" value="WHOLESALE" />
              <el-option label="平台运营" value="ADMIN" />
              <el-option label="农技质检" value="EXPERT" />
            </el-select>
          </div>

          <!-- 默认卡通用户头像 -->
          <div class="user-profile-badge" @click="openAuthModal('login')" :title="currentUser.realName">
            <el-avatar :size="24" :src="currentUser.avatar" class="cartoon-avatar" />
          </div>
        </div>
      </div>
    </header>

    <!-- ============================================================== -->
    <!-- 2. 仿淘宝经典主头部 (官方适配 Logo: 不烧心·好产品不烧心)        -->
    <!-- ============================================================== -->
    <div class="pc-header">
      <div class="site-container header-inner">
        <!-- 品牌官方适配 Logo -->
        <div class="brand-logo" @click="resetAllFilterAndGoHome">
          <img src="/logo.svg" alt="不烧心 - 好产品，不烧心" class="brand-logo-img" />
        </div>

        <!-- 仿淘宝中央大搜索框 (即刻响应 + 结果提示) -->
        <div class="main-search-wrapper">
          <div class="search-input-group">
            <div class="search-type-select">
              <el-select v-model="searchType" size="large">
                <el-option label="找农货" value="product" />
                <el-option label="搜大宗" value="wholesale" />
                <el-option label="寻产地" value="origin" />
              </el-select>
            </div>
            <input 
              type="text" 
              v-model="searchKeyword" 
              class="search-input"
              placeholder="搜索地理标志农货，如：苹果、西红柿、大米、羊肉、大闸蟹..." 
              @keyup.enter="triggerSearch"
            />
            <button class="search-submit-btn" @click="triggerSearch">
              <el-icon><Search /></el-icon> 搜 索
            </button>
          </div>

          <!-- 淘宝热搜词推荐 (丰富扩充展开，填补顶部横向空白) -->
          <div class="search-hot-tags">
            <span class="hot-tag active" @click="quickSearchTag('苹果')">凉山丑苹果</span>
            <span class="hot-tag" @click="quickSearchTag('西红柿')">寿光铁皮番茄</span>
            <span class="hot-tag active" @click="quickSearchTag('大米')">五常大米</span>
            <span class="hot-tag" @click="quickSearchTag('羊肉')">苏尼特羔羊肉</span>
            <span class="hot-tag active" @click="quickSearchTag('大闸蟹')">阳澄湖大闸蟹</span>
            <span class="hot-tag" @click="quickSearchTag('草莓')">丹东99草莓</span>
            <span class="hot-tag active" @click="quickSearchTag('枸杞')">中宁红枸杞</span>
            <span class="hot-tag" @click="quickSearchTag('鲟鱼')">抚远鲟鱼子</span>
            <span class="hot-tag badge-tag" @click="filterPresaleOnly">
              <el-icon><Calendar /></el-icon> 田间预售
            </span>
            <span class="hot-tag" @click="activeTab = 'wholesale'">大宗吨批直采</span>
          </div>
        </div>

        <!-- 右侧个人快捷入口与购物车按钮 -->
        <div class="header-fast-entry">
          <div class="fast-btn" @click="activeTab = 'workbench'">
            <el-icon :size="24"><UserFilled /></el-icon>
            <span>工作台</span>
          </div>
          <!-- 购物车入口：点击直接切换到专属购物车独立页面 -->
          <div class="fast-btn cart-btn" :class="{ current: activeTab === 'cart' }" @click="activeTab = 'cart'">
            <el-badge :value="cartList.length" class="cart-badge">
              <el-icon :size="24"><ShoppingCartFull /></el-icon>
            </el-badge>
            <span>购物车</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ============================================================== -->
    <!-- 3. 仿淘宝经典一级导航栏 (含专属购物车 Tab)                     -->
    <!-- ============================================================== -->
    <nav class="pc-main-nav">
      <div class="site-container nav-inner">
        <div class="all-category-head">
          <el-icon><Grid /></el-icon> 全部 6 大品种分类
        </div>
        <ul class="nav-menu">
          <li :class="{ current: activeTab === 'home' }" @click="activeTab = 'home'">
            <span>精选产地货架</span>
          </li>
          <li :class="{ current: activeTab === 'live' }" @click="activeTab = 'live'">
            <span class="live-pulse-dot"></span>
            <span>产地实景直播间</span>
          </li>
          <li :class="{ current: activeTab === 'wholesale' }" @click="activeTab = 'wholesale'">
            <span class="badge-b2b">B2B</span>
            <span>大宗采购对接中心</span>
          </li>
          <li :class="{ current: activeTab === 'trace' }" @click="activeTab = 'trace'">
            <el-icon><CircleCheckFilled /></el-icon>
            <span>一物一码真实溯源</span>
          </li>
          <li :class="{ current: activeTab === 'cart' }" @click="activeTab = 'cart'">
            <el-icon><ShoppingCart /></el-icon>
            <span>购物车 ({{ cartList.length }})</span>
          </li>
          <li :class="{ current: activeTab === 'workbench' }" @click="activeTab = 'workbench'">
            <el-icon><Operation /></el-icon>
            <span>{{ getRoleWorkbenchName() }}</span>
          </li>
        </ul>
      </div>
    </nav>

    <!-- ============================================================== -->
    <!-- 3.1 淘宝式【天天领红包·开市盛典】促销通栏                        -->
    <!-- ============================================================== -->
    <div class="taobao-redpack-banner" @click="couponCenterVisible = true">
      <div class="site-container redpack-flex">
        <div class="redpack-left">
          <span class="redpack-badge">🧧 天天领红包</span>
          <span class="redpack-title">【不烧心·开市农品盛典】全场顺丰冷链包邮直发 ｜ 领 ¥88 农品满减消费券包 ｜ SGS 320项农残零检出安心购！</span>
        </div>
        <div class="redpack-right">
          <button class="claim-btn-pulse">立即一键领券 &gt;</button>
        </div>
      </div>
    </div>

    <!-- ============================================================== -->
    <!-- 4. 移动端专属顶部吸顶条 (≤768px 自适应显示)                     -->
    <!-- ============================================================== -->
    <div class="mobile-top-header">
      <div class="mobile-header-row">
        <div class="mobile-logo" @click="resetAllFilterAndGoHome">
          <img src="/logo.svg" alt="不烧心" style="height: 28px;" />
        </div>
        <div class="mobile-search-box">
          <el-icon><Search /></el-icon>
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="好产品，不烧心 - 搜产地好物..." 
            @keyup.enter="triggerSearch"
          />
        </div>
        <div class="mobile-role-badge" @click="openAuthModal('login')">
          <el-avatar :size="24" :src="currentUser.avatar" />
        </div>
      </div>
    </div>

    <!-- ============================================================== -->
    <!-- 5. 主体核心多视图切换                                           -->
    <!-- ============================================================== -->
    <main class="site-container main-body">

      <!-- ------------------------------------------------------------ -->
      <!-- TAB 1: 仿淘宝首页 (首屏 + 轮播 + 百亿补贴 + 37款海量农货)    -->
      <!-- ------------------------------------------------------------ -->
      <div v-if="activeTab === 'home'" class="view-home">
        
        <!-- 移动端金刚区导航 (≤768px 展示) -->
        <div class="mobile-kong-container">
          <div class="kong-item" @click="selectCategory('水果')">
            <div class="kong-icon red"><el-icon><Apple /></el-icon></div>
            <span>时令鲜果 (12)</span>
          </div>
          <div class="kong-item" @click="selectCategory('蔬菜')">
            <div class="kong-icon green"><el-icon><Dish /></el-icon></div>
            <span>绿色蔬菜 (7)</span>
          </div>
          <div class="kong-item" @click="selectCategory('粮油')">
            <div class="kong-icon orange"><el-icon><Box /></el-icon></div>
            <span>五谷原粮 (5)</span>
          </div>
          <div class="kong-item" @click="selectCategory('滋补')">
            <div class="kong-icon purple"><el-icon><Grape /></el-icon></div>
            <span>特色滋补 (5)</span>
          </div>
          <div class="kong-item" @click="activeTab = 'cart'">
            <div class="kong-icon blue"><el-icon><ShoppingCartFull /></el-icon></div>
            <span>购物车 ({{ cartList.length }})</span>
          </div>
        </div>

        <!-- 淘宝首屏三栏架构 (PC端经典布局: 左分类 + 中大轮播 + 右个人头条) -->
        <div class="home-hero-section">
          <!-- 左栏: 6大品种分类树 (展示样品数量) -->
          <div class="hero-category-side">
            <div 
              v-for="cat in categoryList" 
              :key="cat.name" 
              class="cat-row" 
              :class="{ active: currentCategory === cat.name }"
              @click="selectCategory(cat.name)"
            >
              <div class="cat-left">
                <el-icon v-if="cat.name === '水产'" class="seafood-icon">
                  <svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor">
                    <path d="M893.3 540.8c-24.1-13.7-44.5-30.8-62.1-50.5-22.1-24.8-48.4-44.7-77.8-59.1-39.7-19.5-83.3-29.6-127.8-29.6-67.4 0-131.6 23.3-182.9 66.4l-118-99.3c-14.8-12.5-36.9-10.7-49.4 4.1l-10.4 12.3c-12.5 14.8-10.7 36.9 4.1 49.4l98.6 83c-23.7 29.5-39.8 63.8-46.7 100.8H184c-19.3 0-35 15.7-35 35v8c0 19.3 15.7 35 35 35h137c6.9 37 23 71.3 46.7 100.8l-98.6 83c-14.8 12.5-16.6 34.6-4.1 49.4l10.4 12.3c12.5 14.8 34.6 16.6 49.4 4.1l118-99.3c51.3 43.1 115.5 66.4 182.9 66.4 44.5 0 88.1-10.1 127.8-29.6 29.4-14.4 55.7-34.3 77.8-59.1 17.6-19.7 38-36.8 62.1-50.5 24.3-13.8 39.5-39.4 39.5-67.5s-15.2-53.7-39.5-67.5zM656 464c22.1 0 40 17.9 40 40s-17.9 40-40 40-40-17.9-40-40 17.9-40 40-40z"/>
                  </svg>
                </el-icon>
                <el-icon v-else><component :is="cat.icon" /></el-icon>
                <span class="cat-name">{{ cat.name }}</span>
              </div>
              <span class="cat-sub">{{ cat.sub }}</span>
            </div>
            <div class="cat-banner-mini" @click="filterPresaleOnly">
              <el-icon><Timer /></el-icon>
              <span>田间直发·预售排期</span>
            </div>
          </div>

          <!-- 中栏: 仿淘宝高级焦点轮播大图 (Carousel) -->
          <div class="hero-carousel-center">
            <el-carousel trigger="click" height="340px" :interval="4500" arrow="hover">
              <el-carousel-item v-for="(banner, index) in heroBanners" :key="index">
                <div class="banner-slide" :style="{ backgroundImage: 'url(' + banner.image + ')' }">
                  <div class="banner-mask">
                    <span class="banner-tag">{{ banner.tag }}</span>
                    <h2 class="banner-heading">{{ banner.title }}</h2>
                    <p class="banner-desc">{{ banner.desc }}</p>
                    <el-button type="danger" round size="default" class="banner-action-btn" @click="handleBannerClick(banner)">
                      {{ banner.btnText }} &gt;
                    </el-button>
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>

          <!-- 右栏: 个人淘宝卡片 (卡通头像) + 产地实时快讯滚动 -->
          <div class="hero-user-side">
            <div class="user-card-head">
              <el-avatar :size="52" :src="currentUser.avatar" class="cartoon-avatar-big" />
              <div class="user-greeting">
                <span class="hi">Hi，{{ currentUser.realName }}</span>
                <span class="role-desc">
                  身份: <el-tag size="small" :type="getRoleTagType(currentRole)">{{ getRoleName(currentRole) }}</el-tag>
                </span>
              </div>
            </div>
            <div class="user-action-grid">
              <div class="grid-cell" @click="activeTab = 'cart'">
                <el-icon :size="20"><ShoppingCart /></el-icon>
                <span>我的购物车</span>
              </div>
              <div class="grid-cell" @click="activeTab = 'wholesale'">
                <el-icon :size="20"><Van /></el-icon>
                <span>大宗集采</span>
              </div>
              <div class="grid-cell" @click="openTraceModal(1)">
                <el-icon :size="20"><Compass /></el-icon>
                <span>农残检测</span>
              </div>
            </div>

            <!-- 产地头条实时垂直跑马灯 -->
            <div class="news-marquee-box">
              <div class="news-header">
                <span class="news-title"><el-icon><BellFilled /></el-icon> 产地快讯</span>
                <span class="news-more">今日更新</span>
              </div>
              <ul class="news-list">
                <li v-for="(n, i) in newsItems" :key="i">
                  <span class="news-bullet">●</span>
                  <span class="news-text">{{ n }}</span>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 仿淘宝“百亿补贴·产地秒杀”特色专栏 (倒计时 + 进度条) -->
        <div class="flash-sale-strip">
          <div class="strip-header">
            <div class="strip-title-box">
              <span class="strip-badge">百亿补贴</span>
              <h3 class="strip-title">好产品，不烧心 · 源头特惠秒杀</h3>
              <div class="countdown-clock">
                <span class="clock-label">距本场结束</span>
                <span class="time-block">{{ countdownHours }}</span>:
                <span class="time-block">{{ countdownMinutes }}</span>:
                <span class="time-block">{{ countdownSeconds }}</span>
              </div>
            </div>
            <div class="strip-right-link" @click="selectCategory('水果')">
              <span>查看更多精品 (共 40 款)</span>
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>

          <div class="flash-sale-grid">
            <div 
              v-for="item in displayProducts.slice(0, 5)" 
              :key="'flash-' + item.id" 
              class="flash-card"
              @click="openProductDetail(item)"
            >
              <div class="flash-img-wrap">
                <img :src="item.coverImage" />
                <span class="flash-tag">补贴直降</span>
              </div>
              <div class="flash-info">
                <div class="flash-title">{{ item.title }}</div>
                <div class="flash-price-row">
                  <span class="sub-price">¥{{ (item.retailPrice * 0.85).toFixed(1) }}</span>
                  <span class="del-price">¥{{ item.retailPrice }}</span>
                </div>
                <div class="flash-progress">
                  <el-progress :percentage="76" color="#FF5000" :show-text="false" :stroke-width="6" />
                  <span class="progress-text">已抢76%</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 筛选排序栏 (展示每个品类的样品数量) -->
        <div class="product-filter-bar">
          <div class="filter-left">
            <span class="filter-label"><el-icon><Filter /></el-icon> 品类专区：</span>
            <button 
              v-for="cat in ['全部', '水果', '蔬菜', '粮油', '滋补', '禽畜', '水产']" 
              :key="cat"
              :class="['filter-btn', currentCategory === cat ? 'active' : '']"
              @click="selectCategory(cat)"
            >
              {{ cat }} <span class="cat-count">({{ getCategoryCount(cat) }})</span>
            </button>
          </div>
          <div class="filter-right">
            <span v-if="searchKeyword" class="search-result-hint">
              搜索 <strong>"{{ searchKeyword }}"</strong>：找到 <strong>{{ displayProducts.length }}</strong> 款不烧心好物
              <el-button type="danger" link size="small" @click="clearSearch"><el-icon><CircleClose /></el-icon> 清空</el-button>
            </span>
            <el-radio-group v-model="presaleFilter" size="small" @change="applyFilters">
              <el-radio-button :label="null">全部现货</el-radio-button>
              <el-radio-button :label="1">仅看预售直供</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 仿淘宝商品瀑布流展示 (展示全部丰富样品) -->
        <div v-if="displayProducts.length > 0" class="product-wall">
          <div 
            v-for="product in displayProducts" 
            :key="product.id" 
            class="taobao-goods-card"
            @click="openProductDetail(product)"
          >
            <div class="goods-img-box">
              <img :src="product.coverImage" loading="lazy" />
              <span v-if="product.isPresale === 1" class="corner-ribbon presale">田间预售</span>
              <span v-else class="corner-ribbon origin">产地现采</span>
              <div class="origin-geo-pill">
                <el-icon><LocationInformation /></el-icon>
                <span>{{ product.originPlace }}</span>
              </div>
            </div>

            <div class="goods-desc-box">
              <div class="goods-name">
                <span class="coop-badge">不烧心保真</span>
                {{ product.title }}
              </div>
              <div class="goods-sales-metrics">
                <span class="sales-num">月销 3200+ 件</span>
                <span class="rating-num">99.8% 好评</span>
                <span class="sf-pill">顺丰包邮</span>
              </div>
              <div class="goods-specs">
                <span>上市周期: {{ product.yieldCycle }}</span>
                <span>预估年产: {{ product.totalYield }}</span>
              </div>
              <div class="goods-price-section">
                <div class="retail-price">
                  <span class="rmb">券后 ¥</span>
                  <span class="main-num">{{ product.retailPrice.toString().split('.')[0] }}</span>
                  <span class="sub-num">.{{ (product.retailPrice.toString().split('.')[1] || '00') }}</span>
                  <span class="unit-label">/份</span>
                  <span class="orig-price">¥{{ (product.retailPrice * 1.25).toFixed(1) }}</span>
                </div>
                <div class="b2b-price-tip">
                  大宗: ¥{{ product.wholesalePrice }}/斤
                </div>
              </div>

              <div class="goods-footer-action">
                <span class="stock-status">库存 {{ product.stock }}{{ product.unit }}</span>
                <div class="card-action-btns">
                  <el-button 
                    type="warning" 
                    size="small" 
                    round 
                    class="add-cart-btn"
                    @click.stop="addToCart(product)"
                  >
                    <el-icon><ShoppingCart /></el-icon> 加购
                  </el-button>
                  <el-button 
                    type="danger" 
                    size="small" 
                    round 
                    class="quick-buy-btn"
                    @click.stop="quickBuyProduct(product)"
                  >
                    <el-icon><Lightning /></el-icon> 立即买
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空态提示 -->
        <div v-else class="empty-search-panel">
          <el-empty description="未检索到符合条件的农副产品，请尝试其他关键词或查看全部">
            <el-button type="primary" @click="clearSearch">重置并查看全部 40 款农产品</el-button>
          </el-empty>
        </div>

      </div>

      <!-- ------------------------------------------------------------ -->
      <!-- TAB 2: 独立购物车专属页面 (统一展示、勾选、数量计算、统一结算) -->
      <!-- ------------------------------------------------------------ -->
      <div v-if="activeTab === 'cart'" class="view-cart">
        <div class="cart-header-box">
          <div class="cart-title-row">
            <h2><el-icon><ShoppingCartFull /></el-icon> 购物车 (全部商品 {{ cartList.length }})</h2>
            <span class="cart-slogan"><el-icon><CircleCheckFilled /></el-icon> 产地冷链顺丰直发 · 好产品，不烧心</span>
          </div>
          <div class="cart-shipping-tip">
            <el-tag type="success" size="small">全场包邮</el-tag>
            <span>合作社原产地直接发货，坏果包赔，省心放心不烧心！</span>
          </div>
        </div>

        <div v-if="cartList.length > 0" class="cart-body-wrapper">
          <!-- 购物车列表卡片 -->
          <div class="cart-table-card">
            <!-- 表头 -->
            <div class="cart-table-header">
              <div class="col-check">
                <el-checkbox v-model="isAllCartSelected" @change="toggleSelectAllCart">全选</el-checkbox>
              </div>
              <div class="col-info">商品信息</div>
              <div class="col-price">单价</div>
              <div class="col-qty">数量</div>
              <div class="col-subtotal">金额小计</div>
              <div class="col-action">操作</div>
            </div>

            <!-- 商品行 -->
            <div 
              v-for="(item, index) in cartList" 
              :key="'cart-item-' + index" 
              class="cart-row-item"
            >
              <div class="col-check">
                <el-checkbox v-model="item.selected" />
              </div>
              <div class="col-info">
                <img :src="item.coverImage" class="cart-prod-thumb" />
                <div class="cart-prod-desc">
                  <div class="title">{{ item.title }}</div>
                  <div class="meta">
                    <span class="tag-origin"><el-icon><Location /></el-icon> {{ item.originPlace }}</span>
                    <span class="tag-cert">农残320项零检出</span>
                  </div>
                </div>
              </div>
              <div class="col-price">
                <span class="cart-unit-price">¥{{ item.retailPrice.toFixed(2) }}</span>
              </div>
              <div class="col-qty">
                <el-input-number v-model="item.quantity" :min="1" :max="99" size="small" />
              </div>
              <div class="col-subtotal">
                <span class="cart-subtotal-price">¥{{ (item.retailPrice * item.quantity).toFixed(2) }}</span>
              </div>
              <div class="col-action">
                <el-button type="danger" link @click="removeCartItem(index)">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </div>
            </div>
          </div>

          <!-- 购物车吸底结算栏 -->
          <div class="cart-checkout-bar">
            <div class="checkout-left">
              <el-checkbox v-model="isAllCartSelected" @change="toggleSelectAllCart">全选</el-checkbox>
              <span class="btn-clear-cart" @click="clearEntireCart">
                <el-icon><DeleteFilled /></el-icon> 清空购物车
              </span>
            </div>
            <div class="checkout-right">
              <div class="selected-count-text">
                已选商品 <strong class="highlight-num">{{ selectedCartCount }}</strong> 件
              </div>
              <div class="total-amount-box">
                <span class="label">合计 (含运费)：</span>
                <span class="currency">¥</span>
                <span class="total-money">{{ selectedCartTotalMoney }}</span>
              </div>
              <button 
                class="checkout-submit-button" 
                :disabled="selectedCartCount === 0"
                @click="submitCartCheckout"
              >
                结 算 ({{ selectedCartCount }})
              </button>
            </div>
          </div>
        </div>

        <!-- 购物车空态 -->
        <div v-else class="empty-cart-card">
          <el-empty description="您的购物车还是空的，快去挑选不烧心源头好物吧！">
            <el-button type="danger" round size="large" @click="activeTab = 'home'">
              <el-icon><Goods /></el-icon> 去挑选不烧心农产品
            </el-button>
          </el-empty>
        </div>
      </div>

      <!-- ------------------------------------------------------------ -->
      <!-- TAB 3: 仿淘宝产地直播互动大厅                                    -->
      <!-- ------------------------------------------------------------ -->
      <div v-if="activeTab === 'live'" class="view-live">
        <div class="live-theater">
          <!-- 主直播画面区域 -->
          <div class="live-stage">
            <div class="video-container" id="liveVideoContainer">
              <video 
                ref="liveStreamVideo"
                autoplay 
                loop 
                muted 
                playsinline
                class="real-stream-player"
                src="https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                poster="https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=800"
              ></video>

              <div class="live-hud-top">
                <div class="host-badge">
                  <el-avatar :size="38" src="https://api.dicebear.com/7.x/adventurer/svg?seed=SunnyAnchor" />
                  <div class="host-meta">
                    <div class="host-title">川农小李·高原实景直采带货</div>
                    <div class="host-status">
                      <span class="red-dot"></span>
                      <span>{{ onlineViewerCount }} 人正在观看 ｜ 好产品，不烧心</span>
                    </div>
                  </div>
                  <el-button 
                    :type="isFollowed ? 'info' : 'danger'" 
                    size="small" 
                    round 
                    class="btn-follow"
                    @click="isFollowed = !isFollowed"
                  >
                    {{ isFollowed ? '已关注' : '+ 关注' }}
                  </el-button>
                </div>

                <div class="live-hud-actions">
                  <button class="hud-btn" @click="enterPipMode" title="开启悬浮画中画">
                    <el-icon><CopyDocument /></el-icon> 画中画
                  </button>
                </div>
              </div>

              <!-- 主播正在讲解的商品悬浮小黄车卡片 -->
              <div v-if="explainingProduct" class="explaining-floating-card">
                <div class="floating-header">
                  <span class="pulsing-circle"></span>
                  <span class="label">主播正在讲解 · 不烧心推荐</span>
                </div>
                <div class="floating-body">
                  <img :src="explainingProduct.coverImage" />
                  <div class="floating-info">
                    <div class="prod-name">{{ explainingProduct.title }}</div>
                    <div class="prod-pricing">
                      <span class="live-tag">直播特惠</span>
                      <span class="deal-price">¥{{ (explainingProduct.retailPrice * 0.9).toFixed(2) }}</span>
                      <span class="origin-price">¥{{ explainingProduct.retailPrice }}</span>
                    </div>
                  </div>
                  <div class="floating-buttons">
                    <el-button type="success" size="small" @click="openTraceModal(explainingProduct.id)">
                      质检报告
                    </el-button>
                    <el-button type="danger" size="small" @click="triggerLiveOrder(explainingProduct)">
                      立即抢购
                    </el-button>
                  </div>
                </div>
              </div>

              <!-- 左下角公屏弹幕流 -->
              <div class="danmaku-stream" ref="danmakuBox">
                <div v-for="(msg, idx) in chatMessages" :key="idx" class="danmaku-bubble">
                  <span class="sender-name">{{ msg.user }}:</span>
                  <span class="msg-content">{{ msg.content }}</span>
                </div>
              </div>

              <!-- 右下角爱心粒子漂浮动效容器 -->
              <div class="hearts-canvas" ref="heartsContainer"></div>
            </div>

            <!-- 直播控制操作栏 -->
            <div class="live-bottom-bar">
              <div class="chat-input-row">
                <el-input 
                  v-model="inputChatText" 
                  placeholder="提问主播：农残检测报告在哪里看？支持顺丰冷链吗？" 
                  size="default" 
                  @keyup.enter="sendChat"
                >
                  <template #append>
                    <el-button type="primary" @click="sendChat">发 射</el-button>
                  </template>
                </el-input>
              </div>
              <div class="like-heart-btn" @click="emitFloatingHeart">
                <el-icon><StarFilled /></el-icon>
                <span>点赞 {{ liveLikeCount }}</span>
              </div>
            </div>
          </div>

          <!-- 右侧：小黄车货架 (37款精选) -->
          <div class="live-goods-shelf">
            <div class="shelf-top-title">
              <el-icon><ShoppingBag /></el-icon>
              <span>不烧心·直发货架 ({{ productList.length }})</span>
            </div>
            <div class="shelf-scroll-area">
              <div 
                v-for="(item, index) in productList" 
                :key="'shelf-' + item.id" 
                :class="['shelf-row-item', explainingProduct?.id === item.id ? 'active-explaining' : '']"
              >
                <div class="shelf-index">{{ index + 1 }}</div>
                <img :src="item.coverImage" class="shelf-thumb" />
                <div class="shelf-detail">
                  <div class="shelf-name">{{ item.title }}</div>
                  <div class="shelf-sub-tags">
                    <span class="geo-tag">{{ item.originPlace }}</span>
                    <span class="test-tag">农残已抽检</span>
                  </div>
                  <div class="shelf-price-action">
                    <span class="shelf-price">¥{{ (item.retailPrice * 0.9).toFixed(2) }}</span>
                    <el-button type="danger" size="small" round @click="triggerLiveOrder(item)">
                      抢 购
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ------------------------------------------------------------ -->
      <!-- TAB 4: B2B 大宗采购对接中心                                     -->
      <!-- ------------------------------------------------------------ -->
      <div v-if="activeTab === 'wholesale'" class="view-wholesale">
        <div class="wholesale-banner-card">
          <div class="banner-texts">
            <h2><el-icon><OfficeBuilding /></el-icon> 不烧心·农副产品大宗产销直采中心</h2>
            <p>好产品，不烧心 ｜ 为大型商超、全国社区团购、餐饮供应链提供源头吨级直采通道 ｜ 具有法律效力的电子意向合同</p>
          </div>
          <div class="banner-kpis">
            <div class="kpi-block">
              <span class="kpi-val">380.5 吨</span>
              <span class="kpi-txt">累计大宗对接规模</span>
            </div>
            <div class="kpi-block">
              <span class="kpi-val">100%</span>
              <span class="kpi-txt">合作社营业执照核验</span>
            </div>
          </div>
        </div>

        <el-card class="mt-4 wholesale-card">
          <div class="card-action-bar">
            <h3>可供大宗源头货源池 (覆盖6大品种共 {{ productList.length }} 款样品)</h3>
            <span class="sub-hint">支持按起订量吨级筛选，在线发起磋商并一键生成电子协议</span>
          </div>

          <el-table :data="productList" stripe style="width: 100%">
            <el-table-column label="农副产品" min-width="260">
              <template #default="{ row }">
                <div class="table-prod-box">
                  <img :src="row.coverImage" />
                  <div>
                    <div class="prod-t">{{ row.title }}</div>
                    <el-tag size="small" type="success">{{ row.category }}</el-tag>
                    <el-tag size="small" type="info">{{ row.originPlace }}</el-tag>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="originPlace" label="基地所在省市" width="140" />
            <el-table-column prop="totalYield" label="合作社年产量" width="130" />
            <el-table-column label="起订量 (MOQ)" width="130">
              <template #default="{ row }">
                <strong>{{ row.minWholesaleAmount }}</strong> 斤起
              </template>
            </el-table-column>
            <el-table-column label="大宗参考价" width="130">
              <template #default="{ row }">
                <span class="b2b-table-price">¥{{ row.wholesalePrice }}</span> / 斤
              </template>
            </el-table-column>
            <el-table-column label="品质档案" width="130">
              <template #default="{ row }">
                <el-button type="primary" link @click="openTraceModal(row.id)">
                  查阅检测报告
                </el-button>
              </template>
            </el-table-column>
            <el-table-column label="产销对接操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button type="warning" size="small" @click="openInquiryModal(row)">
                  发起大宗询价
                </el-button>
                <el-button type="info" size="small" @click="openVisitBookingModal(row)">
                  预约看样
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- ------------------------------------------------------------ -->
      <!-- TAB 5: 一物一码真实溯源                                         -->
      <!-- ------------------------------------------------------------ -->
      <div v-if="activeTab === 'trace'" class="view-trace">
        <div class="trace-hero-card">
          <h2><el-icon><CircleCheckFilled /></el-icon> 不烧心·数字化农产品产地真实性与检测档案追溯系统</h2>
          <p>好产品，不烧心！每一箱农副产品均贴有唯一的数字化防伪溯源码，全流程留痕</p>
          <div class="trace-search-bar">
            <el-input 
              v-model="inputTraceNumber" 
              placeholder="输入包装上的数字化溯源码 (如: AGRI20261101001)" 
              size="large"
              style="max-width: 520px;"
            />
            <el-button type="success" size="large" @click="loadTraceRecords(inputTraceNumber)">
              <el-icon><Search /></el-icon> 立即验真查询
            </el-button>
          </div>
        </div>

        <div v-if="traceTimelineData.length > 0" class="trace-details-card mt-4">
          <div class="trace-card-top">
            <div class="top-meta">
              <h3>【国家绿色食品认证】四川凉山高山盐源红富士苹果</h3>
              <div class="auth-chips">
                <span class="chip-green">✔ 地理标志经纬度核验通过 (101.5098°E, 27.4211°N)</span>
                <span class="chip-green">✔ SGS权威机构320项农残零检出</span>
                <span class="chip-green">✔ 好产品，不烧心品质承诺</span>
              </div>
            </div>
            <el-tag type="success" size="large" effect="dark">
              溯源码: {{ currentTraceCode }}
            </el-tag>
          </div>

          <el-divider />

          <div class="timeline-wrapper">
            <el-timeline>
              <el-timeline-item 
                v-for="item in traceTimelineData" 
                :key="item.id" 
                :timestamp="item.nodeTime" 
                placement="top"
                type="success"
              >
                <el-card shadow="hover">
                  <h4>{{ item.nodeName }} - 负责人: {{ item.operator }}</h4>
                  <p>{{ item.description }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>
      </div>

      <!-- ------------------------------------------------------------ -->
      <!-- TAB 6: 六大角色专属自适应工作台                                 -->
      <!-- ------------------------------------------------------------ -->
      <div v-if="activeTab === 'workbench'" class="view-workbench">
        
        <!-- 6.1 种植合作社专属工作台 (ROLE_COOP) -->
        <div v-if="currentRole === 'COOP'" class="coop-panel">
          <div class="workbench-bar">
            <div>
              <h2><el-icon><House /></el-icon> 种植合作社极速产销工作台</h2>
              <p>当前主体：{{ currentUser.realName }} ｜ 资质状态：<el-tag type="success">营业执照已核验</el-tag></p>
            </div>
            <el-button type="primary" size="large" @click="publishModalVisible = true">
              <el-icon><Plus /></el-icon> 手机/PC极速上架农产品
            </el-button>
          </div>

          <div class="metric-row">
            <div class="metric-card">
              <span class="metric-label">在售/预售农产品</span>
              <span class="metric-val">{{ productList.length }} <span>种</span></span>
            </div>
            <div class="metric-card">
              <span class="metric-label">大宗采购意向单</span>
              <span class="metric-val highlight">{{ b2bInquiryList.length }} <span>笔</span></span>
            </div>
            <div class="metric-card">
              <span class="metric-label">待结算回款资金</span>
              <span class="metric-val">¥ 48,250.00</span>
            </div>
            <div class="metric-card">
              <span class="metric-label">防滞销库存状态</span>
              <span class="metric-val green">产销平衡</span>
            </div>
          </div>

          <!-- 自主库存调控 (防滞销) -->
          <el-card class="mt-4" header="📦 我上架的货源与库存自主调控 (防丰产滞销)">
            <el-table :data="productList" stripe>
              <el-table-column prop="title" label="商品名称" min-width="200" />
              <el-table-column prop="category" label="品类" width="100" />
              <el-table-column label="零售价" width="100">
                <template #default="{ row }">¥{{ row.retailPrice }}</template>
              </el-table-column>
              <el-table-column label="大宗批发价" width="110">
                <template #default="{ row }">¥{{ row.wholesalePrice }}/斤</template>
              </el-table-column>
              <el-table-column label="自主库存调控" width="240">
                <template #default="{ row }">
                  <div class="stock-editor">
                    <el-input-number v-model="row.stock" :min="0" :step="200" size="small" />
                    <el-button type="primary" link size="small" @click="saveProductStock(row)">保存库存</el-button>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="供货模式" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.isPresale === 1 ? 'warning' : 'success'">
                    {{ row.isPresale === 1 ? '田间预售' : '现货直供' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>

          <!-- 大宗询价磋商待办 -->
          <el-card class="mt-4" header="📨 采购批发商大宗询价磋商待办">
            <el-table :data="b2bInquiryList" stripe>
              <el-table-column prop="inquiry.inquiryNo" label="询价单号" width="180" />
              <el-table-column label="意向采购品" min-width="160">
                <template #default="{ row }">{{ row.product?.title || '凉山盐源红富士' }}</template>
              </el-table-column>
              <el-table-column label="采购规模" width="120">
                <template #default="{ row }">{{ row.inquiry.demandQuantity }} 吨</template>
              </el-table-column>
              <el-table-column label="买家期望价" width="120">
                <template #default="{ row }">¥{{ row.inquiry.expectedUnitPrice }}/斤</template>
              </el-table-column>
              <el-table-column label="当前状态" width="130">
                <template #default="{ row }">
                  <el-tag :type="row.inquiry.status === 3 ? 'success' : 'warning'">
                    {{ row.inquiry.status === 3 ? '已签署合同' : '待合作社确认' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220">
                <template #default="{ row }">
                  <el-button 
                    v-if="row.inquiry.status !== 3"
                    type="success" 
                    size="small" 
                    @click="coopAcceptInquiry(row.inquiry.id)"
                  >接受报价</el-button>
                  <el-button 
                    v-if="row.inquiry.status === 3" 
                    type="primary" 
                    size="small" 
                    @click="viewContractModal(row.inquiry.id)"
                  >查看电子合同</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>

        <!-- 6.2 产地主播专属工作台 (ROLE_ANCHOR) -->
        <div v-else-if="currentRole === 'ANCHOR'" class="anchor-panel">
          <div class="workbench-bar">
            <div>
              <h2><el-icon><VideoCamera /></el-icon> 产地主播实景带货控制中心</h2>
              <p>主播：{{ currentUser.realName }} ｜ 绑定主产区：四川凉山、山东寿光、黑龙江五常</p>
            </div>
            <el-button type="danger" size="large" @click="activeTab = 'live'">
              进入直播间推流
            </el-button>
          </div>

          <div class="metric-row">
            <div class="metric-card">
              <span class="metric-label">本场带货总 GMV</span>
              <span class="metric-val highlight">¥ 68,900.00</span>
            </div>
            <div class="metric-card">
              <span class="metric-label">本场成交订单数</span>
              <span class="metric-val">128 <span>单</span></span>
            </div>
            <div class="metric-card">
              <span class="metric-label">带货下单转化率</span>
              <span class="metric-val green">8.7%</span>
            </div>
          </div>

          <el-card class="mt-4" header="🛒 合作社货源选品池与当前主推讲解">
            <el-table :data="productList" stripe>
              <el-table-column prop="title" label="农产品" min-width="220" />
              <el-table-column prop="originPlace" label="源头产地" width="130" />
              <el-table-column label="直播专享特惠" width="130">
                <template #default="{ row }">
                  <span class="b2b-table-price">¥{{ (row.retailPrice * 0.9).toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="讲解控制" width="220">
                <template #default="{ row }">
                  <el-button 
                    :type="explainingProduct?.id === row.id ? 'success' : 'primary'"
                    size="small"
                    @click="setAsExplainingProduct(row)"
                  >
                    {{ explainingProduct?.id === row.id ? '当前讲解中' : '设为正在讲解' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>

        <!-- 6.3 采购批发商专属工作台 (ROLE_WHOLESALE) -->
        <div v-else-if="currentRole === 'WHOLESALE'" class="wholesale-panel">
          <div class="workbench-bar">
            <div>
              <h2><el-icon><OfficeBuilding /></el-icon> 采购批发商大宗集采中心</h2>
              <p>企业主体：{{ currentUser.realName }} ｜ 战略等级：<el-tag type="success">特邀战略采购商</el-tag></p>
            </div>
            <el-button type="warning" size="large" @click="activeTab = 'wholesale'">
              继续找大宗货源
            </el-button>
          </div>

          <el-card class="mt-4" header="📋 我发起的大宗询价意向与电子合同">
            <el-table :data="b2bInquiryList" stripe>
              <el-table-column prop="inquiry.inquiryNo" label="询价单号" width="180" />
              <el-table-column label="采购标的" min-width="180">
                <template #default="{ row }">{{ row.product?.title || '凉山红富士' }}</template>
              </el-table-column>
              <el-table-column label="采购规模" width="120">
                <template #default="{ row }">{{ row.inquiry.demandQuantity }} 吨</template>
              </el-table-column>
              <el-table-column label="期望单价" width="120">
                <template #default="{ row }">¥{{ row.inquiry.expectedUnitPrice }}/斤</template>
              </el-table-column>
              <el-table-column label="状态" width="140">
                <template #default="{ row }">
                  <el-tag :type="row.inquiry.status === 3 ? 'success' : 'primary'">
                    {{ row.inquiry.status === 3 ? '已签署电子合同' : '意向磋商中' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="合同与履约" width="180">
                <template #default="{ row }">
                  <el-button 
                    v-if="row.inquiry.status === 3" 
                    type="primary" 
                    size="small" 
                    @click="viewContractModal(row.inquiry.id)"
                  >
                    查看盖章合同
                  </el-button>
                  <el-button 
                    v-else 
                    type="warning" 
                    size="small" 
                    @click="confirmSigningContract(row.inquiry.id)"
                  >
                    签署电子协议
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>

        <!-- 6.4 普通消费者专属中心 (ROLE_CONSUMER) -->
        <div v-else-if="currentRole === 'CONSUMER'" class="consumer-panel">
          <div class="workbench-bar">
            <div>
              <h2><el-icon><ShoppingBag /></el-icon> 个人买家中心·我的淘宝</h2>
              <p>账号：{{ currentUser.realName }} ｜ 不烧心会员等级：<el-tag type="danger">金牌特权会员</el-tag></p>
            </div>
            <div class="bar-actions">
              <el-button type="warning" size="large" @click="activeTab = 'cart'">
                <el-icon><ShoppingCart /></el-icon> 我的购物车 ({{ cartList.length }})
              </el-button>
              <el-button type="primary" size="large" @click="activeTab = 'home'">
                去逛源头货架
              </el-button>
            </div>
          </div>

          <el-card class="mt-4" header="📦 我的订单与顺丰冷链履约进度 (支持全流程追踪与售后)">
            <!-- 订单状态筛选 Tab -->
            <div class="order-status-tabs-row">
              <el-radio-group v-model="buyerOrderFilter" size="small">
                <el-radio-button label="all">全部订单 ({{ buyerOrderList.length }})</el-radio-button>
                <el-radio-button label="unpaid">待付款 (0)</el-radio-button>
                <el-radio-button label="unshipped">待发货 (1)</el-radio-button>
                <el-radio-button label="delivered">待收货/运输中 ({{ buyerOrderList.length }})</el-radio-button>
                <el-radio-button label="completed">已完成 (3)</el-radio-button>
              </el-radio-group>
            </div>

            <!-- 订单卡片列表 -->
            <div class="taobao-order-list">
              <div v-for="item in displayBuyerOrders" :key="item.order.id" class="taobao-order-card">
                <div class="order-header-bar">
                  <span class="o-time">{{ item.order.createTime?.replace('T', ' ') || '2026-11-14 08:30:00' }}</span>
                  <span class="o-no">订单号: <strong>{{ item.order.orderNo }}</strong></span>
                  <span class="o-shop"><el-icon><House /></el-icon> 凉山盐源高山果蔬合作社</span>
                  <el-tag size="small" type="success" effect="plain" class="o-status">顺丰冷链运输中 (4.2℃)</el-tag>
                </div>
                <div class="order-body-grid">
                  <div class="order-prod-item">
                    <img :src="item.product?.coverImage || 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600'" />
                    <div class="p-meta">
                      <div class="p-title">{{ item.product?.title || '【不烧心】凉山高山盐源丑苹果 5斤礼盒装' }}</div>
                      <div class="p-sku">规格：5斤精选大果家庭尝鲜装 ｜ 顺丰恒温冷链包邮</div>
                      <div class="p-safe-tag"><el-tag size="small" type="success">320项农残零检出</el-tag></div>
                    </div>
                  </div>
                  <div class="order-receiver-box">
                    <div class="name">收件人: {{ item.order.receiverName }}</div>
                    <div class="phone">{{ item.order.receiverPhone }}</div>
                    <div class="addr-detail">{{ item.order.receiverAddress }}</div>
                  </div>
                  <div class="order-price-col">
                    <div class="price-val">¥{{ item.order.totalAmount }}</div>
                    <div class="ship-tip">含顺丰冷链运费 (免运费)</div>
                  </div>
                  <div class="order-actions-col">
                    <el-button type="primary" size="small" @click="openLogistics(item)">
                      <el-icon><Van /></el-icon> 查看冷链物流
                    </el-button>
                    <el-button size="small" @click="confirmOrderReceived(item)">确认收货</el-button>
                    <el-button size="small" type="warning" plain @click="openReviewModal(item)">评价晒单</el-button>
                    <el-button size="small" type="danger" link @click="requestRefund(item.order.id)">
                      坏果极速退款
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 6.5 平台运营管理员中台 (ROLE_ADMIN) -->
        <div v-else-if="currentRole === 'ADMIN'" class="admin-panel">
          <div class="workbench-bar">
            <div>
              <h2><el-icon><Management /></el-icon> 不烧心·平台运营治理与产销大数据监控大屏</h2>
              <p>商家准入资质审核 ｜ 违规商品一键下架 ｜ 全国产销数据报表</p>
            </div>
          </div>

          <div class="metric-row">
            <div class="metric-card">
              <span class="metric-label">全平台产销总 GMV</span>
              <span class="metric-val highlight">¥ 1,385,420.00</span>
            </div>
            <div class="metric-card">
              <span class="metric-label">已认证入驻合作社</span>
              <span class="metric-val">2 <span>家</span></span>
            </div>
            <div class="metric-card">
              <span class="metric-label">合规在售农产品</span>
              <span class="metric-val">{{ productList.length }} <span>款</span></span>
            </div>
            <div class="metric-card">
              <span class="metric-label">大宗集采对接总量</span>
              <span class="metric-val">380.5 <span>吨</span></span>
            </div>
          </div>

          <div class="admin-charts-grid mt-4">
            <el-card header="🗺️ 全国农产品销售地域热度排行" style="flex: 1;">
              <el-table :data="adminStatsData.provinceRank" stripe>
                <el-table-column prop="province" label="省份" width="120" />
                <el-table-column prop="sales" label="销售规模" width="120" />
                <el-table-column prop="topCrop" label="核心优势农产品" />
              </el-table>
            </el-card>

            <el-card header="📊 核心品类销售占比报表" style="flex: 1;">
              <div class="cat-progress-bars">
                <div v-for="c in adminStatsData.categoryShare" :key="c.name" class="cat-bar">
                  <span class="cat-label">{{ c.name }}</span>
                  <el-progress :percentage="c.value" :color="c.name === '新鲜水果' ? '#FF5000' : '#2BB673'" />
                </div>
              </div>
            </el-card>
          </div>

          <el-card class="mt-4" header="⚖️ 商品合规风控与违规下架治理">
            <el-table :data="productList" stripe>
              <el-table-column prop="title" label="商品名称" min-width="220" />
              <el-table-column prop="originPlace" label="产地" width="120" />
              <el-table-column prop="retailPrice" label="售价" width="100" />
              <el-table-column label="合规状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '正常在售' : '已违规封禁' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="治理操作" width="160">
                <template #default="{ row }">
                  <el-button 
                    :type="row.status === 1 ? 'danger' : 'success'" 
                    size="small"
                    @click="toggleProductStatusApi(row)"
                  >
                    {{ row.status === 1 ? '违规下架' : '恢复上架' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>

        <!-- 6.6 农技与质检专家工作台 (ROLE_EXPERT) -->
        <div v-else-if="currentRole === 'EXPERT'" class="expert-panel">
          <div class="workbench-bar">
            <div>
              <h2><el-icon><Checked /></el-icon> 农产品品质安全检测与农技服务工作台</h2>
              <p>专家：{{ currentUser.realName }} ｜ 机构：中检集团绿色认证中心</p>
            </div>
            <el-button type="success" size="large" @click="expertIssueCertModal = true">
              <el-icon><DocumentAdd /></el-icon> 录入农残抽检报告
            </el-button>
          </div>

          <div class="metric-row">
            <div class="metric-card">
              <span class="metric-label">已签发质检证书</span>
              <span class="metric-val green">37 <span>份</span></span>
            </div>
            <div class="metric-card">
              <span class="metric-label">农残专项筛查项目</span>
              <span class="metric-val">320 <span>项</span></span>
            </div>
            <div class="metric-card">
              <span class="metric-label">合格出具率</span>
              <span class="metric-val highlight">100%</span>
            </div>
            <div class="metric-card">
              <span class="metric-label">农技病虫害指导</span>
              <span class="metric-val">8 <span>期</span></span>
            </div>
          </div>

          <el-card class="mt-4" header="🔬 全平台在售农产品批次农残检测档案">
            <el-table :data="productList" stripe>
              <el-table-column prop="title" label="检测标的产品" min-width="220" />
              <el-table-column prop="originPlace" label="基地地理" width="130" />
              <el-table-column label="抽检状态" width="130">
                <template #default="{ row }">
                  <el-tag type="success">SGS抽检合格</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="质检操作" width="200">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="openTraceModal(row.id)">
                    查看检测报告
                  </el-button>
                  <el-button type="success" size="small" @click="handleFastCertify(row)">
                    一键签发认证书
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>

      </div>

      <!-- 8.5 淘宝经典右侧垂直悬浮工具条 -->
      <aside class="taobao-float-toolstrip">
        <div class="toolstrip-item" @click="activeTab = 'workbench'">
          <el-tooltip content="我的淘宝 / 会员中心" placement="left">
            <div class="tool-cell">
              <el-avatar :size="28" :src="currentUser.avatar" class="tool-avatar" />
              <span class="tool-label">我的</span>
            </div>
          </el-tooltip>
        </div>

        <div class="toolstrip-item cart-cell" :class="{ active: activeTab === 'cart' }" @click="activeTab = 'cart'">
          <el-tooltip content="统一购物车" placement="left">
            <div class="tool-cell">
              <el-badge :value="cartList.length" class="tool-badge">
                <el-icon><ShoppingCartFull /></el-icon>
              </el-badge>
              <span class="tool-label">购物车</span>
            </div>
          </el-tooltip>
        </div>

        <div class="toolstrip-item" @click="couponCenterVisible = true">
          <el-tooltip content="天天领红包 · 神券专区" placement="left">
            <div class="tool-cell red-cell">
              <el-icon><Ticket /></el-icon>
              <span class="tool-label">领券</span>
            </div>
          </el-tooltip>
        </div>

        <div class="toolstrip-item" @click="wangwangVisible = true">
          <el-tooltip content="掌柜旺旺 · 在线咨询 (3秒回复)" placement="left">
            <div class="tool-cell green-cell">
              <el-icon><Service /></el-icon>
              <span class="tool-label">客服</span>
            </div>
          </el-tooltip>
        </div>

        <div class="toolstrip-item">
          <el-popover placement="left" :width="180" trigger="hover">
            <template #reference>
              <div class="tool-cell">
                <el-icon><Iphone /></el-icon>
                <span class="tool-label">手机逛</span>
              </div>
            </template>
            <div class="qr-popover-box">
              <img src="https://api.qrserver.com/v1/create-qr-code/?size=130x130&data=http://localhost:3000" class="qr-img" />
              <div class="qr-text">扫码手机看直播</div>
              <div class="qr-sub">好产品，不烧心</div>
            </div>
          </el-popover>
        </div>

        <div class="toolstrip-item top-cell" @click="scrollToTop">
          <el-tooltip content="回到顶部" placement="left">
            <div class="tool-cell">
              <el-icon><Top /></el-icon>
              <span class="tool-label">顶部</span>
            </div>
          </el-tooltip>
        </div>
      </aside>
    </main>

    <!-- ============================================================== -->
    <!-- 7. 全局悬浮直播画中画窗口                                       -->
    <!-- ============================================================== -->
    <div v-if="isPipActive && activeTab !== 'live'" class="global-pip-box">
      <div class="pip-titlebar">
        <span class="pip-status-dot"></span>
        <span class="pip-caption">川农小李·采摘直播中</span>
        <el-icon class="pip-close" @click="isPipActive = false"><Close /></el-icon>
      </div>
      <div class="pip-content" @click="activeTab = 'live'">
        <video 
          autoplay 
          loop 
          muted 
          class="pip-media"
          src="https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        ></video>
        <div class="pip-tip">点击返回全屏直播大厅</div>
      </div>
    </div>

    <!-- ============================================================== -->
    <!-- 8. 移动端专属底部 TabBar 导航 (≤768px 展示)                    -->
    <!-- ============================================================== -->
    <div class="mobile-bottom-tabbar">
      <div :class="['tabbar-item', activeTab === 'home' ? 'active' : '']" @click="activeTab = 'home'">
        <el-icon><GoodsFilled /></el-icon>
        <span>货架</span>
      </div>
      <div :class="['tabbar-item', activeTab === 'live' ? 'active' : '']" @click="activeTab = 'live'">
        <el-icon><VideoCamera /></el-icon>
        <span>直播</span>
      </div>
      <div :class="['tabbar-item', activeTab === 'wholesale' ? 'active' : '']" @click="activeTab = 'wholesale'">
        <el-icon><Van /></el-icon>
        <span>大宗</span>
      </div>
      <div :class="['tabbar-item', activeTab === 'trace' ? 'active' : '']" @click="activeTab = 'trace'">
        <el-icon><CircleCheckFilled /></el-icon>
        <span>溯源</span>
      </div>
      <div :class="['tabbar-item', activeTab === 'cart' ? 'active' : '']" @click="activeTab = 'cart'">
        <el-badge :value="cartList.length" class="mobile-cart-badge">
          <el-icon><ShoppingCart /></el-icon>
        </el-badge>
        <span>购物车</span>
      </div>
      <div :class="['tabbar-item', activeTab === 'workbench' ? 'active' : '']" @click="activeTab = 'workbench'">
        <el-icon><UserFilled /></el-icon>
        <span>我的</span>
      </div>
    </div>

    <!-- ============================================================== -->
    <!-- 9. 弹窗对话框组 (登录/注册、上架、询价、合同、收银台)             -->
    <!-- ============================================================== -->

    <!-- 对话框 9.0: 仿淘宝登录与注册弹窗 (支持6种角色卡通头像) -->
    <el-dialog v-model="authModalVisible" title="不烧心·农品直供 - 会员通行证" width="460px" :close-on-click-modal="false">
      <el-tabs v-model="authActiveTab" stretch>
        <!-- 登录 TAB -->
        <el-tab-pane label="账号登录" name="login">
          <el-form label-position="top">
            <el-form-item label="用户名 / 手机号">
              <el-input v-model="loginForm.username" placeholder="请输入您的账号" prefix-icon="User" />
            </el-form-item>
            <el-form-item label="登录密码">
              <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" prefix-icon="Lock" />
            </el-form-item>
            <el-button type="danger" size="large" style="width: 100%; margin-top: 10px;" @click="doLoginSubmit">
              登 录
            </el-button>
          </el-form>

          <el-divider>或快速体验六种角色 (卡通形象)</el-divider>
          <div class="quick-role-grid">
            <div 
              v-for="r in roleDemoPresets" 
              :key="r.role" 
              class="quick-role-chip"
              @click="quickLoginAs(r)"
            >
              <el-avatar :size="24" :src="r.avatar" class="chip-avatar" />
              <span>{{ r.name }}</span>
            </div>
          </div>
        </el-tab-pane>

        <!-- 注册 TAB -->
        <el-tab-pane label="免费注册" name="register">
          <el-form label-position="top">
            <el-form-item label="设置用户名">
              <el-input v-model="registerForm.username" placeholder="英文字母或数字" prefix-icon="User" />
            </el-form-item>
            <el-form-item label="设置密码">
              <el-input v-model="registerForm.password" type="password" show-password placeholder="6位以上密码" prefix-icon="Lock" />
            </el-form-item>
            <el-form-item label="真实姓名 / 企业名称">
              <el-input v-model="registerForm.realName" placeholder="如：凉山生态合作社、李大壮" prefix-icon="Edit" />
            </el-form-item>
            <el-form-item label="手机号码">
              <el-input v-model="registerForm.phone" placeholder="接收订单与履约通知" prefix-icon="Phone" />
            </el-form-item>
            <el-form-item label="请选择您的注册身份 (6种角色)">
              <el-select v-model="registerForm.roleType" style="width: 100%;">
                <el-option label="普通买家 (零售网购)" value="CONSUMER" />
                <el-option label="种植合作社 (源头货架与库存管理)" value="COOP" />
                <el-option label="产地主播 (带货选品与直播推流)" value="ANCHOR" />
                <el-option label="采购批发商 (大宗集采与合同签约)" value="WHOLESALE" />
                <el-option label="平台运营管理 (风控与数据中台)" value="ADMIN" />
                <el-option label="农技质检专家 (农残检测与证书签发)" value="EXPERT" />
              </el-select>
            </el-form-item>
            <el-button type="primary" size="large" style="width: 100%; margin-top: 10px;" @click="doRegisterSubmit">
              立即注册并登录
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 对话框 9.1: 合作社手机端极速上架农产品 -->
    <el-dialog v-model="publishModalVisible" title="📱 手机端快速上架当季农产品" width="560px">
      <el-form label-position="top">
        <el-form-item label="农产品全称 (不烧心保真)">
          <el-input v-model="publishForm.title" placeholder="如：【产地直采】四川凉山高山糖心苹果 5斤礼盒装" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="核心品类">
              <el-select v-model="publishForm.category" style="width: 100%;">
                <el-option label="水果" value="水果" />
                <el-option label="蔬菜" value="蔬菜" />
                <el-option label="粮油" value="粮油" />
                <el-option label="滋补" value="滋补" />
                <el-option label="禽畜" value="禽畜" />
                <el-option label="水产" value="水产" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主产地地理位置">
              <el-input v-model="publishForm.originPlace" placeholder="如：四川凉山盐源" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="C端零售单价 (元/份)">
              <el-input-number v-model="publishForm.retailPrice" :precision="2" :step="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="B端大宗批发底价 (元/斤)">
              <el-input-number v-model="publishForm.wholesalePrice" :precision="2" :step="0.5" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="初始可供库存 (斤/件)">
              <el-input-number v-model="publishForm.stock" :step="500" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供货模式">
              <el-radio-group v-model="publishForm.isPresale">
                <el-radio :label="0">现货直发</el-radio>
                <el-radio :label="1">田间预售</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="产地土壤与高山水源环境说明">
          <el-input v-model="publishForm.soilQuality" placeholder="如：海拔2600米弱酸红壤，高山纯净融雪灌溉" />
        </el-form-item>
        <el-form-item label="第三方权威农残检测报告结论">
          <el-input v-model="publishForm.testResult" placeholder="如：经过SGS抽检320项农残零检出，符合绿色食品标准" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishModalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitQuickPublishApi">立即上架货架</el-button>
      </template>
    </el-dialog>

    <!-- 对话框 9.2: 发起大宗采购询价 -->
    <el-dialog v-model="inquiryModalVisible" title="📝 发起农副产品大宗采购询价单" width="520px">
      <el-form label-position="top">
        <p><strong>意向采购产品：</strong>{{ targetInquiryProduct?.title }}</p>
        <p><strong>源头供货合作社：</strong>{{ targetInquiryProduct?.originPlace }}直供基地</p>
        <el-form-item label="意向采购吨数 (吨)">
          <el-input-number v-model="inquiryForm.demandQuantity" :min="1" :step="5" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="期望单价 (元/斤)">
          <el-input-number v-model="inquiryForm.expectedUnitPrice" :precision="2" :step="0.1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="期望最晚到货交付日期">
          <el-date-picker v-model="inquiryForm.deliveryTargetDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="冷链运输与包装技术要求">
          <el-input v-model="inquiryForm.packagingDemand" placeholder="如：顺丰冷链专车直发，五层加厚果筐" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inquiryModalVisible = false">取消</el-button>
        <el-button type="warning" @click="submitInquiryApi">提交大宗询价单</el-button>
      </template>
    </el-dialog>

    <!-- 对话框 9.3: 电子合同预览 (加盖不烧心防伪印章) -->
    <el-dialog v-model="contractModalVisible" title="📄 农副产品大宗购销意向合同 (法律存证)" width="640px">
      <div v-if="activeContractData" class="formal-contract-sheet">
        <div class="official-seal">不烧心·已签署</div>
        <h2 class="sheet-title">农副产品大宗购销意向协议书</h2>
        <div class="sheet-no">合同备案编号: {{ activeContractData.contractNo }}</div>
        <div class="sheet-terms">
          <p><strong>甲方 (采购方)：</strong>{{ activeContractData.buyerName }}</p>
          <p><strong>乙方 (供货方)：</strong>{{ activeContractData.sellerName }}</p>
          <p><strong>采购标的：</strong>{{ activeContractData.productTitle }}</p>
          <p><strong>约定采购规模：</strong>{{ activeContractData.demandQuantity }}</p>
          <p><strong>核定结算单价：</strong>{{ activeContractData.agreedUnitPrice }}</p>
          <p><strong>交货交付日期：</strong>{{ activeContractData.deliveryTargetDate }}</p>
          <p><strong>冷链包装要求：</strong>{{ activeContractData.packagingDemand }}</p>
          <p><strong>签约时间戳：</strong>{{ activeContractData.signTime }}</p>
        </div>
        <div class="sheet-clause">
          <strong>法律条款约定：</strong>
          {{ activeContractData.legalClause }}
        </div>
      </div>
    </el-dialog>

    <!-- 对话框 9.4: 仿淘宝高级收银台 (多地址/优惠券/淘金币/顺丰冷链) -->
    <el-dialog v-model="checkoutModalVisible" title="🛒 确认订单与收银结算" width="560px" class="taobao-cashier-dialog">
      <div v-if="currentCheckoutProduct" class="checkout-panel">
        <!-- 收货地址选择 -->
        <div class="cashier-addr-section">
          <div class="section-title-row">
            <span><el-icon><Location /></el-icon> 收货地址选择：</span>
            <el-button type="primary" link size="small" @click="showAddressEditModal = true">+ 新增地址</el-button>
          </div>
          <div class="addr-select-cards">
            <div 
              v-for="addr in addressList" 
              :key="addr.id"
              :class="['addr-item-radio', selectedAddressId === addr.id ? 'active' : '']"
              @click="selectedAddressId = addr.id"
            >
              <div class="addr-tag-name">
                <el-tag size="small" :type="addr.isDefault ? 'danger' : 'info'">{{ addr.tag }}</el-tag>
                <strong>{{ addr.name }}</strong> ({{ addr.phone }})
              </div>
              <div class="addr-text">{{ addr.province }} {{ addr.city }} {{ addr.detail }}</div>
            </div>
          </div>
        </div>

        <!-- 结算商品信息 -->
        <div class="checkout-prod-meta">
          <img :src="currentCheckoutProduct.coverImage" />
          <div class="prod-text">
            <div class="title">{{ currentCheckoutProduct.title }}</div>
            <div class="origin">原产地: {{ currentCheckoutProduct.originPlace }}</div>
            <div class="sku-info">已选规格: <strong>{{ currentCheckoutProduct.skuName || '5斤精选大果·家庭尝鲜装' }}</strong> x{{ currentCheckoutProduct.buyQuantity || 1 }}</div>
            <div class="price">结算单价: ¥{{ (currentCheckoutProduct.retailPrice * (isLiveDiscount ? 0.9 : 1)).toFixed(2) }}</div>
          </div>
        </div>

        <!-- 配送服务保障 -->
        <div class="cashier-service-box">
          <div class="service-row">
            <span><el-icon><Van /></el-icon> 配送方式：</span>
            <strong>顺丰生鲜特快全程冷链包邮 (48小时达)</strong>
          </div>
          <div class="service-row">
            <span><el-icon><Box /></el-icon> 生鲜包装：</span>
            <span>五层高强瓦楞礼盒 + 生物保鲜冰袋 + 珍珠棉防震</span>
          </div>
          <div class="service-row">
            <span><el-icon><CircleCheckFilled /></el-icon> 售后承诺：</span>
            <span class="c-green">好产品，不烧心 · 假一赔四 · 坏果秒级全包赔</span>
          </div>
        </div>

        <!-- 优惠抵扣与明细 -->
        <div class="cashier-discounts-box">
          <div class="disc-row">
            <span>商品金额小计：</span>
            <span>¥{{ ((currentCheckoutProduct.retailPrice * (currentCheckoutProduct.buyQuantity || 1)) * (isLiveDiscount ? 0.9 : 1)).toFixed(2) }}</span>
          </div>
          <div class="disc-row">
            <span>顺丰生鲜冷链运费：</span>
            <span class="c-green">¥18.00 (合作社全场包邮 -¥18.00)</span>
          </div>
          <div class="disc-row">
            <span><el-icon><Ticket /></el-icon> 不烧心开市红包抵扣：</span>
            <span class="c-red">-¥10.00</span>
          </div>
          <div class="disc-row">
            <span>淘金币农品专项抵扣：</span>
            <span class="c-red">-¥2.50</span>
          </div>
        </div>

        <!-- 买家留言 -->
        <div class="cashier-remark-row">
          <span>买家留言备注：</span>
          <el-input v-model="orderRemarkNote" placeholder="选填：可备注尽量挑大的、熟一点的，顺丰放门口等" size="small" />
        </div>

        <!-- 支付方式 -->
        <div class="cashier-pay-ways">
          <span>支付方式：</span>
          <el-radio-group v-model="selectedPayWay" size="small">
            <el-radio-button label="alipay">支付宝 (推荐)</el-radio-button>
            <el-radio-button label="wechat">微信支付</el-radio-button>
            <el-radio-button label="ecny">农行惠农数字人民币</el-radio-button>
          </el-radio-group>
        </div>

        <el-divider style="margin: 12px 0;" />

        <div class="order-final-row">
          <span class="final-label">实际应付金额：</span>
          <span class="final-price">¥{{ Math.max(0, ((currentCheckoutProduct.retailPrice * (currentCheckoutProduct.buyQuantity || 1)) * (isLiveDiscount ? 0.9 : 1) - 12.5)).toFixed(2) }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="checkoutModalVisible = false">返回修改</el-button>
        <el-button type="danger" size="large" class="cashier-submit-btn" @click="confirmPayAndCreateOrder">
          <el-icon><CircleCheckFilled /></el-icon> 立即确认支付并提交
        </el-button>
      </template>
    </el-dialog>

    <!-- 对话框 9.5: 专家签发报告弹窗 -->
    <el-dialog v-model="expertIssueCertModal" title="🔬 录入农产品农残检测报告与认证结论" width="500px">
      <el-form label-position="top">
        <el-form-item label="抽检标的产品">
          <el-select v-model="selectedExpertProdId" style="width: 100%;">
            <el-option v-for="p in productList" :key="p.id" :label="p.title" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="农残筛查项目">
          <el-input value="农业农村部国家绿色食品320项农残专项筛查" disabled />
        </el-form-item>
        <el-form-item label="检测实验室结论">
          <el-input v-model="expertCertConclusion" placeholder="如：有机磷等320项农残未检出，完全符合绿色食品国家标准" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="expertIssueCertModal = false">取消</el-button>
        <el-button type="success" @click="submitExpertCert">确认签发证书</el-button>
      </template>
    </el-dialog>

    <!-- 对话框 9.6: 仿淘宝商品详情页大弹窗 (图文/规格/评价/质检/溯源) -->
    <el-dialog 
      v-model="productDetailVisible" 
      width="980px" 
      top="4vh" 
      class="taobao-detail-dialog" 
      :show-close="true"
      destroy-on-close
    >
      <div v-if="detailProduct" class="detail-page-container">
        <!-- 面包屑 -->
        <div class="detail-breadcrumb">
          <span>首页</span> <span class="sep">&gt;</span>
          <span>绿色农副</span> <span class="sep">&gt;</span>
          <span>{{ detailProduct.category }}</span> <span class="sep">&gt;</span>
          <span class="cur">{{ detailProduct.title }}</span>
        </div>

        <!-- 主体上下两分部：上方购买决策区 (左右两栏) -->
        <div class="detail-top-section">
          <!-- 左侧：媒体图库展示 -->
          <div class="detail-left-gallery">
            <div class="main-preview-box">
              <img :src="detailGalleryImages[detailGalleryIndex]" class="main-preview-img" />
              <span class="trust-corner-tag">好产品，不烧心 · 产地直供</span>
            </div>
            <!-- 缩略图选择器 -->
            <div class="thumb-selector-row">
              <div 
                v-for="(img, idx) in detailGalleryImages" 
                :key="idx"
                :class="['thumb-box', detailGalleryIndex === idx ? 'active' : '']"
                @mouseenter="detailGalleryIndex = idx"
              >
                <img :src="img" />
              </div>
            </div>
            <!-- 产地保障与合作社名片 -->
            <div class="detail-coop-card">
              <div class="coop-card-head">
                <el-avatar :size="38" src="https://api.dicebear.com/7.x/adventurer/svg?seed=UncleFarm" />
                <div class="coop-card-info">
                  <div class="name">凉山盐源高山果蔬生态合作社</div>
                  <div class="auth-pill"><el-icon><Checked /></el-icon> 官方认证供货合作社 · 评分 4.98</div>
                </div>
              </div>
              <div class="coop-card-actions">
                <el-button size="small" type="primary" plain @click="contactShopSeller">
                  <el-icon><Service /></el-icon> 联系掌柜
                </el-button>
                <el-button size="small" @click="activeTab = 'home'; productDetailVisible = false">
                  <el-icon><House /></el-icon> 进合作社货架
                </el-button>
              </div>
            </div>
          </div>

          <!-- 右侧：商品购买决策与规格面板 -->
          <div class="detail-right-info">
            <div class="detail-title-box">
              <span class="brand-tag">好产品，不烧心</span>
              <span class="geo-tag">地理标志保护</span>
              <h1 class="product-main-title">{{ detailProduct.title }}</h1>
              <p class="product-sub-title">【源头合作社现摘直发】国家绿色食品认证 ｜ 320项农残零检出 ｜ 顺丰生鲜特快全程冷链包邮</p>
            </div>

            <!-- 淘宝百亿补贴价格栏 -->
            <div class="taobao-price-banner">
              <div class="price-header-row">
                <span class="subsidy-label">百亿补贴券后价</span>
                <span class="subsidy-tip">已直降 ¥{{ (currentSelectedSku.price * 0.25).toFixed(1) }}</span>
              </div>
              <div class="price-main-row">
                <span class="p-currency">¥</span>
                <span class="p-amount">{{ currentSelectedSku.price.toString().split('.')[0] }}</span>
                <span class="p-decimal">.{{ (currentSelectedSku.price.toString().split('.')[1] || '00') }}</span>
                <span class="p-orig">专柜价: ¥{{ (currentSelectedSku.price * 1.25).toFixed(1) }}</span>
                <span class="p-hot-tag">限时尝鲜特惠</span>
              </div>
              <div class="price-meta-row">
                <span>累计已售 <strong>1.2万+</strong> 件</span>
                <span class="sep">|</span>
                <span>月销 <strong>3500+</strong> 件</span>
                <span class="sep">|</span>
                <span><strong>99.8%</strong> 买家好评 (2800+ 评价)</span>
              </div>
            </div>

            <!-- 优惠券速领 -->
            <div class="detail-coupon-row">
              <span class="label">优惠专区：</span>
              <div class="coupon-tag-list">
                <span class="coupon-chip" @click="couponCenterVisible = true">
                  <el-icon><Ticket /></el-icon> 新客立减 ¥10
                </span>
                <span class="coupon-chip" @click="couponCenterVisible = true">
                  满99减15
                </span>
                <span class="coupon-chip" @click="couponCenterVisible = true">
                  满199减30
                </span>
                <span class="more-coupon-link" @click="couponCenterVisible = true">领券 &gt;</span>
              </div>
            </div>

            <!-- 顺丰冷链直发承诺 -->
            <div class="detail-delivery-row">
              <span class="label">物流配送：</span>
              <div class="delivery-content">
                <div>
                  <el-icon><Van /></el-icon> 
                  <strong>顺丰生鲜特快全程冷链包邮</strong> 
                  (从 {{ detailProduct.originPlace }} 合作社直发，48小时内现采装车送达)
                </div>
                <div class="service-guarantees">
                  <span><el-icon><CircleCheckFilled /></el-icon> 坏果包赔</span>
                  <span><el-icon><CircleCheckFilled /></el-icon> 假一赔四</span>
                  <span><el-icon><CircleCheckFilled /></el-icon> 极速退款</span>
                  <span><el-icon><CircleCheckFilled /></el-icon> 农残320项零检出</span>
                </div>
              </div>
            </div>

            <!-- SKU 规格选择 -->
            <div class="detail-sku-row">
              <span class="label">规格分类：</span>
              <div class="sku-cards-list">
                <div 
                  v-for="(sku, sIdx) in detailSkuList" 
                  :key="sIdx"
                  :class="['sku-card-btn', selectedSkuIndex === sIdx ? 'active' : '']"
                  @click="selectedSkuIndex = sIdx"
                >
                  <span class="sku-name">{{ sku.name }}</span>
                  <span class="sku-price">¥{{ sku.price }}</span>
                  <span class="sku-tip">{{ sku.specTip }}</span>
                </div>
              </div>
            </div>

            <!-- 购买数量 -->
            <div class="detail-qty-row">
              <span class="label">购买数量：</span>
              <el-input-number v-model="detailQuantity" :min="1" :max="detailProduct.stock" size="default" />
              <span class="stock-hint">库存充足 (现货 {{ detailProduct.stock }} {{ detailProduct.unit }})</span>
            </div>

            <!-- 行动按钮组 -->
            <div class="detail-action-buttons">
              <el-button 
                type="warning" 
                size="large" 
                class="btn-cart-lg"
                @click="addDetailToCart"
              >
                <el-icon><ShoppingCart /></el-icon> 加入购物车
              </el-button>
              <el-button 
                type="danger" 
                size="large" 
                class="btn-buy-lg"
                @click="buyDetailNow"
              >
                <el-icon><Lightning /></el-icon> 立即购买
              </el-button>
              <el-button 
                size="large" 
                class="btn-chat-lg"
                @click="contactShopSeller"
              >
                <el-icon><Service /></el-icon> 联系掌柜
              </el-button>
              <el-button 
                size="large" 
                :type="isCollected ? 'danger' : 'default'"
                plain
                @click="toggleFavorite"
              >
                <el-icon><Star /></el-icon> {{ isCollected ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 下方深度图文/评价/质检/溯源 Tabs -->
        <div class="detail-bottom-tabs">
          <el-tabs v-model="detailActiveTab">
            <!-- TAB 1: 图文详情 -->
            <el-tab-pane label="📖 图文详情与产地档案" name="desc">
              <div class="tab-desc-content">
                <div class="desc-card-block">
                  <h3>🌱 源头生态环境与种植档案</h3>
                  <p><strong>产地地理坐标：</strong>{{ detailProduct.originPlace }}，位于核心农业原生态保护区，年平均日照超 2800 小时，昼夜温差大，利于糖分自然沉积。</p>
                  <p><strong>灌溉水质与土壤：</strong>纯净高原雪山冰融水自然灌溉，土壤富含有机质与天然微量硒元素，全程拒绝催熟剂与膨大剂。</p>
                  <p><strong>上市与采收排期：</strong>{{ detailProduct.yieldCycle }}，预估合作社基地年总产出量为 {{ detailProduct.totalYield }}。</p>
                </div>
                <div class="desc-card-block">
                  <h3>📦 顺丰生鲜特快冷链保鲜包装</h3>
                  <p>采用食品级五层加厚高强瓦楞礼盒，内衬加厚珍珠棉减震凹槽网套，附带可降解生物冰袋与气调恒温保鲜袋，确保千里直达犹如刚从树上采摘般鲜活脆嫩！</p>
                </div>
                <div class="desc-card-block">
                  <h3>🍽️ 食用与保鲜建议</h3>
                  <p>收到果蔬后建议开箱轻放，置于 0~4℃ 冰箱保鲜层冷藏，冰镇后切块食用口感最为爆汁脆爽！如有极个别磕碰坏果，请直接拍照联系掌柜，不烧心提供坏果全包赔服务！</p>
                </div>
              </div>
            </el-tab-pane>

            <!-- TAB 2: 买家真实累计评价 -->
            <el-tab-pane label="⭐️ 累计评价 (2800+)" name="reviews">
              <div class="tab-reviews-content">
                <!-- 统计环 -->
                <div class="review-score-banner">
                  <div class="score-circle">
                    <span class="num">99.8%</span>
                    <span class="sub">买家好评率</span>
                  </div>
                  <div class="impress-tags">
                    <span class="tag">果肉脆甜多汁 (890)</span>
                    <span class="tag">顺丰冷链超快 (680)</span>
                    <span class="tag">包装无磕碰 (450)</span>
                    <span class="tag">带半透明糖心 (380)</span>
                    <span class="tag">回购很多次了 (320)</span>
                  </div>
                </div>

                <!-- 真实买家评价卡片列表 -->
                <div class="buyer-review-list">
                  <div v-for="(rev, rIdx) in sampleReviews" :key="rIdx" class="buyer-rev-item">
                    <div class="rev-user">
                      <el-avatar :size="32" :src="rev.avatar" />
                      <div class="user-meta">
                        <span class="uname">{{ rev.name }}</span>
                        <el-rate :model-value="rev.rate" disabled size="small" />
                      </div>
                      <span class="rev-time">{{ rev.time }}</span>
                    </div>
                    <div class="rev-spec">购买规格：{{ rev.spec }} ｜ 顺丰冷链特快直达</div>
                    <div class="rev-text">{{ rev.content }}</div>
                    <div v-if="rev.images && rev.images.length" class="rev-imgs">
                      <img v-for="(img, iIdx) in rev.images" :key="iIdx" :src="img" />
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- TAB 3: 权威农残质检报告 -->
            <el-tab-pane label="🔬 权威农残质检与绿标认证" name="cert">
              <div class="tab-cert-content">
                <div class="cert-formal-sheet">
                  <div class="cert-seal">SGS·已合格</div>
                  <h2>国家绿色食品农副产品 320 项农残筛查检验报告书</h2>
                  <div class="cert-code">检测备案号: SGS-AGRI-202611-00892</div>
                  <div class="cert-table-box">
                    <table class="cert-table">
                      <tbody>
                        <tr><td><strong>受检产品名称</strong></td><td>{{ detailProduct.title }}</td><td><strong>基地生产单位</strong></td><td>凉山盐源高山果蔬合作社</td></tr>
                        <tr><td><strong>抽检检测机构</strong></td><td>SGS通标标准技术服务 / 农业农村部质检中心</td><td><strong>检测日期</strong></td><td>2026-11-12</td></tr>
                        <tr><td><strong>筛查检测指标</strong></td><td>有机磷、有机氯、拟除虫菊酯等 320 项农药残留专项筛查</td><td><strong>检测结论</strong></td><td><span class="pass-tag">全部零检出 · 100% 合格</span></td></tr>
                      </tbody>
                    </table>
                  </div>
                  <div class="expert-sign-box">
                    <p><strong>质检首席专家评语：</strong>经质检中心实验室精密气相色谱/质谱多级筛查，320项指标全部未检出任何农药残留，符合绿色食品认证标准，达到生食与直供级品质。好产品，不烧心！</p>
                    <div class="sign-meta">签发专家：中检院绿色农副认证组 ｜ 2026年11月</div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- TAB 4: 一物一码溯源链 -->
            <el-tab-pane label="🔍 一物一码真实溯源链路" name="trace">
              <div class="tab-trace-content">
                <el-timeline>
                  <el-timeline-item timestamp="2026-03-15" placement="top" type="primary">
                    <h4>高原基地育苗与播种施肥</h4>
                    <p>施用羊粪发酵有机肥与微量元素，杜绝化学催熟化肥。</p>
                  </el-timeline-item>
                  <el-timeline-item timestamp="2026-06-20" placement="top" type="primary">
                    <h4>人工疏果与物理双层套袋</h4>
                    <p>高原纯净雪山水源灌溉，熊蜂天然授粉，套袋隔离外界灰尘与害虫。</p>
                  </el-timeline-item>
                  <el-timeline-item timestamp="2026-11-14 06:00" placement="top" type="success">
                    <h4>晨采摘收与人工糖度筛选</h4>
                    <p>测得果心平均糖度达 16.5°Brix，达特优级标准。</p>
                  </el-timeline-item>
                  <el-timeline-item timestamp="2026-11-14 10:30" placement="top" type="success">
                    <h4>顺丰冷链专车装箱发运</h4>
                    <p>入库预冷至 4℃，恒温气调专班冷链车运往全国枢纽。</p>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </el-dialog>

    <!-- 对话框 9.7: 天天领红包·神券立减弹窗 -->
    <el-dialog v-model="couponCenterVisible" title="🧧 不烧心·天天领红包 神券中心" width="540px" class="coupon-center-dialog">
      <div class="coupon-banner-top">
        <div class="c-title">好产品，不烧心 · 开市专享补贴包</div>
        <div class="c-sub">点击一键领取，结算收银台自动为您抵扣！</div>
      </div>
      <div class="coupon-cards-grid">
        <div v-for="c in platformCoupons" :key="c.id" :class="['coupon-ticket-card', c.claimed ? 'claimed' : '']">
          <div class="ticket-left">
            <span class="t-currency">¥</span>
            <span class="t-amount">{{ c.discount }}</span>
          </div>
          <div class="ticket-middle">
            <div class="t-name">{{ c.name }}</div>
            <div class="t-condition">{{ c.minSpend === 0 ? '无门槛使用' : `满 ¥${c.minSpend} 可用` }}</div>
            <div class="t-expiry"><el-tag size="small" type="danger">{{ c.tag }}</el-tag> {{ c.expiry }}</div>
          </div>
          <div class="ticket-right">
            <el-button 
              :type="c.claimed ? 'info' : 'danger'" 
              size="small" 
              round
              :disabled="c.claimed"
              @click="claimCoupon(c)"
            >
              {{ c.claimed ? '已领取' : '立即领取' }}
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="danger" size="large" @click="couponCenterVisible = false">完成并去挑选好物</el-button>
      </template>
    </el-dialog>

    <!-- 抽屉 9.8: 旺旺掌柜在线即时客服抽屉 -->
    <el-drawer v-model="wangwangVisible" title="💬 旺旺在线客服 · 掌柜值班中" size="420px" class="wangwang-drawer">
      <div class="wangwang-container">
        <!-- 客服名片 -->
        <div class="service-header-strip">
          <el-avatar :size="36" src="https://api.dicebear.com/7.x/adventurer/svg?seed=UncleFarm" />
          <div class="service-info">
            <div class="s-name">凉山盐源高山生态果蔬合作社 · 掌柜小美</div>
            <div class="s-status"><span class="green-dot"></span> 田间在线中 · 平均 3 秒极速响应</div>
          </div>
        </div>

        <!-- 当前咨询商品卡片 -->
        <div v-if="detailProduct" class="current-consult-prod">
          <img :src="detailProduct.coverImage" />
          <div class="cp-text">
            <div class="cp-title">{{ detailProduct.title }}</div>
            <div class="cp-price">¥{{ detailProduct.retailPrice }} / 份 (顺丰包邮)</div>
          </div>
          <el-button size="small" type="warning" plain @click="sendProductLinkToWangwang">发送链接</el-button>
        </div>

        <!-- 聊天气泡流 -->
        <div class="chat-bubble-stream" ref="wangwangScrollBox">
          <div v-for="(msg, mIdx) in wangwangMessages" :key="mIdx" :class="['bubble-row', msg.sender]">
            <el-avatar 
              :size="30" 
              :src="msg.sender === 'buyer' ? currentUser.avatar : 'https://api.dicebear.com/7.x/adventurer/svg?seed=UncleFarm'" 
            />
            <div class="bubble-content">
              <div class="bubble-time">{{ msg.time }}</div>
              <div class="bubble-text">{{ msg.content }}</div>
            </div>
          </div>
        </div>

        <!-- 常见问题快捷提问气泡 -->
        <div class="quick-question-pills">
          <span class="q-pill" @click="quickAskWangwang('顺丰包邮吗？什么时候发货？')">顺丰包邮吗？</span>
          <span class="q-pill" @click="quickAskWangwang('如果收到有坏果怎么赔偿？')">坏果怎么赔？</span>
          <span class="q-pill" @click="quickAskWangwang('是今年新采摘的带糖心吗？')">带糖心吗？</span>
          <span class="q-pill" @click="quickAskWangwang('大宗采购批发有优惠合同吗？')">大宗采购批发？</span>
        </div>

        <!-- 输入发送栏 -->
        <div class="wangwang-input-bar">
          <el-input 
            v-model="inputWangwangText" 
            placeholder="向合作社掌柜提问..." 
            @keyup.enter="sendCustomWangwang"
          />
          <el-button type="primary" @click="sendCustomWangwang">发 送</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 对话框 9.9: 顺丰生鲜冷链运输全程温控轨迹追踪弹窗 -->
    <el-dialog v-model="logisticsModalVisible" title="🚚 顺丰生鲜冷链全程温控实时轨迹" width="620px">
      <div v-if="currentTrackingOrder" class="logistics-container">
        <!-- 冷链状态看板 -->
        <div class="cold-chain-head-card">
          <div class="sf-logo-title">
            <span class="sf-brand">SF 顺丰速运</span>
            <span class="tracking-code">运单号: {{ currentTrackingOrder.order?.trackingNo || 'SF1482910394' }}</span>
          </div>
          <div class="temp-indicator">
            <span class="temp-badge">4.2℃ 恒温冷鲜</span>
            <span class="temp-text">全程冷链车温湿度传感器联机监控中</span>
          </div>
        </div>

        <!-- 顺丰运输路由时间轴 -->
        <div class="sf-timeline-box">
          <el-timeline>
            <el-timeline-item timestamp="2026-11-15 08:00" placement="top" type="success" size="large">
              <h4>派送中 · 顺丰朝阳酒仙桥营业点</h4>
              <p>派送小哥 王师傅 (电话: 138-1234-9988) 正在冷鲜专车派送，请保持手机畅通！</p>
            </el-timeline-item>
            <el-timeline-item timestamp="2026-11-15 05:20" placement="top" type="primary">
              <h4>到达北京顺义生鲜冷链一级枢纽</h4>
              <p>干线冷链集装箱完成卸车，智能气调分拣并调拨至末端配送网点。</p>
            </el-timeline-item>
            <el-timeline-item timestamp="2026-11-14 20:40" placement="top" type="primary">
              <h4>全货机航空干线冷链直飞</h4>
              <p>搭载顺丰航空 O3-6922 专机从成都双流直飞北京首都机场。</p>
            </el-timeline-item>
            <el-timeline-item timestamp="2026-11-14 13:10" placement="top" type="primary">
              <h4>成都双流冷链枢纽中心</h4>
              <p>经快速预冷检测合格，完成安检并打板入仓。</p>
            </el-timeline-item>
            <el-timeline-item timestamp="2026-11-14 08:30" placement="top" type="info">
              <h4>产地合作社揽收完成</h4>
              <p>四川凉山州盐源县合作社采摘入箱，顺丰冷链专班专车 (川W·8821冷) 已揽收。</p>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-dialog>

    <!-- 对话框 9.10: 支付成功与电子回执单弹窗 -->
    <el-dialog v-model="orderSuccessModalVisible" title="🎉 支付成功 · 产地顺丰直发通知" width="460px" align-center>
      <div class="order-success-panel">
        <el-icon class="success-icon-big" color="#2ED573"><CircleCheckFilled /></el-icon>
        <h2 class="success-title">支付成功！感谢支持源头合作社</h2>
        <p class="success-slogan">好产品，不烧心 ｜ 源头纯天然 绿色放心购</p>
        
        <div class="receipt-box">
          <div class="r-row"><span>订单编号：</span><strong>{{ lastCreatedOrderNo }}</strong></div>
          <div class="r-row"><span>顺丰冷链单号：</span><strong class="c-blue">{{ lastCreatedSfNo }}</strong></div>
          <div class="r-row"><span>发货基地：</span><span>四川凉山盐源高山生态果蔬合作社</span></div>
          <div class="r-row"><span>配送方式：</span><span>顺丰生鲜特快全程冷链包邮</span></div>
          <div class="r-row"><span>承诺时效：</span><span>48小时内现摘现发，坏果包赔</span></div>
        </div>

        <div class="receipt-actions">
          <el-button type="primary" size="large" @click="orderSuccessModalVisible = false; openLogistics({ order: { trackingNo: lastCreatedSfNo } })">
            <el-icon><Van /></el-icon> 查看顺丰冷链追踪
          </el-button>
          <el-button size="large" @click="orderSuccessModalVisible = false; activeTab = 'workbench'">
            查看我的订单
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 对话框 9.11: 买家五星好评与晒单弹窗 -->
    <el-dialog v-model="reviewModalVisible" title="⭐️ 农产品评价与晒单" width="480px">
      <el-form label-position="top">
        <el-form-item label="整体满意度 (好产品不烧心)">
          <el-rate v-model="reviewForm.rating" show-text :texts="['差评', '勉强', '一般', '推荐', '超赞！脆甜不烧心']" />
        </el-form-item>
        <el-form-item label="真实品尝体验与评价心得">
          <el-input 
            v-model="reviewForm.comment" 
            type="textarea" 
            :rows="4" 
            placeholder="分享果肉脆甜度、水分、顺丰冷链速度等体验，给其他吃客提供参考..." 
          />
        </el-form-item>
        <el-form-item label="晒单买家秀 (已自动关联产地实拍)">
          <div class="review-upload-preview">
            <img src="https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=200" />
            <img src="https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=200" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewModalVisible = false">取消</el-button>
        <el-button type="danger" @click="submitBuyerReview">提交五星好评</el-button>
      </template>
    </el-dialog>

    <!-- 对话框 9.12: 新增/管理收货地址弹窗 -->
    <el-dialog v-model="showAddressEditModal" title="📍 新增收货地址" width="480px">
      <el-form label-position="top">
        <el-form-item label="收货人姓名">
          <el-input v-model="newAddressForm.name" placeholder="请输入收件人真实姓名" />
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input v-model="newAddressForm.phone" placeholder="请输入11位手机号码" />
        </el-form-item>
        <el-form-item label="所在省市区">
          <el-input v-model="newAddressForm.province" placeholder="如：北京市海淀区" />
        </el-form-item>
        <el-form-item label="详细街道门牌号">
          <el-input v-model="newAddressForm.detail" placeholder="如：中关村南大街1号院5号楼801室" />
        </el-form-item>
        <el-form-item label="地址标签">
          <el-radio-group v-model="newAddressForm.tag" size="small">
            <el-radio-button label="家">家</el-radio-button>
            <el-radio-button label="公司">公司</el-radio-button>
            <el-radio-button label="仓库">仓库</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressEditModal = false">取消</el-button>
        <el-button type="primary" @click="saveNewAddress">保存并使用该地址</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import api from './api'
import { DEFAULT_37_PRODUCTS } from './mockData.js'

// 当前主 Tab: home | cart | live | wholesale | trace | workbench
const activeTab = ref('home')
const currentRegion = ref('北京')
const handleRegionChange = (r) => {
  currentRegion.value = r
  ElMessage.success(`已切换送达城市为：【${r}】，已为您匹配周边顺丰次日达原产地货源！`)
}

// ==========================================
// 淘宝收银台与收货地址管理
// ==========================================
const orderRemarkNote = ref('')
const selectedPayWay = ref('alipay')
const selectedAddressId = ref(1)
const showAddressEditModal = ref(false)
const newAddressForm = ref({
  name: '',
  phone: '',
  province: '北京市',
  city: '海淀区',
  detail: '',
  tag: '家'
})
const addressList = ref([
  { id: 1, name: '张小敏', phone: '186****5678', province: '北京市', city: '朝阳区', detail: '望京科技大厦B座1201室', isDefault: true, tag: '公司' },
  { id: 2, name: '李淘淘', phone: '139****1234', province: '上海市', city: '浦东新区', detail: '张江高科技园南区 淘鲜达直营仓', isDefault: false, tag: '仓库' },
  { id: 3, name: '王生鲜', phone: '135****8899', province: '成都市', city: '武侯区', detail: '天府三街腾讯大厦A座8层', isDefault: false, tag: '家' }
])

const saveNewAddress = () => {
  if (!newAddressForm.value.name || !newAddressForm.value.phone || !newAddressForm.value.detail) {
    ElMessage.warning('请填写完整的收件人姓名、电话与详细地址')
    return
  }
  const newId = Date.now()
  addressList.value.push({
    id: newId,
    name: newAddressForm.value.name,
    phone: newAddressForm.value.phone,
    province: newAddressForm.value.province,
    city: newAddressForm.value.city,
    detail: newAddressForm.value.detail,
    tag: newAddressForm.value.tag,
    isDefault: false
  })
  selectedAddressId.value = newId
  showAddressEditModal.value = false
  ElMessage.success('收货地址新增成功，已自动设为当前结算地址')
}

// 支付成功回执单
const orderSuccessModalVisible = ref(false)
const lastCreatedOrderNo = ref('TB20261114008921')
const lastCreatedSfNo = ref('SF148291039401')

// ==========================================
// 仿淘宝商品详情页状态 (Product Detail)
// ==========================================
const productDetailVisible = ref(false)
const detailProduct = ref(null)
const detailGalleryIndex = ref(0)
const selectedSkuIndex = ref(0)
const detailQuantity = ref(1)
const detailActiveTab = ref('desc')
const isCollected = ref(false)

const detailGalleryImages = computed(() => {
  if (!detailProduct.value) return []
  const baseImg = detailProduct.value.coverImage
  return [
    baseImg,
    'https://images.unsplash.com/photo-1542838132-92c53300491e?w=800',
    'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=800',
    'https://images.unsplash.com/photo-1595974482597-4b8da8879bc5?w=800'
  ]
})

const detailSkuList = computed(() => {
  if (!detailProduct.value) return []
  const p = detailProduct.value.retailPrice
  return [
    { name: '5斤精选大果·家庭尝鲜装', price: p, weight: '5斤', specTip: '现摘现发·果香浓郁' },
    { name: '10斤特惠大果·礼盒精装 (立省¥15)', price: +(p * 1.85).toFixed(1), weight: '10斤', specTip: '走亲访友精选礼盒' },
    { name: '20斤大宗整箱·批发特惠 (立省¥35)', price: +(p * 3.4).toFixed(1), weight: '20斤', specTip: '家庭囤货/餐饮直发' }
  ]
})

const currentSelectedSku = computed(() => {
  return detailSkuList.value[selectedSkuIndex.value] || { name: '5斤精选装', price: detailProduct.value?.retailPrice || 0, weight: '5斤' }
})

const sampleReviews = ref([
  {
    name: '北京吃客·小美',
    avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=Reviewer1',
    rate: 5,
    time: '2026-11-13',
    spec: '5斤精选大果·家庭尝鲜装',
    content: '顺丰冷链第二天一早就送到了！开箱果香味扑鼻，切开全是半透明的蜜心，又脆又甜水分超足，老人小孩都抢着吃，好产品真的不烧心！',
    images: [
      'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=200',
      'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=200'
    ]
  },
  {
    name: '上海淘淘·陈先生',
    avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=Reviewer2',
    rate: 5,
    time: '2026-11-12',
    spec: '10斤特惠大果·礼盒精装',
    content: '买来送长辈的，包装特别严实，五层加厚瓦楞箱没有任何磕碰。查了一物一码，确实是凉山盐源高山基地直采的，320项农残未检出吃着特别放心。',
    images: []
  },
  {
    name: '广州吃货老张',
    avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=Reviewer3',
    rate: 5,
    time: '2026-11-10',
    spec: '20斤大宗整箱·批发特惠',
    content: '合作社源头直发确实比超市便宜太多了！单果均匀分量足，已经推荐给单位同事团购了，回购第五次了。',
    images: [
      'https://images.unsplash.com/photo-1595974482597-4b8da8879bc5?w=200'
    ]
  }
])

const toggleFavorite = () => {
  isCollected.value = !isCollected.value
  ElMessage.success(isCollected.value ? '已成功收藏该产地好物！可随时在个人中心查看' : '已取消收藏')
}

// ==========================================
// 天天领红包与优惠券系统
// ==========================================
const couponCenterVisible = ref(false)
const platformCoupons = ref([
  { id: 1, name: '新客尝鲜无门槛红包', discount: 10, minSpend: 0, tag: '全场通用', expiry: '7天内有效', claimed: false },
  { id: 2, name: '时令鲜果满减优惠券', discount: 15, minSpend: 99, tag: '水果专享', expiry: '本周末前有效', claimed: false },
  { id: 3, name: '绿色有机放心消费券', discount: 30, minSpend: 199, tag: '全场通用', expiry: '30天内有效', claimed: false },
  { id: 4, name: 'B2B大宗采购补贴津贴', discount: 100, minSpend: 1000, tag: '大宗吨批', expiry: '本月内有效', claimed: false }
])

const claimCoupon = (c) => {
  c.claimed = true
  ElNotification({
    title: '🎉 领券成功！',
    message: `已为您发放【${c.name} (立减¥${c.discount})】，结算收银台将自动为您抵扣！`,
    type: 'success',
    duration: 3000
  })
}

// ==========================================
// 旺旺在线即时客服抽屉
// ==========================================
const wangwangVisible = ref(false)
const inputWangwangText = ref('')
const wangwangMessages = ref([
  { sender: 'service', time: '刚刚', content: '您好！我是凉山盐源高山果蔬生态合作社值班掌柜小美。好产品，不烧心！请问有什么关于果品、顺丰物流或大宗集采可以帮您解答的呢？' }
])

const quickAskWangwang = (q) => {
  wangwangMessages.value.push({ sender: 'buyer', time: '刚刚', content: q })
  setTimeout(() => {
    let reply = '亲亲，我们是产地直发，今天清晨采摘，下午顺丰冷链直发，保证新鲜！'
    if (q.includes('快递') || q.includes('包邮')) {
      reply = '亲，我们全场默认发【顺丰生鲜特快全程冷链】，全国大部分省市48小时内直达，全场包邮免运费哦！'
    } else if (q.includes('坏果') || q.includes('赔')) {
      reply = '亲亲放心，我们平台贯彻“好产品，不烧心”坏果秒赔承诺！如有坏果拍照发给客服，按坏果比例极速秒退款，绝不推脱！'
    } else if (q.includes('糖心') || q.includes('采摘')) {
      reply = '高原昼夜温差大、日照超2800小时，果子自然成熟糖心率高达90%以上，果肉酥脆化渣！'
    } else if (q.includes('大宗') || q.includes('批发')) {
      reply = '大宗整车/整吨直发支持在线签署《大宗意向电子合同》，提供企业对公结算与检验报告，可在‘B2B大宗采购中心’直接发起询价单！'
    }
    wangwangMessages.value.push({ sender: 'service', time: '刚刚', content: reply })
  }, 500)
}

const sendCustomWangwang = () => {
  if (!inputWangwangText.value.trim()) return
  const q = inputWangwangText.value.trim()
  wangwangMessages.value.push({ sender: 'buyer', time: '刚刚', content: q })
  inputWangwangText.value = ''
  setTimeout(() => {
    wangwangMessages.value.push({
      sender: 'service',
      time: '刚刚',
      content: `收到亲亲的咨询：“${q}”。合作社掌柜正在田间地头为您核对该批次鲜果数据，请放心选购！好产品，不烧心！`
    })
  }, 700)
}

const sendProductLinkToWangwang = () => {
  if (!detailProduct.value) return
  wangwangMessages.value.push({
    sender: 'buyer',
    time: '刚刚',
    content: `【咨询商品】${detailProduct.value.title} (单价: ¥${detailProduct.value.retailPrice} / 产地: ${detailProduct.value.originPlace})`
  })
  setTimeout(() => {
    wangwangMessages.value.push({
      sender: 'service',
      time: '刚刚',
      content: `亲，这款【${detailProduct.value.title.slice(0, 10)}...】是我们合作社当季热销爆款，320项农残零检出，现在下单赠送运费险与坏果包赔，性价比极高！`
    })
  }, 600)
}

// ==========================================
// 顺丰冷链物流与买家评价
// ==========================================
const logisticsModalVisible = ref(false)
const currentTrackingOrder = ref(null)
const openLogistics = (order) => {
  currentTrackingOrder.value = order
  logisticsModalVisible.value = true
}

const reviewModalVisible = ref(false)
const currentReviewOrder = ref(null)
const reviewForm = ref({ rating: 5, comment: '苹果非常脆甜多汁，半透明糖心清晰可见，顺丰冷链包装很完美无任何磕碰，全家人都特别喜欢！' })

const openReviewModal = (order) => {
  currentReviewOrder.value = order
  reviewModalVisible.value = true
}

const submitBuyerReview = () => {
  reviewModalVisible.value = false
  ElNotification({
    title: '评价晒单成功！',
    message: '感谢您对优质原产地农产品的真实评价与鼓励！好产品，不烧心',
    type: 'success'
  })
}

const confirmOrderReceived = (order) => {
  ElNotification({
    title: '确认收货成功！',
    message: '已确认收货，款项已安全划拨至种植合作社账户！',
    type: 'success'
  })
}

// 买家订单状态筛选
const buyerOrderFilter = ref('all')
const displayBuyerOrders = computed(() => {
  if (buyerOrderFilter.value === 'all') return buyerOrderList.value
  if (buyerOrderFilter.value === 'delivered') return buyerOrderList.value
  return buyerOrderList.value
})

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 登录与六大角色状态 (默认卡通头像)
const isLoggedIn = ref(true)
const currentRole = ref('CONSUMER')
const currentUser = ref({
  id: 6,
  username: 'consumer_user',
  realName: '不烧心金牌吃客小淘',
  roleType: 'CONSUMER',
  avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=FelixBuyer'
})

// 登录/注册弹窗与表单
const authModalVisible = ref(false)
const authActiveTab = ref('login')
const loginForm = ref({ username: 'consumer_user', password: '123' })
const registerForm = ref({
  username: '',
  password: '',
  realName: '',
  phone: '',
  roleType: 'CONSUMER'
})

// 6种角色的快捷预设 (全部使用现代扁平卡通头像)
const roleDemoPresets = ref([
  { role: 'CONSUMER', name: '普通买家', user: 'consumer_user', realName: '不烧心金牌吃客小淘', avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=FelixBuyer', icon: 'ShoppingBag' },
  { role: 'COOP', name: '种植合作社', user: 'coop_liangshan', realName: '凉山盐源高山生态合作社', avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=UncleFarm', icon: 'House' },
  { role: 'ANCHOR', name: '产地主播', user: 'anchor_xiaoli', realName: '川农小李·直采主播', avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=SunnyAnchor', icon: 'VideoCamera' },
  { role: 'WHOLESALE', name: '采购批发商', user: 'buyer_jingdong', realName: '鼎鲜优选全国集采部', avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=ManagerZhang', icon: 'OfficeBuilding' },
  { role: 'ADMIN', name: '平台管理员', user: 'admin', realName: '平台总控运营管家', avatar: 'https://api.dicebear.com/7.x/bottts/svg?seed=SuperAdmin', icon: 'Management' },
  { role: 'EXPERT', name: '农技质检专家', user: 'expert_zhijian', realName: '中检院绿色农副认证组', avatar: 'https://api.dicebear.com/7.x/bottts/svg?seed=QualityExpert', icon: 'Checked' }
])

// 搜索与品类
const searchType = ref('product')
const searchKeyword = ref('')
const currentCategory = ref('全部')
const presaleFilter = ref(null)

// 6大品种分类 (附带样品数量)
const categoryList = ref([
  { name: '水果', sub: '苹果/脐橙/荔枝/草莓/芒果 (12款)', icon: 'Apple' },
  { name: '蔬菜', sub: '西红柿/黄瓜/娃娃菜/紫薯 (7款)', icon: 'Dish' },
  { name: '粮油', sub: '五常大米/山茶油/小米 (5款)', icon: 'Box' },
  { name: '滋补', sub: '中宁枸杞/野山参/雪莲蜜 (6款)', icon: 'Grape' },
  { name: '禽畜', sub: '苏尼特羊排/走地土鸡 (5款)', icon: 'Food' },
  { name: '水产', sub: '阳澄湖大闸蟹/鲟鱼子/黄鱼 (5款)', icon: 'Fish' }
])

// 轮播图配置 (不烧心品牌)
const heroBanners = ref([
  {
    image: 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=1200',
    tag: '不烧心·产地直发盛典',
    title: '四川凉山盐源高山苹果直采季',
    desc: '海拔2600米纯净雪山水源灌溉 ｜ 脆甜爆汁半透明糖心 ｜ 好产品，不烧心',
    btnText: '立即抢先尝鲜'
  },
  {
    image: 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=1200',
    tag: '绿色有机认证',
    title: '寿光无公害自然熟铁皮番茄',
    desc: '熊蜂天然授粉拒绝催熟膨大 ｜ 权威农残320项零检出 ｜ 沙瓤浓郁小时候味道',
    btnText: '进入直采专区'
  },
  {
    image: 'https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=1200',
    tag: '大宗集采对接',
    title: '赣南脐橙吨级大宗集采开启',
    desc: '原产地合作社现摘直装 ｜ 在线提交吨级询价单 ｜ 电子意向合同在线签约',
    btnText: '发起大宗集采'
  }
])

// 产地快讯
const newsItems = ref([
  '“好产品，不烧心”品质宣言：全平台在售农特产农残抽检合格率达100%',
  '凉山盐源基地今日采收80吨，顺丰冷链专班专车直达发运',
  '鼎鲜优选大宗集采中心与赣南脐橙基地完成30吨电子意向合同签约',
  '中检院绿色农副认证专家对平台37处产地样品完成专项核验全部过审'
])

// 核心商品列表 (预置37款地标农产品，秒开即见，后端响应后自动无缝同步)
const productList = ref([...DEFAULT_37_PRODUCTS])

// 搜索与筛选动态计算 (保证0延迟高亮过滤)
const displayProducts = computed(() => {
  return productList.value.filter(p => {
    if (currentCategory.value !== '全部' && p.category !== currentCategory.value) {
      return false
    }
    if (presaleFilter.value !== null && p.isPresale !== presaleFilter.value) {
      return false
    }
    if (searchKeyword.value.trim()) {
      const kw = searchKeyword.value.trim().toLowerCase()
      const titleMatch = (p.title || '').toLowerCase().includes(kw)
      const originMatch = (p.originPlace || '').toLowerCase().includes(kw)
      const catMatch = (p.category || '').toLowerCase().includes(kw)
      if (!titleMatch && !originMatch && !catMatch) {
        return false
      }
    }
    return true
  })
})

const getCategoryCount = (cat) => {
  if (cat === '全部') return productList.value.length
  return productList.value.filter(x => x.category === cat).length
}

// ==========================================
// 购物车专属逻辑 (独立展示页面)
// ==========================================
const cartList = ref([
  {
    id: 1,
    title: '【不烧心严选】四川凉山盐源丑苹果 高原红富士 脆甜多汁带糖心 5斤装',
    originPlace: '四川凉山',
    retailPrice: 39.9,
    coverImage: 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600',
    quantity: 2,
    selected: true
  },
  {
    id: 13,
    title: '【有机直采】山东寿光自然熟铁皮西红柿 沙瓤爆汁生吃小番茄 4斤整箱',
    originPlace: '山东寿光',
    retailPrice: 29.8,
    coverImage: 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600',
    quantity: 1,
    selected: true
  }
])

const isAllCartSelected = computed({
  get: () => cartList.value.length > 0 && cartList.value.every(x => x.selected),
  set: (val) => {
    cartList.value.forEach(x => x.selected = val)
  }
})

const toggleSelectAllCart = (val) => {
  isAllCartSelected.value = val
}

const selectedCartCount = computed(() => {
  return cartList.value.filter(x => x.selected).reduce((acc, cur) => acc + cur.quantity, 0)
})

const selectedCartTotalMoney = computed(() => {
  const sum = cartList.value
    .filter(x => x.selected)
    .reduce((acc, cur) => acc + cur.retailPrice * cur.quantity, 0)
  return sum.toFixed(2)
})

const removeCartItem = (idx) => {
  cartList.value.splice(idx, 1)
  ElMessage.success('已从购物车移除该商品')
}

const clearEntireCart = () => {
  cartList.value = []
  ElMessage.info('购物车已清空')
}

const submitCartCheckout = () => {
  if (selectedCartCount.value === 0) {
    ElMessage.warning('请至少勾选一件要结算的农产品')
    return
  }
  currentCheckoutProduct.value = {
    id: 999,
    title: `不烧心购物车合并结算 (共 ${selectedCartCount.value} 件源头好物)`,
    originPlace: '全国绿色源头直供基地',
    retailPrice: parseFloat(selectedCartTotalMoney.value),
    coverImage: cartList.value.find(x => x.selected)?.coverImage || 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600'
  }
  isLiveDiscount.value = false
  checkoutModalVisible.value = true
}

// 直播与弹幕
const liveStreamVideo = ref(null)
const danmakuBox = ref(null)
const heartsContainer = ref(null)
const onlineViewerCount = ref(3528)
const liveLikeCount = ref(1892)
const isFollowed = ref(false)
const inputChatText = ref('')
const explainingProduct = ref(DEFAULT_37_PRODUCTS[0])
const isPipActive = ref(false)

const chatMessages = ref([
  { user: '北京吃客', content: '苹果带不带半透明糖心呀？' },
  { user: '主播-小李', content: '放心！不烧心品质保证，高原日照充足，切开全是蜜心！' },
  { user: '广州陈先生', content: '已经下了两箱，顺丰冷链非常快！' },
  { user: '杭州采购李总', content: '你们合作社大宗有冷链车专发吗？' }
])

// 溯源状态
const inputTraceNumber = ref('AGRI20261101001')
const currentTraceCode = ref('AGRI20261101001')
const traceTimelineData = ref([])

// 询价与订单
const b2bInquiryList = ref([])
const buyerOrderList = ref([])

// 弹窗状态
const publishModalVisible = ref(false)
const publishForm = ref({
  title: '',
  category: '水果',
  originPlace: '四川凉山盐源',
  retailPrice: 42.0,
  wholesalePrice: 2.6,
  stock: 6000,
  isPresale: 0,
  soilQuality: '海拔2600米弱酸红壤，有机质含量3.2%',
  testResult: '320项农残零检出，符合绿色食品标准'
})

const inquiryModalVisible = ref(false)
const targetInquiryProduct = ref(null)
const inquiryForm = ref({
  demandQuantity: 20,
  expectedUnitPrice: 2.3,
  deliveryTargetDate: '2026-12-10',
  packagingDemand: '顺丰恒温冷链直达，五层瓦楞加厚果筐'
})

const contractModalVisible = ref(false)
const activeContractData = ref(null)

const checkoutModalVisible = ref(false)
const currentCheckoutProduct = ref(null)
const isLiveDiscount = ref(false)

// 专家质检弹窗
const expertIssueCertModal = ref(false)
const selectedExpertProdId = ref(1)
const expertCertConclusion = ref('经过SGS与农业农村部质检中心320项农残专项筛查，未检出任何禁用农药残留，符合绿色食品标准。好产品，不烧心！')

// 倒计时
const countdownHours = ref('02')
const countdownMinutes = ref('45')
const countdownSeconds = ref('18')
let timer = null
let danmakuTimer = null

// 管理员统计报表
const adminStatsData = ref({
  provinceRank: [
    { province: '四川省', sales: '45.2万', topCrop: '盐源丑苹果' },
    { province: '山东省', sales: '38.6万', topCrop: '寿光西红柿' },
    { province: '江西省', sales: '26.4万', topCrop: '赣南脐橙' },
    { province: '黑龙江省', sales: '18.9万', topCrop: '五常大米' },
    { province: '内蒙古', sales: '15.2万', topCrop: '苏尼特羊肉' },
    { province: '江苏省', sales: '12.8万', topCrop: '阳澄湖大闸蟹' }
  ],
  categoryShare: [
    { name: '时令鲜果', value: 36 },
    { name: '绿色蔬菜', value: 24 },
    { name: '五谷粮油', value: 16 },
    { name: '特色滋补', value: 12 },
    { name: '生态禽畜', value: 8 },
    { name: '原产水产', value: 4 }
  ]
})

// ==========================================
// 业务逻辑实现
// ==========================================

const openAuthModal = (tabName) => {
  authActiveTab.value = tabName
  authModalVisible.value = true
}

const doLoginSubmit = async () => {
  if (!loginForm.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  try {
    const res = await api.post('/auth/login', {
      username: loginForm.value.username,
      password: loginForm.value.password || '123456'
    })
    if (res.data?.user) {
      currentUser.value = res.data.user
      currentRole.value = res.data.user.roleType
      isLoggedIn.value = true
      authModalVisible.value = false
      ElNotification({
        title: '登录成功',
        message: `欢迎回来，${currentUser.value.realName}！当前身份为【${getRoleName(currentRole.value)}】`,
        type: 'success'
      })
      loadInquiries()
      loadBuyerOrders()
    }
  } catch (err) {
    ElMessage.error(err.message || '登录失败')
  }
}

const doRegisterSubmit = async () => {
  if (!registerForm.value.username || !registerForm.value.password) {
    ElMessage.warning('请填写用户名与密码')
    return
  }
  try {
    const res = await api.post('/auth/register', registerForm.value)
    if (res.data?.user) {
      currentUser.value = res.data.user
      currentRole.value = res.data.user.roleType
      isLoggedIn.value = true
      authModalVisible.value = false
      ElNotification({
        title: '注册成功',
        message: `恭喜您成为【${getRoleName(currentRole.value)}】，好产品，不烧心！`,
        type: 'success'
      })
      loadInquiries()
      loadBuyerOrders()
    }
  } catch (err) {
    ElMessage.error(err.message || '注册失败')
  }
}

const quickLoginAs = (preset) => {
  currentUser.value = {
    id: 1,
    username: preset.user,
    realName: preset.realName,
    roleType: preset.role,
    avatar: preset.avatar
  }
  currentRole.value = preset.role
  isLoggedIn.value = true
  authModalVisible.value = false
  ElNotification({
    title: '角色已切换',
    message: `您当前正以【${preset.name}】卡通身份体验平台！好产品，不烧心`,
    type: 'success'
  })
  loadInquiries()
  loadBuyerOrders()
}

const handleLogout = () => {
  isLoggedIn.value = false
  currentUser.value = {
    id: 0,
    realName: '未登录访客',
    roleType: 'CONSUMER',
    avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=GuestVisitor'
  }
  currentRole.value = 'CONSUMER'
  ElMessage.info('您已退出登录')
}

const handleFastRoleSwitch = (role) => {
  const match = roleDemoPresets.value.find(x => x.role === role)
  if (match) {
    quickLoginAs(match)
  }
}

const getRoleName = (role) => {
  const map = {
    CONSUMER: '普通买家',
    COOP: '种植合作社',
    ANCHOR: '产地主播',
    WHOLESALE: '采购批发商',
    ADMIN: '平台运营管理',
    EXPERT: '农技质检专家'
  }
  return map[role] || '买家'
}

const getRoleTagType = (role) => {
  const map = {
    CONSUMER: 'primary',
    COOP: 'success',
    ANCHOR: 'danger',
    WHOLESALE: 'warning',
    ADMIN: 'info',
    EXPERT: 'success'
  }
  return map[role] || 'info'
}

const getRoleIcon = (role) => {
  const map = {
    CONSUMER: 'ShoppingBag',
    COOP: 'House',
    ANCHOR: 'VideoCamera',
    WHOLESALE: 'OfficeBuilding',
    ADMIN: 'Management',
    EXPERT: 'Checked'
  }
  return map[role] || 'User'
}

const getRoleWorkbenchName = () => {
  switch (currentRole.value) {
    case 'COOP': return '合作社产销工作台'
    case 'ANCHOR': return '主播带货控制台'
    case 'WHOLESALE': return '采购商集采中枢'
    case 'ADMIN': return '运营治理大屏'
    case 'EXPERT': return '农技质检服务台'
    default: return '我的淘宝'
  }
}

// 搜索交互
const triggerSearch = () => {
  activeTab.value = 'home'
  if (searchKeyword.value.trim()) {
    ElMessage.success(`已为您检索到 ${displayProducts.value.length} 款不烧心农产品！`)
  }
}

const quickSearchTag = (kw) => {
  searchKeyword.value = kw
  triggerSearch()
}

const clearSearch = () => {
  searchKeyword.value = ''
  currentCategory.value = '全部'
  presaleFilter.value = null
  ElMessage.info('已恢复查看全部农产品')
}

const resetAllFilterAndGoHome = () => {
  clearSearch()
  activeTab.value = 'home'
}

const selectCategory = (cat) => {
  currentCategory.value = cat
  activeTab.value = 'home'
}

const filterPresaleOnly = () => {
  presaleFilter.value = 1
  activeTab.value = 'home'
}

const applyFilters = () => {
  activeTab.value = 'home'
}

// 获取全量商品列表 (37款地标农产品)
const fetchProductsFromBackend = async () => {
  try {
    const res = await api.get('/product/list')
    if (res.data && res.data.length > 0) {
      productList.value = res.data
      if (!explainingProduct.value) {
        explainingProduct.value = productList.value[0]
      }
    }
  } catch (err) {
    console.error('获取商品失败', err)
  }
}

// 直播与爱心
const emitFloatingHeart = () => {
  liveLikeCount.value += 1
  if (!heartsContainer.value) return

  const heart = document.createElement('div')
  heart.className = 'floating-heart'
  heart.innerHTML = '❤️'

  const colors = ['#ff2a6d', '#ff5000', '#05d9e8', '#f9c80e', '#00e676']
  const randomColor = colors[Math.floor(Math.random() * colors.length)]
  const randomLeft = Math.floor(Math.random() * 60) + 10
  const randomRotate = Math.floor(Math.random() * 60) - 30

  heart.style.color = randomColor
  heart.style.left = randomLeft + 'px'
  heart.style.transform = `rotate(${randomRotate}deg)`

  heartsContainer.value.appendChild(heart)

  setTimeout(() => {
    if (heart && heart.parentNode) {
      heart.parentNode.removeChild(heart)
    }
  }, 1600)
}

const sendChat = () => {
  if (!inputChatText.value.trim()) return
  chatMessages.value.push({
    user: currentUser.value.realName.slice(0, 4),
    content: inputChatText.value.trim()
  })
  inputChatText.value = ''
  emitFloatingHeart()

  setTimeout(() => {
    if (danmakuBox.value) {
      danmakuBox.value.scrollTop = danmakuBox.value.scrollHeight
    }
  }, 100)
}

const enterPipMode = () => {
  isPipActive.value = true
  activeTab.value = 'home'
  ElMessage.success('已开启画中画悬浮小窗，您可在浏览货架或结算时持续收听讲解！')
}

const triggerLiveOrder = (item) => {
  currentCheckoutProduct.value = item
  isLiveDiscount.value = true
  checkoutModalVisible.value = true
}

const setAsExplainingProduct = (item) => {
  explainingProduct.value = item
  api.post('/live/switch-product', { liveId: 1, productId: item.id }).catch(() => {})
  ElMessage.success(`已将【${item.title.slice(0, 10)}】设为直播间当前讲解商品！`)
}

// 溯源码查询
const loadTraceRecords = async (code) => {
  currentTraceCode.value = code
  try {
    const res = await api.get('/product/trace', { params: { traceCode: code } })
    if (res.data && res.data.length > 0) {
      traceTimelineData.value = res.data
    }
  } catch (e) {}
  ElNotification({
    title: '溯源码核验成功',
    message: `已获取批次号 ${code} 的权威质检与全流程留痕！好产品，不烧心`,
    type: 'success'
  })
}

const openTraceModal = (productId) => {
  activeTab.value = 'trace'
  loadTraceRecords('AGRI20261101001')
}

// 大宗询价
const openInquiryModal = (product) => {
  targetInquiryProduct.value = product
  inquiryModalVisible.value = true
}

const submitInquiryApi = async () => {
  try {
    const payload = {
      buyerId: currentUser.value.id || 5,
      productId: targetInquiryProduct.value.id,
      demandQuantity: inquiryForm.value.demandQuantity,
      unit: '吨',
      expectedUnitPrice: inquiryForm.value.expectedUnitPrice,
      deliveryTargetDate: inquiryForm.value.deliveryTargetDate,
      packagingDemand: inquiryForm.value.packagingDemand
    }
    await api.post('/wholesale/inquiry', payload)
    inquiryModalVisible.value = false
    ElNotification({
      title: '大宗询价单提交成功',
      message: '合作社已收到您的吨级意向需求，稍后将在线审核与回复！',
      type: 'success'
    })
    loadInquiries()
  } catch (err) {
    ElMessage.error('提交失败：' + err.message)
  }
}

const coopAcceptInquiry = async (inquiryId) => {
  try {
    await api.post('/wholesale/coop-response', { inquiryId, accept: true })
    ElMessage.success('已接受采购商报价，已自动转入电子合同签署流程！')
    loadInquiries()
  } catch (err) {
    ElMessage.error(err.message)
  }
}

const viewContractModal = async (inquiryId) => {
  try {
    const res = await api.post('/wholesale/generate-contract', { inquiryId })
    if (res.data) {
      activeContractData.value = res.data
    }
  } catch (err) {
    activeContractData.value = {
      contractNo: 'BSX-CT-20261114008',
      buyerName: '鼎鲜优选全国连锁生鲜集采中心',
      sellerName: '凉山盐源高山生态果蔬合作社',
      productTitle: '四川凉山盐源高原红富士丑苹果 (特级果)',
      demandQuantity: '30.0 吨',
      agreedUnitPrice: '2.35 元/斤',
      deliveryTargetDate: '2026-12-15',
      packagingDemand: '五层加厚防震果筐，顺丰冷链恒温车直发',
      signTime: '2026-11-14 09:30:00',
      legalClause: '供需双方达成不烧心产地直供协议，首付款20%到账后启动采摘，冷链直发并经第三方SGS质检合格后付清尾款。好产品，不烧心！'
    }
  }
  contractModalVisible.value = true
}

const confirmSigningContract = (inquiryId) => {
  coopAcceptInquiry(inquiryId)
  viewContractModal(inquiryId)
}

// 合作社库存
const saveProductStock = async (row) => {
  try {
    await api.post('/product/update-stock', { productId: row.id, newStock: row.stock })
    ElMessage.success(`农产品【${row.title.slice(0, 8)}】库存已成功调控为 ${row.stock} ${row.unit}！`)
  } catch (err) {
    ElMessage.error('库存更新失败')
  }
}

const submitQuickPublishApi = async () => {
  if (!publishForm.value.title) {
    ElMessage.warning('请输入农产品标题')
    return
  }
  try {
    const payload = {
      coopId: 1,
      title: publishForm.value.title,
      category: publishForm.value.category,
      originPlace: publishForm.value.originPlace,
      retailPrice: publishForm.value.retailPrice,
      wholesalePrice: publishForm.value.wholesalePrice,
      minWholesaleAmount: 500,
      stock: publishForm.value.stock,
      unit: '斤',
      yieldCycle: '当季上市',
      totalYield: '年产200吨',
      coverImage: 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600',
      isPresale: publishForm.value.isPresale,
      soilQuality: publishForm.value.soilQuality,
      waterEnvironment: '高山优质水源',
      testResult: publishForm.value.testResult,
      originStory: '坚持纯天然绿色生态种植，好产品，不烧心！'
    }
    await api.post('/product/quick-publish', payload)
    publishModalVisible.value = false
    ElNotification({
      title: '极速上架成功',
      message: '农产品已成功录入系统货架与直播选品池！',
      type: 'success'
    })
    fetchProductsFromBackend()
  } catch (err) {
    ElMessage.error('上架失败：' + err.message)
  }
}

// 下单结算
const confirmPayAndCreateOrder = async () => {
  try {
    const isCartMerge = currentCheckoutProduct.value?.id === 999
    const prodId = isCartMerge ? (cartList.value.find(x => x.selected)?.id || 1) : (currentCheckoutProduct.value.id || 1)
    const addr = addressList.value.find(x => x.id === selectedAddressId.value) || addressList.value[0]
    
    const payload = {
      buyerId: currentUser.value.id || 6,
      productId: prodId,
      quantity: currentCheckoutProduct.value?.buyQuantity || 1,
      orderType: isLiveDiscount.value ? 2 : 1,
      receiverName: addr.name,
      receiverPhone: addr.phone,
      receiverAddress: `${addr.province} ${addr.city} ${addr.detail}`
    }
    await api.post('/order/create', payload)
    
    // 生成真实格式订单号与顺丰单号
    lastCreatedOrderNo.value = 'TB' + Date.now() + Math.floor(Math.random() * 899 + 100)
    lastCreatedSfNo.value = 'SF' + (1400000000000 + Math.floor(Math.random() * 899999999999))
    
    checkoutModalVisible.value = false
    // 从购物车移除已结算商品
    if (isCartMerge) {
      cartList.value = cartList.value.filter(x => !x.selected)
    }
    
    orderSuccessModalVisible.value = true
    
    fetchProductsFromBackend()
    loadBuyerOrders()
  } catch (err) {
    ElMessage.error(err.message || '下单失败')
  }
}

const addToCart = (product) => {
  const existing = cartList.value.find(x => x.id === product.id)
  if (existing) {
    existing.quantity += 1
  } else {
    cartList.value.push({
      id: product.id,
      title: product.title,
      originPlace: product.originPlace,
      retailPrice: product.retailPrice,
      coverImage: product.coverImage,
      quantity: 1,
      selected: true
    })
  }
  ElNotification({
    title: '已加入购物车',
    message: `已将【${product.title.slice(0, 10)}...】加入购物车，可随时去统一结算！`,
    type: 'success',
    duration: 2000
  })
}

// 专家质检
const submitExpertCert = () => {
  expertIssueCertModal.value = false
  ElNotification({
    title: '检测证书签发成功',
    message: '农产品已被加盖“不烧心·国家绿色食品认证”官方印章，全网公示！',
    type: 'success'
  })
}

const handleFastCertify = (product) => {
  ElMessage.success(`已为【${product.title.slice(0, 8)}】签发SGS 320项农残零检出认证合格证书！`)
}

// 加载询价与订单
const loadInquiries = async () => {
  try {
    const res = await api.get('/wholesale/my-inquiries', {
      params: { userId: currentUser.value.id, roleType: currentRole.value }
    })
    if (res.data) {
      b2bInquiryList.value = res.data
    }
  } catch (e) {}
}

const loadBuyerOrders = async () => {
  try {
    const res = await api.get('/order/my-orders', {
      params: { buyerId: currentUser.value.id }
    })
    if (res.data) {
      buyerOrderList.value = res.data
    }
  } catch (e) {}
}

const toggleProductStatusApi = async (row) => {
  const newStatus = row.status === 1 ? 2 : 1
  try {
    await api.post('/admin/product-status', { productId: row.id, status: newStatus })
    row.status = newStatus
    ElMessage[newStatus === 2 ? 'warning' : 'success'](
      newStatus === 2 ? '该违规商品已被强制下架封禁！' : '该商品已恢复上架销售！'
    )
  } catch (e) {
    ElMessage.error('治理操作失败')
  }
}

const requestRefund = async (orderId) => {
  try {
    await api.post('/order/refund', { orderId, reason: '生鲜磕碰坏果极速退款' })
    ElMessage.success('生鲜坏果极速退款已受理工单，款项将在原路退回！不烧心，售后无忧')
    loadBuyerOrders()
  } catch (e) {
    ElMessage.error('售后申请失败')
  }
}

// 开启仿淘宝丰富商品详情页
const openProductDetail = (product) => {
  detailProduct.value = product
  selectedSkuIndex.value = 0
  detailQuantity.value = 1
  detailGalleryIndex.value = 0
  detailActiveTab.value = 'desc'
  isCollected.value = false
  productDetailVisible.value = true
}

// 快速立即购买 (直接拉起收银台)
const quickBuyProduct = (product) => {
  detailProduct.value = product
  selectedSkuIndex.value = 0
  detailQuantity.value = 1
  currentCheckoutProduct.value = {
    ...product,
    skuName: '5斤精选大果·家庭尝鲜装',
    buyQuantity: 1,
    retailPrice: product.retailPrice
  }
  isLiveDiscount.value = false
  checkoutModalVisible.value = true
}

// 在详情页中加入购物车
const addDetailToCart = () => {
  if (!detailProduct.value) return
  const sku = currentSelectedSku.value
  const existing = cartList.value.find(x => x.id === detailProduct.value.id && x.skuName === sku.name)
  if (existing) {
    existing.quantity += detailQuantity.value
  } else {
    cartList.value.push({
      id: detailProduct.value.id,
      title: detailProduct.value.title,
      skuName: sku.name,
      originPlace: detailProduct.value.originPlace,
      retailPrice: sku.price,
      coverImage: detailProduct.value.coverImage,
      quantity: detailQuantity.value,
      selected: true
    })
  }
  ElNotification({
    title: '已加入购物车！',
    message: `已将【${detailProduct.value.title.slice(0, 10)}...】(${sku.name} x${detailQuantity.value}) 加入购物车！`,
    type: 'success',
    duration: 2500
  })
}

// 在详情页中立即购买
const buyDetailNow = () => {
  if (!detailProduct.value) return
  const sku = currentSelectedSku.value
  currentCheckoutProduct.value = {
    ...detailProduct.value,
    skuName: sku.name,
    buyQuantity: detailQuantity.value,
    retailPrice: sku.price
  }
  isLiveDiscount.value = false
  productDetailVisible.value = false
  checkoutModalVisible.value = true
}

// 联系掌柜
const contactShopSeller = () => {
  wangwangVisible.value = true
}

const openVisitBookingModal = (product) => {
  ElMessage.success(`已向【${product.originPlace}】基地提交实地看样预约，合作社将联系您安排接待！`)
}

const handleBannerClick = (banner) => {
  if (banner.title.includes('大宗')) {
    activeTab.value = 'wholesale'
  } else {
    activeTab.value = 'home'
  }
}

// 定时任务
onMounted(() => {
  fetchProductsFromBackend()
  loadInquiries()
  loadBuyerOrders()
  loadTraceRecords('AGRI20261101001')

  let seconds = 3600 * 2 + 45 * 60 + 18
  timer = setInterval(() => {
    seconds--
    if (seconds <= 0) seconds = 3600 * 3
    const h = Math.floor(seconds / 3600)
    const m = Math.floor((seconds % 3600) / 60)
    const s = seconds % 60
    countdownHours.value = String(h).padStart(2, '0')
    countdownMinutes.value = String(m).padStart(2, '0')
    countdownSeconds.value = String(s).padStart(2, '0')
  }, 1000)

  const autoDanmakus = [
    { user: '山东老王', content: '西红柿口感酸甜比怎么样？' },
    { user: '系统提醒', content: '🎉 用户【杭州采购】进入了田间直播间' },
    { user: '主播-小李', content: '好产品，不烧心！今天现摘现发，下午全部顺丰冷链装车！' },
    { user: '江苏小美', content: '在购物车直接全选下单了，期待发货！' },
    { user: '大宗采购群', content: '收到合同，定金已经走对公汇出。' }
  ]
  let dIdx = 0
  danmakuTimer = setInterval(() => {
    if (chatMessages.value.length > 20) {
      chatMessages.value.shift()
    }
    chatMessages.value.push(autoDanmakus[dIdx % autoDanmakus.length])
    dIdx++
    onlineViewerCount.value += Math.floor(Math.random() * 5) - 2
    emitFloatingHeart()
    if (danmakuBox.value) {
      danmakuBox.value.scrollTop = danmakuBox.value.scrollHeight
    }
  }, 3500)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (danmakuTimer) clearInterval(danmakuTimer)
})
</script>

<style scoped>
/* ============================================================== */
/* 仿淘宝电商高级交互与详情页视觉体系                              */
/* ============================================================== */

/* 1. 顶部城市切换与快捷菜单 */
.region-dropdown {
  display: inline-flex;
  align-items: center;
}
.region-pill.interactive {
  cursor: pointer;
  background: rgba(255, 80, 0, 0.08);
  color: #ff5000;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}
.region-pill.interactive:hover {
  background: #ff5000;
  color: #ffffff;
}

.top-quick-links {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-right: 4px;
  white-space: nowrap !important;
  flex-shrink: 0 !important;
}
.quick-link {
  font-size: 11px;
  color: #666;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  line-height: 1;
  white-space: nowrap !important;
  flex-shrink: 0 !important;
  transition: color 0.2s;
}
.quick-link:hover {
  color: #ff5000;
}
.quick-link .c-red {
  color: #ff5000;
  margin-left: 1px;
  font-weight: 700;
}
.qr-popover-box {
  text-align: center;
  padding: 8px 4px;
}
.qr-popover-box .qr-img {
  width: 120px;
  height: 120px;
  margin: 0 auto;
  display: block;
}
.qr-popover-box .qr-text {
  font-size: 13px;
  font-weight: bold;
  color: #333;
  margin-top: 6px;
}
.qr-popover-box .qr-sub {
  font-size: 11px;
  color: #2ED573;
  font-weight: 600;
}

/* 2. 淘宝式【天天领红包】促销横幅 (不占全屏，宽度和整体适配对齐) */
.taobao-redpack-banner {
  background: linear-gradient(90deg, #ff4e00 0%, #ff1f00 50%, #ff5000 100%);
  color: #ffffff;
  padding: 8px 16px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(255, 31, 0, 0.2);
  max-width: 1440px;
  width: 95%;
  margin: 10px auto;
  border-radius: 8px;
  box-sizing: border-box;
}
@media (min-width: 1680px) {
  .taobao-redpack-banner {
    max-width: 1560px;
    width: 95%;
  }
}
@media (max-width: 1280px) {
  .taobao-redpack-banner {
    width: 98%;
  }
}
.redpack-flex {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.redpack-left {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}
.redpack-badge {
  background: #ffd100;
  color: #d63031;
  font-weight: 900;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.15);
}
.redpack-title {
  font-weight: 600;
  letter-spacing: 0.3px;
}
.claim-btn-pulse {
  background: #ffffff;
  color: #ff3800;
  border: none;
  font-weight: 800;
  font-size: 12px;
  padding: 5px 14px;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 6px rgba(0,0,0,0.15);
}
.claim-btn-pulse:hover {
  transform: scale(1.05);
  background: #fff8e1;
}

/* 3. 商品卡片电商属性增强 */
.goods-sales-metrics {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 4px 0 6px 0;
  font-size: 11px;
}
.goods-sales-metrics .sales-num {
  color: #999;
}
.goods-sales-metrics .rating-num {
  color: #ff5000;
  font-weight: 600;
}
.goods-sales-metrics .sf-pill {
  background: #e6f7ff;
  color: #1890ff;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: bold;
}
.goods-price-section .orig-price {
  font-size: 11px;
  color: #999;
  text-decoration: line-through;
  margin-left: 6px;
}
.card-action-btns {
  display: flex;
  align-items: center;
  gap: 6px;
}
.card-action-btns .quick-buy-btn {
  background: #ff5000;
  border-color: #ff5000;
  color: #fff;
  font-weight: bold;
}

/* 4. 淘宝右侧固定悬浮工具栏 */
.taobao-float-toolstrip {
  position: fixed;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: #ffffff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  border-radius: 8px;
  padding: 8px 4px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 1999;
}
.toolstrip-item {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 6px;
  border-radius: 6px;
  transition: all 0.2s;
}
.toolstrip-item:hover {
  background: #fff0ea;
}
.tool-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
}
.tool-avatar {
  border: 2px solid #ff5000;
}
.tool-label {
  font-size: 11px;
  color: #555;
  font-weight: 500;
}
.toolstrip-item:hover .tool-label {
  color: #ff5000;
}
.cart-cell .tool-badge {
  color: #ff5000;
}
.tool-cell.red-cell {
  color: #e74c3c;
}
.tool-cell.green-cell {
  color: #27ae60;
}
.top-cell {
  border-top: 1px solid #f0f0f0;
  margin-top: 4px;
}

/* 5. 仿淘宝商品详情页大弹窗 */
.taobao-detail-dialog .el-dialog__header {
  padding: 10px 20px;
  margin-right: 0;
  border-bottom: 1px solid #f0f0f0;
}
.detail-page-container {
  padding: 0 4px;
}
.detail-breadcrumb {
  font-size: 12px;
  color: #666;
  margin-bottom: 16px;
}
.detail-breadcrumb .sep {
  margin: 0 6px;
  color: #ccc;
}
.detail-breadcrumb .cur {
  color: #ff5000;
  font-weight: 600;
}

.detail-top-section {
  display: flex;
  gap: 28px;
}
.detail-left-gallery {
  width: 400px;
  flex-shrink: 0;
}
.main-preview-box {
  width: 100%;
  height: 380px;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  background: #f7f7f7;
  border: 1px solid #eee;
}
.main-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}
.main-preview-box:hover .main-preview-img {
  transform: scale(1.08);
}
.trust-corner-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(46, 213, 115, 0.95);
  color: #fff;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 800;
  backdrop-filter: blur(4px);
}
.thumb-selector-row {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}
.thumb-box {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}
.thumb-box.active {
  border-color: #ff5000;
}
.thumb-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-coop-card {
  margin-top: 16px;
  padding: 12px;
  background: #f9fbf9;
  border: 1px solid #e1efe1;
  border-radius: 10px;
}
.coop-card-head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.coop-card-info .name {
  font-size: 13px;
  font-weight: bold;
  color: #2c3e50;
}
.coop-card-info .auth-pill {
  font-size: 11px;
  color: #27ae60;
  margin-top: 2px;
}
.coop-card-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.detail-right-info {
  flex: 1;
}
.detail-title-box .brand-tag {
  background: #ff5000;
  color: #fff;
  font-size: 11px;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 3px;
  margin-right: 6px;
}
.detail-title-box .geo-tag {
  background: #e8f5e9;
  color: #2e7d32;
  font-size: 11px;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 3px;
}
.product-main-title {
  font-size: 20px;
  font-weight: 900;
  color: #111;
  margin: 8px 0 6px 0;
  line-height: 1.4;
}
.product-sub-title {
  font-size: 12px;
  color: #777;
  margin-bottom: 12px;
}

.taobao-price-banner {
  background: linear-gradient(135deg, #fff5f0 0%, #ffece4 100%);
  border-radius: 10px;
  padding: 14px 18px;
  border: 1px solid #ffd8c7;
  margin-bottom: 14px;
}
.price-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.subsidy-label {
  background: #ff5000;
  color: #fff;
  font-size: 11px;
  font-weight: 800;
  padding: 2px 8px;
  border-radius: 4px;
}
.subsidy-tip {
  font-size: 12px;
  color: #e65100;
  font-weight: 600;
}
.price-main-row {
  display: flex;
  align-items: baseline;
  margin: 6px 0;
}
.p-currency {
  font-size: 16px;
  color: #ff5000;
  font-weight: bold;
}
.p-amount {
  font-size: 32px;
  color: #ff5000;
  font-weight: 900;
  letter-spacing: -1px;
}
.p-decimal {
  font-size: 18px;
  color: #ff5000;
  font-weight: bold;
}
.p-orig {
  font-size: 13px;
  color: #999;
  text-decoration: line-through;
  margin-left: 12px;
}
.p-hot-tag {
  margin-left: auto;
  background: #ffecb3;
  color: #d84315;
  font-size: 11px;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 4px;
}
.price-meta-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #666;
  border-top: 1px dashed #ffd8c7;
  padding-top: 8px;
  margin-top: 6px;
}

.detail-coupon-row,
.detail-delivery-row,
.detail-sku-row,
.detail-qty-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 14px;
  font-size: 13px;
}
.detail-coupon-row .label,
.detail-delivery-row .label,
.detail-sku-row .label,
.detail-qty-row .label {
  width: 76px;
  color: #888;
  flex-shrink: 0;
  padding-top: 4px;
}
.coupon-tag-list {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.coupon-chip {
  background: #ffebe8;
  color: #ff3800;
  border: 1px dashed #ff5000;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
}
.more-coupon-link {
  color: #ff5000;
  font-size: 12px;
  cursor: pointer;
  font-weight: bold;
}

.delivery-content {
  font-size: 13px;
  color: #333;
}
.service-guarantees {
  display: flex;
  gap: 14px;
  margin-top: 6px;
  font-size: 11px;
  color: #27ae60;
  font-weight: 600;
}

.sku-cards-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}
.sku-card-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.sku-card-btn.active {
  border-color: #ff5000;
  background: #fff8f5;
  box-shadow: 0 0 0 1px #ff5000 inset;
}
.sku-card-btn .sku-name {
  font-weight: 700;
  color: #333;
}
.sku-card-btn .sku-price {
  color: #ff5000;
  font-weight: 800;
}
.sku-card-btn .sku-tip {
  font-size: 11px;
  color: #888;
}

.stock-hint {
  font-size: 12px;
  color: #999;
  margin-left: 14px;
  align-self: center;
}

.detail-action-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
}
.btn-cart-lg {
  background: #ff9400;
  border-color: #ff9400;
  color: #fff;
  font-weight: bold;
  flex: 1.2;
}
.btn-buy-lg {
  background: #ff5000;
  border-color: #ff5000;
  color: #fff;
  font-weight: bold;
  flex: 1.2;
}
.btn-chat-lg {
  color: #ff5000;
  border-color: #ffd8c7;
  background: #fff8f5;
}

.detail-bottom-tabs {
  margin-top: 30px;
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}
.tab-desc-content .desc-card-block {
  background: #fdfdfd;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 14px;
}
.desc-card-block h3 {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #2c3e50;
}
.desc-card-block p {
  font-size: 13px;
  color: #555;
  line-height: 1.7;
  margin: 4px 0;
}

.review-score-banner {
  display: flex;
  align-items: center;
  gap: 24px;
  background: #fafafa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}
.score-circle {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.score-circle .num {
  font-size: 28px;
  font-weight: 900;
  color: #ff5000;
}
.score-circle .sub {
  font-size: 11px;
  color: #999;
}
.impress-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.impress-tags .tag {
  background: #fff0ea;
  color: #ff5000;
  padding: 4px 10px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 600;
}

.buyer-review-list .buyer-rev-item {
  border-bottom: 1px solid #f0f0f0;
  padding: 14px 0;
}
.rev-user {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-meta .uname {
  font-weight: bold;
  font-size: 13px;
  margin-right: 8px;
}
.rev-user .rev-time {
  margin-left: auto;
  font-size: 12px;
  color: #aaa;
}
.rev-spec {
  font-size: 11px;
  color: #999;
  margin: 4px 0;
}
.rev-text {
  font-size: 13px;
  color: #333;
  line-height: 1.6;
}
.rev-imgs {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}
.rev-imgs img {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  object-fit: cover;
}

.cert-formal-sheet {
  border: 2px solid #27ae60;
  background: #fdfdfd;
  padding: 24px;
  border-radius: 8px;
  position: relative;
}
.cert-seal {
  position: absolute;
  top: 20px;
  right: 24px;
  width: 90px;
  height: 90px;
  border: 3px solid #27ae60;
  border-radius: 50%;
  color: #27ae60;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: rotate(-15deg);
  opacity: 0.85;
}
.cert-table {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}
.cert-table td {
  border: 1px solid #ddd;
  padding: 8px 12px;
  font-size: 13px;
}
.pass-tag {
  color: #27ae60;
  font-weight: bold;
}
.expert-sign-box {
  background: #f4fbf4;
  padding: 12px;
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.6;
}

/* 6. 天天领红包神券中心弹窗 */
.coupon-center-dialog .coupon-banner-top {
  text-align: center;
  margin-bottom: 16px;
}
.coupon-banner-top .c-title {
  font-size: 18px;
  font-weight: 900;
  color: #ff3800;
}
.coupon-banner-top .c-sub {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
}
.coupon-cards-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.coupon-ticket-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #fff5f2 0%, #ffe9e2 100%);
  border: 1px solid #ffcdbc;
  border-radius: 10px;
  padding: 12px 16px;
  position: relative;
}
.coupon-ticket-card.claimed {
  opacity: 0.7;
}
.ticket-left {
  display: flex;
  align-items: baseline;
  width: 90px;
  color: #ff3800;
}
.ticket-left .t-currency {
  font-size: 16px;
  font-weight: bold;
}
.ticket-left .t-amount {
  font-size: 36px;
  font-weight: 900;
  letter-spacing: -1px;
}
.ticket-middle {
  flex: 1;
}
.ticket-middle .t-name {
  font-size: 14px;
  font-weight: bold;
  color: #222;
}
.ticket-middle .t-condition {
  font-size: 12px;
  color: #e65100;
  margin: 2px 0;
}
.ticket-middle .t-expiry {
  font-size: 11px;
  color: #888;
}

/* 7. 旺旺在线即时客服抽屉 */
.wangwang-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.service-header-strip {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.service-info .s-name {
  font-size: 13px;
  font-weight: bold;
  color: #333;
}
.service-info .s-status {
  font-size: 11px;
  color: #27ae60;
  display: flex;
  align-items: center;
  gap: 4px;
}
.green-dot {
  width: 7px;
  height: 7px;
  background: #27ae60;
  border-radius: 50%;
  display: inline-block;
}

.current-consult-prod {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fbfbfb;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 8px;
  margin: 10px 0;
}
.current-consult-prod img {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  object-fit: cover;
}
.current-consult-prod .cp-text {
  flex: 1;
}
.cp-text .cp-title {
  font-size: 12px;
  font-weight: 600;
  color: #333;
}
.cp-text .cp-price {
  font-size: 12px;
  color: #ff5000;
  font-weight: bold;
}

.chat-bubble-stream {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.bubble-row {
  display: flex;
  gap: 8px;
}
.bubble-row.buyer {
  flex-direction: row-reverse;
}
.bubble-content {
  max-width: 75%;
}
.bubble-time {
  font-size: 10px;
  color: #aaa;
  margin-bottom: 2px;
}
.bubble-row.buyer .bubble-time {
  text-align: right;
}
.bubble-text {
  padding: 8px 12px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
}
.bubble-row.service .bubble-text {
  background: #f0f0f0;
  color: #333;
  border-top-left-radius: 2px;
}
.bubble-row.buyer .bubble-text {
  background: #ff5000;
  color: #fff;
  border-top-right-radius: 2px;
}

.quick-question-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 0;
  border-top: 1px solid #f0f0f0;
}
.q-pill {
  background: #f5f5f5;
  color: #666;
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.q-pill:hover {
  background: #fff0ea;
  color: #ff5000;
}

.wangwang-input-bar {
  display: flex;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid #eee;
}

/* 8. 顺丰冷链物流与回执单 */
.cold-chain-head-card {
  background: #000;
  color: #fff;
  border-radius: 8px;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.sf-brand {
  font-size: 18px;
  font-weight: 900;
  color: #e60012;
  margin-right: 12px;
}
.tracking-code {
  font-size: 13px;
  color: #ccc;
}
.temp-badge {
  background: #2ed573;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}
.temp-text {
  font-size: 11px;
  color: #aaa;
  display: block;
  margin-top: 2px;
}

.order-success-panel {
  text-align: center;
  padding: 10px 0;
}
.success-icon-big {
  font-size: 56px;
}
.success-title {
  font-size: 20px;
  font-weight: bold;
  color: #2c3e50;
  margin: 10px 0 4px 0;
}
.success-slogan {
  font-size: 12px;
  color: #27ae60;
  font-weight: 600;
  margin-bottom: 16px;
}
.receipt-box {
  background: #f9fbf9;
  border: 1px solid #e1efe1;
  border-radius: 8px;
  padding: 12px 16px;
  text-align: left;
  font-size: 13px;
  margin-bottom: 20px;
}
.receipt-box .r-row {
  margin: 6px 0;
  display: flex;
  justify-content: space-between;
}
.receipt-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

/* 9. 仿淘宝收银台增强 */
.cashier-addr-section {
  background: #fafafa;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px 14px;
  margin-bottom: 12px;
}
.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  font-weight: bold;
  margin-bottom: 8px;
}
.addr-select-cards {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.addr-item-radio {
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}
.addr-item-radio.active {
  border-color: #ff5000;
  background: #fff8f5;
  box-shadow: 0 0 0 1px #ff5000 inset;
}
.addr-tag-name {
  font-size: 13px;
  margin-bottom: 2px;
}
.addr-text {
  font-size: 12px;
  color: #666;
}

.cashier-service-box,
.cashier-discounts-box {
  background: #fafafa;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px 14px;
  margin-top: 10px;
  font-size: 12px;
}
.service-row,
.disc-row {
  display: flex;
  justify-content: space-between;
  margin: 5px 0;
}
.c-green {
  color: #27ae60;
  font-weight: bold;
}
.c-red {
  color: #ff5000;
  font-weight: bold;
}
.c-blue {
  color: #1890ff;
  font-weight: bold;
}

.cashier-remark-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  font-size: 13px;
}
.cashier-pay-ways {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  font-size: 13px;
}

/* 10. 买家订单卡片流 */
.order-status-tabs-row {
  margin-bottom: 14px;
}
.taobao-order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.taobao-order-card {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}
.order-header-bar {
  background: #f7f7f7;
  padding: 8px 14px;
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 14px;
}
.order-header-bar .o-no {
  color: #333;
}
.order-header-bar .o-shop {
  font-weight: bold;
  color: #444;
}
.order-header-bar .o-status {
  margin-left: auto;
}
.order-body-grid {
  display: flex;
  align-items: center;
  padding: 14px;
  gap: 16px;
}
.order-prod-item {
  display: flex;
  gap: 12px;
  flex: 2;
}
.order-prod-item img {
  width: 70px;
  height: 70px;
  border-radius: 6px;
  object-fit: cover;
}
.order-prod-item .p-meta .p-title {
  font-size: 13px;
  font-weight: bold;
  color: #333;
}
.order-prod-item .p-meta .p-sku {
  font-size: 11px;
  color: #888;
  margin: 3px 0;
}
.order-receiver-box {
  flex: 1.2;
  font-size: 12px;
  color: #555;
  border-left: 1px solid #f0f0f0;
  padding-left: 14px;
}
.order-receiver-box .addr-line {
  color: #999;
  font-size: 11px;
}
.order-price-col {
  flex: 1;
  text-align: center;
  border-left: 1px solid #f0f0f0;
}
.order-price-col .price-val {
  font-size: 16px;
  font-weight: 900;
  color: #333;
}
.order-price-col .ship-tip {
  font-size: 11px;
  color: #999;
}
.order-actions-col {
  display: flex;
  flex-direction: column;
  gap: 6px;
  border-left: 1px solid #f0f0f0;
  padding-left: 14px;
}

/* ============================================================== */
/* “不烧心”经典视觉体系规范                                        */
/* ============================================================== */
.taobao-root {
  min-height: 100vh;
  background-color: #f4f5f7;
  color: #333333;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "PingFang SC", "Microsoft YaHei", sans-serif;
}

.site-container {
  max-width: 1440px;
  width: 95%;
  margin: 0 auto;
  padding: 0 16px;
  box-sizing: border-box;
}
@media (min-width: 1680px) {
  .site-container {
    max-width: 1560px;
    width: 95%;
  }
}
@media (max-width: 1280px) {
  .site-container {
    width: 98%;
    padding: 0 10px;
  }
}

/* 顶部 Bar (精致11px小字体，单行防换行，纯正淘宝电商质感) */
.pc-top-bar {
  background-color: #f5f5f5;
  border-bottom: 1px solid #e5e5e5;
  font-size: 11px;
  color: #666;
  line-height: 1;
}
.top-bar-inner {
  height: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.top-bar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  white-space: nowrap !important;
  flex-shrink: 0 !important;
}
.region-pill {
  color: #ff5000;
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap !important;
  flex-shrink: 0 !important;
  cursor: pointer;
}
.brand-slogan-pill {
  background: #e8f5e9;
  color: #2e7d32;
  padding: 2px 7px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 3px;
  white-space: nowrap !important;
  flex-shrink: 0 !important;
}
.auth-bar-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  white-space: nowrap !important;
  flex-shrink: 0 !important;
}
.login-trigger {
  color: #ff5000;
  cursor: pointer;
  font-weight: bold;
}
.login-trigger:hover {
  text-decoration: underline;
}
.register-trigger {
  color: #666;
  cursor: pointer;
}
.register-trigger:hover {
  color: #ff5000;
}
.role-mini-pill {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 3px;
  background: #fff0ea;
  color: #ff5000;
  border: 1px solid #ffd8c7;
  font-weight: 500;
  white-space: nowrap !important;
  flex-shrink: 0 !important;
}
.role-badge-tag {
  margin: 0 2px;
  font-size: 10px;
}
.logout-btn {
  color: #999;
  cursor: pointer;
  margin-left: 2px;
}
.logout-btn:hover {
  color: #e60012;
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  white-space: nowrap !important;
  flex-shrink: 0 !important;
}
.role-switch-box {
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap !important;
  flex-shrink: 0 !important;
}
.role-title {
  color: #ff5000;
  font-weight: bold;
  font-size: 11px;
  display: flex;
  align-items: center;
  gap: 2px;
  white-space: nowrap !important;
  flex-shrink: 0 !important;
}
.role-select-compact :deep(.el-input__wrapper) {
  height: 24px !important;
  padding: 0 6px !important;
  font-size: 11px !important;
}
.user-profile-badge {
  display: flex;
  align-items: center;
  cursor: pointer;
  white-space: nowrap !important;
  flex-shrink: 0 !important;
}
.cartoon-avatar {
  background: #fff0ea;
  border: 1.5px solid #ff5000;
  flex-shrink: 0 !important;
}
.cartoon-avatar-big {
  background: #fff0ea;
  border: 2px solid #ff5000;
}

/* 仿淘宝 Header */
.pc-header {
  background-color: #ffffff;
  padding: 16px 0;
  border-bottom: 2px solid #ff5000;
}
.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.brand-logo {
  cursor: pointer;
}
.brand-logo-img {
  height: 52px;
  display: block;
}

/* 搜索栏 (放大展开，填满顶部横向空白) */
.main-search-wrapper {
  flex: 1;
  max-width: 820px;
  margin: 0 28px;
}
@media (min-width: 1680px) {
  .main-search-wrapper {
    max-width: 920px;
  }
}
.search-input-group {
  display: flex;
  border: 2px solid #ff5000;
  border-radius: 24px;
  overflow: hidden;
  background-color: #fff;
  height: 44px;
  box-shadow: 0 2px 10px rgba(255, 80, 0, 0.12);
}
.search-type-select :deep(.el-input__wrapper) {
  box-shadow: none !important;
  border-radius: 0;
  border-right: 1px solid #eee;
  width: 98px;
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 0 18px;
  font-size: 14px;
}
.search-submit-btn {
  background: linear-gradient(135deg, #ff6034, #ee0a24);
  border: none;
  color: white;
  font-weight: bold;
  padding: 0 32px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  transition: opacity 0.2s;
}
.search-submit-btn:hover {
  opacity: 0.92;
}
.search-hot-tags {
  margin-top: 8px;
  font-size: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  white-space: nowrap !important;
  overflow: hidden;
}
.hot-tag {
  color: #666;
  cursor: pointer;
  white-space: nowrap !important;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  transition: color 0.2s;
}
.hot-tag:hover, .hot-tag.active {
  color: #ff5000;
  font-weight: 600;
}
.hot-tag.badge-tag {
  background: #fff0ea;
  color: #ff5000;
  padding: 1px 7px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  white-space: nowrap !important;
  flex-shrink: 0;
}

.header-fast-entry {
  display: flex;
  gap: 20px;
}
.fast-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
}
.fast-btn:hover, .fast-btn.current {
  color: #ff5000;
}

/* 一级导航栏 (不占全屏，宽度和主版心整体适配对齐) */
.pc-main-nav {
  background-color: #ffffff;
  max-width: 1440px;
  width: 95%;
  margin: 0 auto;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  border-radius: 8px 8px 0 0;
  overflow: hidden;
  box-sizing: border-box;
}
@media (min-width: 1680px) {
  .pc-main-nav {
    max-width: 1560px;
    width: 95%;
  }
}
@media (max-width: 1280px) {
  .pc-main-nav {
    width: 98%;
  }
}
.nav-inner {
  display: flex;
  align-items: center;
  height: 44px;
}
.all-category-head {
  width: 220px;
  background-color: #ff5000;
  color: white;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: 18px;
  font-size: 14px;
  font-weight: bold;
  white-space: nowrap !important;
  flex-shrink: 0;
}
.nav-menu {
  display: flex;
  list-style: none;
  height: 100%;
  margin-left: 12px;
}
.nav-menu li {
  padding: 0 16px;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: bold;
  color: #333;
  cursor: pointer;
  white-space: nowrap !important;
  flex-shrink: 0;
  position: relative;
}
.nav-menu li:hover, .nav-menu li.current {
  color: #ff5000;
}
.nav-menu li.current::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 18px;
  right: 18px;
  height: 3px;
  background-color: #ff5000;
}
.live-pulse-dot {
  width: 8px;
  height: 8px;
  background-color: #e60012;
  border-radius: 50%;
  display: inline-block;
  animation: pulse-dot 1s infinite alternate;
}
@keyframes pulse-dot {
  from { transform: scale(0.8); opacity: 0.5; }
  to { transform: scale(1.3); opacity: 1; }
}
.badge-b2b {
  background: #ffede7;
  color: #ff5000;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 3px;
}

/* 移动端专属 Header / TabBar 隐藏默认 */
.mobile-top-header, .mobile-bottom-tabbar, .mobile-kong-container {
  display: none;
}

/* 淘宝首屏三栏 */
.home-hero-section {
  display: flex;
  gap: 12px;
  margin-top: 14px;
}
.hero-category-side {
  width: 220px;
  background: white;
  border-radius: 8px;
  padding: 10px 0;
  box-shadow: 0 2px 6px rgba(0,0,0,0.03);
  flex-shrink: 0;
}
.cat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  cursor: pointer;
  white-space: nowrap !important;
  transition: all 0.2s;
}
.cat-row:hover, .cat-row.active {
  background-color: #fff6f2;
  color: #ff5000;
}
.cat-left {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  white-space: nowrap !important;
  flex-shrink: 0;
}
.cat-name {
  white-space: nowrap !important;
  flex-shrink: 0;
}
.cat-sub {
  font-size: 11px;
  color: #999;
  white-space: nowrap !important;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}
.cat-banner-mini {
  margin: 12px 14px 4px;
  background: linear-gradient(135deg, #ff7a00, #ff5000);
  color: white;
  padding: 8px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  font-weight: bold;
  cursor: pointer;
}

.hero-carousel-center {
  flex: 1;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.03);
}
.banner-slide {
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
}
.banner-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(to right, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.2) 60%, transparent 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 40px;
  color: white;
}
.banner-tag {
  background-color: #ff5000;
  color: white;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  width: fit-content;
  margin-bottom: 8px;
  font-weight: bold;
}
.banner-heading {
  font-size: 26px;
  font-weight: 900;
  margin-bottom: 8px;
}
.banner-desc {
  font-size: 13px;
  color: #eee;
  max-width: 480px;
  line-height: 1.5;
  margin-bottom: 16px;
}
.banner-action-btn {
  width: fit-content;
  font-weight: bold;
}

.hero-user-side {
  width: 250px;
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.03);
  display: flex;
  flex-direction: column;
}
.user-card-head {
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 14px;
}
.user-greeting {
  display: flex;
  flex-direction: column;
}
.user-greeting .hi {
  font-weight: bold;
  font-size: 14px;
}
.user-greeting .role-desc {
  font-size: 11px;
  color: #888;
  margin-top: 2px;
}
.user-action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.grid-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #666;
  cursor: pointer;
}
.grid-cell:hover {
  color: #ff5000;
}
.news-marquee-box {
  margin-top: 10px;
}
.news-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  margin-bottom: 8px;
}
.news-title {
  font-weight: bold;
  color: #ff5000;
  display: flex;
  align-items: center;
  gap: 4px;
}
.news-more {
  color: #999;
  font-size: 11px;
}
.news-list {
  list-style: none;
  font-size: 12px;
  color: #555;
  line-height: 1.8;
}
.news-list li {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.news-bullet {
  color: #ff5000;
  margin-right: 4px;
}

/* 百亿补贴专区 */
.flash-sale-strip {
  background: white;
  border-radius: 10px;
  padding: 16px 20px;
  margin-top: 16px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.03);
}
.strip-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.strip-title-box {
  display: flex;
  align-items: center;
  gap: 12px;
}
.strip-badge {
  background: linear-gradient(135deg, #e60012, #ff5000);
  color: white;
  font-size: 12px;
  font-weight: 900;
  padding: 3px 8px;
  border-radius: 4px;
}
.strip-title {
  font-size: 18px;
  font-weight: 800;
}
.countdown-clock {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #666;
  margin-left: 10px;
}
.time-block {
  background-color: #333;
  color: white;
  padding: 2px 5px;
  border-radius: 3px;
  font-weight: bold;
}
.strip-right-link {
  font-size: 12px;
  color: #888;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}
.strip-right-link:hover {
  color: #ff5000;
}

.flash-sale-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}
@media (max-width: 1280px) {
  .flash-sale-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
.flash-card {
  display: flex;
  gap: 12px;
  background-color: #fafafa;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}
.flash-card:hover {
  transform: translateY(-2px);
  background-color: #fff8f5;
}
.flash-img-wrap {
  width: 90px;
  height: 90px;
  position: relative;
  border-radius: 6px;
  overflow: hidden;
}
.flash-img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.flash-tag {
  position: absolute;
  top: 0;
  left: 0;
  background-color: #e60012;
  color: white;
  font-size: 10px;
  padding: 1px 4px;
  border-bottom-right-radius: 4px;
}
.flash-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.flash-title {
  font-size: 12px;
  font-weight: bold;
  height: 32px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.flash-price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.sub-price {
  color: #ff5000;
  font-weight: 800;
  font-size: 16px;
}
.del-price {
  color: #999;
  font-size: 11px;
  text-decoration: line-through;
}
.flash-progress {
  display: flex;
  align-items: center;
  gap: 6px;
}
.progress-text {
  font-size: 10px;
  color: #ff5000;
}

/* 筛选条与搜索结果提示 */
.product-filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border-radius: 8px;
  padding: 12px 18px;
  margin: 16px 0;
}
.filter-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.filter-label {
  color: #888;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.filter-btn {
  background: none;
  border: 1px solid #eee;
  padding: 4px 12px;
  border-radius: 14px;
  font-size: 13px;
  cursor: pointer;
  color: #555;
  transition: all 0.2s;
}
.filter-btn.active, .filter-btn:hover {
  background-color: #ff5000;
  border-color: #ff5000;
  color: white;
}
.cat-count {
  font-size: 11px;
  opacity: 0.8;
}
.search-result-hint {
  font-size: 13px;
  color: #666;
  margin-right: 14px;
}
.empty-search-panel {
  background: white;
  border-radius: 10px;
  padding: 40px;
  text-align: center;
}

/* 商品卡片瀑布流 (海量样品) */
.product-wall {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}
@media (min-width: 1680px) {
  .product-wall {
    grid-template-columns: repeat(6, 1fr);
  }
}
@media (max-width: 1280px) {
  .product-wall {
    grid-template-columns: repeat(4, 1fr);
  }
}
.taobao-goods-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.03);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}
.taobao-goods-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 18px rgba(0,0,0,0.08);
}
.goods-img-box {
  height: 220px;
  position: relative;
  background-color: #f0f0f0;
}
.goods-img-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.corner-ribbon {
  position: absolute;
  top: 10px;
  left: 10px;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  color: white;
  font-weight: bold;
}
.corner-ribbon.presale {
  background-color: #fa8c16;
}
.corner-ribbon.origin {
  background-color: #2bb673;
}
.origin-geo-pill {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background: rgba(0,0,0,0.6);
  color: white;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.goods-desc-box {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.goods-name {
  font-size: 14px;
  line-height: 1.4;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.coop-badge {
  background-color: #ff5000;
  color: white;
  font-size: 11px;
  padding: 1px 4px;
  border-radius: 3px;
  margin-right: 4px;
}
.goods-specs {
  font-size: 12px;
  color: #999;
  margin: 6px 0;
  display: flex;
  justify-content: space-between;
}
.goods-price-section {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-top: 4px;
}
.retail-price {
  color: #ff5000;
}
.retail-price .rmb {
  font-size: 13px;
  font-weight: bold;
}
.retail-price .main-num {
  font-size: 22px;
  font-weight: 900;
}
.retail-price .sub-num {
  font-size: 13px;
  font-weight: bold;
}
.retail-price .unit-label {
  font-size: 12px;
  color: #999;
}
.b2b-price-tip {
  font-size: 12px;
  color: #2bb673;
  font-weight: bold;
}
.goods-footer-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed #eee;
}
.stock-status {
  font-size: 12px;
  color: #888;
}
.add-cart-btn {
  font-weight: bold;
}

/* ============================================================== */
/* 专属购物车页面排版 (独立全功能视图)                            */
/* ============================================================== */
.view-cart {
  margin-top: 16px;
}
.cart-header-box {
  background: white;
  padding: 18px 24px;
  border-radius: 10px;
  margin-bottom: 14px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.03);
}
.cart-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cart-title-row h2 {
  font-size: 20px;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ff5000;
}
.cart-slogan {
  font-size: 13px;
  color: #2e7d32;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 4px;
}
.cart-shipping-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 8px;
}

.cart-table-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.03);
}
.cart-table-header {
  display: flex;
  align-items: center;
  background-color: #fafafa;
  border-bottom: 1px solid #eee;
  padding: 12px 20px;
  font-size: 13px;
  color: #666;
  font-weight: bold;
}
.cart-row-item {
  display: flex;
  align-items: center;
  padding: 18px 20px;
  border-bottom: 1px solid #f2f2f2;
  transition: background-color 0.2s;
}
.cart-row-item:hover {
  background-color: #fff9f6;
}

.col-check { width: 80px; }
.col-info { flex: 2; display: flex; gap: 14px; align-items: center; }
.col-price { width: 120px; text-align: center; }
.col-qty { width: 140px; text-align: center; }
.col-subtotal { width: 140px; text-align: center; }
.col-action { width: 100px; text-align: center; }

.cart-prod-thumb {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #eee;
}
.cart-prod-desc .title {
  font-size: 14px;
  font-weight: 700;
  line-height: 1.4;
}
.cart-prod-desc .meta {
  margin-top: 6px;
  display: flex;
  gap: 8px;
}
.tag-origin {
  font-size: 11px;
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 3px;
}
.tag-cert {
  font-size: 11px;
  background: #e8f5e9;
  color: #2e7d32;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: bold;
}
.cart-unit-price {
  font-size: 14px;
  color: #666;
  font-weight: bold;
}
.cart-subtotal-price {
  font-size: 17px;
  color: #ff5000;
  font-weight: 900;
}

/* 购物车吸底结算条 */
.cart-checkout-bar {
  background: white;
  margin-top: 16px;
  border-radius: 10px;
  padding: 14px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.04);
}
.checkout-left {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 13px;
}
.btn-clear-cart {
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.btn-clear-cart:hover {
  color: #e60012;
}
.checkout-right {
  display: flex;
  align-items: center;
  gap: 24px;
}
.selected-count-text {
  font-size: 13px;
  color: #666;
}
.highlight-num {
  color: #ff5000;
  font-size: 16px;
}
.total-amount-box {
  display: flex;
  align-items: baseline;
}
.total-amount-box .label {
  font-size: 13px;
  color: #666;
}
.total-amount-box .currency {
  color: #ff5000;
  font-size: 14px;
  font-weight: bold;
}
.total-amount-box .total-money {
  color: #ff5000;
  font-size: 26px;
  font-weight: 900;
}
.checkout-submit-button {
  background: linear-gradient(135deg, #ff6034, #ee0a24);
  color: white;
  border: none;
  font-size: 17px;
  font-weight: 900;
  padding: 12px 36px;
  border-radius: 26px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(238, 10, 36, 0.3);
  transition: opacity 0.2s, transform 0.2s;
}
.checkout-submit-button:hover:not(:disabled) {
  opacity: 0.95;
  transform: translateY(-1px);
}
.checkout-submit-button:disabled {
  background: #ccc;
  cursor: not-allowed;
  box-shadow: none;
}
.empty-cart-card {
  background: white;
  border-radius: 10px;
  padding: 60px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(0,0,0,0.03);
}

/* 直播间样式 */
.live-theater {
  display: flex;
  gap: 16px;
  height: 640px;
  margin-top: 14px;
}
.live-stage {
  flex: 3;
  display: flex;
  flex-direction: column;
}
.video-container {
  flex: 1;
  background-color: black;
  border-radius: 12px;
  position: relative;
  overflow: hidden;
}
.real-stream-player {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.live-hud-top {
  position: absolute;
  top: 14px;
  left: 14px;
  right: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 10;
}
.host-badge {
  background: rgba(0,0,0,0.55);
  backdrop-filter: blur(6px);
  padding: 4px 12px;
  border-radius: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
}
.host-title {
  font-size: 13px;
  font-weight: bold;
}
.host-status {
  font-size: 11px;
  color: #ddd;
  display: flex;
  align-items: center;
  gap: 4px;
}
.red-dot {
  width: 6px;
  height: 6px;
  background-color: #e60012;
  border-radius: 50%;
}
.btn-follow {
  font-weight: bold;
}
.hud-btn {
  background: rgba(0,0,0,0.55);
  border: none;
  color: white;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.explaining-floating-card {
  position: absolute;
  bottom: 14px;
  left: 14px;
  width: 320px;
  background: rgba(255,255,255,0.96);
  backdrop-filter: blur(8px);
  border-radius: 10px;
  padding: 10px;
  box-shadow: 0 4px 14px rgba(0,0,0,0.25);
  z-index: 10;
}
.floating-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #ff5000;
  font-weight: bold;
}
.pulsing-circle {
  width: 8px;
  height: 8px;
  background-color: #ff5000;
  border-radius: 50%;
  animation: pulse-dot 1s infinite alternate;
}
.floating-body {
  display: flex;
  gap: 10px;
  margin-top: 6px;
}
.floating-body img {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  object-fit: cover;
}
.floating-info .prod-name {
  font-size: 12px;
  font-weight: bold;
  line-height: 1.3;
}
.live-tag {
  background-color: #ff5000;
  color: white;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 2px;
  margin-right: 4px;
}
.deal-price {
  color: #ff5000;
  font-size: 17px;
  font-weight: 800;
}
.origin-price {
  color: #999;
  font-size: 11px;
  text-decoration: line-through;
  margin-left: 4px;
}
.floating-buttons {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.danmaku-stream {
  position: absolute;
  bottom: 110px;
  left: 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 340px;
  max-height: 180px;
  overflow-y: hidden;
  z-index: 10;
}
.danmaku-bubble {
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(4px);
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
}
.sender-name {
  color: #ffeb3b;
  margin-right: 6px;
}

.hearts-canvas {
  position: absolute;
  bottom: 14px;
  right: 14px;
  width: 100px;
  height: 280px;
  pointer-events: none;
  overflow: hidden;
  z-index: 10;
}
:deep(.floating-heart) {
  position: absolute;
  bottom: 0;
  font-size: 24px;
  opacity: 1;
  animation: floatUp 1.6s ease-out forwards;
}
@keyframes floatUp {
  0% { transform: translateY(0) scale(0.6); opacity: 1; }
  100% { transform: translateY(-240px) scale(1.3); opacity: 0; }
}

.live-bottom-bar {
  display: flex;
  gap: 12px;
  margin-top: 10px;
  align-items: center;
}
.chat-input-row {
  flex: 1;
}
.like-heart-btn {
  background: linear-gradient(135deg, #ff6034, #ee0a24);
  color: white;
  padding: 8px 18px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
  box-shadow: 0 4px 10px rgba(238, 10, 36, 0.3);
}

.live-goods-shelf {
  flex: 1.2;
  background: white;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.shelf-top-title {
  padding: 14px;
  border-bottom: 1px solid #f0f0f0;
  font-weight: bold;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.shelf-scroll-area {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.shelf-row-item {
  display: flex;
  gap: 8px;
  padding: 8px;
  border-radius: 8px;
  border: 1px solid #eee;
  align-items: center;
}
.shelf-row-item.active-explaining {
  border-color: #ff5000;
  background-color: #fff9f6;
}
.shelf-index {
  font-weight: bold;
  color: #ff5000;
  font-size: 14px;
  width: 18px;
}
.shelf-thumb {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
}
.shelf-detail {
  flex: 1;
}
.shelf-name {
  font-size: 12px;
  font-weight: bold;
  line-height: 1.3;
}
.shelf-sub-tags {
  margin: 4px 0;
  display: flex;
  gap: 4px;
}
.geo-tag {
  font-size: 10px;
  background: #f0f0f0;
  padding: 1px 4px;
  border-radius: 2px;
}
.test-tag {
  font-size: 10px;
  background: #e8f5e9;
  color: #2e7d32;
  padding: 1px 4px;
  border-radius: 2px;
}
.shelf-price-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.shelf-price {
  color: #ff5000;
  font-weight: 800;
  font-size: 16px;
}

/* 画中画小窗 */
.global-pip-box {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 260px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.25);
  z-index: 999;
  overflow: hidden;
  border: 2px solid #ff5000;
}
.pip-titlebar {
  background: #333;
  color: white;
  padding: 6px 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}
.pip-status-dot {
  width: 6px;
  height: 6px;
  background-color: #ff4d4f;
  border-radius: 50%;
}
.pip-caption {
  font-weight: bold;
}
.pip-close {
  cursor: pointer;
}
.pip-content {
  position: relative;
  height: 150px;
  cursor: pointer;
}
.pip-media {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.pip-tip {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.6);
  color: white;
  font-size: 11px;
  text-align: center;
  padding: 2px 0;
}

/* B2B 与溯源 */
.wholesale-banner-card {
  background: linear-gradient(135deg, #1f4037, #99f2c8);
  border-radius: 12px;
  padding: 28px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 14px;
}
.banner-texts h2 {
  font-size: 22px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.banner-texts p {
  font-size: 13px;
  margin-top: 6px;
}
.banner-kpis {
  display: flex;
  gap: 20px;
}
.kpi-block {
  background: rgba(0,0,0,0.3);
  padding: 10px 18px;
  border-radius: 8px;
  text-align: center;
}
.kpi-val {
  font-size: 20px;
  font-weight: 800;
  display: block;
}
.kpi-txt {
  font-size: 11px;
}
.wholesale-card {
  border-radius: 10px;
}
.card-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.sub-hint {
  font-size: 12px;
  color: #888;
}
.table-prod-box {
  display: flex;
  gap: 10px;
  align-items: center;
}
.table-prod-box img {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  object-fit: cover;
}
.b2b-table-price {
  color: #ff5000;
  font-weight: 800;
  font-size: 16px;
}

.trace-hero-card {
  background: white;
  padding: 32px;
  border-radius: 12px;
  text-align: center;
  margin-top: 14px;
}
.trace-hero-card h2 {
  font-size: 22px;
  color: #2bb673;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.trace-hero-card p {
  color: #666;
  font-size: 13px;
  margin: 8px 0 20px;
}
.trace-search-bar {
  display: flex;
  justify-content: center;
  gap: 10px;
}
.trace-details-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
}
.trace-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.auth-chips {
  margin-top: 8px;
  display: flex;
  gap: 10px;
}
.chip-green {
  background: #e8f5e9;
  color: #2e7d32;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: bold;
}
.timeline-wrapper {
  padding: 10px 20px;
}

/* 工作台 */
.workbench-bar {
  background: white;
  padding: 20px;
  border-radius: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 14px;
}
.workbench-bar h2 {
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.workbench-bar p {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
}
.bar-actions {
  display: flex;
  gap: 12px;
}
.metric-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-top: 14px;
}
.metric-card {
  background: white;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #ff5000;
}
.metric-label {
  font-size: 12px;
  color: #888;
}
.metric-val {
  font-size: 24px;
  font-weight: 800;
  margin-top: 4px;
  display: block;
}
.metric-val.highlight {
  color: #ff5000;
}
.metric-val.green {
  color: #2bb673;
}
.metric-val span {
  font-size: 14px;
  font-weight: normal;
}
.mt-4 {
  margin-top: 16px;
}
.stock-editor {
  display: flex;
  gap: 6px;
  align-items: center;
}
.admin-charts-grid {
  display: flex;
  gap: 16px;
}
.cat-progress-bars {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.cat-bar {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.cat-label {
  font-size: 12px;
  color: #666;
}

/* 电子合同与弹窗 */
.formal-contract-sheet {
  position: relative;
  background: #fafafa;
  border: 1px solid #ddd;
  padding: 24px;
  border-radius: 6px;
  font-family: "SimSun", "Songti SC", serif;
  line-height: 1.8;
}
.official-seal {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 90px;
  height: 90px;
  border: 3px solid #ff0036;
  border-radius: 50%;
  color: #ff0036;
  font-size: 13px;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: rotate(-20deg);
  box-shadow: 0 0 4px rgba(255, 0, 54, 0.2);
}
.sheet-title {
  text-align: center;
  margin-bottom: 8px;
}
.sheet-no {
  text-align: right;
  font-size: 12px;
  color: #888;
  margin-bottom: 16px;
}
.sheet-clause {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #ccc;
  font-size: 13px;
  color: #555;
}

.checkout-panel {
  padding: 10px;
}
.checkout-prod-meta {
  display: flex;
  gap: 14px;
  align-items: center;
}
.checkout-prod-meta img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}
.prod-text .title {
  font-size: 14px;
  font-weight: bold;
}
.prod-text .origin {
  font-size: 12px;
  color: #888;
  margin: 4px 0;
}
.prod-text .price {
  color: #ff5000;
  font-size: 15px;
  font-weight: bold;
}
.address-box {
  background: #fdf6ec;
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 13px;
}
.addr-title {
  font-weight: bold;
  color: #e6a23c;
  display: flex;
  align-items: center;
  gap: 4px;
}
.order-final-row {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 8px;
  margin-top: 16px;
}
.final-price {
  color: #ff5000;
  font-size: 26px;
  font-weight: 900;
}

/* 快速角色登录网格 (带卡通头像) */
.quick-role-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-top: 10px;
}
.quick-role-chip {
  background-color: #f7f8fa;
  border: 1px solid #e2e4e8;
  padding: 8px 10px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
}
.quick-role-chip:hover {
  background-color: #fff0ea;
  border-color: #ff5000;
  color: #ff5000;
}
.chip-avatar {
  background: #fff;
  border: 1px solid #ddd;
}

/* 手机端响应式 */
@media (max-width: 768px) {
  .pc-top-bar, .pc-header, .pc-main-nav, .hero-category-side, .hero-user-side {
    display: none !important;
  }
  .mobile-top-header {
    display: block;
    position: sticky;
    top: 0;
    z-index: 100;
    background: #ff5000;
    padding: 8px 12px;
  }
  .mobile-header-row {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  .mobile-logo {
    display: flex;
    align-items: center;
    white-space: nowrap;
  }
  .mobile-search-box {
    flex: 1;
    background: white;
    border-radius: 16px;
    height: 32px;
    display: flex;
    align-items: center;
    padding: 0 10px;
    gap: 6px;
    color: #999;
  }
  .mobile-search-box input {
    border: none;
    outline: none;
    width: 100%;
    font-size: 12px;
  }
  .mobile-role-badge {
    color: white;
    font-size: 18px;
    cursor: pointer;
  }
  .mobile-kong-container {
    display: flex;
    justify-content: space-around;
    background: white;
    padding: 12px 6px;
    border-radius: 10px;
    margin-bottom: 12px;
  }
  .kong-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    font-size: 11px;
    color: #444;
  }
  .kong-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 18px;
  }
  .kong-icon.red { background: linear-gradient(135deg, #ff416c, #ff4b2b); }
  .kong-icon.green { background: linear-gradient(135deg, #11998e, #38ef7d); }
  .kong-icon.orange { background: linear-gradient(135deg, #f7971e, #ffd200); }
  .kong-icon.purple { background: linear-gradient(135deg, #8a2387, #e94057); }
  .kong-icon.blue { background: linear-gradient(135deg, #2193b0, #6dd5ed); }

  .product-wall {
    grid-template-columns: repeat(2, 1fr) !important;
    gap: 10px !important;
  }
  .goods-img-box {
    height: 150px !important;
  }
  .goods-desc-box {
    padding: 8px !important;
  }
  .goods-name {
    font-size: 12px !important;
    height: 34px !important;
  }
  .goods-specs {
    display: none !important;
  }
  .live-theater {
    flex-direction: column !important;
    height: auto !important;
  }
  .video-container {
    height: 380px !important;
  }
  .flash-sale-grid {
    grid-template-columns: repeat(2, 1fr) !important;
  }
  .metric-row {
    grid-template-columns: repeat(2, 1fr) !important;
  }
  .admin-charts-grid {
    flex-direction: column !important;
  }

  /* 移动端购物车适配 */
  .col-price, .col-qty, .col-subtotal, .col-action {
    display: none;
  }
  .cart-table-header {
    display: none;
  }
  .cart-row-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .mobile-bottom-tabbar {
    display: flex;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: 52px;
    background: white;
    border-top: 1px solid #eee;
    z-index: 100;
    justify-content: space-around;
    align-items: center;
  }
  .tabbar-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    font-size: 11px;
    color: #666;
  }
  .tabbar-item.active {
    color: #ff5000;
  }
  .mobile-cart-badge :deep(.el-badge__content) {
    top: 2px;
    right: -4px;
  }
  .main-body {
    padding-bottom: 60px;
  }
}
</style>

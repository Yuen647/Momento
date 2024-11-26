<template>
    <el-card class="page-container">
      <div class="scroll-content" @scroll="handleScroll">
        <el-row :gutter="20">
          <el-col v-for="(article, index) in articles.slice(0, displayedItems)" :key="article.id" :span="6">
            <!-- 监听 el-card 的点击事件，传递笔记 id -->
            <el-card shadow="hover" class="box-card" @click="openDrawer(article.id, article)" style="cursor: pointer;">
              <!-- 影像展示 -->
              <img :src="article.imgUris" alt="影像"
                style="height: 200px; width: 100%; object-fit: cover; border-radius: 5px;" />
  
              <!-- 文章标题 -->
              <div style="margin-top: 10px; font-weight: bold;">
                {{ article.title }}
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
    <!-- 笔记详情抽屉 -->
    <el-drawer v-model="visibleDrawer" title="笔记详情" direction="rtl" size="50%">
      <!-- 抽屉内容容器 -->
      <div style="display: flex; flex-direction: column; height: 100%;">
        <div class="scroll-drawercontent" @scroll="handleScroll"><!-- 无限滚动 -->
          <!-- 图片展示区域 -->
          <div style="flex: 0 0 auto;">
            <img :src="article.imgUris" alt="笔记图片"
              style="width: 100%; height: 400px; object-fit: cover; border-radius: 5px;" />
          </div>
          <!-- 标题和内容区域 -->
          <div style="flex: 1 1 auto; padding: 20px;">
            <!-- 标题 -->
            <h2 style="margin: 0 0 16px 0;font-size: 24px;">{{ article.title }}</h2>
            <!-- 内容 -->
            <p style="font-size: 16px; line-height: 1.6;">{{ article.content }}</p>
            <!-- <div>
              内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容
            </div> -->
          </div>
  
  
        </div>
  
  
        <div
          style="display: flex; justify-content: space-between; padding: 20px; border-top: 1px solid #eaeaea; position: absolute; bottom: 0; left: 0; right: 0; background-color: white;">
          <!-- 用户信息 -->
          <div style="display: flex; align-items: center;">
            <img :src="article.avatar" alt="用户头像"
              style="width: 40px; height: 40px; border-radius: 50%; margin-right: 10px;" />
            <span>{{ article.creatorName }}</span>
          </div>
          <!-- 关注按钮 -->
          <el-button :type="article.isFollowed ? 'success' : 'primary'" @click="toggleFollow(article)">
            {{ article.isFollowed ? '已关注' : '未关注' }}
          </el-button>
  
  
          <!-- 操作按钮 -->
          <div>
            <!-- 点赞按钮 -->
            <el-button :type="article.isLiked ? 'success' : 'primary'" @click="toggleLike(article)" icon="el-icon-thumb"
              style="width: 80px;">
              {{ article.isLiked ? '已点赞' : '点赞' }}
            </el-button>
  
            <!-- 收藏按钮 -->
            <el-button :type="article.isCollect ? 'success' : 'primary'" @click="toggleCollect(article)"
              icon="el-icon-star-on" style="width: 80px;">
              {{ article.isCollect ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </template>
  
  <script setup>
  import { Star } from '@element-plus/icons-vue'
  import { ref, onMounted } from 'vue'
  import { collectListService, collectListDetailService, doCollectService, unCollectService, dolikeNoteService, nolikeNoteService, likeListService } from '@/api/collect.js'
  import { noFollowService, doFollowService, followService, currentUserService } from '@/api/follow.js'
  //控制抽屉是否显示
  const visibleDrawer = ref(false)
  // Category and articles data
  const articles = ref([
    /* {
      "id": 4,
      title: "陕西旅游攻略",
      categoryId: 3,
    } */
  ])
  const article = ref({
    title: '文章标题',
    content: '文章的详细内容...',
    creatorName: '用户名',
    avatar: "http://127.0.0.1:9000/momento/7e8a005b04524f429402216d235aac7c.jpg",
    imgUris: 'https://img.quanxiaoha.com/quanxiaoha/f97361c0429d4bb1bc276ab835843065.jpg',
    createid: 6101
  })
  const follow = ref([
    {
      userId: 6101,
      avatar: "头像",
      nickname: "zidu",
      introduction: "deng"
    }
  ])
  const like = ref([
    {
      id: "1856303133941039128",
      title: "今天又在开会",
      creatorId: "6101"
    }
  ])
  const categoryId = ref('')
  const PageNo = ref(1);
  // Infinite scroll
  const displayedItems = ref(10)
  const handleScroll = (event) => {
    const { scrollTop, clientHeight, scrollHeight } = event.target
    if (scrollTop + clientHeight >= scrollHeight - 50) {
      displayedItems.value += 10
    }
  }
  const toggleFollow = async (row) => {// 切换关注状态
    if (row.isFollowed) {//取关
      let params = { unfollowUserId: row.creatorId };
      let result = await noFollowService(params); // 调用取关接口
      if (result.success) {//必须得成功才能操作
        row.isFollowed = !row.isFollowed // 切换关注状态
      }
    } else {//关注
      let params = { followUserId: row.creatorId };
      let result = await doFollowService(params); // 调用关注接口
      if (result.success) {//必须得成功才能操作
        row.isFollowed = !row.isFollowed // 切换关注状态
      }
    }
  }
  const toggleLike = async (row) => {// 切换点赞状态
    if (row.isLiked) {//取消点赞
      let params = { id: row.id };
      let result = await nolikeNoteService(params);
      if (result.success) {//必须得成功才能操作
        row.isLiked = !row.isLiked
      }
    } else {
      let params = { id: row.id };
      let result = await dolikeNoteService(params);
      if (result.success) {//必须得成功才能操作
        row.isLiked = !row.isLiked
      }
    }
  }
  const toggleCollect = async (article) => { // 切换收藏状态
    if (article.isCollect) {
      // 如果已经收藏，取消收藏
      let params = {
        id: article.id
      };
      await unCollectService(params); // 取消收藏的 API 请求
    } else {
      // 如果未收藏，添加收藏
      let params = {
        id: article.id
      };
      await doCollectService(params); // 添加收藏的 API 请求
    }
    // 切换收藏状态
    article.isCollect = !article.isCollect;
  };
  const size = ref(1)//收藏列表请求参数
  const page = ref(1)//收藏列表请求参数
  const userId = ref(6101) // 用户ID
  const currentUser = async () => {//获取当前用户ID
    let result = await currentUserService();
    /* console.log(result) */
    userId.value = result.data.id;
  }
  const collectList = async () => {//获取收藏列表相关
    let params = {
      size: size.value,
      userId: userId.value,
      page: page.value
    }
    let result = await collectListService(params);
    articles.value = result.data;
    // 为了并行处理多个收藏笔记详情的请求，使用 Promise.all
    const updatedArticles = await Promise.all(
      articles.value.map(async (article) => {
        /* console.log(article.id); */
        let params = {
          id: article.id
        }
        const detailResult = await collectListDetailService(params);
        /* console.log(detailResult); */
        // 将 imgUris 属性附加到当前文章对象上
        return {
          ...article,
          imgUris: detailResult.data.imgUris
        };
      })
    );
    // 更新 articles.value
    articles.value = updatedArticles;
  }
  const collectListDetail = async (id) => {//获取收藏笔记相关
    let params = {
      id: id
    }
    let result = await collectListDetailService(params);
    article.value = {
      ...result.data, // 保留原有属性
      isFollowed: false,
      isLiked: false,
      isCollect: true
    };
    await followList();
    // 检查是否在关注列表中
    if (follow.value.some(item => item.id === article.value.id)) {
      article.value.isFollowed = true; // 如果在关注列表中，设置为 true
    }
    await likeList();
    // 检查是否在点赞列表中
    if (like.value.some(item => item.id === article.value.id)) {
      article.value.isLiked = true; // 如果在关注列表中，设置为 true
    }
  }
  const followList = async () => {//获取关注列表
    let params = {
      userId: userId.value,
      PageNo: PageNo.value
    };
    let result = await followService(params);
    follow.value = result.data;
  };
  const likeList = async () => {//获取点赞列表
    let params = {
      size: size.value,
      userId: userId.value,
      page: page.value
    };
    let result = await likeListService(params);
    like.value = result.data;
  };
  // 处理点击事件并传递文章 ID
  const openDrawer = async (id, selectedArticle) => {
    await collectListDetail(id/* .toString() */); // 等待数据加载完成
    visibleDrawer.value = true;  // 然后打开抽屉
  };
  const fetchAllData = async () => {
    await currentUser();
    await collectList();
  };
  onMounted(() => {
    fetchAllData(); // 组件加载时调用
  });
  </script>
  
  <style scoped>
  .page-container {
    min-height: 93%;
    box-sizing: border-box;
  }
  /* 搜索表单固定在顶部 */
  .fixed-form {
    position: sticky;
    top: 0;
    background-color: #fff;
    padding: 10px;
    z-index: 10;
  }
  /* 无限滚动内容 */
  .scroll-content {
    max-height: 70vh;
    overflow-y: auto;
  }
  /* 无限滚动内容 */
  .scroll-drawercontent {
    max-height: 80vh;
    overflow-y: auto;
  }
  .avatar-uploader {
    :deep() {
      .avatar {
        width: 178px;
        height: 178px;
        display: block;
      }
      .el-upload {
        border: 1px dashed var(--el-border-color);
        border-radius: 6px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        transition: var(--el-transition-duration-fast);
      }
      .el-upload:hover {
        border-color: var(--el-color-primary);
      }
      .el-icon.avatar-uploader-icon {
        font-size: 28px;
        color: #8c939d;
        width: 178px;
        height: 178px;
        text-align: center;
      }
    }
  }
  .editor {
    width: 100%;
    :deep(.ql-editor) {
      min-height: 200px;
    }
  }
  </style>
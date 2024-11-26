<template>
    <!-- 抽屉 -->
    <el-button type="primary" @click="visibleDrawer = true">关注 {{ followCount }}</el-button>
    <el-button type="primary" @click="visibleDrawerFans = true">粉丝 {{ fansCount }} </el-button>
    <el-drawer v-model="visibleDrawer" title="关注的人" direction="rtl" size="100%">
      <div class="scroll-content" @scroll="handleScroll">
        <el-table :data="follow.slice(0, displayedItems)" style="width: 100%">
  
  
          <el-table-column label="" width="100">
            <template #default="{ row }">
              <el-avatar class="mr-2" :size="50" :src=row.avatar />
            </template>
          </el-table-column>
  
          <el-table-column label=" " width="200" prop="nickname"></el-table-column><!-- labei后写表单最上面的名字 -->、
  
          <el-table-column label=" " prop="avatar">
            <template #default="{ row }">
              <el-button type="text" @click="handleRowClick(row)">
                {{ row.title }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column label=" " prop="introduction"></el-table-column>
          <el-table-column label=" " width="120">
            <template #default="{ row }">
              <el-button :type="row.isFollowed ? 'success' : 'primary'" @click="toggleFollow(row)">
                {{ row.isFollowed ? '已关注' : '未关注' }}
              </el-button>
            </template>
          </el-table-column>
  
          <template #empty>
            <el-empty description="还没有关注的人哦" />
          </template>
        </el-table>
      </div>
    </el-drawer>
  
    <el-drawer v-model="visibleDrawerFans" title="粉丝" direction="rtl" size="100%">
      <div class="scroll-content" @scroll="handleScroll">
        <el-table :data="fans.slice(0, displayedItems)" style="width: 100%">
  
          <el-table-column label="" width="100">
            <template #default="{ row }">
              <el-avatar class="mr-2" :size="50" :src=row.avatar />
            </template>
          </el-table-column>
  
          <el-table-column label=" " width="200" prop="nickname"></el-table-column><!-- labei后写表单最上面的名字 -->、
  
          <el-table-column label=" " prop="avatar">
            <template #default="{ row }">
              <el-button type="text" @click="handleRowClick(row)">
                {{ row.title }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column label=" " width="100" prop="introduction"></el-table-column>
  
          <template #empty>
            <el-empty description="还没有粉丝哦" />
          </template>
        </el-table>
      </div>
    </el-drawer>
  
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  //控制抽屉是否显示
  const visibleDrawer = ref(false)
  const visibleDrawerFans = ref(false)
  const follow = ref([
    {
      userId: 6101,
      avatar: "头像",
      nickname: "zidu",
      introduction: "deng"
    }
  ])
  const fans = ref([
    {
      userId: 6101,
      avatar: "头像",
      nickname: "zidu",
      introduction: "deng"
    }
  ])
  import { currentUserService, followService, fansService, noFollowService, doFollowService } from '@/api/follow.js'
  const followCount = ref() // 初始关注数量
  const fansCount = ref() // 初始粉丝数量
  const displayedItems = ref(10)
  const handleScroll = (event) => {//无限滚动
    const { scrollTop, clientHeight, scrollHeight } = event.target
    if (scrollTop + clientHeight >= scrollHeight - 50) {
      displayedItems.value += 10
    }
  }
  const toggleFollow = async (row) => {
    if (row.isFollowed) {//取关
      let params = { unfollowUserId: row.userId };
      await noFollowService(params); // 调用取关接口
    } else {//关注
      let params = { followUserId: row.userId };
      await doFollowService(params); // 调用关注接口
    }
    row.isFollowed = !row.isFollowed // 切换关注状态
  }
  const userId = ref(6101) // 用户ID
  const PageNo = ref(1);
  const currentUser = async () => {//获取当前用户ID
    let result = await currentUserService();
    userId.value = result.data.id;
  }
  const followList = async () => {//获取关注列表相关
    let params = {
      userId: userId.value,
      PageNo: PageNo.value
    }
    let result = await followService(params);
    followCount.value = result.totalCount;
    follow.value = result.data;
    follow.value.forEach(item => {
      item.isFollowed = true; // 新增是否被关注的属性
      // 检查 avatar 是否为 null 或 undefined
      if (item.avatar && item.avatar !== null && item.avatar !== undefined) {
        item.avatar = item.avatar
      }
      else {
        item.avatar = "https://img.quanxiaoha.com/quanxiaoha/f97361c0429d4bb1bc276ab835843065.jpg"; // 默认头像
      }
    });
  }
  const fansList = async () => {//获取粉丝列表相关
    let params = {
      userId: userId.value,
      PageNo: PageNo.value
    }
    let result = await fansService(params);
    fansCount.value = result.totalCount;
    fans.value = result.data;
    fans.value.forEach(item => {
      item.isFollowed = true; // 新增是否被关注的属性
      // 检查 avatar 是否为 null 或 undefined
      if (item.avatar && item.avatar !== null && item.avatar !== undefined) {
        item.avatar = item.avatar
      }
      else {
        item.avatar = "https://img.quanxiaoha.com/quanxiaoha/f97361c0429d4bb1bc276ab835843065.jpg"; // 默认头像
      }
    });
  }
  const fetchAllData = async () => {
    await currentUser(); // 确保先获取用户ID
    await followList();  // 获取关注列表
    await fansList();    // 获取粉丝列表
  };
  onMounted(() => {
    fetchAllData(); // 组件加载时调用
  });
  </script>
  
  <style setup>
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
  .scroll-content {
    max-height: 60vh;
    overflow-y: auto;
  }
  </style>
  
<template>
  <div class="home-container">
    <h1 class="page-title">首页</h1>
    <div class="waterfall">
      <div class="note-card" v-for="note in notes" :key="note.id">
        <h3 class="note-title">{{ note.title }}</h3>
        <p class="note-content">{{ note.content }}</p>
        <small class="note-date">{{ formatDate(note.updateTime) }}</small>
      </div>
    </div>
    <div class="load-more-container">
      <button 
        class="load-more" 
        @click="loadMoreNotes" 
        :disabled="loading || noMoreNotes"
      >
        <span v-if="loading">加载中...</span>
        <span v-else-if="noMoreNotes">没有更多内容了</span>
        <span v-else>加载更多</span>
      </button>
    </div>
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { getToken } from '@/composables/cookie';
import { ref } from 'vue';

export default {
  name: 'Home',
  setup() {
    const loading = ref(false);
    const error = ref(null);
    const noMoreNotes = ref(false);
    const notes = ref([]);
    const page = ref(1);
    const size = ref(10);

    // 格式化日期
    const formatDate = (dateString) => {
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    };

    // 获取笔记
    const fetchNotes = async () => {
      try {
        const token = getToken();
        if (!token) {
          console.error('Token not found!');
          alert('未找到认证信息，请重新登录。');
          return;
        }
        loading.value = true;
        const response = await axios.post(
          `/api/note/note/list`,
          { size: size.value, page: page.value },
          {
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (response.data.success) {
          const fetchedNotes = response.data.data;
          if (fetchedNotes.length < size.value) {
            noMoreNotes.value = true;
          }
          notes.value = [...notes.value, ...fetchedNotes];
        } else {
          console.error("获取笔记失败:", response.data.message);
          error.value = "获取笔记失败: " + response.data.message;
        }
      } catch (err) {
        console.error('Error fetching notes:', err);
        error.value = "获取笔记时发生错误，请稍后重试。";
      } finally {
        loading.value = false;
      }
    };

    // 加载更多笔记
    const loadMoreNotes = () => {
      if (!noMoreNotes.value && !loading.value) {
        page.value++;
        fetchNotes();
      }
    };

    // 组件挂载时获取初始笔记
    fetchNotes();

    return {
      notes,
      loadMoreNotes,
      loading,
      noMoreNotes,
      error,
      formatDate,
    };
  },
};
</script>

<style scoped>
.home-container {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f9fafb;
  box-sizing: border-box;
}

.page-title {
  font-size: 2.5em;
  margin-bottom: 40px;
  color: #333;
  text-align: center;
}

.waterfall {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  justify-content: center;
}

.note-card {
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 25px 20px;
  border-radius: 12px;
  width: calc(33.333% - 30px);
  box-sizing: border-box;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.note-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
}

.note-title {
  font-size: 1.75em;
  margin-bottom: 15px;
  color: #1a202c;
  text-align: left;
}

.note-content {
  flex-grow: 1;
  font-size: 1em;
  color: #4a5568;
  margin-bottom: 20px;
  text-align: left;
  line-height: 1.6;
}

.note-date {
  font-size: 0.85em;
  color: #a0aec0;
  text-align: left;
}

.load-more-container {
  margin-top: 40px;
  text-align: center;
}

.load-more {
  padding: 12px 30px;
  font-size: 1.1em;
  color: #ffffff;
  background-color: #3182ce;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.2s ease;
}

.load-more:disabled {
  background-color: #a0aec0;
  cursor: not-allowed;
}

.load-more:not(:disabled):hover {
  background-color: #2b6cb0;
  transform: translateY(-2px);
}

.error-message {
  margin-top: 30px;
  color: #e53e3e;
  font-weight: bold;
  text-align: center;
  font-size: 1em;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .note-card {
    width: calc(50% - 30px);
  }
}

@media (max-width: 600px) {
  .note-card {
    width: 100%;
  }

  .waterfall {
    gap: 20px;
  }

  .load-more {
    width: 100%;
    padding: 12px 0;
  }
}
</style>

<template>
  <div class="my-notes-container">
    <h1>我的笔记</h1>
    
    <!-- 过滤选项 -->
    <div class="filter-actions">
      <button 
        class="filter-btn" 
        :class="{ active: status === 1 }" 
        @click="changeStatus(1)"
      >
        正常笔记
      </button>
      <button 
        class="filter-btn" 
        :class="{ active: status === 2 }" 
        @click="changeStatus(2)"
      >
        已删除笔记
      </button>
    </div>
    
    <div v-if="loading" class="loading-spinner">
      <p>加载中...</p>
    </div>
    
    <div v-else>
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
      
      <div v-else>
        <div class="actions" v-if="status === 1">
          <button class="publish-btn" @click="openPublishModal">发布笔记</button>
        </div>
        
        <div class="notes-grid">
          <!-- 使用 sortedNotes 进行渲染，并根据 isTop 添加类 -->
          <div 
            class="my-note-card" 
            :class="{ 'is-top': note.isTop }" 
            v-for="note in sortedNotes" 
            :key="note.id"
          >
            <h3>{{ note.title }}</h3>
            <p>{{ note.content }}</p>
            <small>{{ formatDate(note.updateTime) }}</small>
            <div class="note-images" v-if="note.imgUris && note.imgUris.length">
              <img v-for="(img, index) in note.imgUris" :key="index" :src="img" alt="笔记图片" />
            </div>
            <div class="note-actions">
              <button class="edit-btn" @click="editNote(note)" v-if="status === 1">编辑</button>
              <button class="top-btn" @click="toggleTop(note)" :class="{ active: note.isTop }">
                {{ note.isTop ? '取消置顶' : '置顶' }}
              </button>
              <button class="visibility-btn" @click="toggleVisibility(note)" :class="{ active: note.visible === 1 }">
                {{ note.visible === 1 ? '公开' : '仅自己可见' }}
              </button>
              <button 
                class="delete-btn" 
                v-if="status === 1" 
                @click="deleteNote(note)"
              >
                删除
              </button>
              <button 
                class="restore-btn" 
                v-else 
                @click="restoreNote(note)"
              >
                恢复
              </button>
            </div>
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
      </div>
    </div>
    
    <!-- 编辑笔记模态窗口 -->
    <transition name="fade">
      <div v-if="isEditModalOpen" class="modal-overlay" @click.self="closeEditModal">
        <div class="modal">
          <h2>编辑笔记</h2>
          <form @submit.prevent="submitEditNote" class="form">
            <label>
              标题:
              <input type="text" v-model="currentNote.title" required />
            </label>
            <label>
              内容:
              <textarea v-model="currentNote.content" required></textarea>
            </label>
            <!-- 图片上传部分 -->
            <label>
              上传图片:
              <input type="file" multiple @change="handleEditImageUpload" accept="image/*" />
            </label>
            <div class="image-previews">
              <div v-for="(img, index) in currentNote.imgUris" :key="index" class="image-preview">
                <img :src="img" alt="预览图片" />
                <button type="button" @click="removeImage(currentNote.imgUris, index)">×</button>
              </div>
            </div>
            <div class="form-buttons">
              <button type="submit" class="btn save-btn" :disabled="loading">保存修改</button>
              <button type="button" class="btn cancel-btn" @click="closeEditModal">取消</button>
            </div>
          </form>
        </div>
      </div>
    </transition>
    
    <!-- 发布笔记模态窗口 -->
    <transition name="fade">
      <div v-if="isPublishModalOpen" class="modal-overlay" @click.self="closePublishModal">
        <div class="modal">
          <h2>发布笔记</h2>
          <form @submit.prevent="submitPublishNote" class="form">
            <label>
              标题:
              <input type="text" v-model="newNote.title" required />
            </label>
            <label>
              内容:
              <textarea v-model="newNote.content" required></textarea>
            </label>
            <!-- 图片上传部分 -->
            <label>
              上传图片:
              <input type="file" multiple @change="handlePublishImageUpload" accept="image/*" />
            </label>
            <div class="image-previews">
              <div v-for="(img, index) in newNote.imgUris" :key="index" class="image-preview">
                <img :src="img" alt="预览图片" />
                <button type="button" @click="removeImage(newNote.imgUris, index)">×</button>
              </div>
            </div>
            <!-- <label>
              主题 ID:
              <input type="number" v-model="newNote.topicId" />
            </label> -->
            <div class="form-buttons">
              <button type="submit" class="btn save-btn" :disabled="loading">发布</button>
              <button type="button" class="btn cancel-btn" @click="closePublishModal">取消</button>
            </div>
          </form>
        </div>
      </div>
    </transition>
    
  </div>
</template>





<script>
import axios from 'axios';
import { getToken } from '@/composables/cookie';
import { ref, reactive, computed } from 'vue';

export default {
  setup() {
    const myNotes = ref([]);
    const userId = ref(null);
    const size = ref(10);
    const page = ref(1);
    const loading = ref(false);
    const error = ref(null);
    const noMoreNotes = ref(false);

    const isEditModalOpen = ref(false);
    const isPublishModalOpen = ref(false);
    const currentNote = reactive({
      id: null,
      title: "",
      content: "",
      updateTime: "",
      isTop: false,
      visible: 1,
      imgUris: [] // 初始化为数组
    });
    const newNote = reactive({
      type: 0,
      imgUris: [], // 初始化为数组
      title: "",
      content: "",
      topicId: 1
    });

    const status = ref(1); // 1: 正常展示, 2: 查看被删除的笔记

    // 计算属性：格式化日期
    const formatDate = (dateString) => {
      const date = new Date(dateString);
      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    };

    // 获取当前用户信息
    const fetchCurrentUser = async () => {
      try {
        const token = getToken();
        if (!token) {
          throw new Error('未找到认证信息，请重新登录。');
        }
        const response = await axios.get('/api/user/user/current', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        if (response.data.success) {
          userId.value = response.data.data.id;
          fetchMyNotes();
        } else {
          throw new Error(response.data.message || '获取用户信息失败。');
        }
      } catch (err) {
        console.error('获取用户信息时出错:', err);
        error.value = err.message || '获取用户信息时出错。';
      }
    };

    // 获取用户的笔记
    const fetchMyNotes = async () => {
      if (!userId.value) {
        error.value = '无法获取用户 ID，无法获取笔记列表。';
        return;
      }
      try {
        loading.value = true;
        const token = getToken();
        const response = await axios.post(
          '/api/note/note/UserNoteList',
          {
            userId: userId.value,
            size: size.value,
            page: page.value,
            status: status.value // 添加 status 参数
          },
          {
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${token}`,
            },
          }
        );
        if (response.data.success) {
          const fetchedNotes = response.data.data.map(note => ({
            ...note,
            imgUris: note.imgUris || [], // 确保imgUris为数组
            isTop: !!note.isTop // 确保isTop为布尔类型
          }));
          if (fetchedNotes.length < size.value) {
            noMoreNotes.value = true;
          }
          myNotes.value = [...myNotes.value, ...fetchedNotes];
        } else {
          throw new Error(response.data.message || '获取笔记列表失败。');
        }
      } catch (err) {
        console.error('获取笔记列表时出错:', err);
        error.value = err.message || '获取笔记列表时出错。';
      } finally {
        loading.value = false;
      }
    };

    // 计算属性：排序后的笔记
    const sortedNotes = computed(() => {
      return myNotes.value.slice().sort((a, b) => {
        if (a.isTop === b.isTop) {
          return new Date(b.updateTime) - new Date(a.updateTime);
        }
        return b.isTop - a.isTop;
      });
    });

    // 切换笔记状态
    const changeStatus = (newStatus) => {
      if (status.value !== newStatus) {
        status.value = newStatus;
        // 重置笔记列表和分页信息
        myNotes.value = [];
        page.value = 1;
        noMoreNotes.value = false;
        // 重新获取笔记
        fetchMyNotes();
      }
    };

    // 加载更多笔记
    const loadMoreNotes = () => {
      if (!noMoreNotes.value && !loading.value) {
        page.value++;
        fetchMyNotes();
      }
    };

    // 编辑笔记
    const editNote = (note) => {
      Object.assign(currentNote, note);
      // 确保imgUris是数组
      currentNote.imgUris = note.imgUris ? [...note.imgUris] : [];
      isEditModalOpen.value = true;
    };

    // 提交编辑笔记
    const submitEditNote = async () => {
      try {
        loading.value = true;
        const token = getToken();
        const response = await axios.post(
          '/api/note/note/update',
          {
            id: currentNote.id,
            title: currentNote.title,
            content: currentNote.content,
            imgUris: currentNote.imgUris // 发送图片URL数组（Base64数据）
          },
          {
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${token}`,
            },
          }
        );
        if (response.data.success) {
          // 更新笔记列表
          const index = myNotes.value.findIndex(note => note.id === currentNote.id);
          if (index !== -1) {
            myNotes.value[index] = {
              ...myNotes.value[index],
              title: currentNote.title,
              content: currentNote.content,
              imgUris: [...currentNote.imgUris], // 更新图片URL
              updateTime: new Date().toISOString(),
              isTop: currentNote.isTop
            };
          }
          alert('笔记已更新。');
          closeEditModal();
        } else {
          throw new Error(response.data.message || '更新笔记失败。');
        }
      } catch (err) {
        console.error('更新笔记时出错:', err);
        alert(err.message || '更新笔记时出错。');
      } finally {
        loading.value = false;
      }
    };

    // 关闭编辑模态窗口
    const closeEditModal = () => {
      isEditModalOpen.value = false;
      // 重置当前笔记
      Object.assign(currentNote, {
        id: null,
        title: "",
        content: "",
        updateTime: "",
        isTop: false,
        visible: 1,
        imgUris: []
      });
    };

    // 发布笔记
    const openPublishModal = () => {
      isPublishModalOpen.value = true;
    };

    const closePublishModal = () => {
      isPublishModalOpen.value = false;
      // 重置新笔记
      Object.assign(newNote, {
        type: 0,
        imgUris: [],
        title: "",
        content: "",
        topicId: 1
      });
    };

    const submitPublishNote = async () => {
      try {
        loading.value = true;
        const token = getToken();
        const response = await axios.post(
          '/api/note/note/publish',
          {
            type: newNote.type,
            imgUris: newNote.imgUris, // 发送图片URL数组（Base64数据）
            title: newNote.title,
            content: newNote.content,
            topicId: newNote.topicId
          },
          {
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${token}`,
            },
          }
        );
        if (response.data.success) {
          alert('笔记已发布。');
          console.log(newNote.imgUris);
          // 重新加载笔记列表
          myNotes.value = [];
          page.value = 1;
          noMoreNotes.value = false;
          fetchMyNotes();
          closePublishModal();
        } else {
          throw new Error(response.data.message || '发布笔记失败。');
        }
      } catch (err) {
        console.error('发布笔记时出错:', err);
        alert(err.message || '发布笔记时出错。');
      } finally {
        loading.value = false;
      }
    };

    // 处理图片上传（发布笔记）
    const handlePublishImageUpload = (event) => {
      const files = event.target.files;
      if (!files.length) return;

      Array.from(files).forEach(file => {
        const reader = new FileReader();
        reader.onload = (e) => {
          newNote.imgUris.push(e.target.result); // 添加 Base64 数据 URL
        };
        reader.onerror = (err) => {
          console.error('图片读取出错:', err);
          alert('图片读取出错。');
        };
        reader.readAsDataURL(file);
      });

      // 清空文件输入
      event.target.value = '';
    };

    // 处理图片上传（编辑笔记）
    const handleEditImageUpload = (event) => {
      const files = event.target.files;
      if (!files.length) return;

      Array.from(files).forEach(file => {
        const reader = new FileReader();
        reader.onload = (e) => {
          currentNote.imgUris.push(e.target.result); // 添加 Base64 数据 URL
        };
        reader.onerror = (err) => {
          console.error('图片读取出错:', err);
          alert('图片读取出错。');
        };
        reader.readAsDataURL(file);
      });

      // 清空文件输入
      event.target.value = '';
    };

    // 移除图片
    const removeImage = (imgUrisArray, index) => {
      imgUrisArray.splice(index, 1);
    };

    // 删除笔记
    const deleteNote = async (note) => {
      if (!confirm('确定要删除这篇笔记吗？')) return;
      try {
        loading.value = true;
        const token = getToken();
        const response = await axios.post(
          '/api/note/note/delete',
          {
            type: note.type,
            videoUri: note.videoUri || null,
            title: note.title,
            content: note.content,
            topicId: note.topicId,
            id: note.id
          },
          {
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${token}`,
            },
          }
        );
        if (response.data.success) {
          alert('笔记已删除。');
          // 从列表中移除
          myNotes.value = myNotes.value.filter(n => n.id !== note.id);
        } else {
          throw new Error(response.data.message || '删除笔记失败。');
        }
      } catch (err) {
        console.error('删除笔记时出错:', err);
        alert(err.message || '删除笔记时出错。');
      } finally {
        loading.value = false;
      }
    };

    // 恢复已删除的笔记
    const restoreNote = async (note) => {
      try {
        loading.value = true;
        const token = getToken();
        const response = await axios.post(
          '/api/note/note/restore', // 确保后端有此 API
          {
            id: note.id
          },
          {
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${token}`,
            },
          }
        );
        if (response.data.success) {
          alert('笔记已恢复。');
          // 从已删除的列表中移除
          myNotes.value = myNotes.value.filter(n => n.id !== note.id);
        } else {
          throw new Error(response.data.message || '恢复笔记失败。');
        }
      } catch (err) {
        console.error('恢复笔记时出错:', err);
        alert(err.message || '恢复笔记时出错。');
      } finally {
        loading.value = false;
      }
    };

    // 置顶或取消置顶笔记
    const toggleTop = async (note) => {
      try {
        loading.value = true;
        const token = getToken();
        const response = await axios.post(
          '/api/note/note/top',
          {
            id: note.id,
            isTop: !note.isTop
          },
          {
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${token}`,
            },
          }
        );
        if (response.data.success) {
          note.isTop = !note.isTop;
          alert(note.isTop ? '笔记已置顶。' : '笔记已取消置顶。');
          // 无需手动排序，因为 sortedNotes 会自动响应变化
        } else {
          throw new Error(response.data.message || '操作失败。');
        }
      } catch (err) {
        console.error('置顶笔记时出错:', err);
        alert(err.message || '操作时出错。');
      } finally {
        loading.value = false;
      }
    };

    // 切换笔记可见性
    const toggleVisibility = async (note) => {
      try {
        loading.value = true;
        const token = getToken();
        const response = await axios.post(
          '/api/note/note/visible/onlyme',
          {
            id: note.id,
            visible: note.visible === 1 ? 0 : 1 // 修正字段名和逻辑
          },
          {
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${token}`,
            },
          }
        );
        if (response.data.success) {
          note.visible = note.visible === 1 ? 0 : 1;
          alert(note.visible === 1 ? '笔记现在公开可见。' : '笔记现在仅自己可见。');
        } else {
          throw new Error(response.data.message || '操作失败。');
        }
      } catch (err) {
        console.error('切换可见性时出错:', err);
        alert(err.message || '操作时出错。');
      } finally {
        loading.value = false;
      }
    };

    // 组件挂载时获取用户信息和初始笔记
    fetchCurrentUser();

    return {
      myNotes,
      sortedNotes, // 添加计算属性到返回对象
      status, // 添加 status 变量
      loading,
      error,
      noMoreNotes,
      loadMoreNotes,
      changeStatus, // 添加过滤方法到返回对象
      editNote,
      isEditModalOpen,
      currentNote,
      submitEditNote,
      closeEditModal,
      formatDate,
      isPublishModalOpen,
      openPublishModal,
      closePublishModal,
      newNote,
      submitPublishNote,
      deleteNote,
      restoreNote, // 添加恢复方法到返回对象
      toggleTop,
      toggleVisibility,
      handlePublishImageUpload,
      handleEditImageUpload,
      removeImage
    };
  },
};
</script>






<style scoped>
.my-notes-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f8f9fa;
  box-sizing: border-box;
}

h1 {
  text-align: center;
  margin-bottom: 30px;
  color: #343a40;
}

/* 过滤按钮样式 */
.filter-actions {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
  gap: 10px;
}

.filter-btn {
  padding: 8px 16px;
  font-size: 1em;
  color: #fff;
  background-color: #6c757d;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.filter-btn.active {
  background-color: #007bff;
}

.filter-btn:hover:not(.active) {
  background-color: #5a6268;
}

.actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.publish-btn {
  padding: 10px 20px;
  font-size: 1em;
  color: #fff;
  background-color: #17a2b8;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.publish-btn:hover {
  background-color: #138496;
}

.notes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.my-note-card {
  background: #ffffff;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.my-note-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.my-note-card h3 {
  font-size: 1.5em;
  margin-bottom: 10px;
  color: #007bff;
}

.my-note-card p {
  font-size: 1em;
  color: #555;
  margin-bottom: 15px;
  height: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.my-note-card small {
  font-size: 0.8em;
  color: #999;
}

.note-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.note-images img {
  width: 100%;
  max-width: 100px;
  height: auto;
  border-radius: 5px;
  object-fit: cover;
}

.note-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
}

.edit-btn, .delete-btn, .top-btn, .visibility-btn, .restore-btn {
  padding: 5px 10px;
  font-size: 0.9em;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.edit-btn {
  background-color: #ffc107;
  color: #212529;
}

.edit-btn:hover {
  background-color: #e0a800;
}

.delete-btn {
  background-color: #dc3545;
  color: #fff;
}

.delete-btn:hover {
  background-color: #c82333;
}

.restore-btn {
  background-color: #17a2b8;
  color: #fff;
}

.restore-btn:hover {
  background-color: #138496;
}

.top-btn {
  background-color: #28a745;
  color: #fff;
}

.top-btn.active {
  background-color: #218838;
}

.top-btn:hover:not(.active) {
  background-color: #218838;
}

.visibility-btn {
  background-color: #17a2b8;
  color: #fff;
}

.visibility-btn.active {
  background-color: #138496;
}

.visibility-btn:hover:not(.active) {
  background-color: #138496;
}

.load-more-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.load-more {
  padding: 10px 20px;
  font-size: 1em;
  color: #fff;
  background-color: #28a745;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.load-more:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.load-more:hover:not(:disabled) {
  background-color: #218838;
}

.error-message {
  color: red;
  text-align: center;
  margin-top: 20px;
  font-weight: bold;
}

.loading-spinner {
  text-align: center;
  font-size: 1.2em;
  color: #007bff;
}

/* 模态窗口样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: #ffffff;
  padding: 30px;
  border-radius: 10px;
  width: 90%;
  max-width: 600px; /* 增加宽度以容纳图片预览 */
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  position: relative;
}

.modal h2 {
  margin-bottom: 20px;
  color: #343a40;
}

.form {
  display: flex;
  flex-direction: column;
}

.form label {
  margin-bottom: 15px;
  color: #333;
  font-weight: bold;
}

.form input,
.form textarea {
  width: 100%;
  padding: 10px;
  margin-top: 5px;
  border: 1px solid #ccc;
  border-radius: 5px;
  box-sizing: border-box;
}

.form textarea {
  resize: vertical;
  height: 100px;
}

.image-previews {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.image-preview {
  position: relative;
  width: 100px;
  height: 100px;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 5px;
}

.image-preview button {
  position: absolute;
  top: 5px;
  right: 5px;
  background-color: rgba(220, 53, 69, 0.8);
  border: none;
  color: #fff;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  cursor: pointer;
  font-size: 0.8em;
  line-height: 20px;
  text-align: center;
  padding: 0;
}

.image-preview button:hover {
  background-color: rgba(220, 53, 69, 1);
}

.form-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.save-btn {
  background-color: #28a745;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.save-btn:hover {
  background-color: #218838;
}

.cancel-btn {
  background-color: #6c757d;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.cancel-btn:hover {
  background-color: #5a6268;
}

/* 过渡效果 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .notes-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
  
  .my-note-card p {
    height: 60px;
  }
}

@media (max-width: 480px) {
  .notes-grid {
    grid-template-columns: 1fr;
  }
  
  .my-note-card p {
    height: 80px;
  }
  
  .modal {
    padding: 20px;
  }
  
  .form-buttons {
    flex-direction: column;
    align-items: stretch;
  }
  
  .save-btn,
  .cancel-btn {
    width: 100%;
  }

  .image-preview {
    width: 80px;
    height: 80px;
  }
}

 /* 新增样式：置顶标识
.my-note-card::before {
  content: '';
  display: block;
  width: 0;
  height: 0;
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  border-bottom: 10px solid #ffc107;
  position: absolute;
  top: -10px;
  left: 50%;
  transform: translateX(-50%);
} 

.my-note-card.is-top::before {
  border-bottom-color: #28a745; 
} */

/* 新增样式：恢复按钮 */
.restore-btn {
  background-color: #17a2b8;
  color: #fff;
}

.restore-btn:hover {
  background-color: #138496;
}
</style>

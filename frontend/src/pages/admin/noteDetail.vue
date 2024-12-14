<template>
    <div class="note-detail-container">
        <el-card class="note-detail-card" shadow="hover">
            <div v-if="note">
                <h2 class="note-title">{{ note.title }}</h2>
                <div class="note-info">
                    <div class="image-carousel">
                        <el-carousel :interval="5000" type="card" height="300px">
                            <el-carousel-item v-for="(img, index) in note.imgUris" :key="index">
                                <img :src="img" alt="Note Image" class="carousel-image" />
                            </el-carousel-item>
                        </el-carousel>
                    </div>
                    <p class="note-content">{{ note.content }}</p>
                    <p class="note-updated">Last updated: {{ note.updateTime }}</p>
                    <div class="creator-info">
                        <img :src="note.avatar" alt="Creator Avatar" class="creator-avatar"
                            @click="goToUserDetail(note.creatorId)" />
                        <span class="creator-name">{{ note.creatorName }}</span>
                    </div>
                </div>

                <!-- 点赞和收藏 -->
                <div class="note-actions">
                    <el-button type="text" @click="toggleLike">
                        {{ isLiked ? '❤️' : '🤍' }}
                        {{ likeCount }}
                    </el-button>
                    <el-button type="text" @click="toggleCollect">
                        {{ isCollected ? '⭐' : '☆' }}
                        {{ collectCount }}
                    </el-button>
                    <el-button type="text" @click="scrollToComments">
                        💬 {{ comments.length }}
                    </el-button>
                </div>

                <!-- 评论区 -->
                <div class="comments-section">
                    <el-form :model="newComment" label-width="80px" class="comment-form">
                        <el-form-item label="Comment">
                            <el-input type="textarea" v-model="newComment.content"
                                placeholder="Write a comment..."></el-input>
                        </el-form-item>
                        <el-button type="primary" @click="submitComment">Submit</el-button>
                    </el-form>

                    <div class="comments-list">
                        <el-card v-for="comment in comments" :key="comment.commentId" class="comment-card">
                            <div class="comment-header">
                                <img :src="comment.userAvatar" alt="User Avatar" class="comment-avatar" />
                                <span class="comment-author">{{ comment.userName }}</span>
                                <span class="comment-time">{{ comment.createTime }}</span>
                            </div>
                            <p class="comment-content">{{ comment.content }}</p>
                            <el-button size="mini" @click="showReplyInput(comment.commentId)">Reply</el-button>

                            <div v-if="replyInputs[comment.commentId]" class="reply-input">
                                <el-input type="textarea" v-model="replyContent[comment.commentId]"
                                    placeholder="Write a reply..."></el-input>
                                <el-button size="mini" type="primary"
                                    @click="submitReply(comment.commentId)">Submit</el-button>
                            </div>

                            <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
                                <el-card v-for="reply in comment.replies" :key="reply.id" class="reply-card">
                                    <div class="reply-header">
                                        <img :src="reply.userAvatar" alt="User Avatar" class="reply-avatar" />
                                        <span class="reply-author">{{ reply.userName }}</span>
                                        <span class="reply-time">{{ reply.createTime }}</span>
                                    </div>
                                    <p class="reply-content">{{ reply.content }}</p>
                                </el-card>
                            </div>
                        </el-card>
                    </div>
                </div>
            </div>
            <div v-else>
                <p>Loading note details...</p>
            </div>
        </el-card>
    </div>
</template>

<script>
import { getNoteDetailService, addCommentService, getNoteCommentsService, getCommentRepliesService } from "@/api/note.js";
import { ElMessage } from "element-plus";
import { dolikeNoteService, nolikeNoteService, doCollectService, unCollectService, noteCountService } from "@/api/collect.js";
export default {
    data() {
        return {
            note: null, // 当前笔记详情
            comments: [], // 评论列表
            newComment: { content: "" }, // 新评论内容
            replyInputs: {}, // 控制每个评论的回复输入框是否显示
            replyContent: {}, // 存储每个评论的回复内容
            isLiked: false,
            isCollected: false,
            likeCount: 0,
            collectCount: 0,
        };
    },
    methods: {
        async fetchNoteCount() {
            const noteId = this.$route.params.id;
            if (!noteId) {
                console.warn("Note ID is invalid. Skipping fetchNoteCount.");
                return;
            }
            try {
                const response = await noteCountService({ noteId });
                console.log("笔记计数接口返回数据：", response);
                if (response.success) {
                    this.likeCount = response.data.likeTotal || 0;
                    this.collectCount = response.data.collectTotal || 0;
                    console.log("点赞数：", this.likeCount);
                    console.log("收藏数：", this.collectCount);
                } else {
                    // 接口返回的成功标识为 false，可以在这里设置默认值
                    console.warn("Failed to fetch note count, using defaults.");
                    this.likeCount = 0;
                    this.collectCount = 0;
                }
            } catch (error) {
                // 捕获500错误，在控制台记录，但不弹错误提示，使用默认值兜底
                console.error("获取笔记计数信息失败，使用默认计数值:", error);
                this.likeCount = 0;
                this.collectCount = 0;
                // 可选：不使用 ElMessage 提示用户错误，将其默默处理
                // ElMessage.error("获取计数失败，已使用默认值");
            }
        },
        async fetchNoteDetail() {
            const noteId = this.$route.params.id;
            if (!noteId) {
                console.warn("Note ID is null or undefined. Skipping fetch.");
                return;
            }
            this.note = null;
            try {
                const response = await getNoteDetailService({ id: noteId });
                if (response.success) {
                    this.note = response.data;
                    this.isLiked = JSON.parse(localStorage.getItem(`note_${noteId}_liked`)) || false;
                    this.isCollected = JSON.parse(localStorage.getItem(`note_${noteId}_collected`)) || false;
                    await this.fetchNoteCount(); // 获取计数信息
                    this.fetchComments();
                } else {
                    //ElMessage.error(response.message || "Failed to fetch note details.");
                    console.error("获取笔记计数信息失败，使用默认计数值:", error);
                    this.likeCount = 0;
                    this.collectCount = 0;
                }
            } catch (error) {
                console.error("Error fetching note detail:", error);
                ElMessage.error("An error occurred while fetching note details.");
            }
        },
        fetchComments() {
            const noteId = this.$route.params.id;
            getNoteCommentsService({ noteId, page: 1, size: 10 })
                .then((response) => {
                    if (response.success) {
                        this.comments = response.data;
                        this.comments.forEach((comment) => {
                            this.fetchReplies(comment.commentId);
                        });
                    } else {
                        ElMessage.error(response.message || "Failed to fetch comments.");
                    }
                })
                .catch((error) => {
                    console.error("Error fetching comments:", error);
                    ElMessage.error("An error occurred while fetching comments.");
                });
        },
        fetchReplies(commentId) {
            getCommentRepliesService({ commentId })
                .then((response) => {
                    if (response.success) {
                        const commentIndex = this.comments.findIndex((comment) => comment.commentId === commentId);
                        if (commentIndex !== -1) {
                            // Vue 3 中直接赋值可以触发响应式更新
                            this.comments[commentIndex].replies = response.data;
                        }
                    } else {
                        ElMessage.error(response.message || "Failed to fetch replies.");
                    }
                })
                .catch((error) => {
                    console.error("Error fetching replies:", error);
                    ElMessage.error("An error occurred while fetching replies.");
                });
        },
        submitComment() {
            const noteId = this.$route.params.id;
            if (!this.newComment.content.trim()) {
                ElMessage.warning("Comment content cannot be empty.");
                return;
            }
            addCommentService({ noteId, content: this.newComment.content })
                .then((response) => {
                    if (response.success) {
                        ElMessage.success("Comment added successfully.");
                        this.newComment.content = ""; // 清空输入框
                        this.fetchComments(); // 重新加载评论列表
                    } else {
                        ElMessage.error(response.message || "Failed to add comment.");
                    }
                })
                .catch((error) => {
                    console.error("Error adding comment:", error);
                    ElMessage.error("An error occurred while adding comment.");
                });
        },
        showReplyInput(commentId) {
            this.replyInputs[commentId] = true;
        },
        submitReply(parentCommentId) {
            const noteId = this.$route.params.id;
            const content = this.replyContent[parentCommentId]?.trim();
            if (!content) {
                ElMessage.warning("Reply content cannot be empty.");
                return;
            }
            addCommentService({ noteId, content, parentId: parentCommentId })
                .then((response) => {
                    if (response.success) {
                        ElMessage.success("Reply added successfully.");
                        this.replyContent[parentCommentId] = ""; // 清空回复输入框
                        this.fetchReplies(parentCommentId); // 重新加载回复列表
                    } else {
                        ElMessage.error(response.message || "Failed to add reply.");
                    }
                })
                .catch((error) => {
                    console.error("Error adding reply:", error);
                    ElMessage.error("An error occurred while adding reply.");
                });
        },
        goToUserDetail(userId) {
            this.$router.push({ name: "UserDetail", params: { id: userId } });
        },
        async toggleLike() {
            const noteId = this.$route.params.id;
            const service = this.isLiked ? nolikeNoteService : dolikeNoteService;

            try {
                const response = await service({ id: noteId });
                if (response.success) {
                    this.isLiked = !this.isLiked;
                    localStorage.setItem(`note_${noteId}_liked`, JSON.stringify(this.isLiked));
                    await this.fetchNoteCount(); // 重新获取最新计数
                    ElMessage.success(this.isLiked ? "点赞成功" : "取消点赞成功");
                } else {
                    ElMessage.error(response.message || "操作失败");
                }
            } catch (error) {
                console.error("Error toggling like:", error);
                ElMessage.error("操作失败");
            }
        },
        async toggleCollect() {
            const noteId = this.$route.params.id;
            const service = this.isCollected ? unCollectService : doCollectService;

            try {
                const response = await service({ id: noteId });
                if (response.success) {
                    this.isCollected = !this.isCollected;
                    localStorage.setItem(`note_${noteId}_collected`, JSON.stringify(this.isCollected));
                    await this.fetchNoteCount(); // 重新获取最新计数
                    ElMessage.success(this.isCollected ? "收藏成功" : "取消收藏成功");
                } else {
                    ElMessage.error(response.message || "操作失败");
                }
            } catch (error) {
                console.error("Error toggling collect:", error);
                ElMessage.error("操作失败");
            }
        },
    },
    mounted() {
        this.fetchNoteDetail();
    },
    watch: {
        "$route.params.id": {
            handler(newId) {
                if (!newId) {
                    console.warn("Route param 'id' is invalid. Skipping fetch.");
                    this.note = null; // 清空数据
                    return;
                }
                this.note = null; // 清空旧数据，显示加载状态
                this.fetchNoteDetail(); // 重新加载新笔记详情
            },
            immediate: true, // 确保初次加载时也触发
        },
    },
};
</script>


<style scoped>
.note-detail-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 73vh;
    /* 占满窗口高度 */
    overflow: hidden;
    /* 禁止外部滚动 */
    padding: 0;
    background-color: #f5f5f5;
}

.note-detail-card {
    width: 90%;
    max-width: 1200px;
    height: 100%;
    /* 尽可能覆盖窗口 */
    overflow-y: auto;
    /* 允许内容滚动 */
    padding: 20px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    border-radius: 10px;
    background: #fff;
}

.note-title {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 20px;
}

.note-info {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.image-carousel {
    width: 100%;
    height: 300px;
    margin-bottom: 15px;
}

.carousel-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 8px;
}

.note-content {
    font-size: 16px;
    line-height: 1.5;
}

.note-updated {
    font-size: 14px;
    color: #999;
}

.creator-info {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 15px;
}

.creator-avatar {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    cursor: pointer;
}

.creator-name {
    font-size: 16px;
    font-weight: bold;
    cursor: pointer;
}

.comments-section {
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #eee;
}

.comment-form {
    margin-bottom: 20px;
}

.comments-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.comment-card {
    padding: 15px;
    border: 1px solid #ddd;
    border-radius: 8px;
    background-color: #f9f9f9;
}

.comment-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;
}

.comment-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
}

.comment-author {
    font-size: 14px;
    font-weight: bold;
}

.comment-time {
    font-size: 12px;
    color: #888;
    margin-left: auto;
}

.comment-content {
    font-size: 14px;
    line-height: 1.4;
}

.reply-input {
    margin-top: 10px;
    padding-left: 20px;
    /* 缩进以区分层次 */
    border-left: 2px solid #f0f0f0;
}

.replies-list {
    margin-top: 15px;
    padding-left: 20px;
    /* 缩进以区分层次 */
    border-left: 2px solid #f0f0f0;
}

.reply-card {
    margin-bottom: 10px;
    padding: 10px;
    background-color: #fafafa;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.reply-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 5px;
}

.reply-avatar {
    width: 30px;
    height: 30px;
    border-radius: 50%;
}

.reply-author {
    font-weight: bold;
    color: #333;
}

.reply-time {
    font-size: 12px;
    color: #888;
    margin-left: auto;
}

.reply-content {
    font-size: 14px;
    line-height: 1.4;
    color: #555;
}
</style>
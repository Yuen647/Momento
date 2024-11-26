import axios from "@/axios";

//获取用户收藏笔记信息
export function collectListService(params) {
    return axios.post("/note/note/collected/list", params)
}

//查看收藏笔记详情
export function collectListDetailService(params) {
    return axios.post("/note/note/detail", params)
}

//笔记收藏
export function doCollectService(params) {
    return axios.post("/note/note/collect", params)
}

//笔记取消收藏
export function unCollectService(params) {
    return axios.post("/note/note/uncollect", params)
}

//点赞列表
export function likeListService(params) {
    return axios.post("/note/note/liked/list", params)
}

//点赞笔记
export function dolikeNoteService(params) {
    return axios.post("/note/note/like", params)
}

//取消点赞笔记
export function nolikeNoteService(params) {
    return axios.post("/note/note/unlike", params)
}
package com.tongji.momento.kv.biz.service;

import com.tongji.framework.common.response.Response;
import com.tongji.momento.kv.dto.req.AddNoteContentReqDTO;
import com.tongji.momento.kv.dto.req.DeleteNoteContentReqDTO;
import com.tongji.momento.kv.dto.req.FindNoteContentReqDTO;
import com.tongji.momento.kv.dto.rsp.FindNoteContentRspDTO;


public interface NoteContentService {

    /**
     * 添加笔记内容
     *
     * @param addNoteContentReqDTO
     * @return
     */
    Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO);

    /**
     * 查询笔记内容
     *
     * @param findNoteContentReqDTO
     * @return
     */
    Response<FindNoteContentRspDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO);

    /**
     * 删除笔记内容
     *
     * @param deleteNoteContentReqDTO
     * @return
     */
    Response<?> deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO);

}
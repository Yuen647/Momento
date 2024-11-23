package com.tongji.momento.kv.api;

import com.tongji.framework.common.response.Response;
import com.tongji.momento.kv.constant.ApiConstants;
import com.tongji.momento.kv.dto.req.AddNoteContentReqDTO;
import com.tongji.momento.kv.dto.req.DeleteNoteContentReqDTO;
import com.tongji.momento.kv.dto.req.FindNoteContentReqDTO;
import com.tongji.momento.kv.dto.rsp.FindNoteContentRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface KeyValueFeignApi {

    String PREFIX = "/kv";

    @PostMapping(value = PREFIX + "/note/content/add")
    Response<?> addNoteContent(@RequestBody AddNoteContentReqDTO addNoteContentReqDTO);

    @PostMapping(value = PREFIX + "/note/content/find")
    Response<FindNoteContentRspDTO> findNoteContent(@RequestBody FindNoteContentReqDTO findNoteContentReqDTO);

    @PostMapping(value = PREFIX + "/note/content/delete")
    Response<?> deleteNoteContent(@RequestBody DeleteNoteContentReqDTO deleteNoteContentReqDTO);
}



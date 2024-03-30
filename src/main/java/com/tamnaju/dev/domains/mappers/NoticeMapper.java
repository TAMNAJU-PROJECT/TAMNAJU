package com.tamnaju.dev.domains.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tamnaju.dev.domains.entities.NoticeEntity;
import com.tamnaju.dev.domains.vos.SearchVo;

@Mapper
public interface NoticeMapper {
    public NoticeEntity findNoticeById(@Param(value = "id") int id);

    public NoticeEntity[] findNotices(SearchVo searchVo);

    public int findNoticesWithoutData(SearchVo searchVo);

    public int saveNotice(NoticeEntity noticeEntity);
}

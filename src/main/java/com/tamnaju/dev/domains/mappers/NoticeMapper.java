package com.tamnaju.dev.domains.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tamnaju.dev.domains.entities.NoticeEntity;

@Mapper
public interface NoticeMapper {
    public NoticeEntity findNoticeById(@Param(value = "id") int id);

    public NoticeEntity[] findNoticesByNotice(@Param(value = "noticeEntity") NoticeEntity noticeEntity,
            @Param(value = "orderBy") String orderBy,
            @Param(value = "limit") int limit,
            @Param(value = "offset") int offset);

    public int findNoticesByNoticeWithoutData(@Param(value = "noticeEntity") NoticeEntity noticeEntity);

    public int saveNotice(NoticeEntity noticeEntity);
}

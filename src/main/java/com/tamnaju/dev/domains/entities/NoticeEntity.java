package com.tamnaju.dev.domains.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeEntity {
    private String id;
    private String userId;
    private String title;
    private String content;
    private String view;
    private String postedAt;
    private String modifiedAt;
    private String deletedAt;

    public NoticeEntity noticeDtoToNoticeEntity(NoticeEntity noticeEntity) {
        return NoticeEntity.builder()
                .id(noticeEntity.getId())
                .userId(noticeEntity.getUserId())
                .title(noticeEntity.getTitle())
                .content(noticeEntity.getContent())
                .view(noticeEntity.getView())
                .postedAt(noticeEntity.getPostedAt())
                .modifiedAt(noticeEntity.getModifiedAt())
                .deletedAt(noticeEntity.getDeletedAt())
                .build();
    }
}

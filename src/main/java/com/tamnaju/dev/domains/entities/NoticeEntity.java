package com.tamnaju.dev.domains.entities;

import com.tamnaju.dev.domains.dtos.NoticeDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeEntity {
    private int id;
    private String userId;
    private String title;
    private String content;
    private int view;
    private String postedAt;
    private String modifiedAt;
    private String deletedAt;

    public static NoticeEntity noticeDtoToNoticeEntity(NoticeDto noticeDto) {
        return NoticeEntity.builder()
                .id(noticeDto.getId())
                .userId(noticeDto.getUserId())
                .title(noticeDto.getTitle())
                .content(noticeDto.getContent())
                .view(noticeDto.getView())
                .postedAt(noticeDto.getPostedAt())
                .modifiedAt(noticeDto.getModifiedAt())
                .deletedAt(noticeDto.getDeletedAt())
                .build();
    }
}

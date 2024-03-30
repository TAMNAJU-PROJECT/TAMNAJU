package com.tamnaju.dev.domains.dtos;

import com.tamnaju.dev.domains.entities.NoticeEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDto {
    private int id;
    private String userId;
    @NotBlank
    @Pattern(regexp = "^[A-Za-z가-힣\\s]{1,25}$")
    private String title;
    @NotBlank
    @Pattern(regexp = "^[A-Za-z가-힣\\s]{1,500}$")
    private String content;
    private int view;
    private String postedAt;
    private String modifiedAt;
    private String deletedAt;

    private String orderBy;
    private int limit;
    private int page;

    public static NoticeDto noticeEntityToNoticeDto(NoticeEntity noticeEntity) {
        return NoticeDto.builder()
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

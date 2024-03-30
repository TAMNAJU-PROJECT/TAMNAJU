package com.tamnaju.dev.domains.vos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchVo {
    private String userId;
    private String title;
    private String content;

    private String orderBy;
    private int limit;
    private int page;
    private int offset;
}

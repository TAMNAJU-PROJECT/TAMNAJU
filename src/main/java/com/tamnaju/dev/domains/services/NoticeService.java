package com.tamnaju.dev.domains.services;

import org.springframework.stereotype.Service;

import com.tamnaju.dev.domains.dtos.NoticeDto;
import com.tamnaju.dev.domains.entities.NoticeEntity;
import com.tamnaju.dev.domains.mappers.NoticeMapper;
import com.tamnaju.dev.domains.results.notice.NoticePostResult;
import com.tamnaju.dev.domains.vos.SearchVo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NoticeService {
    private NoticeMapper noticeMapper;

    public NoticeService(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    public NoticePostResult insertNotice(NoticeDto noticeDto) {
        NoticeEntity noticeEntity = NoticeEntity.noticeDtoToNoticeEntity(noticeDto);
        return noticeMapper.saveNotice(noticeEntity) > 0
                ? NoticePostResult.SUCCESS
                : NoticePostResult.FAILURE;
    }

    public NoticeDto selecNoticeById(int id) {
        NoticeEntity noticeEntity = noticeMapper.findNoticeById(id);
        NoticeDto noticeDto = null;
        if (noticeEntity != null) {
            noticeEntity.setView(noticeEntity.getView() + 1);
            noticeDto = NoticeDto.noticeEntityToNoticeDto(noticeEntity);
        }
        return noticeDto;
    }

    public NoticeDto[] selectNotices(SearchVo searchVo) {
        if (searchVo.getUserId() == null) {
            searchVo.setUserId("");
        }
        if (searchVo.getTitle() == null) {
            searchVo.setTitle("");
        }
        if (searchVo.getContent() == null) {
            searchVo.setContent("");
        }

        String orderBy = searchVo.getOrderBy();
        int limit = searchVo.getLimit();
        int page = searchVo.getPage();

        // orderBy, limit, page 값이 예상된 범위를 벗어나지 않는지 검증한다.
        // 만약 벗어난다면 서버에 대한 공격일 가능성을 있으므로 간단한 로그를 남긴다.
        if (orderBy == null ||
                (!orderBy.equals("id") &&
                        !orderBy.equals("user_id") &&
                        !orderBy.equals("title") &&
                        !orderBy.equals("content"))) {
            log.warn("[NoticeService] selectNotices()" +
                    "\n\tUnexpected Column Name : " + orderBy);
            orderBy = "id";
            searchVo.setOrderBy("id");
        }
        if (limit <= 0) {
            log.warn("[NoticeService] selectNotices" +
                    "\n\tToo Small Limit Number : " + limit);
            limit = 10;
            searchVo.setLimit(10);
        }
        if (page <= 0) {
            log.warn("[NoticeService] selectNotices" +
                    "\n\tToo Small Page Number : " + page);
            page = 1;
            searchVo.setPage(1);
        }

        searchVo.setOffset(limit * (page - 1));

        // 요청한 페이지가 조회 가능한 범위를 초과할 경우,
        // 조회 가능한 마지막 페이지를 응답하고, 로그를 남긴다.
        int count = noticeMapper.findNoticesWithoutData(searchVo);
        if (page > Math.ceil(count / limit)) {
            log.warn("[NoticeService] selectNotices" +
                    "\n\tToo Big Page Number : " + page);
            page = (int) Math.ceil(count / limit);
            searchVo.setPage((int) Math.ceil(count / limit));
        }

        NoticeEntity[] noticeEntities = noticeMapper.findNotices(searchVo);

        // DB로부터 읽어낸 NoticeEntity 배열이 null이 아니라면, NoticeDto 배열로 변환한다.
        NoticeDto[] noticeDtos = null;
        if (noticeEntities.length != 0) {
            noticeDtos = new NoticeDto[noticeEntities.length];
            for (int i = 0; i < noticeEntities.length; i++) {
                noticeDtos[i] = NoticeDto.noticeEntityToNoticeDto(noticeEntities[i]);
            }
        }

        return noticeDtos;
    }

    public int selectNoticesWithoutData(SearchVo searchVo) {
        int count = noticeMapper.findNoticesWithoutData(searchVo);
        return count;
    }
}

package com.learning_forum.service;

import com.learning_forum.domain.STATUS;
import com.learning_forum.dto.request.GetPostOrComment;
import com.learning_forum.dto.request.ReportRequest;
import com.learning_forum.dto.respone.ListReportResponse;
import com.learning_forum.dto.respone.PostOrCommentResponse;
import com.learning_forum.dto.respone.ReportResponse;
import com.learning_forum.entity.Post;
import com.learning_forum.entity.Report;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.mapper.ReportMapper;
import com.learning_forum.repository.CommentRepository;
import com.learning_forum.repository.PostRepository;
import com.learning_forum.repository.ReportRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportService {

    ReportRepository reportRepository;
    ReportMapper reportMapper;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // Tạo mới báo cáo
    public void createReport(ReportRequest request) {
        log.info("Creating report with request: {}", request);

        Report report = reportMapper.toReport(request);
        report.setStatus(STATUS.PENDING);
        reportRepository.save(report);
    }

    // Lấy tất cả báo cáo
    public ListReportResponse getAll(int page, int size, String sortBy, String order, String search) {
        log.info("Fetching all reports with page: {}, size: {}, sortBy: {}, order: {}, search: {}", page, size, sortBy, order, search);

        int pageIndex = Math.max(page - 1, 0);

        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        Specification<Report> spec = getReportSpecification(search);
        Page<Report> reports = reportRepository.findAll(spec, pageable);
        List<ReportResponse> response = reports.getContent()
                .stream()
                .map(reportMapper::toReportForAdmin)
                .collect(Collectors.toList());

        return new ListReportResponse(response, reports.getTotalElements(), reports.getTotalPages(), page, size);
    }

    // Specification tìm kiếm theo tiêu đề bài viết
    private static @NotNull Specification<Report> getReportSpecification(String search) {
        Specification<Report> spec = Specification.where(null); // không lọc mặc định gì cả

        if (search != null && !search.trim().isEmpty()) {
            Specification<Report> searchSpec = (root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("title")),
                            "%" + search.toLowerCase() + "%"
                    );
            spec = spec.and(searchSpec);
        }

        return spec;
    }

    // Lấy Bài viết hoặc bình luận theo ID
    public PostOrCommentResponse getPostOrCommentById(GetPostOrComment request) {
        log.info("Get post or comment by: {}", request);
        if (request.getPostId() != null && request.getCommentId() == null) {
            // Lấy bài viết theo ID
            return reportMapper.toPostOrCommentResponse(postRepository.findPostById(request.getPostId())
                    .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND)));
        } else if (request.getCommentId() != null && request.getPostId() == null) {
            // Lấy bình luận theo ID
            return reportMapper.toPostOrCommentResponse(commentRepository.findCommentById(request.getCommentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND)));
        } else {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // Xoá Bài viết hoặc bình luận theo ID
    public void deletePostOrCommentById(GetPostOrComment request) {
        log.info("Delete post or comment by: {}", request);

        if (request.getPostId() != null && request.getCommentId() == null) {
            // Xoá bài viết
            Post post = postRepository.findPostById(request.getPostId())
                    .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
            post.setStatus(STATUS.BLOCKED);
            postRepository.save(post);
            //         Đánh dấu report ban đầu là đã xử lý
            Report report = reportRepository.findReportById(request.getReportId())
                    .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));
            report.setStatus(STATUS.APPROVED);
            reportRepository.save(report);

        } else if (request.getCommentId() != null && request.getPostId() == null) {
            // Xoá bình luận
            // Lấy tất cả report liên quan tới comment
            List<Report> reports = reportRepository.findByCommentId(request.getCommentId());
            for (Report report : reports) {
                report.setComment(null); // bỏ liên kết với comment
                report.setStatus(STATUS.APPROVED); // đánh dấu là đã xử lý
            }
            reportRepository.saveAll(reports); // cập nhật lại các report

            commentRepository.findCommentById(request.getCommentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

            // Sau khi xoá report, mới xoá comment
            commentRepository.deleteById(request.getCommentId());

        } else {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}

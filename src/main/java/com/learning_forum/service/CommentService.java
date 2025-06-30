package com.learning_forum.service;

import com.learning_forum.dto.request.CommentRequest;
import com.learning_forum.dto.request.DeleteCommentRequest;
import com.learning_forum.dto.request.UpdateCommentRequest;
import com.learning_forum.dto.respone.CommentResponse;
import com.learning_forum.dto.respone.ListCommentResponse;
import com.learning_forum.entity.Comment;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.mapper.CommentMapper;
import com.learning_forum.repository.CommentRepository;
import com.learning_forum.repository.PostRepository;
import com.learning_forum.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {

    CommentRepository commentRepository;
    CommentMapper commentMapper;
    PostRepository postRepository;
    UserRepository userRepository;
    NotificationService notificationService;

    // Comment on post
    public CommentResponse createComment(CommentRequest request) {
        postRepository.findById(request.getPostId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Comment comment = commentMapper.toComment(request);

        //gửi thông báo đến tác giả bài viết
        notificationService.notifyAuthorPostCommented(request.getPostId(), request.getUserId());

        return commentMapper.toCommentResponse(commentRepository.save(comment));
    }

    // get list comment by post id
    public ListCommentResponse getListCommentByPostId(String postId, int page, int size, String sortBy, String order) {
        postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        Sort.Direction direction = order.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);

        List<CommentResponse> commentResponses = commentPage.getContent().stream()
                .map(commentMapper::toCommentResponse)
                .collect(Collectors.toList());

        return ListCommentResponse.builder()
                .comments(commentResponses)
                .total(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .page(page)
                .size(size)
                .build();
    }

    // Delete comment by id
    public void deleteComment(DeleteCommentRequest request) {
        Comment comment = commentRepository.findById(request.getCommentId())
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        commentRepository.delete(comment);
    }

    // Update comment
    public void updateComment(String id, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        commentMapper.updateComment(comment, request);
        commentRepository.save(comment);
    }

    //Lấy số lượng bình luận trong tháng này
    public int countCommentsInCurrentMonth() {
        log.info("Counting comments in the current month");
        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59);
        return commentRepository.countCommentsInCurrentMonth(startOfMonth, endOfMonth);
    }
}

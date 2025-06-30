package com.learning_forum.service;

import com.learning_forum.config.SecurityConfig;
import com.learning_forum.domain.STATUS;
import com.learning_forum.domain.USER_ROLE;
import com.learning_forum.dto.request.ApproveOrRejectPostRequest;
import com.learning_forum.dto.request.DeleteRequest;
import com.learning_forum.dto.request.UploadDocumentRequest;
import com.learning_forum.dto.respone.DocumentFromCategoryResponse;
import com.learning_forum.dto.respone.DocumentResponse;
import com.learning_forum.dto.respone.ListDocumentResponse;
import com.learning_forum.dto.respone.ListDocumentResponseForAdmin;
import com.learning_forum.entity.Document;
import com.learning_forum.entity.Post;
import com.learning_forum.entity.User;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.mapper.DocumentMapper;
import com.learning_forum.repository.CategoryRepository;
import com.learning_forum.repository.DocumentRepository;
import com.learning_forum.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileStorageService {

    DocumentRepository documentRepository;
    DocumentMapper documentMapper;
    CategoryRepository categoryRepository;
    UserRepository userRepository;
    SecurityConfig securityConfig;

    public String storeFile(MultipartFile file) throws IOException {
        String uploadDir = System.getProperty("user.dir") + "/uploads/Image";
        Path uploadPath = Paths.get(uploadDir);

        // Tạo thư mục nếu chưa tồn tại
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        file.transferTo(filePath.toFile());

        return "/uploads/Image/" + fileName;
    }

    public String storeDocument(MultipartFile file, UploadDocumentRequest request) throws IOException {
        String uploadDir = System.getProperty("user.dir") + "/uploads/Document";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        if (request.getStatus() == null) {
            request.setStatus(STATUS.PENDING);
        }

        String fileName = request.getFileName();
        if (!fileName.contains(".")) {
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            if (!extension.isEmpty()) {
                fileName = fileName + "." + extension;
            }
        }

        String contentType = file.getContentType();
        if (!contentType.startsWith("application/") && !contentType.startsWith("text/")) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        Path filePath = uploadPath.resolve(fileName);
        int count = 1;
        while (Files.exists(filePath)) {
            String name = FilenameUtils.getBaseName(fileName);
            String extension = FilenameUtils.getExtension(fileName);
            fileName = name + "_" + count + (extension.isEmpty() ? "" : "." + extension);
            filePath = uploadPath.resolve(fileName);
            count++;
        }

        // Tạo Document entity từ mapper
        Document document = documentMapper.toDocument(request, fileName);

        // Lưu entity vào DB
        documentRepository.save(document);

        // Lưu file vật lý
        file.transferTo(filePath.toFile());

        return "/uploads/Document/" + fileName;
    }

    public ListDocumentResponseForAdmin getAllDocuments(int page, int size, String sortBy, String order, String search) {
        log.info("getAllDocuments" + " with page: {}, size: {}, sortBy: {}, order: {}, search: {}", page, size, sortBy, order, search);

        int pageIndex = Math.max(page - 1, 0);
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        Specification<Document> spec = getDocumentSpecification(search);
        Page<Document> documents = documentRepository.findAll(spec, pageable);

        List<DocumentResponse> response = documents.getContent()
                .stream()
                .map(documentMapper::toDocumentResponseForAdmin)
                .collect(Collectors.toList());

        return new ListDocumentResponseForAdmin(response, documents.getTotalElements(), documents.getTotalPages(), page, size);
    }

    private static Specification<Document> getDocumentSpecification(String search) {
        Specification<Document> spec = Specification.where(
                (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), STATUS.APPROVED)
        );

        if (search != null && !search.trim().isEmpty()) {
            Specification<Document> searchSpec = (root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("fileName")),
                            "%" + search.toLowerCase() + "%"
                    );
            spec = spec.and(searchSpec);
        }

        return spec;
    }

    // Lấy tất cả tài liệu cho admin
    public ListDocumentResponseForAdmin getAllDocumentsForAdmin(int page, int size, String sortBy, String order, String search, String status) {
        log.info("getAllDocumentsForAdmin with page: {}, size: {}, sortBy: {}, order: {}, search: {}", page, size, sortBy, order, search);

        int pageIndex = Math.max(page - 1, 0);
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        Specification<Document> spec = getDocumentSpecificationForAdmin(search, status);
        Page<Document> documents = documentRepository.findAll(spec, pageable);

        List<DocumentResponse> response = documents.getContent()
                .stream()
                .map(documentMapper::toDocumentResponseForAdmin)
                .collect(Collectors.toList());

        return new ListDocumentResponseForAdmin(response, documents.getTotalElements(), documents.getTotalPages(), page, size);
    }

    private static Specification<Document> getDocumentSpecificationForAdmin(String search, String status) {
        Specification<Document> spec = Specification.where(null);

        if (search != null && !search.trim().isEmpty()) {
            Specification<Document> searchSpec = (root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("fileName")),
                            "%" + search.toLowerCase() + "%"
                    );
            spec = spec.and(searchSpec);
        }
        // Lọc theo status
        if (status != null && !status.trim().isEmpty()) {
            Specification<Document> statusSpec = (root, query, cb) ->
                    cb.equal(root.get("status"), status);
            spec = spec.and(statusSpec);
        }

        return spec;
    }

    public ListDocumentResponse getDocumentsByCategory(String id, int page, int size, String sortBy, String order) {
        log.info("getDocumentsByCategory with id: {}, page: {}, size: {}, sortBy: {}, order: {}", id, page, size, sortBy, order);

        int pageIndex = Math.max(page - 1, 0);
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));
        Page<Document> documentPage = documentRepository.findAllByCategoryIdAndStatus(id, STATUS.APPROVED, pageable);

        List<DocumentResponse> documentResponses = documentPage.getContent()
                .stream()
                .map(documentMapper::toDocumentResponseForUser)
                .collect(Collectors.toList());

        var categoryName = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND))
                .getName();

        return new ListDocumentResponse(
                categoryName,
                documentResponses,
                documentPage.getTotalElements(),
                documentPage.getTotalPages(),
                page,
                size
        );
    }

    public void approveOrRejectDocument(ApproveOrRejectPostRequest request) {
        log.info("approveOrRejectDocument with request: {}", request);
        Document document = documentRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        if (request.getRejectReason() == null) {
            document.setRejectReason(null);
            document.setStatus(STATUS.APPROVED);
        } else {
            document.setStatus(STATUS.REJECTED);
            document.setRejectReason(request.getRejectReason());
        }

        documentRepository.save(document);
    }

    //Lấy số lượng tài liệu được tải lên trong tháng hiện tại
    public int countDocumentsInCurrentMonth() {
        log.info("Counting documents in the current month");
        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59);

        return documentRepository.countByCreatedAtBetween(startOfMonth, endOfMonth, STATUS.APPROVED);
    }

    // Xóa tài liệu
    public void deleteDocument(String id) {
        log.info("Deleting document with id: {}", id);
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        // Xóa file vật lý
        String filePath = System.getProperty("user.dir") + "/uploads/Document/" + document.getFileName();
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            throw new AppException(ErrorCode.FILE_DELETION_FAILED);
        }

        // Xóa bản ghi trong cơ sở dữ liệu
        documentRepository.delete(document);
    }

    // Lấy tất cả tài liệu của người dùng
    public ListDocumentResponse getDocumentsByUserId(
            int page, int size, String sortBy, String order, String search) {
        var currentUsername = securityConfig.getCurrentUsername();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        var userId = user.getId();
        int pageIndex = Math.max(page - 1, 0);
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        if (search != null && search.trim().isEmpty()) {
            search = null; // tránh lỗi chuỗi rỗng
        }

        Page<Document> documentPage = documentRepository.findByUserAndStatusAndSearch(
                userId, STATUS.APPROVED, search, pageable);

        List<DocumentResponse> documentResponses = documentPage.getContent()
                .stream()
                .map(documentMapper::toDocumentResponseForUser)
                .collect(Collectors.toList());

        return new ListDocumentResponse(
                "Documents by User",
                documentResponses,
                documentPage.getTotalElements(),
                documentPage.getTotalPages(),
                page,
                size
        );
    }

    // Xóa tài liệu theo ID
    public void deleteDocumentById(DeleteRequest request) {
        log.info("Deleting document with id: {}", request.getId());
        Document document = documentRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        // Kiểm tra quyền của người dùng
        String currentUsername = securityConfig.getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (
                !document.getUser().getId().equals(currentUser.getId()) &&
                        !(currentUser.getRole().equals(USER_ROLE.ADMIN) || currentUser.getRole().equals(USER_ROLE.SUPER_ADMIN))
        ) {
            log.error("Unauthorized attempt to delete document by user: {}", currentUsername);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        // Xóa file vật lý
        String filePath = System.getProperty("user.dir") + "/uploads/Document/" + document.getFileName();
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            throw new AppException(ErrorCode.FILE_DELETION_FAILED);
        }

        // Xóa bản ghi trong cơ sở dữ liệu
        documentRepository.delete(document);
    }
}

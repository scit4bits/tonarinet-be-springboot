package org.scit4bits.tonarinetserver.controller;

import org.junit.jupiter.api.Test;
import org.scit4bits.tonarinetserver.entity.FileAttachment;
import org.scit4bits.tonarinetserver.entity.FileAttachment.FileType;
import org.scit4bits.tonarinetserver.repository.FileAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for FileAttachment entity and repository
 */
@SpringBootTest
@TestPropertySource(properties = {
    "upload.path=c:/test-upload",
    "spring.servlet.multipart.location=c:/test-upload"
})
@Transactional
public class FileAttachmentIntegrationTest {

    @Autowired
    private FileAttachmentRepository fileAttachmentRepository;

    @Test
    void testFileAttachmentEntitySaveAndFind() {
        // Create a test FileAttachment
        FileAttachment fileAttachment = FileAttachment.builder()
            .filepath("c:/test-upload/test-file.txt")
            .originalFilename("test-file.txt")
            .isPrivate(false)
            .uploadedBy(1)
            .type(FileType.ATTACHMENT)
            .articleId(null)
            .filesize(1024)
            .build();

        // Save the entity
        FileAttachment saved = fileAttachmentRepository.save(fileAttachment);

        // Verify it was saved
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFilepath()).isEqualTo("c:/test-upload/test-file.txt");
        assertThat(saved.getOriginalFilename()).isEqualTo("test-file.txt");
        assertThat(saved.getIsPrivate()).isFalse();
        assertThat(saved.getUploadedBy()).isEqualTo(1);
        assertThat(saved.getType()).isEqualTo(FileType.ATTACHMENT);
        assertThat(saved.getFilesize()).isEqualTo(1024);
        assertThat(saved.getUploadedAt()).isNotNull();

        // Test finding by uploaded user
        var filesByUser = fileAttachmentRepository.findByUploadedBy(1);
        assertThat(filesByUser).hasSize(1);
        assertThat(filesByUser.get(0).getId()).isEqualTo(saved.getId());

        // Test finding by type
        var filesByType = fileAttachmentRepository.findByType(FileType.ATTACHMENT);
        assertThat(filesByType).hasSizeGreaterThanOrEqualTo(1);

        // Test counting by user
        long count = fileAttachmentRepository.countByUploadedBy(1);
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testFileAttachmentWithImageType() {
        // Create a test image FileAttachment
        FileAttachment imageFile = FileAttachment.builder()
            .filepath("c:/test-upload/test-image.jpg")
            .originalFilename("test-image.jpg")
            .isPrivate(true)
            .uploadedBy(2)
            .type(FileType.IMAGE)
            .articleId(100)
            .filesize(2048)
            .build();

        // Save the entity
        FileAttachment saved = fileAttachmentRepository.save(imageFile);

        // Verify image-specific properties
        assertThat(saved.getType()).isEqualTo(FileType.IMAGE);
        assertThat(saved.getIsPrivate()).isTrue();
        assertThat(saved.getArticleId()).isEqualTo(100);

        // Test finding by article ID and type
        var imagesByArticle = fileAttachmentRepository.findByArticleIdAndType(100, FileType.IMAGE);
        assertThat(imagesByArticle).hasSize(1);

        // Test finding by privacy status
        var privateFiles = fileAttachmentRepository.findByUploadedByAndIsPrivate(2, true);
        assertThat(privateFiles).hasSize(1);
    }
}

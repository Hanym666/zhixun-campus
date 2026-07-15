package com.zhixun.item.service;

import com.zhixun.common.enums.ItemStatus;
import com.zhixun.common.exception.BusinessException;
import com.zhixun.item.domain.ItemImage;
import com.zhixun.item.domain.ItemPost;
import com.zhixun.item.dto.ItemImageResponse;
import com.zhixun.item.exception.ItemResultCode;
import com.zhixun.item.mapper.ItemImageMapper;
import com.zhixun.item.mapper.ItemPostMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class ItemImageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final int MAX_IMAGES = 6;

    private final ItemImageMapper imageMapper;
    private final ItemPostMapper postMapper;
    private final Path imageDirectory;
    private final String publicPath;

    public ItemImageService(ItemImageMapper imageMapper, ItemPostMapper postMapper,
                            @Value("${zhixun.storage.item-images-path}") String imageDirectory,
                            @Value("${zhixun.storage.item-images-public-path}") String publicPath) {
        this.imageMapper = imageMapper;
        this.postMapper = postMapper;
        this.imageDirectory = Path.of(imageDirectory).toAbsolutePath().normalize();
        this.publicPath = publicPath.endsWith("/") ? publicPath : publicPath + "/";
    }

    @PostConstruct
    void initializeStorage() throws IOException {
        Files.createDirectories(imageDirectory);
    }

    @Transactional
    public ItemImageResponse upload(Long postId, Long publisherId, MultipartFile file) {
        requireEditableOwner(postId, publisherId);
        int count = imageMapper.countByPostId(postId);
        if (count >= MAX_IMAGES) {
            throw new BusinessException(ItemResultCode.IMAGE_LIMIT_EXCEEDED);
        }
        String extension = validateAndDetectExtension(file);
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;
        Path target = resolveFilename(filename);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            ItemImage image = new ItemImage();
            image.setPostId(postId);
            image.setImageUrl(publicPath + filename);
            image.setSortOrder(count);
            image.setCoverImage(count == 0);
            imageMapper.insert(image);
            return toResponse(image);
        } catch (IOException exception) {
            deleteFileQuietly(target);
            throw new BusinessException("图片保存失败");
        } catch (RuntimeException exception) {
            deleteFileQuietly(target);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<ItemImageResponse> list(Long postId) {
        return imageMapper.findByPostId(postId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void delete(Long postId, Long imageId, Long publisherId) {
        requireEditableOwner(postId, publisherId);
        ItemImage image = imageMapper.findById(imageId)
                .filter(value -> postId.equals(value.getPostId()))
                .orElseThrow(() -> new BusinessException(ItemResultCode.IMAGE_NOT_FOUND));
        imageMapper.deleteById(imageId);
        if (Boolean.TRUE.equals(image.getCoverImage())) {
            imageMapper.promoteFirstToCover(postId);
        }
        Path file = resolveFilename(filenameFromUrl(image.getImageUrl()));
        afterCommit(() -> deleteFileQuietly(file));
    }

    private ItemPost requireEditableOwner(Long postId, Long publisherId) {
        ItemPost post = postMapper.findById(postId)
                .orElseThrow(() -> new BusinessException(ItemResultCode.ITEM_NOT_FOUND));
        if (!publisherId.equals(post.getPublisherId())) {
            throw new BusinessException(ItemResultCode.ITEM_OPERATION_FORBIDDEN);
        }
        if (post.getStatus() != ItemStatus.DRAFT && post.getStatus() != ItemStatus.SEARCHING) {
            throw new BusinessException(ItemResultCode.ITEM_STATUS_INVALID);
        }
        return post;
    }

    private String validateAndDetectExtension(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ItemResultCode.IMAGE_FORMAT_INVALID);
        }
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null || image.getWidth() < 1 || image.getHeight() < 1
                    || image.getWidth() > 10000 || image.getHeight() > 10000) {
                throw new BusinessException(ItemResultCode.IMAGE_FORMAT_INVALID);
            }
            byte[] header = file.getBytes();
            if (header.length >= 8 && header[0] == (byte) 0x89 && header[1] == 0x50
                    && header[2] == 0x4E && header[3] == 0x47) {
                return ".png";
            }
            if (header.length >= 3 && header[0] == (byte) 0xFF
                    && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
                return ".jpg";
            }
        } catch (IOException ignored) {
            // Converted to a stable business error below.
        }
        throw new BusinessException(ItemResultCode.IMAGE_FORMAT_INVALID);
    }

    private Path resolveFilename(String filename) {
        Path resolved = imageDirectory.resolve(filename).normalize();
        if (!resolved.startsWith(imageDirectory)) {
            throw new BusinessException(ItemResultCode.IMAGE_FORMAT_INVALID);
        }
        return resolved;
    }

    private String filenameFromUrl(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
    }

    private void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private void deleteFileQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // Orphan cleanup can be handled by a scheduled maintenance job.
        }
    }

    private ItemImageResponse toResponse(ItemImage image) {
        return new ItemImageResponse(image.getId(), image.getImageUrl(),
                image.getSortOrder(), image.getCoverImage());
    }
}

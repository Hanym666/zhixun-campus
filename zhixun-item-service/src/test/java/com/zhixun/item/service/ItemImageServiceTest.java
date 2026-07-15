package com.zhixun.item.service;

import com.zhixun.common.enums.ItemStatus;
import com.zhixun.common.exception.BusinessException;
import com.zhixun.item.domain.ItemImage;
import com.zhixun.item.domain.ItemPost;
import com.zhixun.item.mapper.ItemImageMapper;
import com.zhixun.item.mapper.ItemPostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemImageServiceTest {

    @Mock ItemImageMapper imageMapper;
    @Mock ItemPostMapper postMapper;
    @TempDir Path tempDirectory;

    @Test
    void storesValidatedPngWithGeneratedFilename() throws Exception {
        ItemImageService service = service();
        when(postMapper.findById(1L)).thenReturn(Optional.of(editablePost()));
        when(imageMapper.countByPostId(1L)).thenReturn(0);
        doAnswer(invocation -> {
            ItemImage image = invocation.getArgument(0);
            image.setId(12L);
            return 1;
        }).when(imageMapper).insert(any(ItemImage.class));

        var response = service.upload(1L, 7L,
                new MockMultipartFile("file", "test.png", "image/png", pngBytes()));

        assertThat(response.id()).isEqualTo(12L);
        assertThat(response.coverImage()).isTrue();
        assertThat(response.imageUrl()).startsWith("/uploads/items/").endsWith(".png");
        assertThat(Files.list(tempDirectory)).hasSize(1);
    }

    @Test
    void rejectsContentThatIsNotAnImage() throws Exception {
        ItemImageService service = service();
        when(postMapper.findById(1L)).thenReturn(Optional.of(editablePost()));
        when(imageMapper.countByPostId(1L)).thenReturn(0);

        assertThatThrownBy(() -> service.upload(1L, 7L,
                new MockMultipartFile("file", "fake.png", "image/png", "not-image".getBytes())))
                .isInstanceOf(BusinessException.class);
    }

    private ItemImageService service() throws Exception {
        ItemImageService service = new ItemImageService(imageMapper, postMapper,
                tempDirectory.toString(), "/uploads/items/");
        service.initializeStorage();
        return service;
    }

    private ItemPost editablePost() {
        ItemPost post = new ItemPost();
        post.setId(1L);
        post.setPublisherId(7L);
        post.setStatus(ItemStatus.DRAFT);
        return post;
    }

    private byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}

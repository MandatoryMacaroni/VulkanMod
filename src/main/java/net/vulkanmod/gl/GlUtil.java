package net.vulkanmod.gl;

import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.shader.SPIRVUtils;
import org.apache.commons.lang3.Validate;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.*;

public abstract class GlUtil {

    public static SPIRVUtils.ShaderKind extToShaderKind(String ext) {
        return switch (ext) {
            case ".vsh" -> SPIRVUtils.ShaderKind.VERTEX_SHADER;
            case ".fsh" -> SPIRVUtils.ShaderKind.FRAGMENT_SHADER;
            default -> throw new RuntimeException("unknown shader type: " + ext);
        };
    }

    // Created buffer will need to be freed
    public static ByteBuffer RGBtoRGBA_buffer(ByteBuffer in) {
        Validate.isTrue(in.remaining() % 3 == 0, "Unexpected buffer stride");

        int outSize = in.remaining() * 4 / 3;
        ByteBuffer out = MemoryUtil.memAlloc(outSize);

        int j = 0;
        for (int i = 0; i < outSize; i += 4, j += 3) {
            out.put(i, in.get(j));
            out.put(i + 1, in.get(j + 1));
            out.put(i + 2, in.get(j + 2));
            out.put(i + 3, (byte) 0xFF);
        }

        return out;
    }

    public static ByteBuffer BGRAtoRGBA_buffer(ByteBuffer in) {
        Validate.isTrue(in.remaining() % 4 == 0, "Unexpected buffer stride");

        int outSize = in.remaining();
        ByteBuffer out = MemoryUtil.memAlloc(outSize);

        long ptr = MemoryUtil.memAddress0(out);
        long srcPtr = MemoryUtil.memAddress0(in);

        // TODO write in place (don't free the returned buffer in that case)
        for (int i = 0; i < outSize; i += 4) {
            int color = MemoryUtil.memGetInt(srcPtr + i);

            color = (color << 24) & 0xFF000000 | (color >> 8) & 0xFFFFFF;

            MemoryUtil.memPutInt(ptr + i, color);
        }

        return out;
    }

    public static int vulkanFormat(int glFormat, int type) {
        return switch (glFormat) {
            case GL11.GL_RGBA, GL30.GL_RGBA8 ->
                switch (type) {
                    case GL11.GL_UNSIGNED_BYTE, GL11.GL_BYTE, GL30.GL_UNSIGNED_INT_8_8_8_8, GL30.GL_UNSIGNED_INT_8_8_8_8_REV -> VK_FORMAT_R8G8B8A8_UNORM;
                    default -> throw new IllegalStateException("Unexpected type: " + type);
                };
            case GL30.GL_BGRA ->
                switch (type) {
                    case GL11.GL_UNSIGNED_BYTE, GL11.GL_BYTE, GL30.GL_UNSIGNED_INT_8_8_8_8, GL30.GL_UNSIGNED_INT_8_8_8_8_REV -> VK_FORMAT_B8G8R8A8_UNORM;
                    default -> throw new IllegalStateException("Unexpected type: " + type);
                };
            case GL30.GL_BGR -> 
                switch (type) {
                    case GL11.GL_UNSIGNED_BYTE, GL11.GL_BYTE -> VK_FORMAT_B8G8R8_UNORM;
                    default -> throw new IllegalStateException("Unexpected type: " + type);
                };
            case GL11.GL_RGB ->
                switch (type) {
                    case GL11.GL_UNSIGNED_BYTE, GL11.GL_BYTE -> VK_FORMAT_R8G8B8_UNORM;
                    default -> throw new IllegalStateException("Unexpected type: " + type);
                };
            case GL30.GL_RG ->
                switch (type) {
                    case GL11.GL_UNSIGNED_BYTE, GL11.GL_BYTE -> VK_FORMAT_R8G8_UNORM;
                    default -> throw new IllegalStateException("Unexpected type: " + type);
            };
            case GL11.GL_RED ->
                switch (type) {
                    case GL11.GL_UNSIGNED_BYTE, GL11.GL_BYTE -> VK_FORMAT_R8_UNORM;
                    default -> throw new IllegalStateException("Unexpected type: " + type);
                };
            case GL11.GL_DEPTH_COMPONENT, GL30.GL_DEPTH_COMPONENT32F, GL30.GL_DEPTH_COMPONENT24, GL30.GL_DEPTH32F_STENCIL8, GL30.GL_DEPTH24_STENCIL8, GL30.GL_DEPTH_COMPONENT16 ->
                Vulkan.getDefaultDepthFormat();
            default -> throw new IllegalStateException("Unexpected format: " + glFormat);
        };
    }

    // TODO: refactor
    public static int vulkanFormat(int glInternalFormat) {
        return switch (glInternalFormat) {
            case GL30.GL_UNSIGNED_INT_8_8_8_8_REV -> VK_FORMAT_R8G8B8A8_UINT;
            case GL11.GL_DEPTH_COMPONENT, GL30.GL_DEPTH_COMPONENT32F, GL30.GL_DEPTH_COMPONENT24 ->
                Vulkan.getDefaultDepthFormat();
            default -> throw new IllegalStateException("Unexpected value: " + glInternalFormat);
        };
    }

    public static int getGlFormat(int vFormat) {
        return switch (vFormat) {
            case VK_FORMAT_R8G8B8A8_UNORM -> GL11.GL_RGBA;
            case VK_FORMAT_B8G8R8A8_UNORM -> GL30.GL_BGRA;
            case VK_FORMAT_B8G8R8_UNORM -> GL30.GL_BGR;
            case VK_FORMAT_R8G8_UNORM -> GL30.GL_RG;
            case VK_FORMAT_R8_UNORM -> GL11.GL_RED;
            case VK_FORMAT_R8G8B8_UNORM -> GL11.GL_RGB;
            default -> throw new IllegalStateException("Unexpected value: " + vFormat);
        };
    }
}
